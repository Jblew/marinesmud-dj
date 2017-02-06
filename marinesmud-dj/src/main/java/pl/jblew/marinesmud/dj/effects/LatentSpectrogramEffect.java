/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
public class LatentSpectrogramEffect implements Effect {
    public int width = 1;
    public boolean enabled = true;

    @JsonIgnore
    private final Object sync = new Object();

    public LatentSpectrogramEffect(int width) {
        if (width % 2 == 0) {
            throw new IllegalArgumentException("Width must be: 1, 3, 5 or 7");
        }
        if (width > 7) {
            throw new IllegalArgumentException("Width must be: 1, 3, 5 or 7");
        }
        this.width = width;
    }

    public LatentSpectrogramEffect() {

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
    public LatentSpectrogramEffect deriveEffect() {
        synchronized (sync) {
            LatentSpectrogramEffect clone = new LatentSpectrogramEffect();
            clone.enabled = this.enabled;
            clone.width = this.width;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<LatentSpectrogramEffectPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return LatentSpectrogramEffect.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            LatentSpectrogramEffectPanel p = new LatentSpectrogramEffectPanel();
            myPanelRef.set(p);
            return p;
        }

        private float lastHue = 0;

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            boolean _enabled;
            synchronized (sync) {
                _enabled = enabled;
            }
            if (_enabled) {
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
                    float hue = (float) SpectrogramPanel.frequencyToBin(pitch, 1000) / 1000f;
                    float difference = hue - lastHue;
                    float newhue0 = lastHue + Math.min(Math.max(difference, -0.01f), 0.01f);
                    float newhue1 = lastHue + Math.min(Math.max(difference, -0.04f), 0.04f);
                    float newhue2 = lastHue + Math.min(Math.max(difference, -0.3f), 0.3f);
                    float newhue3 = lastHue + Math.min(Math.max(difference, -0.5f), 0.5f);
                    lastHue = newhue0;

                    float[] hues = new float[]{};
                    switch (width) {
                        case 1:
                            hues = new float[]{newhue3};
                            break;
                        case 3:
                            hues = new float[]{newhue1, newhue3, newhue1};
                            break;
                        case 5:
                            hues = new float[]{newhue1, newhue2, newhue3, newhue2, newhue1};
                            break;
                        case 7:
                            hues = new float[]{newhue0, newhue1, newhue2, newhue3, newhue2, newhue1, newhue0};
                            break;
                        default:
                            hues = new float[]{};
                            break;
                    }

                    RGBDevice[] rgbDevices = Arrays.stream(deviceGroup.getDevices()).sequential()
                            .filter(d -> d instanceof RGBDevice)
                            .map(d -> (RGBDevice) d)
                            .toArray(RGBDevice[]::new);
                    for (int i = 0; i < hues.length; i++) {
                        if (rgbDevices.length > i) {
                            rgbDevices[i].setColor(Color.getHSBColor(hues[i], 1f, 1f));
                        }
                    }

                    LatentSpectrogramEffectPanel panel = myPanelRef.get();
                    if (panel != null) {
                        panel.setPitch(Color.getHSBColor(newhue3, 1f, 1f), pitch);
                    }
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                LatentSpectrogramEffect.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }
    }

    private static class LatentSpectrogramEffectPanel extends EffectPanel {
        private final JLabel pitchLabel = new JLabel("Waiting for pitch...");

        public LatentSpectrogramEffectPanel() {
            pitchLabel.setForeground(Color.WHITE);
            pitchLabel.setBackground(Color.BLACK);
            this.add(pitchLabel);
        }

        public void setPitch(Color c, float pitch) {
            SwingUtilities.invokeLater(() -> {
                pitchLabel.setText(pitch + " Hz");
                //pitchLabel.setForeground(Color.GREEN);
                //setBackground(c);
            });
        }
    }

}
