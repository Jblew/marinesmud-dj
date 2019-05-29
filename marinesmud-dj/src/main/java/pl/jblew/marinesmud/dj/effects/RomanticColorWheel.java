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
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.effects.visualutil.GradientHue;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.gui.util.EnumComboBox;
import pl.jblew.marinesmud.dj.gui.util.Knob;
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
public class RomanticColorWheel implements Effect {
    public boolean enabled = true;
    public float speed = 1.0f;
    public float hueSpace = 0.1f;
    public GradientHue.Gradient gradient = GradientHue.Gradient.NATURAL_COLORWHEEL;

    @JsonIgnore
    private final Object sync = new Object();

    public RomanticColorWheel(float speed) {
        this.speed = speed;
    }

    public RomanticColorWheel() {

    }

    @Override
    public String getName() {
        return "Romantic Color Wheel";
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
    public RomanticColorWheel deriveEffect() {
        synchronized (sync) {
            RomanticColorWheel clone = new RomanticColorWheel();
            clone.enabled = this.enabled;
            clone.speed = this.speed;
            clone.hueSpace = hueSpace;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<RomanticColorWheelPanel> myPanelRef = new AtomicReference<>(null);
        private final GradientHue gradientHue;

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
            gradientHue = new GradientHue(gradient);
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return RomanticColorWheel.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            synchronized (sync) {
                RomanticColorWheelPanel p = new RomanticColorWheelPanel(speed, RomanticColorWheel.this.gradient);
                myPanelRef.set(p);
                return p;
            }
        }

        private float hue = 0;
        private int line = 0;

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            RomanticColorWheelPanel panel = fetchVariablesFromUI();//if disabled, returns null
            if (panel != null) {
                

                RGBDevice[] rgbDevices = Arrays.stream(deviceGroup.getDevices()).sequential()
                        .filter(d -> d instanceof RGBDevice)
                        .map(d -> (RGBDevice) d)
                        .toArray(RGBDevice[]::new);
                float tmphue = hue;
                for (RGBDevice d : rgbDevices) {
                    Color color = gradientHue.getColor(tmphue, line);
                    d.setColor(color);
                    tmphue += hueSpace;
                }
                synchronized (sync) {
                    hue += 0.05f * speed / (float) StaticConfig.CLOCK_FREQUENCY_HZ;
                }
                if (hue > 1f) {
                    hue -= 1f;
                    line++;
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                RomanticColorWheel.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }

        private RomanticColorWheelPanel fetchVariablesFromUI() {
            synchronized (sync) {
                if (enabled) {
                    RomanticColorWheelPanel panel = myPanelRef.get();
                    if (panel != null) {
                        RomanticColorWheel.this.speed = (float) panel.speedSlider.getValue() / 100f;
                        SwingUtilities.invokeLater(() -> panel.speedLabel.setText("Speed (" + RomanticColorWheel.this.speed + ")"));
                        
                        //RomanticColorWheel.this.hueSpace = (float) panel.hueSpaceSpinner.getValue();
                        
                        RomanticColorWheel.this.gradient = panel.gradientSelector.getSelectedEnum();
                        if(RomanticColorWheel.this.gradient != gradientHue.getGradient()) {
                            System.out.println("Loading gradient: "+gradient);
                            gradientHue.loadGradient(gradient);
                        }
                        
                        return panel;
                    }
                }
                return null;
            }
        }
    }

    private class RomanticColorWheelPanel extends EffectPanel {
        private final JLabel speedLabel = new JLabel("Speed (0.5)");
        private final JSlider speedSlider = new JSlider(1, 200, 50);
        /*private final JSpinner hueSpaceSpinner = new JSpinner(new SpinnerNumberModel(
                                RomanticColorWheel.this.hueSpace,
                                0d, 1d, 0.001d
                        ));*/
        private final EnumComboBox<GradientHue.Gradient> gradientSelector;

        public RomanticColorWheelPanel(float initialSpeed, GradientHue.Gradient initialGradient) {
            this.setLayout(new GridLayout(3, 2));

            gradientSelector = new EnumComboBox<>(initialGradient);

            speedSlider.setValue((int) (initialSpeed * 100f));

            this.add(speedLabel);
            this.add(speedSlider);
            
            this.add(new JLabel("Hue space (%): "));
            //this.add(hueSpaceSpinner);

            this.add(new JLabel("Select gradient"));
            this.add(gradientSelector);
        }
    }
}
