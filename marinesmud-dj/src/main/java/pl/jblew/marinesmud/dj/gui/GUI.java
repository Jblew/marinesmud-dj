/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.Mixer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.effects.Effects;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.MixerChangeListener;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.tarsos.InputPanel;
import pl.jblew.marinesmud.dj.util.GUIUtil;

/**
 *
 * @author teofil
 */
public class GUI {
    private final SoundProcessingManager spm;
    private final JFrame frame;
    //private final BWSpectrogramPanel spectrogramPanel;
    private final MixerChangeListener mixerChangeListener;
    private final Effects effects;
    private final SceneSetup.Current sceneSetup;

    public GUI(final MixerChangeListener mixerChangeListener, Effects effects, SoundProcessingManager spm, SceneSetup.Current sceneSetup) {
        this.mixerChangeListener = mixerChangeListener;
        this.spm = spm;
        this.effects = effects;
        this.sceneSetup = sceneSetup;

        GUIUtil.assertEDTThread();
        this.frame = new JFrame();
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("MarinesmudDJ");

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 2));

        JPanel inputPanel = new InputPanel();

        inputPanel.addPropertyChangeListener("mixer", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                mixerChangeListener.mixerChanged((Mixer) arg0.getNewValue());
            }
        });
        northPanel.add(inputPanel);
        
        JScrollPane sceneScroll = new JScrollPane(new ScenePanel(sceneSetup));
        sceneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sceneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        northPanel.add(sceneScroll);

        frame.add(northPanel, BorderLayout.NORTH);

        JPanel effectsPanel = new JPanel(new GridLayout(1, 0, 5, 5));
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

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
