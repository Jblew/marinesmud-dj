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
    private final PitchPreview aThis = this;

    public PitchPreview(SoundProcessingManager spm) {

    }

    @Override
    public String getName() {
        return "Pitch preview";
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

    private class MyWorker extends EffectWorker {
        private final AtomicReference<PitchPreviewPanel> myPanelRef = new AtomicReference<>(null);
        private final Listener listener = (attachment) -> {
            float pitch = (Float) attachment;
            PitchPreviewPanel panel = myPanelRef.get();
            if (panel != null) {
                panel.setPitch(pitch, true);
            }
        };
        
        public MyWorker(DeviceGroup initialDeviceGroup) {
            
        }

        @Override
        public void init() {
            PitchProcessor.getInstance().addListener(listener);
        }

        @Override
        public void stop() {
            PitchProcessor.getInstance().removeListener(listener);
        }

        @Override
        public Effect getEffect() {
            return aThis;
        }

        @Override
        public EffectPanel createEffectPanel() {
            PitchPreviewPanel p = new PitchPreviewPanel();
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void setDeviceGroup(DeviceGroup group) {
        }
    }

    private static class PitchPreviewPanel extends EffectPanel {
        private final JLabel pitchLabel = new JLabel("Waiting for pitch...");

        public PitchPreviewPanel() {
            pitchLabel.setForeground(Color.RED);
            this.add(pitchLabel);
        }

        public void setPitch(double pitch, boolean probability) {
            SwingUtilities.invokeLater(() -> {
                pitchLabel.setForeground(Color.GREEN);
                pitchLabel.setText(pitch + " Hz");
            });
        }
    }

}
