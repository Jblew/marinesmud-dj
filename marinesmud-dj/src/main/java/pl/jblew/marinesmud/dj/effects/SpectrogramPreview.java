/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
    private final SpectrogramPreview aThis = this;
    
    public SpectrogramPreview(SoundProcessingManager spm) {

    }

    @Override
    public String getName() {
        return "Spectrogram preview";
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public Processor [] getRequiredProcessors() {
        return new Processor [] {FFTProcessor.getInstance()};
    }
    
    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }
    
    private class MyWorker extends EffectWorker {
        private final AtomicReference<SpectrogramPreviewPanel> myPanelRef = new AtomicReference<>(null);
        private final Listener listener = (attachment) -> {
            float [] amplitudes = (float []) attachment;
            SpectrogramPreviewPanel panel = myPanelRef.get();
            if(panel != null) {
                panel.drawFFT(0, amplitudes);
            }
        };
        
        public MyWorker(DeviceGroup initialDeviceGroup) {
            
        }
        
        @Override
        public void init() {
            FFTProcessor.getInstance().addListener(listener);
        }

        @Override
        public void stop() {
            FFTProcessor.getInstance().removeListener(listener);
        }

        @Override
        public Effect getEffect() {
            return aThis;
        }
        
        @Override
        public EffectPanel createEffectPanel() {
            SpectrogramPreviewPanel p = new SpectrogramPreviewPanel();
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void setDeviceGroup(DeviceGroup group) {
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
            spectrogramPanel.drawFFT(pitch, amplitudes);
        }
    }

}
