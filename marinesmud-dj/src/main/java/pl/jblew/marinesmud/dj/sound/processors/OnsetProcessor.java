/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound.processors;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import pl.jblew.marinesmud.dj.config.StaticConfig;

/**
 *
 * @author teofil
 */
/*
public class OnsetProcessor implements Processor {
    private static final OnsetProcessor INSTANCE = new OnsetProcessor();
    private final List<Object> results = Collections.synchronizedList(new LinkedList<>());

    private final OnsetHandler onsetHandler = (double time, double salience) -> {
        Result r = new Result();
        r.salience = salience;
        results.add(r);
    };

    private final double sensitivity = 50;
    private final double threshold = 0.4;

    private OnsetProcessor() {

    }

    public static OnsetProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void init(AudioDispatcher dispatcher) {
        ComplexOnsetDetector onsetDetector = new ComplexOnsetDetector(StaticConfig.BUFFER_SIZE, threshold,0.07,-60);
        onsetDetector.setHandler(onsetHandler);
        dispatcher.addAudioProcessor(
                //new PercussionOnsetDetector(
                //        StaticConfig.SAMPLE_RATE, StaticConfig.BUFFER_SIZE,
                //        onsetHandler,
                //        sensitivity, threshold)
                onsetDetector
        );

        //dispatcher.addAudioProcessor(new be.tarsos.dsp.pitch.PitchProcessor(PitchEstimationAlgorithm.YIN, StaticConfig.SAMPLE_RATE, StaticConfig.BUFFER_SIZE, pitchHandler));
        //System.out.println("Init PitchProcessor");
    }

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
        public double salience;
    }
}
*/