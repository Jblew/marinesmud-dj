/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound.processors;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.util.fft.FFT;
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.util.Listener;
import pl.jblew.marinesmud.dj.util.ListenersManager;

/**
 *
 * @author teofil
 */
public class FFTProcessor implements AudioProcessor, Processor {
    private static final FFTProcessor INSTANCE = new FFTProcessor();
    private final ListenersManager listenersManager = new ListenersManager();

    private final int bufferSize = StaticConfig.BUFFER_SIZE;
    private final FFT fft;
    private final float[] amplitudes;

    private FFTProcessor() {
        fft = new FFT(bufferSize);
        amplitudes = new float[bufferSize / 2];
    }

    public static FFTProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioFloatBuffer = audioEvent.getFloatBuffer();
        float[] transformbuffer = new float[bufferSize * 2];
        System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
        fft.forwardTransform(transformbuffer);
        fft.modulus(transformbuffer, amplitudes);

        listenersManager.fireEvent(amplitudes);
        return true;
    }

    @Override
    public void processingFinished() {

    }

    @Override
    public void init(AudioDispatcher dispatcher) {
        dispatcher.addAudioProcessor(this);
        System.out.println("Init FFTProcessor");
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
