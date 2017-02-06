/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound.processors;

import be.tarsos.dsp.AudioDispatcher;
import java.util.List;
import pl.jblew.marinesmud.dj.util.Listener;

/**
 *
 * @author teofil
 */
public interface Processor {
    public void init(AudioDispatcher dispatcher);
    //public void stop();
    public Processor [] getRequiredProcessors();
    //public void addListener(Listener l);
    //public void removeListener(Listener l);
    public void clearResults();
    public Iterable<Object> getResults();
    public void process();
}
