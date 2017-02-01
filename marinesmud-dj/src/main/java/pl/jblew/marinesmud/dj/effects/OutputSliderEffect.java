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
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.DimmableDevice;
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
public class OutputSliderEffect implements Effect {

    public OutputSliderEffect(SoundProcessingManager spm) {

    }

    @Override
    @JsonIgnore
    public String getName() {
        return "Outpus sliders";
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

    private class MyWorker extends EffectWorker {
        private final AtomicReference<DeviceGroup> deviceGroupRef = new AtomicReference<>(null);
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup initialDeviceGroup) {
            deviceGroupRef.set(initialDeviceGroup);
        }

        @Override
        public void init() {
        }

        @Override
        public void stop() {
        }

        @Override
        public Effect getEffect() {
            return OutputSliderEffect.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            MyEffectPanel p = new MyEffectPanel(deviceGroupRef);
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void setDeviceGroup(DeviceGroup group) {
            deviceGroupRef.set(group);
        }

    }

    private static class MyEffectPanel extends EffectPanel {
        private final JSlider [] sliders = new JSlider [5];

        public MyEffectPanel(AtomicReference<DeviceGroup> deviceGroupRef) {
           setLayout(new GridLayout(0,2));
           for(int i = 0;i < sliders.length; i++) {
               int sliderIndex = i;
               JSlider slider = new JSlider(0,1000,0);
               JLabel sliderLabel = new JLabel((sliderIndex+1)+" (0%)");
               slider.addChangeListener(e -> {
                   float percent = (float)slider.getValue()/1000f;
                   float v = (float) Math.pow(percent, 2);
                   DeviceGroup g = deviceGroupRef.get();
                   if(g != null) {
                       DMXDevice [] devices = g.getDevices();
                       if(devices.length > sliderIndex) {
                           if(devices[sliderIndex] instanceof DimmableDevice) {
                               ((DimmableDevice)devices[sliderIndex]).setCommonLevel(v);
                           }
                       }
                   }
                   else {
                       
                   }
                   sliderLabel.setText((sliderIndex+1)+" ("+(int)(v*100f)+"%)");
               });
               
               sliders[i] = slider;
               
               this.add(sliderLabel);
               this.add(slider);
           }
        }
    }

}
