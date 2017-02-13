/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.BorderLayout;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.App;
import pl.jblew.marinesmud.dj.config.Config;
import pl.jblew.marinesmud.dj.config.ConfigLoader;
import pl.jblew.marinesmud.dj.dmx.OutputManager;
import pl.jblew.marinesmud.dj.dmx.PortChangeListener;
import pl.jblew.marinesmud.dj.effects.PreconfiguredEffects;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.MixerChangeListener;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.gui.util.GUIUtil;

/**
 *
 * @author teofil
 */
public class GUI {
    private final OutputManager outputManager;
    private final SoundProcessingManager spm;
    private final JFrame frame;
    //private final BWSpectrogramPanel spectrogramPanel;
    private final MixerChangeListener mixerChangeListener;
    private final WindowAdapter windowCloseListener;
    private final PortChangeListener portChangeListener;
    private final PreconfiguredEffects effects;
    private final SceneSetup.Current sceneSetup;
    private final Config config;
    private final App app;

    private final JLabel statusBar;

    public GUI(MixerChangeListener mixerChangeListener, PortChangeListener portChangeListener, PreconfiguredEffects effects, SoundProcessingManager spm, SceneSetup.Current sceneSetup, OutputManager outputManager, Config config, App app) {
        this.mixerChangeListener = mixerChangeListener;
        this.portChangeListener = portChangeListener;
        this.spm = spm;
        this.effects = effects;
        this.sceneSetup = sceneSetup;
        this.outputManager = outputManager;
        this.config = config;
        this.app = app;

        this.frame = new JFrame();
        this.statusBar = new JLabel();

        this.windowCloseListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                try {
                    ConfigLoader.save(config);
                    System.out.println("Config saved!");
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                System.exit(0);
            }
        };
    }

    public void show() {
        GUIUtil.assertEDTThread();
        try {
            UIManager.setLookAndFeel(UIManager
                    .getSystemLookAndFeelClassName());
            IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        } catch (Exception e) {
            // ignore failure to set default look en feel;
        }
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setTitle("MarinesmudDJ");
        frame.addWindowListener(windowCloseListener);

        NorthToolbar toolBar = new NorthToolbar(outputManager, config, app, sceneSetup.getSceneSetup());

        toolBar.addPropertyChangeListener("mixer", (evt) -> {
            mixerChangeListener.mixerChanged((Mixer) evt.getNewValue());
        });

        toolBar.addPropertyChangeListener("port", (evt) -> {
            portChangeListener.portChanged((String) evt.getNewValue());
        });

        frame.add(toolBar, BorderLayout.NORTH);

        /*JScrollPane sceneScroll = new JScrollPane(new ScenePanel(sceneSetup));
        sceneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sceneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.add(sceneScroll, BorderLayout.EAST);*/

 /*JPanel effectsPanel = new JPanel(new GridLayout(1, 0, 5, 5));
        //effectsPanel.add(spectrogramPanel, BorderLayout.CENTER);
        effectsPanel.setBorder(new TitledBorder("Effects"));

        effectsPanel.add(new EffectSelectorPanel(effects, effectsPanel, spm, sceneSetup));

        AtomicReference<AddEffectPanel> aep = new AtomicReference<>(null);
        aep.set(new AddEffectPanel(e -> {
            if (aep.get() != null) {
                effectsPanel.remove(aep.get());
            }
            effectsPanel.add(new EffectSelectorPanel(effects, effectsPanel, spm, sceneSetup));
            if (aep.get() != null) {
                effectsPanel.add(aep.get());
            }
            effectsPanel.updateUI();
        }));
        effectsPanel.add(aep.get());
        
        JScrollPane scrollPane = new JScrollPane(effectsPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);     
        frame.add(scrollPane, BorderLayout.CENTER);*/
        //DMXFlowChart flowChart = new DMXFlowChart(sceneSetup);
        //frame.add(flowChart.createComponent(), BorderLayout.SOUTH);
        JScrollPane sceneScroll = new JScrollPane(new GroupLinesPanel(effects, spm, sceneSetup));
        sceneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sceneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //sceneScroll.setPreferredSize(new Dimension(800, 500));
        frame.add(sceneScroll, BorderLayout.CENTER);

        frame.add(statusBar, BorderLayout.SOUTH);

        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    public void silentClose() {
        GUIUtil.assertEDTThread();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.removeWindowListener(windowCloseListener);
        frame.setVisible(false);
    }

    public void updateStatus(int percentBusy) {
        GUIUtil.assertEDTThread();
        statusBar.setText("Clock busy time: " + percentBusy + "%");
    }

    public JFrame getFrame() {
        return frame;
    }
}
