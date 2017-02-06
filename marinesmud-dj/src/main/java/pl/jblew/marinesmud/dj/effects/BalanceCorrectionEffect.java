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
public class BalanceCorrectionEffect implements Effect {
    public boolean enabled = true;
    public float[] knobs = null;

    @JsonIgnore
    private final Object sync = new Object();

    public BalanceCorrectionEffect() {

    }

    @Override
    @JsonIgnore
    public String getName() {
        return "Balance correction";
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
    public BalanceCorrectionEffect deriveEffect() {
        synchronized (sync) {
            BalanceCorrectionEffect clone = new BalanceCorrectionEffect();
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

            int numOfKnobs = Arrays.stream(deviceGroup.getDevices()).mapToInt(d -> d.getLevelsCount()).max().orElse(0);

            synchronized (sync) {
                if (knobs == null) {
                    knobs = new float[numOfKnobs];
                    for(int i = 0;i < numOfKnobs;i++) knobs[i] = 1f;
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
            return BalanceCorrectionEffect.this;
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
            synchronized (sync) {
                _enabled = enabled;
            }

            if (_enabled) {
                MyEffectPanel myPanel = myPanelRef.get();
                if (myPanel != null) {

                    synchronized (sync) {
                        for (int i = 0; i < knobs.length; i++) {
                            knobs[i] = (float) myPanel.sliders[i].getValue() / 1000f;

                            int index = i;
                            String newLabel = ((int) (knobs[i] * 100f)) + "%";
                            if (!myPanel.sliderLabels[i].getText().equals(newLabel)) {
                                SwingUtilities.invokeLater(() -> {
                                    myPanel.sliderLabels[index].setText(newLabel);
                                });
                            }
                        }

                        DMXDevice[] devices = deviceGroup.getDevices();
                        for (int i = 0; i < devices.length; i++) {
                            devices[i].multiplyLevels(knobs);
                        }
                    }
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                BalanceCorrectionEffect.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }

    }

    private class MyEffectPanel extends EffectPanel {
        private final JSlider[] sliders;
        private final JLabel[] sliderLabels;

        public MyEffectPanel(DeviceGroup deviceGroup) {
            setLayout(new GridLayout(0, 2));

            sliders = new JSlider[knobs.length];
            sliderLabels = new JLabel[sliders.length];

            for (int i = 0; i < sliders.length; i++) {
                sliders[i] = new JSlider(750, 1000, (int) (knobs[i] * 1000f));
                sliderLabels[i] = new JLabel("Waiting for");

                this.add(sliderLabels[i]);
                this.add(sliders[i]);
            }
        }
    }

}
