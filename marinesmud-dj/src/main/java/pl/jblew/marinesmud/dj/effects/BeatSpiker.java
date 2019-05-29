/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.effects.visualutil.GradientHue;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
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
public class BeatSpiker implements Effect {
    public boolean enabled = true;

    @JsonIgnore
    private final Object sync = new Object();

    public BeatSpiker() {
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor[]{/*OnsetProcessor.getInstance()*/};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }

    @Override
    public BeatSpiker deriveEffect() {
        synchronized (sync) {
            BeatSpiker clone = new BeatSpiker();
            clone.enabled = this.enabled;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);
        
        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return BeatSpiker.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            MyEffectPanel p = new MyEffectPanel();
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
                boolean wasOnset = false;
                /*for (Object res_ : OnsetProcessor.getInstance().getResults()) {
                    OnsetProcessor.Result result = (OnsetProcessor.Result) res_;
                    wasOnset = true;
                }*/
                
                for(DMXDevice d : deviceGroup.getDevices()) {
                    d.setCommonLevel((wasOnset? 1f : 0f));
                    if(wasOnset) System.out.println("Bum!");
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                BeatSpiker.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }
    }

    private static class MyEffectPanel extends EffectPanel {
        private final JLabel beatLabel = new JLabel("Waiting for beats...");

        public MyEffectPanel() {
            this.add(beatLabel);
        }
    }

}
