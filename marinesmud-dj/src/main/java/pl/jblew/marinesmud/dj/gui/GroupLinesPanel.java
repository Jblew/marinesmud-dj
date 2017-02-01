/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.effects.Effects;
import pl.jblew.marinesmud.dj.gui.util.ColoredToggleButton;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;

/**
 *
 * @author teofil
 */
public class GroupLinesPanel extends JPanel {
    public GroupLinesPanel(Effects effects, SoundProcessingManager spm, SceneSetup.Current sceneSetup) {
        this.setLayout(new GridLayout(0, 1));

        for (DeviceGroup group : sceneSetup.groups) {
            this.add(new DevicePanel(group, effects, spm, sceneSetup));
        }
    }

    private static class DevicePanel extends JPanel {
        private final DeviceGroup group;

        public DevicePanel(DeviceGroup group, Effects effects, SoundProcessingManager spm, SceneSetup.Current sceneSetup) {
            this.group = group;
            //this.setPreferredSize(new Dimension(800, 200));
            this.setBorder(new TitledBorder(group.getName()));
            this.setLayout(new GridLayout(1, 0));

            JPanel labelPanel = new JPanel();
            labelPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            labelPanel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel(group.getName());
            titleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24));
            labelPanel.add(titleLabel, BorderLayout.NORTH);

            int rows = (int) Math.ceil((float) group.getDevices().length / 25f) * 5;
            System.out.println("rows=" + rows);
            JPanel previewPanel = new JPanel(new GridLayout(5, 5, 5, 5));
            previewPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            for (DMXDevice device : group.getDevices()) {
                JComponent c = device.newComponent();
                c.setPreferredSize(new Dimension(32, 32));
                previewPanel.add(c);
            }
            for (int i = 0; i < (rows * 5 - group.getDevices().length); i++) {
                JPanel p = new JPanel();
                p.setBackground(Color.WHITE);
                p.setBorder(BorderFactory.createLoweredBevelBorder());
                previewPanel.add(p);
            }
            labelPanel.add(previewPanel, BorderLayout.CENTER);

            //JLabel enableButton = new JLabel("Enabled", IconFontSwing.buildIcon(GoogleMaterialDesignIcons.POWER_SETTINGS_NEW, 24, Color.WHITE), SwingConstants.CENTER);
            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            JCheckBox enableButton = new JCheckBox("Enabled");
            enableButton.setForeground(Color.WHITE);
            enableButton.setSelected(group.isEnabled());
            if (enableButton.isSelected()) {
                southPanel.setBackground(Color.GREEN.darker());
                enableButton.setText("Enabled");
            } else {
                southPanel.setBackground(Color.RED.darker());
                enableButton.setText("Disabled");
            }
            enableButton.addActionListener((e) -> {
                if (enableButton.isSelected()) {
                    southPanel.setBackground(Color.GREEN.darker());
                    enableButton.setText("Enabled");
                } else {
                    southPanel.setBackground(Color.RED.darker());
                    enableButton.setText("Disabled");
                }
            });
            southPanel.add(enableButton, BorderLayout.CENTER);

            JSpinner prioritySpinner = new JSpinner(new SpinnerNumberModel(group.getPriority(), 0, 100, 1));
            prioritySpinner.addChangeListener(e -> {
                group.setPriority((int) prioritySpinner.getValue());
            });
            southPanel.add(prioritySpinner, BorderLayout.EAST);

            labelPanel.add(southPanel, BorderLayout.SOUTH);

            this.add(labelPanel);

            AtomicReference<AddEffectPanel> aep = new AtomicReference<>(null);
            aep.set(new AddEffectPanel(e -> {
                if (aep.get() != null) {
                    this.remove(aep.get());
                }
                this.add(new EffectSelectorPanel(effects, this, spm, sceneSetup));
                if (aep.get() != null) {
                    this.add(aep.get());
                }
                this.updateUI();
            }));
            this.add(aep.get());
        }
    }
}
