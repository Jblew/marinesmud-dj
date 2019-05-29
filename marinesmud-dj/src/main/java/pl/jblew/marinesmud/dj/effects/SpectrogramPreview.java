/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.gui.BWSpectrogramPanel;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.FFTProcessor;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.util.Listener;

/**
 *
 * @author teofil
 */
public class SpectrogramPreview implements Effect {
    public boolean enabled = true;
    
    @JsonIgnore
    private final Object sync = new Object();

    public SpectrogramPreview() {

    }

    public SpectrogramPreview(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return "Spectrogram preview";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    @JsonIgnore
    public Processor[] getRequiredProcessors() {
        return new Processor[]{FFTProcessor.getInstance()};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }
    
    @Override
    public SpectrogramPreview deriveEffect() {
        synchronized (sync) {
            SpectrogramPreview clone = new SpectrogramPreview();
            clone.enabled = this.enabled;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final AtomicBoolean enabled = new AtomicBoolean(SpectrogramPreview.this.enabled);
        private final AtomicReference<SpectrogramPreviewPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {

        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return SpectrogramPreview.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            SpectrogramPreviewPanel p = new SpectrogramPreviewPanel();
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            if (enabled.get()) {
                System.out.println("SpectrogramPreview.process");
                SpectrogramPreviewPanel panel = myPanelRef.get();
                if (panel != null) {
                    System.out.println("Drawing "+FFTProcessor.getInstance().getResultCount()+" amplitudes");
                    for (Object res_ : FFTProcessor.getInstance().getResults()) {
                        float[] amplitudes = ((FFTProcessor.Result) res_).amplitudes;

                        //panel.drawFFT(0, amplitudes);
                        processAmplitudes(amplitudes);
                        System.out.println("FFT drawn");
                    }
                }
            }
            System.out.println("SpectrogramPreview processing finished");
        }
        
        private void processAmplitudes(float [] amplitudes) {
            int groups = 10;
        }

        @Override
        public void setEnabled(boolean enabled) {
            SpectrogramPreview.this.enabled = enabled;
            this.enabled.set(enabled);
        }

        @Override
        public boolean isEnabled() {
            return this.enabled.get();
        }
    }

    private static class SpectrogramPreviewPanel extends EffectPanel {
        private final BWSpectrogramPanel spectrogramPanel;

        public SpectrogramPreviewPanel() {
            spectrogramPanel = new BWSpectrogramPanel(); //should be in constructor. Constructor is called in AWT EDT Thread.

            this.setPreferredSize(new Dimension(400, 250));
            this.setLayout(new BorderLayout());
            //this.add(new JLabel("MultiBarPitchFaderEffectPanel"), BorderLayout.NORTH);
            this.add(spectrogramPanel, BorderLayout.CENTER);
        }

        public void drawFFT(double pitch, float[] amplitudes) {
            //System.out.println("Draw FFT in thread "+Thread.currentThread().getName());
            System.out.println("Start drawing FFT");
            float [] amplitudesNew = new float[amplitudes.length];
            System.arraycopy(amplitudes, 0, amplitudesNew, 0, amplitudes.length);
            spectrogramPanel.drawFFT(pitch, amplitudesNew);
            System.out.println("Stop drawing FFT");
        }
    }

}
