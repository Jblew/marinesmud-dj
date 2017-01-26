/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;

/**
 *
 * @author teofil
 */
public abstract class EffectWorker {
    public abstract Effect getEffect();
    public abstract void init();
    public abstract void stop();
    public abstract void setDeviceGroup(DeviceGroup group);
    public abstract EffectPanel createEffectPanel();
}
