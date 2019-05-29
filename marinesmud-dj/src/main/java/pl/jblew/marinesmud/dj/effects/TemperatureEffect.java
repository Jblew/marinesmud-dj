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
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.effects.visualutil.GradientHue;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.gui.util.EnumComboBox;
import pl.jblew.marinesmud.dj.gui.util.Knob;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.RGBDevice;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.PitchProcessor;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.tarsos.SpectrogramPanel;

/**
 *
 * @author teofil
 */
public class TemperatureEffect implements Effect {
    public int width = 1;
    public boolean enabled = true;
    public GradientHue.Gradient gradient = GradientHue.Gradient.COLORFUL;
    public double minFrequency = 50;
    public double maxFrequency = 2000;
    public double fadeSpeed = 0.05;
    public double hue0LimiterFactor = 0.01;
    public double hue1LimiterFactor = 0.04;
    public double hue2LimiterFactor = 0.3;
    public double hue3LimiterFactor = 0.5;

    @JsonIgnore
    private final Object sync = new Object();

    public TemperatureEffect(int width) {
        if (width % 2 == 0 && width != 8) {
            throw new IllegalArgumentException("Width must be: 1, 3, 5, 7 or 8");
        }
        if (width > 8) {
            throw new IllegalArgumentException("Width must be: 1, 3, 5, 7 or 8");
        }
        this.width = width;
    }

    public TemperatureEffect() {

    }

    @Override
    public String getName() {
        return "Latent spectrogram (" + width + ")";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor[]{PitchProcessor.getInstance()};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }

    @Override
    public TemperatureEffect deriveEffect() {
        synchronized (sync) {
            TemperatureEffect clone = new TemperatureEffect();
            clone.enabled = this.enabled;
            clone.width = this.width;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);
        private final GradientHue gradientHue;

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
            gradientHue = new GradientHue(GradientHue.Gradient.COLORFUL);
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return TemperatureEffect.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            MyEffectPanel p = new MyEffectPanel();
            myPanelRef.set(p);
            return p;
        }

        private double lastHue = 0;
        private double desiredHue = 0;
        private double brightness = 1f;

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            MyEffectPanel panel = fetchVariablesFromUI();//returns null if disabled
            if (panel != null) {
                float pitch = 0;
                float mostProbablePitch = 0;
                float maxProbability = -10f;
                for (Object res_ : PitchProcessor.getInstance().getResults()) {
                    PitchProcessor.Result result = (PitchProcessor.Result) res_;
                    if (result.pitch > 0) {
                        if (result.pitchProbability > maxProbability) {
                            mostProbablePitch = result.pitch;
                            maxProbability = result.pitchProbability;
                        }
                    }
                }
                pitch = mostProbablePitch;
                //TODO: SUM(probability*pitch)/SUM(probability)

                if (pitch > 0) {
                    desiredHue = (float) SpectrogramPanel.frequencyToBin(pitch, 10000, minFrequency, maxFrequency) / 10000f;
                    desiredHue = gradientHue.getHue((float) desiredHue);
                    brightness = 1f;
                } else {
                    brightness *= (1d - fadeSpeed);
                    desiredHue = Math.min(1f, desiredHue * 0.97f);
                }

                double difference = desiredHue - lastHue;
                double newhue0 = lastHue + Math.min(Math.max(difference, -hue0LimiterFactor), hue0LimiterFactor);
                double newhue1 = lastHue + Math.min(Math.max(difference, -hue1LimiterFactor), hue1LimiterFactor);
                double newhue2 = lastHue + Math.min(Math.max(difference, -hue2LimiterFactor), hue2LimiterFactor);
                double newhue3 = lastHue + Math.min(Math.max(difference, -hue3LimiterFactor), hue3LimiterFactor);
                lastHue = newhue0;

                double[] hues = new double[]{};
                switch (width) {
                    case 1:
                        hues = new double[]{newhue3};
                        break;
                    case 3:
                        hues = new double[]{newhue1, newhue3, newhue1};
                        break;
                    case 5:
                        hues = new double[]{newhue1, newhue2, newhue3, newhue2, newhue1};
                        break;
                    case 7:
                        hues = new double[]{newhue0, newhue1, newhue2, newhue3, newhue2, newhue1, newhue0};
                        break;
                    case 8:
                        hues = new double[]{newhue0, newhue1, newhue2, newhue3, newhue3, newhue2, newhue1, newhue0};
                        break;
                    default:
                        hues = new double[]{};
                        break;
                }

                RGBDevice[] rgbDevices = Arrays.stream(deviceGroup.getDevices()).sequential()
                        .filter(d -> d instanceof RGBDevice)
                        .map(d -> (RGBDevice) d)
                        .toArray(RGBDevice[]::new);
                for (int i = 0; i < hues.length; i++) {
                    if (rgbDevices.length > i) {
                        rgbDevices[i].setColor(Color.getHSBColor((float) hues[i], 1f, (float) brightness));
                    }
                }

            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                TemperatureEffect.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }

