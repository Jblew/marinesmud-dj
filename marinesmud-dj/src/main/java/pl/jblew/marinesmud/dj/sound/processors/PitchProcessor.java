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
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.util.Listener;
import pl.jblew.marinesmud.dj.util.ListenersManager;

/**
 *
 * @author teofil
 */
public class PitchProcessor implements Processor {
    private static final PitchProcessor INSTANCE = new PitchProcessor();
    private final ListenersManager listenersManager = new ListenersManager();
    private final PitchDetectionHandler pitchHandler = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult pdr, AudioEvent ae) {
            if(pdr.isPitched()) listenersManager.fireEvent(pdr.getPitch());
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
        System.out.println("Init PitchProcessor");
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
    public void addListener(Listener l) {
        listenersManager.addListener(l);
    }

    @Override
    public void removeListener(Listener l) {
        listenersManager.removeListener(l);
    }
}
