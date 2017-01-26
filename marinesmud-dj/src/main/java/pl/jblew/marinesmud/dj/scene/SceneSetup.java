/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

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
        new LedBar("pod_scena_1", 101),
        new LedBar("pod_scena_2", 102),
        new LedBar("pod_scena_3", 103),
        new LedBar("pod_scena_4", 104),
        new LedBar("pod_scena_5", 105)
    };
    
    public Map<String, String[]> groups = new HashMap<>();
    
    public SceneSetup() {
        groups.put("pod_scena", new String [] {
            "pod_scena_1",
            "pod_scena_2",
            "pod_scena_3",
            "pod_scena_4",
            "pod_scena_5"
        });
    }
    
    public static class Current {
        public final DMXDevice [] devices;
        public final DeviceGroup [] groups;
        
        public Current(SceneSetup setup) {
            devices = setup.devices;
            List<DeviceGroup> groupsList = new LinkedList<>();
            groupsList.add(new DeviceGroup("empty_group", new DMXDevice[]{}));
            
            int i = 0;
            for(String groupName : setup.groups.keySet()) {
                List<DMXDevice> devicesInGroup = new LinkedList<>();
                
                for(final String deviceName : setup.groups.get(groupName)) {
                    DMXDevice device = Arrays.stream(devices)
                            .filter((DMXDevice d) -> d.getName().equals(deviceName))
                            .findFirst().orElse(null);
                    if(device != null) devicesInGroup.add(device);
                }
                
                DeviceGroup group = new DeviceGroup(groupName, devicesInGroup.toArray(new DMXDevice [] {}));
                groupsList.add(group);
            }
            
            groups = groupsList.toArray(new DeviceGroup [] {});
        }
    }
}
