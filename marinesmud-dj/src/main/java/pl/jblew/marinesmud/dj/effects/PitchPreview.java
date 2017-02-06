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
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.gui.BWSpectrogramPanel;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.FFTProcessor;
import pl.jblew.marinesmud.dj.sound.processors.PitchProcessor;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.util.Listener;

/**
 *
 * @author teofil
 */
public class PitchPreview implements Effect {
    public boolean enabled = true;
    
    @JsonIgnore
    private final Object sync = new Object();

    public PitchPreview() {

    }

    public PitchPreview(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    @JsonIgnore
    public String getName() {
        return "Pitch preview";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    @JsonIgnore
    public Processor[] getRequiredProcessors() {
        return new Processor[]{PitchProcessor.getInstance()};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup deviceGroup) {
        return new MyWorker(deviceGroup);
    }
    
    @Override
    public PitchPreview deriveEffect() {
        synchronized (sync) {
            PitchPreview clone = new PitchPreview();
            clone.enabled = this.enabled;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final AtomicBoolean enabled = new AtomicBoolean(PitchPreview.this.enabled);
        private final AtomicReference<PitchPreviewPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {

        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return PitchPreview.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            PitchPreviewPanel p = new PitchPreviewPanel();
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            //System.out.println("process");
            if (enabled.get()) {
                PitchPreviewPanel panel = myPanelRef.get();
                if (panel != null) {
                    //System.out.println("processing pitch. Result count: "+PitchProcessor.getInstance().getResultCount());
                    float pitch = 0;
                    float mostProbablePitch = 0;
                    float maxProbability = -10f;
                    for (Object res_ : PitchProcessor.getInstance().getResults()) {
                        PitchProcessor.Result result = (PitchProcessor.Result) res_;
                        //System.out.println("pitch with value "+result.pitch+", probability="+result.pitchProbability);
                        if (result.pitch > 0) {
                            //System.out.println("result.pitch="+result.pitch);
                            if (result.pitchProbability > maxProbability) {
                                mostProbablePitch = result.pitch;
                                //System.out.println("mostProbablePitch="+result.pitch);
                                maxProbability = result.pitchProbability;
                            }
                        }
                    }
                    pitch = mostProbablePitch;
                    //TODO: SUM(probability*pitch)/SUM(probability)
                    //System.out.println("pitch="+pitch);
                    if(pitch > 0) panel.setPitch(pitch, true);
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            PitchPreview.this.enabled = enabled;
            this.enabled.set(enabled);
        }

        @Override
        public boolean isEnabled() {
            return this.enabled.get();
        }
    }

    private static class PitchPreviewPanel extends EffectPanel {
        private final JLabel pitchLabel = new JLabel("Waiting for pitch...");

        public PitchPreviewPanel() {
            this.add(pitchLabel);
        }

        public void setPitch(double pitch, boolean probability) {
            SwingUtilities.invokeLater(() -> {
                pitchLabel.setText(pitch + " Hz");
            });
        }
    }

}
