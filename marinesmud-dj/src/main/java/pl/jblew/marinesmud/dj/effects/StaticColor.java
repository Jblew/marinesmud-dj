/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.RGBDevice;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.PitchProcessor;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.tarsos.SpectrogramPanel;
import pl.jblew.marinesmud.dj.util.Listener;

/**
 *
 * @author teofil
 */
public class StaticColor implements Effect {
    public boolean enabled = true;
    public float[] color = new float[]{1f, 1f, 1f};

    @JsonIgnore
    private final Object sync = new Object();

    public StaticColor(Color color) {
        this.color = color.getRGBColorComponents(null);
    }

    public StaticColor() {

    }

    @Override
    public String getName() {
        return "StaticColor";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor[]{};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }

    @Override
    public StaticColor deriveEffect() {
        synchronized (sync) {
            StaticColor clone = new StaticColor();
            clone.enabled = this.enabled;
            clone.color = this.color;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<StaticColorPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return StaticColor.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            synchronized (sync) {
                StaticColorPanel p = new StaticColorPanel(new Color(color[0], color[1], color[2]));
                myPanelRef.set(p);
                return p;
            }
        }

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            Color color;
            boolean _enabled;
            synchronized (sync) {
                _enabled = enabled;
                StaticColorPanel panel = myPanelRef.get();
                if (panel != null) {
                    StaticColor.this.color = panel.colorRef.get().getRGBComponents(null);
                }
                color = new Color(StaticColor.this.color[0], StaticColor.this.color[1], StaticColor.this.color[2]);
            }

            if (_enabled) {
                RGBDevice[] rgbDevices = Arrays.stream(deviceGroup.getDevices()).sequential()
                        .filter(d -> d instanceof RGBDevice)
                        .map(d -> (RGBDevice) d)
                        .toArray(RGBDevice[]::new);
                for (RGBDevice d : rgbDevices) {
                    d.setColor(color);
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                StaticColor.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }
    }

    private static class StaticColorPanel extends EffectPanel {
        private final JButton selectColorButton = new JButton("Click here to change color");
        private final AtomicReference<Color> colorRef = new AtomicReference<>(Color.WHITE);

        public StaticColorPanel(Color initialColor) {
            this.setLayout(new FlowLayout());
            colorRef.set(initialColor);

            this.add(selectColorButton);

            selectColorButton.addActionListener(evt -> {
                //colorRef.set(JColorChooser.showDialog(this, "Choose RGB color", colorRef.get()));
                JColorChooser cc = new JColorChooser(colorRef.get());
                cc.getSelectionModel().addChangeListener((changedEvt) -> {
                    colorRef.set(cc.getColor());
                    this.setBackground(colorRef.get());
                });
                JColorChooser.createDialog(this, "Choose Color", false, cc, (evtOk) -> {}, (evtCancel) -> {}).setVisible(true);
                //this.setBackground(colorRef.get());
            });

            this.setBackground(initialColor);
        }
    }

}
