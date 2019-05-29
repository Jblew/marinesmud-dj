/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene.devices;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JPanel;
import jiconfont.IconCode;
import jiconfont.icons.GoogleMaterialDesignIcons;
import pl.jblew.marinesmud.dj.clock.ClockWorker;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.gui.util.GUIUtil;
import pl.jblew.marinesmud.dj.scene.RGBDevice;

/**
 *
 * @author teofil
 */
public class LedBar extends DMXDevice implements RGBDevice {
    public String name;
    public int address;
    public int min = 0;
    public int max = 255;

    @JsonIgnore
    public final Object sync = new Object();
    @JsonIgnore
    private float[] levels = new float[]{0f, 0f, 0f};
    @JsonIgnore
    public final AtomicReference<JPanel> componentRef = new AtomicReference<>(null);

    public LedBar(String name, int address) {
        this.name = name;
        this.address = address;
    }

    public LedBar() {

    }

    @Override
    public void setColor(final Color newColor) {
        if (!ClockWorker.isInClockTaskThread()) {
            throw new RuntimeException("DMX values should be set in clock tasks thread");
        }
        synchronized (sync) {
            levels = newColor.getRGBColorComponents(null);
        }
    }

    @Override
    @JsonIgnore
    public int getStartAddress() {
        return address;
    }

    @Override
    @JsonIgnore
    public int getChannelCount() {
        return 3;
    }
    
    @Override
    @JsonIgnore
    public int getLevelsCount() {
        return 3;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JComponent newPreviewComponent() {
        GUIUtil.assertEDTThread();

        JPanel panel = new JPanel();
        synchronized (sync) {
            panel.setBackground(new Color(levels[0], levels[1], levels[2]));
        }
        panel.setPreferredSize(new Dimension(48, 48));
        componentRef.set(panel);
        return panel;
    }

    @Override
    public byte[] calculateLevels() {
        synchronized (sync) {
            return new byte[]{(byte) (levels[0] * 255f), (byte) (levels[1] * 255f), (byte) (levels[2] * 255f)};
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
        if (panel != null) {
            synchronized (sync) {
                panel.setBackground(new Color(levels[0], levels[1], levels[2]));
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
    @JsonIgnore
    public void setLevels(float[] levels) {
        synchronized(sync) {
            this.levels = levels;
        }
    }

    @Override
    @JsonIgnore
    public Object getSync() {
        return sync;
    }

}
