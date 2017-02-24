/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.gui.util.EnumComboBox;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.util.TaskTuple;

/**
 *
 * @author teofil
 */
public class StrobeEffect implements Effect {
    public boolean enabled = true;
    public int nCycles = 1;
    public StrobeMode mode = StrobeMode.COLOR_TO_BLACK;

    @JsonIgnore
    private final Object sync = new Object();

    @Override
    @JsonIgnore
    public String getName() {
        return getClass().getSimpleName();
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
    public StrobeEffect deriveEffect() {
        synchronized (sync) {
            StrobeEffect clone = new StrobeEffect();
            clone.enabled = this.enabled;
            clone.nCycles = this.nCycles;
            clone.mode = this.mode;
            return clone;
        }
    }

    public static enum StrobeMode {
        BLACK_WHITE((d, isHigh) -> {
            float[] levels = d.getLevels();
            for (int i = 0; i < levels.length; i++) {
                levels[i] = isHigh ? 1f : 0f;
            }
            d.setLevels(levels);
        }),
        COLOR_TO_BLACK((d, isHigh) -> {
            float[] levels = d.getLevels();
            for (int i = 0; i < levels.length; i++) {
                levels[i] = isHigh ? levels[i] : 0f;
            }
            d.setLevels(levels);
        }),
        COLOR_TO_WHITE((d, isHigh) -> {
            float[] levels = d.getLevels();
            for (int i = 0; i < levels.length; i++) {
                levels[i] = isHigh ? 1f : levels[i];
            }
            d.setLevels(levels);
        });

        private final TaskTuple<DMXDevice, Boolean> task;

        StrobeMode(TaskTuple<DMXDevice, Boolean> task) {
            this.task = task;
        }

    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);
        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean isHigh = new AtomicBoolean(false);

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return StrobeEffect.this;
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
                        StrobeEffect.this.mode = myPanel.modeSelector.getSelectedEnum();
                        StrobeEffect.this.nCycles = myPanel.slider.getValue();

                        if (counter.incrementAndGet() >= StrobeEffect.this.nCycles) {
                            isHigh.set(!isHigh.get());
                            counter.set(0);
                        }

                        int freqHz = (StaticConfig.CLOCK_FREQUENCY_HZ) / StrobeEffect.this.nCycles / 2;
                        String labelText = freqHz + " Hz";
                        if (!myPanel.sliderLabel.getText().equals(labelText)) {
                            SwingUtilities.invokeLater(() -> {
                                myPanel.sliderLabel.setText(labelText);
                                myPanel.updateUI();
                            });
                        }

                        DMXDevice[] devices = deviceGroup.getDevices();
                        for (int i = 0; i < devices.length; i++) {
                            if (mode != null) {
                                mode.task.process(devices[i], isHigh.get());
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                StrobeEffect.this.enabled = enabled;
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
        private final JSlider slider;
        private final JLabel sliderLabel;
        private final EnumComboBox<StrobeMode> modeSelector;

        public MyEffectPanel(DeviceGroup deviceGroup) {
            setLayout(new GridLayout(0, 2));

            synchronized (sync) {
                slider = new JSlider(1, 10, StrobeEffect.this.nCycles);
                sliderLabel = new JLabel("Waiting for");
                modeSelector = new EnumComboBox(StrobeEffect.this.mode);
            }

            this.add(sliderLabel);
            this.add(slider);
            this.add(new JLabel("Select mode: "));
            this.add(modeSelector);
        }
    }

}
