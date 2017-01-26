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
public class Effects implements Iterable<Effect> {
    private final List<Effect> effects = Collections.synchronizedList(new LinkedList<Effect>());
    
    public Effects() {}
    
    public  void registerEffect(Effect effect) {
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
