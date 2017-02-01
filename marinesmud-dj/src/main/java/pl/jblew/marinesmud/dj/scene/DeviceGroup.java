/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import pl.jblew.marinesmud.dj.effects.Effect;

/**
 *
 * @author teofil
 */
public class DeviceGroup {
    private final String name;
    private final DMXDevice [] devices;
    private final AtomicBoolean enabled = new AtomicBoolean (true);
    private final AtomicInteger priority = new AtomicInteger(10);
    private final List<Effect> effects = Collections.synchronizedList(new LinkedList<>());
    

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
    
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
    
    public boolean isEnabled() {
        return this.enabled.get();
    }
    
    public void setPriority(int priority) {
        this.priority.set(priority);
    }
    
    public int getPriority() {
        return this.priority.get();
    }

    @Override
    public String toString() {
        return name + " (" + devices.length + ')';
    }
    
    public static class Serializator {
        public String name = "";
        public String [] devices = new String [] {};
        public Effect [] effects = new Effect [] {};
        public boolean enabled = true;
        public int priority = 10;
        
        public Serializator(String name, String [] devices) {
            this.name = name;
            this.devices = devices;
        }
        
        public Serializator() {
            
        }
        
        public DeviceGroup toGroup(DMXDevice [] allDevices) {
            List<DMXDevice> devicesInGroup = new LinkedList<>();
                
                for(final String deviceName : devices) {
                    DMXDevice device = Arrays.stream(allDevices)
                            .filter((DMXDevice d) -> d.getName().equals(deviceName))
                            .findFirst().orElse(null);
                    if(device != null) devicesInGroup.add(device);
                }
                
                DeviceGroup group = new DeviceGroup(this.name, devicesInGroup.toArray(new DMXDevice [] {}));
                
                group.effects.addAll(Arrays.asList(effects));
                
                group.setEnabled(enabled);
                group.setPriority(priority);
                
                return group;
        }
    }
}
