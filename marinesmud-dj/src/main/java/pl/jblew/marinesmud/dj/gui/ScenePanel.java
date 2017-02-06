/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import pl.jblew.marinesmud.dj.config.Config;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.SceneSetup;

/**
 *
 * @author teofil
 */
public class ScenePanel extends JPanel {
    public ScenePanel(SceneSetup.Current sceneSetup) {
        this.setLayout(new GridLayout(0, 1));
        
        for(DeviceGroup group : sceneSetup.groups) {
            
            
            JPanel groupRow = new JPanel();
            groupRow.setLayout(new FlowLayout(FlowLayout.LEFT));
            groupRow.setBorder(new LineBorder(Color.BLACK, 2));
            
            JLabel label = new JLabel(group.getName());
            groupRow.add(label);
            
            for(DMXDevice device : group.getDevices()) {
                groupRow.add(device.newPreviewComponent());
            }
            
            this.add(groupRow);
        }
        
        this.add(new JPanel());
        this.add(new JPanel());
    }
}
