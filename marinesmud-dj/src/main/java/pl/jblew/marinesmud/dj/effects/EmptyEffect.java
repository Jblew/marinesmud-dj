/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class EmptyEffect implements Effect {
    
    public EmptyEffect(SoundProcessingManager spm) {
    }
    
    @Override
    @JsonIgnore
    public String getName() {
        return "None";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    @JsonIgnore
    public Processor[] getRequiredProcessors() {
        return new Processor [] {};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }
    
    private class MyWorker extends EffectWorker {
        //private final Listener listener = (attachment) -> {};
        
        public MyWorker(DeviceGroup initialDeviceGroup) {
            
        }
        
        @Override
        public void init() {
            //Processor.getInstance().addListener(listener);
        }

        @Override
        public void stop() {
            //Processor.getInstance().removeListener(listener);
        }

        @Override
        public Effect getEffect() {
            return EmptyEffect.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            return new EmptyEffectPanel();
        }

        @Override
        public void setDeviceGroup(DeviceGroup group) {
        }
        
    }
    
    public static class EmptyEffectPanel extends EffectPanel {
        public EmptyEffectPanel() {
            this.add(new JLabel("Select effect from list", IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ASSIGNMENT, 16, Color.DARK_GRAY), SwingConstants.CENTER));
    
        }
    }
}
