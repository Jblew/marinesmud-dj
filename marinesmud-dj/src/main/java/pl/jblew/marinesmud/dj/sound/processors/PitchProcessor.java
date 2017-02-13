/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound.processors;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.util.fft.FFT;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.util.Listener;
import pl.jblew.marinesmud.dj.util.ListenersManager;

/**
 *
 * @author teofil
 */
public class PitchProcessor implements Processor {
    private static final PitchProcessor INSTANCE = new PitchProcessor();
    private final List<Object> results = Collections.synchronizedList(new LinkedList<>());
    private final PitchDetectionHandler pitchHandler = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult pdr, AudioEvent ae) {
            if(pdr.isPitched()) {
                Result r = new Result();
                r.pitch = pdr.getPitch();
                //System.out.println("probability="+pdr.getProbability());
                r.pitchProbability = pdr.getProbability();
                results.add(r);
                //System.out.println("PitchProcessor.results.add("+r.pitch+")");
            }
        }
    };


    private PitchProcessor() {

    }

    public static PitchProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void init(AudioDispatcher dispatcher) {
        dispatcher.addAudioProcessor(new be.tarsos.dsp.pitch.PitchProcessor(PitchEstimationAlgorithm.DYNAMIC_WAVELET, StaticConfig.SAMPLE_RATE, StaticConfig.BUFFER_SIZE, pitchHandler));
        
        //dispatcher.addAudioProcessor(new be.tarsos.dsp.pitch.PitchProcessor(PitchEstimationAlgorithm.YIN, StaticConfig.SAMPLE_RATE, StaticConfig.BUFFER_SIZE, pitchHandler));
        
        //System.out.println("Init PitchProcessor");
    }

    /*@Override
    public void stop() {
System.out.println("Stop FFTProcessor");
    }*/

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor[]{};
    }


    @Override
    public void process() {
    }

    @Override
    public Iterable<Object> getResults() {
        //System.out.println("PitchProcessor.getResults()");
        return results;
    }

    @Override
    public void clearResults() {
        //System.out.println("PitchProcessor.clearResults()");
        results.clear();
    }

    public int getResultCount() {
        return results.size();
    }
    
    public static class Result {
        public float pitch;
        public float pitchProbability;
    }
}
