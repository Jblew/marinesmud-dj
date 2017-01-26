/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.effects.Effect;
import pl.jblew.marinesmud.dj.effects.EffectWorker;
import pl.jblew.marinesmud.dj.effects.Effects;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;

/**
 *
 * @author teofil
 */
public class EffectSelectorPanel extends JPanel {
    private final SoundProcessingManager spm;
    private final Effects effects;
    private final TitledBorder titledBorder;
    private final JComboBox comboSelector;
    private EffectWorker currentEffectWorker = null;
    private EffectPanel currentEffectPanel = null;

    public EffectSelectorPanel(Effects effects, JPanel effectsPanel, SoundProcessingManager spm, SceneSetup.Current sceneSetup) {
        this.effects = effects;
        this.spm = spm;

        titledBorder = new TitledBorder("Select effect");

        this.setLayout(new BorderLayout());
        this.setBorder(titledBorder);

        currentEffectPanel = new EffectPanel() {
        };
        this.add(currentEffectPanel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());

        Effect[] effectArray = effects.stream().toArray(Effect[]::new);
        comboSelector = new JComboBox(effectArray);
        comboSelector.addActionListener(e -> {
            effectChanged(comboSelector.getSelectedItem());
        });
        effectChanged(effectArray[0]);
        northPanel.add(comboSelector, BorderLayout.CENTER);

        JButton closeButton = new JButton(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CLOSE, 16, Color.RED.darker().darker()));
        closeButton.addActionListener(e -> {
            effectsPanel.remove(this);
            effectsPanel.updateUI();
            if (currentEffectWorker != null) {
                spm.stopEffectWorker(currentEffectWorker);
            }
        });
        northPanel.add(closeButton, BorderLayout.EAST);

        this.add(northPanel, BorderLayout.NORTH);
        

        JComboBox deviceGroupSelector = new JComboBox(sceneSetup.groups);
        deviceGroupSelector.addActionListener(e -> {
            if(currentEffectWorker != null) {
                currentEffectWorker.setDeviceGroup((DeviceGroup) deviceGroupSelector.getSelectedItem());
            }
        });
        this.add(deviceGroupSelector, BorderLayout.SOUTH);
    }

    private void effectChanged(Object selectedItem) {
        if (currentEffectWorker != null) {
            spm.stopEffectWorker(currentEffectWorker);
        }

        this.currentEffectWorker = spm.initEffectWorker((Effect) selectedItem);
        this.remove(currentEffectPanel);
        currentEffectPanel = currentEffectWorker.createEffectPanel();
        this.add(currentEffectPanel);
        this.updateUI();
    }
}
