/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import pl.jblew.marinesmud.dj.effects.Effect;
import pl.jblew.marinesmud.dj.effects.EffectWorker;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class SoundProcessingManager {
    private final SceneSetup.Current sceneSetup;
    private final MixerChangeListener mixerChangeListener;
    private final LiveSoundProcessor lsp;
    private final Map<Effect, List<EffectWorker>> effects = Collections.synchronizedMap(new HashMap<>());
    private final AtomicReference<Mixer> currentMixerRef = new AtomicReference<>(null);

    public SoundProcessingManager(SceneSetup.Current sceneSetup) {
        lsp = new LiveSoundProcessor(null);

        mixerChangeListener = mixer -> updateMixer(mixer);
        this.sceneSetup = sceneSetup;
    }

    public EffectWorker initEffectWorker(Effect e) {
        EffectWorker worker = e.newWorker(sceneSetup.groups[0]);
        if(effects.containsKey(e)) {
            effects.get(e).add(worker);
        }
        else {
            List<EffectWorker> workers = Collections.synchronizedList(new ArrayList<>());
            workers.add(worker);
            effects.put(e, workers);
        }
        
        worker.init();
        
        System.out.println("Adding effect: "+e);
        updateMixer();
        
        return worker;
    }

    public void stopEffectWorker(EffectWorker worker) {
        if(effects.containsKey(worker.getEffect())) {
            List<EffectWorker> workers = effects.get(worker.getEffect());
            workers.remove(worker);
            if(workers.isEmpty()) effects.remove(worker.getEffect());
            
            worker.stop();
            
            System.out.println("Removing effect "+worker);
            updateMixer();
        }
    }

    public int getEffectCount() {
        return effects.size();
    }

    public MixerChangeListener getMixerChangeListener() {
        return mixerChangeListener;
    }

    private void updateMixer() {
        if (currentMixerRef.get() != null) {
            updateMixer(currentMixerRef.get());
        }
    }

    private void updateMixer(Mixer mixer) {
        try {
            Set<Processor> processorChain = new HashSet<>();

            for (Effect e : effects.keySet()) {
                for (Processor requiredProcessor : e.getRequiredProcessors()) resolveProcessorDependencies(requiredProcessor, processorChain);
            }
            System.out.println("Initializing mixer with processors: " + processorChain.stream().map(p -> p.getClass().getSimpleName()).reduce("", (a, b) -> a+", "+b));
            lsp.setNewMixer(mixer, processorChain.toArray(new Processor[]{}));
            currentMixerRef.set(mixer);
        } catch (LineUnavailableException | UnsupportedAudioFileException ex) {
            Logger.getLogger(SoundProcessingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void resolveProcessorDependencies(Processor processor, Set<Processor> processorChain) {
        if (!processorChain.contains(processor)) {
            processorChain.add(processor);
            for (Processor dependency : processor.getRequiredProcessors()) {
                resolveProcessorDependencies(dependency, processorChain);
            }
        }
    }
}
