/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene.devices;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JPanel;
import jiconfont.IconCode;
import jiconfont.icons.GoogleMaterialDesignIcons;
import pl.jblew.marinesmud.dj.gui.util.GUIUtil;
import pl.jblew.marinesmud.dj.scene.DMXDevice;

/**
 *
 * @author teofil
 */
public class Relay extends DMXDevice {
    public String name;
    public int address;
    public int min = 0;
    public int max = 255;
    public int minimalTogglePeriodMs = 300;
    
    @JsonIgnore
    private final Object sync = new Object();
    @JsonIgnore
    private float [] levels = new float [] {0f};
    @JsonIgnore
    private final AtomicReference<JPanel> componentRef = new AtomicReference<>(null);
    @JsonIgnore
    private long lastToggledMillis = System.currentTimeMillis();
    @JsonIgnore
    private byte desiredState = 0x00;
    @JsonIgnore
    private byte currentState = 0x00;
    
    public Relay(String name, int address) {
        this.name = name;
        this.address = address;
    }
    
    public Relay() {
        
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
    @JsonIgnore
    public int getLevelsCount() {
        return 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JComponent newPreviewComponent() {
        GUIUtil.assertEDTThread();
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setPreferredSize(new Dimension(48, 48));
        componentRef.set(panel);
        return panel;
    }

    @Override
    public byte[] calculateLevels() {
        synchronized(sync) {
            desiredState = (levels[0] > 0.5f)? (byte)0xFF : (byte)0x00;
            
            if(desiredState != currentState && System.currentTimeMillis()-lastToggledMillis > minimalTogglePeriodMs) {
                currentState = desiredState;
                lastToggledMillis = System.currentTimeMillis();
            }
                
            return new byte [] {currentState};
            
        }
    }

    @Override
    @JsonIgnore
    public IconCode getIconCode() {
        return GoogleMaterialDesignIcons.COLOR_LENS;
    }

    @Override
    public void processValues() {
    }

    @Override
    public void updatePreview() {
        GUIUtil.assertEDTThread();
        JPanel panel = componentRef.get();
        if(panel != null) {
            synchronized(sync) {
                panel.setBackground((levels[0] > 0.5f)? Color.WHITE : Color.BLACK);
            }
        }
    }

    @Override
    @JsonIgnore
    public float[] getLevels() {
        synchronized(sync) {
            return levels;
        }
    }

    @Override
    public void setLevels(float[] levels) {
        synchronized(this.sync) {
            this.levels = levels;
        }
    }
    
    @Override
    @JsonIgnore
    public Object getSync() {
        return sync;
    }
}
