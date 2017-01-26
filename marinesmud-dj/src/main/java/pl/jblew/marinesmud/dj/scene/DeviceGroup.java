/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

/**
 *
 * @author teofil
 */
public class DeviceGroup {
    private final String name;
    private final DMXDevice [] devices;

    public DeviceGroup(String name, DMXDevice[] devices) {
        this.name = name;
        this.devices = devices;
    }

    public String getName() {
        return name;
    }

    public DMXDevice[] getDevices() {
        return devices;
    }

    @Override
    public String toString() {
        return name + " (" + devices.length + ')';
    }
    
    
}
