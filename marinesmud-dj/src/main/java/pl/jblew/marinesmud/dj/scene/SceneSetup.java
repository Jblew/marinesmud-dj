/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.jblew.marinesmud.dj.scene.devices.LedBar;
import java.util.LinkedList;
import java.util.List;
import pl.jblew.marinesmud.dj.effects.BalanceCorrectionEffect;
import pl.jblew.marinesmud.dj.effects.Effect;
import pl.jblew.marinesmud.dj.effects.LatentSpectrogramEffect;
import pl.jblew.marinesmud.dj.effects.PitchPreview;
import pl.jblew.marinesmud.dj.effects.StaticSliderEffect;
import pl.jblew.marinesmud.dj.scene.devices.SingleDimmer;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;

/**
 *
 * @author teofil
 */
public class SceneSetup {
    public String setupName = "defaultSetup";

    public DeviceGroup.Serializator[] groups;

    {
        DeviceGroup.Serializator s_statusGroup = new DeviceGroup.Serializator("StatusGroup", new String[]{});
        s_statusGroup.effects = new Effect[]{
            new PitchPreview(false), //new SpectrogramPreview(false)
        };

        DeviceGroup.Serializator s_podScena = new DeviceGroup.Serializator("PodScena", new String[]{"pod_scena_1", "pod_scena_2", "pod_scena_3", "pod_scena_4", "pod_scena_5"});
        s_podScena.effects = new Effect[]{
            new StaticSliderEffect(),
            new LatentSpectrogramEffect(5),
            new BalanceCorrectionEffect()
        };

        DeviceGroup.Serializator s_DemoBar = new DeviceGroup.Serializator("DemoBar", new String[]{"demo_dimmer_1", "demo_dimmer_2", "demo_dimmer_3"});
        s_DemoBar.effects = new Effect[]{
            new StaticSliderEffect()
        };

        groups = new DeviceGroup.Serializator[]{s_statusGroup, s_podScena, s_DemoBar};
    }

    @Override
    public String toString() {
        return setupName;
    }
    
    

    public static class Current {
        @JsonIgnore
        private final SceneSetup setup;
        public final DMXDevice[] devices;
        public final DeviceGroup[] groups;

        public Current(SceneSetup setup, Scene scene) {
            this.setup = setup;
            devices = scene.devices;
            List<DeviceGroup> groupsList = new LinkedList<>();
            //groupsList.add(new DeviceGroup("empty_group", new DMXDevice[]{}));

            int i = 0;
            for (DeviceGroup.Serializator s : setup.groups) {
                groupsList.add(s.toGroup(scene.devices));
            }

            groups = groupsList.toArray(new DeviceGroup[]{});
        }

        public void init(SoundProcessingManager spm) {
            for (DeviceGroup g : groups) {
                g.initEffects(spm);
            }
        }

        public void processEffects(SoundProcessingManager spm) {
            for (DeviceGroup g : groups) {
                g.processEffects(spm);
            }
            spm.clearResults();
        }

        public void processVisualisations() {
            for (DeviceGroup g : groups) {
                g.processVisualisations();
            }
        }

        public void stop(SoundProcessingManager spm) {
            spm.reset();
            
            for (DeviceGroup g : groups) {
                g.stopEffects(spm);
            }
        }

        @JsonIgnore
        public SceneSetup getSceneSetup() {
            return setup;
        }
    }
}
