/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import pl.jblew.marinesmud.dj.scene.devices.LedBar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author teofil
 */
public class SceneSetup {
    public String setupName = "defaultSetup";
    
    public DMXDevice [] devices = new DMXDevice [] {
        new LedBar("pod_scena_1", 9),
        new LedBar("pod_scena_2", 10),
        new LedBar("pod_scena_3", 11),
        new LedBar("pod_scena_4", 12),
        new LedBar("pod_scena_5", 13)
    };
    
    public DeviceGroup.Serializator [] groups;
    {
        DeviceGroup.Serializator s = new DeviceGroup.Serializator("PodScena", new String [] {"pod_scena_1", "pod_scena_2", "pod_scena_3", "pod_scena_4", "pod_scena_5"});    
        //s.effects = new Effect[] {Effects.}
        groups = new DeviceGroup.Serializator [] {s};
    }
    
    @JsonIgnore
    public int getMaxAddr() {
        int maxAddr = 1;
        for(DMXDevice d : devices) {
            maxAddr = Math.max(d.getStartAddress()+d.getChannelCount()-1, maxAddr);
        }
        return maxAddr;
    }
    
    public static class Current {
        public final DMXDevice [] devices;
        public final DeviceGroup [] groups;
        
        public Current(SceneSetup setup) {
            devices = setup.devices;
            List<DeviceGroup> groupsList = new LinkedList<>();
            groupsList.add(new DeviceGroup("empty_group", new DMXDevice[]{}));
            
            int i = 0;
            for(DeviceGroup.Serializator s : setup.groups) {
                groupsList.add(s.toGroup(setup.devices));
            }
            
            groups = groupsList.toArray(new DeviceGroup [] {});
        }
    }
}
