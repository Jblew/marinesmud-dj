/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.dmx;

/**
 *
 * @author teofil
 */
@FunctionalInterface
public interface PortChangeListener {
    public void portChanged(String portName);
}
