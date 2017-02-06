/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import pl.jblew.marinesmud.dj.clock.ClockWorker;
import pl.jblew.marinesmud.dj.effects.Effect;
import pl.jblew.marinesmud.dj.effects.EffectWorker;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class SoundProcessingManager {
    private final MixerChangeListener mixerChangeListener;
    private final LiveSoundProcessor lsp;
    private final AtomicReference<Mixer> currentMixerRef = new AtomicReference<>(null);
    private final AtomicBoolean mixerNeedsUpdate = new AtomicBoolean(false);
    private final Set<Processor> processorChain = Collections.synchronizedSet(new HashSet<>());
    private final Set<EffectWorker> workers = Collections.synchronizedSet(new HashSet<>());
    private final Runnable dispatchAudioTask;
    private final Runnable processAudioTask;

    public SoundProcessingManager() {
        lsp = new LiveSoundProcessor();

        mixerChangeListener = mixer -> {
            currentMixerRef.set(mixer);
            mixerNeedsUpdate.set(true);
        };

        dispatchAudioTask = () -> {
            if (mixerNeedsUpdate.get()) {
                mixerNeedsUpdate.set(false);
                if (currentMixerRef.get() != null) {
                    updateMixer(currentMixerRef.get());
                }
            }
        };
        processAudioTask = () -> {
            for (Processor p : processorChain) {
                p.process();
            }
        };

    }

    public MixerChangeListener getMixerChangeListener() {
        return mixerChangeListener;
    }

    public Runnable getDispatchAudioTask() {
        return dispatchAudioTask;
    }

    public Runnable getProcessAudioTask() {
        return processAudioTask;
    }

    public EffectWorker registerEffectWorker(EffectWorker worker) {
        workers.add(worker);
        updateProcessors();
        return worker;
    }

    public void unregisterEffectWorker(EffectWorker worker) {
        if (workers.contains(worker)) {
            workers.remove(worker);

            System.out.println("Removing effect " + worker);
            updateProcessors();
        }
    }

    public void updateProcessors() {
        Set<Effect> effects = new HashSet<>();
        workers.stream().forEach(ew -> effects.add(ew.getEffect()));

        synchronized (processorChain) {
            processorChain.clear();
            for (Effect e : effects) {
                for (Processor requiredProcessor : e.getRequiredProcessors()) {
                    resolveProcessorDependencies(requiredProcessor);
                }
            }
            mixerNeedsUpdate.set(true);
        }
    }

    public void clearResults() {
        synchronized (processorChain) {
            for (Processor p : processorChain) {
                p.clearResults();
            }
        }
    }

    private void updateMixer(Mixer mixer) {
        synchronized (processorChain) {
            try {
                System.out.println("Initializing mixer with processors: " + processorChain.stream().map(p -> p.getClass().getSimpleName()).reduce("", (a, b) -> a + ", " + b));
                lsp.setNewMixer(mixer, processorChain.toArray(new Processor[]{}));
                currentMixerRef.set(mixer);

                for (EffectWorker worker : workers) {
                    worker.reload();
                }
            } catch (LineUnavailableException | UnsupportedAudioFileException ex) {
                Logger.getLogger(SoundProcessingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void resolveProcessorDependencies(Processor processor) {
        synchronized (processorChain) {
            if (!processorChain.contains(processor)) {
                //queue
                for (Processor dependency : processor.getRequiredProcessors()) {
                    resolveProcessorDependencies(dependency);
                }
                processorChain.add(processor);
            }
        }
    }

    public void reset() {
        synchronized (processorChain) {
            for (Processor p : processorChain) {
                p.clearResults();
            }
            processorChain.clear();
        }
        workers.clear();
    }
}
