/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.JSlider;
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
public class StaticSliderEffect implements Effect {
    public boolean enabled = true;
    public float[] knobs = new float[0];

    @JsonIgnore
    private final Object sync = new Object();

    public StaticSliderEffect() {

    }

    @Override
    @JsonIgnore
    public String getName() {
        return "Static sliders";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    @JsonIgnore
    public Processor[] getRequiredProcessors() {
        return new Processor[]{};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }

    @Override
    public StaticSliderEffect deriveEffect() {
        synchronized (sync) {
            StaticSliderEffect clone = new StaticSliderEffect();
            clone.enabled = this.enabled;
            clone.knobs = this.knobs;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
            
            int numOfKnobs = deviceGroup.getDevices().length;

            synchronized (sync) {
                if (knobs == null) {
                    knobs = new float[numOfKnobs];
                } else if (knobs.length < numOfKnobs) {
                    float[] newknobs = new float[numOfKnobs];
                    for (int i = 0; i < numOfKnobs; i++) {
                        newknobs[i] = (i < knobs.length ? knobs[i] : 1f);
                    }
                    knobs = newknobs;
                }
            }
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return StaticSliderEffect.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            MyEffectPanel p = new MyEffectPanel(deviceGroup);
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            boolean _enabled;
            synchronized(sync) {
                _enabled = enabled;
            }
            if (_enabled) {
                MyEffectPanel myPanel = myPanelRef.get();
                if (myPanel != null) {
                    DMXDevice[] devices = deviceGroup.getDevices();
                    for (int i = 0; i < devices.length; i++) {
                        final int index = i;
                        float knob = (float) myPanel.sliders[i].getValue() / 1000f;
                        knobs[i] = knob;
                        float v = (float) Math.pow(knob, 2);

                        devices[i].setCommonLevel(v);

                        String newLabel = ((int) (v * 255f)) + " (" + ((int) (v * 100f)) + "%)";
                        if (!myPanel.sliderLabels[i].getText().equals(newLabel)) {
                            SwingUtilities.invokeLater(() -> {
                                myPanel.sliderLabels[index].setText(newLabel);
                            });
                        }

                    }
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized(sync) {
                StaticSliderEffect.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized(sync) {
                return enabled;
            }
        }

    }

    private static class MyEffectPanel extends EffectPanel {
        private final JSlider[] sliders;
        private final JLabel[] sliderLabels;

        public MyEffectPanel(DeviceGroup deviceGroup) {
            setLayout(new GridLayout(0, 2));

            DMXDevice[] devices = deviceGroup.getDevices();
            sliders = new JSlider[devices.length];
            sliderLabels = new JLabel[sliders.length];

            for (int i = 0; i < sliders.length; i++) {
                sliders[i] = new JSlider(0, 1000, 0);
                sliderLabels[i] = new JLabel("Waiting for");

                this.add(sliderLabels[i]);
                this.add(sliders[i]);
            }
        }
    }

}
