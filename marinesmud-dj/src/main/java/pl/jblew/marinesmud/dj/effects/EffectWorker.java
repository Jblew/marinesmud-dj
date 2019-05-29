/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;

/**
 *
 * @author teofil
 */
public abstract class EffectWorker {
    public abstract Effect getEffect();
    public abstract void reload();
    public abstract void process(SoundProcessingManager spm, boolean isFirstInChain);
    public abstract void setEnabled(boolean enabled);
    public abstract boolean isEnabled();
    public abstract EffectPanel createEffectPanel();
    
}
