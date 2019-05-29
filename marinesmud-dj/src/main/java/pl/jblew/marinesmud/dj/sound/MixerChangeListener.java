/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound;

import javax.sound.sampled.Mixer;

/**
 *
 * @author teofil
 */
@FunctionalInterface
public interface MixerChangeListener {
    public void mixerChanged(Mixer mixer);
}
