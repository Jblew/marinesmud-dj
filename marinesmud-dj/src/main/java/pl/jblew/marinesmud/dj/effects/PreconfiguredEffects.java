/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author teofil
 */
public class PreconfiguredEffects implements Iterable<Effect> {
    private final List<Effect> effects = Collections.synchronizedList(new LinkedList<Effect>());
    
    public PreconfiguredEffects() {
        this.registerEffect(new EmptyEffect());
        //effects.registerEffect(new SpectrogramPreview());
        this.registerEffect(new PitchPreview());
        this.registerEffect(new LatentSpectrogramEffect(1));
        this.registerEffect(new LatentSpectrogramEffect(3));
        this.registerEffect(new LatentSpectrogramEffect(5));
        this.registerEffect(new StaticSliderEffect());
        this.registerEffect(new BalanceCorrectionEffect());
        this.registerEffect(new RomanticColorWheel());
        this.registerEffect(new StaticColor());
        this.registerEffect(new StrobeEffect());
        this.registerEffect(new BeatSpiker());
        this.registerEffect(new KeyboardSpiker());
    }
        
    private  void registerEffect(Effect effect) {
        effects.add(effect);
    }

    @Override
    public Iterator<Effect> iterator() {
        return effects.iterator();
    }
    
    public Stream<Effect> stream() {
        return effects.stream();
    }
}
