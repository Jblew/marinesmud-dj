/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene.devices;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jiconfont.IconCode;
import jiconfont.icons.GoogleMaterialDesignIcons;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DimmableDevice;
import pl.jblew.marinesmud.dj.gui.util.GUIUtil;
import pl.jblew.marinesmud.dj.scene.RGBDevice;

/**
 *
 * @author teofil
 */
public class SingleDimmer implements DMXDevice, DimmableDevice {
    public String name;
    public int address;
    public int min = 0;
    public int max = 255;
    
    @JsonIgnore
    public final AtomicReference<Float> valueRef = new AtomicReference<>(0f);
    
    @JsonIgnore
    public final AtomicReference<Color> currentColorRef = new AtomicReference<>(Color.BLACK);
    @JsonIgnore
    public final AtomicReference<JPanel> componentRef = new AtomicReference<>(null);
    
    public SingleDimmer(String name, int address) {
        this.name = name;
        this.address = address;
    }
    
    public SingleDimmer() {
        
    }


    @Override
    @JsonIgnore
    public int getStartAddress() {
        return address;
    }

    @Override
    @JsonIgnore
    public int getChannelCount() {
        return 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JComponent newComponent() {
        GUIUtil.assertEDTThread();
        
        JPanel panel = new JPanel();
        panel.setBackground(currentColorRef.get());
        panel.setPreferredSize(new Dimension(48, 48));
        componentRef.set(panel);
        return panel;
    }

    @Override
    public byte[] calculateLevels() {
        float f = valueRef.get();
        return new byte [] {(byte)(int)(f*255f)};
    }

    @Override
    public void setMasterGain(float level) {
        
    }

    @Override
    public void setCommonLevel(float level) {
        valueRef.set(level);
    }

    @Override
    public IconCode getIconCode() {
        return GoogleMaterialDesignIcons.COLOR_LENS;
    }
    
    
}