        private MyEffectPanel fetchVariablesFromUI() {
            synchronized (sync) {
                MyEffectPanel panel = myPanelRef.get();
                if (panel != null) {
                    TemperatureEffect.this.gradient = panel.gradientSelector.getSelectedEnum();
                    if (TemperatureEffect.this.gradient != gradientHue.getGradient()) {
                        gradientHue.loadGradient(gradient);
                    }

                    TemperatureEffect.this.minFrequency = (Double) panel.minFrequencyKnob.getKnob().getValue();
                    TemperatureEffect.this.maxFrequency = (Double) panel.maxFrequencyKnob.getKnob().getValue();
                    TemperatureEffect.this.hue0LimiterFactor = (Double) panel.hue0LimiferFactorKnob.getKnob().getValue();
                    TemperatureEffect.this.hue1LimiterFactor = (Double) panel.hue1LimiferFactorKnob.getKnob().getValue();
                    TemperatureEffect.this.hue2LimiterFactor = (Double) panel.hue2LimiferFactorKnob.getKnob().getValue();
                    TemperatureEffect.this.hue3LimiterFactor = (Double) panel.hue3LimiferFactorKnob.getKnob().getValue();
                    TemperatureEffect.this.fadeSpeed = (Double) panel.fadeSpeedKnob.getKnob().getValue();

                    if (enabled) {
                        return panel;
                    }
                }
                return null;
            }
        }
    }

    private class MyEffectPanel extends EffectPanel {
        private final EnumComboBox<GradientHue.Gradient> gradientSelector;
        private final Knob<JSpinner> minFrequencyKnob;
        private final Knob<JSpinner> maxFrequencyKnob;
        private final Knob<JSpinner> hue0LimiferFactorKnob;
        private final Knob<JSpinner> hue1LimiferFactorKnob;
        private final Knob<JSpinner> hue2LimiferFactorKnob;
        private final Knob<JSpinner> hue3LimiferFactorKnob;
        private final Knob<JSpinner> fadeSpeedKnob;

        public MyEffectPanel() {
            this.setLayout(new GridLayout(0, 1));

            synchronized (sync) {
                gradientSelector = new EnumComboBox<>(TemperatureEffect.this.gradient);

                minFrequencyKnob = new Knob("Min frequency: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.minFrequency,
                                10d, 25000d, 1d
                        )),
                        "Hz");

                maxFrequencyKnob = new Knob("Max frequency: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.maxFrequency,
                                10d, 25000d, 1d
                        )),
                        "Hz");

                hue0LimiferFactorKnob = new Knob("Hue 0 limiter: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.hue0LimiterFactor,
                                0.001d, 1d, 0.001d
                        )),
                        "(%/100)");

                hue1LimiferFactorKnob = new Knob("Hue 1 limiter: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.hue1LimiterFactor,
                                0.001d, 1d, 0.001d
                        )),
                        "(%/100)");

                hue2LimiferFactorKnob = new Knob("Hue 2 limiter: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.hue2LimiterFactor,
                                0.001d, 1d, 0.001d
                        )),
                        "(%/100)");

                hue3LimiferFactorKnob = new Knob("Hue 3 limiter: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.hue3LimiterFactor,
                                0.001d, 1d, 0.001d
                        )),
                        "(%/100)");

                fadeSpeedKnob = new Knob("Fade speed: ",
                        new JSpinner(new SpinnerNumberModel(
                                TemperatureEffect.this.fadeSpeed,
                                0d, 1d, 0.001d
                        )),
                        "(%/100)");
            }

            this.add(gradientSelector);
            this.add(minFrequencyKnob);
            this.add(maxFrequencyKnob);
            this.add(hue0LimiferFactorKnob);
            this.add(hue1LimiferFactorKnob);
            this.add(hue2LimiferFactorKnob);
            this.add(hue3LimiferFactorKnob);
            this.add(fadeSpeedKnob);
        }
    }

}
