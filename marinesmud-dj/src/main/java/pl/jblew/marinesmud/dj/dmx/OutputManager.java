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
public interface OutputManager {
    public String[] listSerialPorts();
    public PortChangeListener getPortChangeListener();
}
