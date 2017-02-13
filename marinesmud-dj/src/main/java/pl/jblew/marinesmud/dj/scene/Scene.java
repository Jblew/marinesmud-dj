/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.jblew.marinesmud.dj.scene.devices.LedBar;
import pl.jblew.marinesmud.dj.scene.devices.Relay;
import pl.jblew.marinesmud.dj.scene.devices.SingleDimmer;

/**
 *
 * @author teofil
 */
public class Scene {
    public DMXDevice[] devices = new DMXDevice[]{
        new SingleDimmer("demo_dimmer_1", 9),
        new SingleDimmer("demo_dimmer_2", 10),
        new SingleDimmer("demo_dimmer_3", 11),
        new LedBar("pod_scena_1", 20),
        new LedBar("pod_scena_2", 23),
        new LedBar("pod_scena_3", 26),
        new LedBar("pod_scena_4", 29),
        new LedBar("pod_scena_5", 32),
        new Relay("gniazdko_A", 50),
        new Relay("gniazdko_B", 51),
        new Relay("gniazdko_C", 52),
        new Relay("gniazdko_D", 53),

    };

    @JsonIgnore
    public int getMaxAddr() {
        int maxAddr = 1;
        for (DMXDevice d : devices) {
            maxAddr = Math.max(d.getStartAddress() + d.getChannelCount() - 1, maxAddr);
        }
        return maxAddr;
    }
}
