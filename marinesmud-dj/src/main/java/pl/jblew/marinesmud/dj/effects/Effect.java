/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public interface Effect {
    public String getName();
    public Processor [] getRequiredProcessors();
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup);
    
    @Override
    public String toString();
}
