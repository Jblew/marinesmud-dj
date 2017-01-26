/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound;

import pl.jblew.marinesmud.dj.sound.processors.FFTProcessor;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class LiveSoundProcessor {
    
    
    private final FFTProcessor fftProcessor;

    private AudioDispatcher dispatcher;
    private Mixer currentMixer;

    public LiveSoundProcessor(FFTProcessor fftProcessor) {
        this.fftProcessor = fftProcessor;
    }

    public void setNewMixer(Mixer mixer, Processor [] processorChain) throws LineUnavailableException, UnsupportedAudioFileException {

        if (dispatcher != null) {
            dispatcher.stop();
        }
        final AudioFormat format = new AudioFormat(StaticConfig.SAMPLE_RATE, 16, 1, true, false);
        final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line;
        line = (TargetDataLine) mixer.getLine(dataLineInfo);
        final int numberOfSamples = StaticConfig.BUFFER_SIZE;
        line.open(format, numberOfSamples);
        line.start();
        final AudioInputStream stream = new AudioInputStream(line);

        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
        // create a new dispatcher
        dispatcher = new AudioDispatcher(audioStream, StaticConfig.BUFFER_SIZE, StaticConfig.OVERLAP);
        currentMixer = mixer;

        for(Processor p : processorChain) {
            p.init(dispatcher);
        }
        
		// add a processor, handle pitch event.
        //dispatcher.addAudioProcessor(new HighPass(5000, sampleRate));
        //dispatcher.addAudioProcessor(new LowPassFS(1000, sampleRate));
        //dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
        /*dispatcher.addAudioProcessor(new PercussionOnsetDetector(sampleRate,
         bufferSize, new OnsetHandler() {

         public void handleOnset(double time, double salience) {
         //colorophone.setBrightnessForAll(maxBrightness);
         }
         }, 0, 0));*/
        //dispatcher.addAudioProcessor(fftProcessor);

        // run the dispatcher (on a new thread).
        new Thread(dispatcher, "Audio dispatching").start();
    }
}
