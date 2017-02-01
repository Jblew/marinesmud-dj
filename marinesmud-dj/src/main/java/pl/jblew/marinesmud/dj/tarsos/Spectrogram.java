/*
 *      _______                       _____   _____ _____  
 *     |__   __|                     |  __ \ / ____|  __ \ 
 *        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
 *        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
 *        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
 *                                                         
 * -------------------------------------------------------------
 *
 * TarsosDSP is developed by Joren Six at IPEM, University Ghent
 *  
 * -------------------------------------------------------------
 *
 *  Info: http://0110.be/tag/TarsosDSP
 *  Github: https://github.com/JorenSix/TarsosDSP
 *  Releases: http://0110.be/releases/TarsosDSP/
 *  
 *  TarsosDSP includes modified source code by various authors,
 *  for credits and info, see README.
 * 
 */
package pl.jblew.marinesmud.dj.tarsos;

import pl.jblew.marinesmud.dj.gui.NorthToolbar;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.util.fft.FFT;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JToolBar;
import pl.jblew.marinesmud.dj.visualise.ColorophoneWindow;

public class Spectrogram extends JFrame implements PitchDetectionHandler {

    /**
     *
     */
    private static final long serialVersionUID = 1383896180290138076L;
    private final SpectrogramPanel panel;
    private AudioDispatcher dispatcher;
    private Mixer currentMixer;
    private PitchEstimationAlgorithm algo;
    private double pitch;
    private double pitchProbability;
    private double last_pitch = 0;

    private float sampleRate = 44100;
    private int bufferSize = 1024 * 4;
    private int overlap = 768 * 4;
    private double percussion_sensitivity = 96d;
    private double percussion_threshold = 12d;
    private float minBrightness = 0.5f;
    private float maxBrightness = 1f;
    private float fadefactor = 0.9f;

    private String fileName;
    private final ColorophoneWindow colorophone;
    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    private ActionListener algoChangeListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            String name = e.getActionCommand();
            PitchEstimationAlgorithm newAlgo = PitchEstimationAlgorithm.valueOf(name);
            algo = newAlgo;
            try {
                setNewMixer(currentMixer);
            } catch (LineUnavailableException e1) {
                e1.printStackTrace();
            } catch (UnsupportedAudioFileException e1) {
                e1.printStackTrace();
            }
        }
    };

    public Spectrogram(String fileName) {
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Spectrogram");
        panel = new SpectrogramPanel();
        algo = PitchEstimationAlgorithm.DYNAMIC_WAVELET;
        this.fileName = fileName;
        
        this.colorophone = new ColorophoneWindow(7);

        JPanel pitchDetectionPanel = new PitchDetectionPanel(algoChangeListener);

        JToolBar inputPanel = new InputPanel();

        inputPanel.addPropertyChangeListener("mixer",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent arg0) {
                        try {
                            setNewMixer((Mixer) arg0.getNewValue());
                        } catch (LineUnavailableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (UnsupportedAudioFileException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

        JPanel containerPanel = new JPanel(new GridLayout(1, 0));
        containerPanel.add(inputPanel);
        containerPanel.add(pitchDetectionPanel);
        this.add(containerPanel, BorderLayout.NORTH);

        JPanel otherContainer = new JPanel(new BorderLayout());
        otherContainer.add(panel, BorderLayout.CENTER);
        otherContainer.setBorder(new TitledBorder("3. Utter a sound (whistling works best)"));

        this.add(otherContainer, BorderLayout.CENTER);
        
        exec.scheduleAtFixedRate(new Runnable() {

            public void run() {
                paintTick();
            }
        }, 30, 30, TimeUnit.MILLISECONDS);
    }

    private void setNewMixer(Mixer mixer) throws LineUnavailableException, UnsupportedAudioFileException {

        if (dispatcher != null) {
            dispatcher.stop();
        }
        if (fileName == null) {
            final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
                    false);
            final DataLine.Info dataLineInfo = new DataLine.Info(
                    TargetDataLine.class, format);
            TargetDataLine line;
            line = (TargetDataLine) mixer.getLine(dataLineInfo);
            final int numberOfSamples = bufferSize;
            line.open(format, numberOfSamples);
            line.start();
            final AudioInputStream stream = new AudioInputStream(line);

            JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
            // create a new dispatcher
            dispatcher = new AudioDispatcher(audioStream, bufferSize,
                    overlap);
        } else {
            try {
                File audioFile = new File(fileName);
                dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
                AudioFormat format = AudioSystem.getAudioFileFormat(audioFile).getFormat();
                dispatcher.addAudioProcessor(new AudioPlayer(format));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        currentMixer = mixer;

		// add a processor, handle pitch event.
        //dispatcher.addAudioProcessor(new HighPass(5000, sampleRate));
        //dispatcher.addAudioProcessor(new LowPassFS(1000, sampleRate));
        dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
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

    AudioProcessor fftProcessor = new AudioProcessor() {

        FFT fft = new FFT(bufferSize);
        float[] amplitudes = new float[bufferSize / 2];
        //float[] lastAmplitudes = new float[bufferSize / 2];

        @Override
        public void processingFinished() {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean process(AudioEvent audioEvent) {
            //System.arraycopy(amplitudes, 0, lastAmplitudes, 0, amplitudes.length);

            float[] audioFloatBuffer = audioEvent.getFloatBuffer();
            float[] transformbuffer = new float[bufferSize * 2];
            System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
            fft.forwardTransform(transformbuffer);
            fft.modulus(transformbuffer, amplitudes);

            panel.drawFFT(pitch, amplitudes, fft);
            
                
                //colorophone.setBrightnessForAll(Math.max(minBrightness, fadefactor*colorophone.getBrightness(0)));
            return true;
        }

    };

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        if (pitchDetectionResult.isPitched()) {
            pitch = pitchDetectionResult.getPitch();
            pitchProbability = pitchDetectionResult.getProbability();
        } else {
            pitch = -1;
        }
    }
    
    public void paintTick() {
        double usable_pitch = pitch;
            if(pitch > 0) last_pitch = pitch;
            else usable_pitch = last_pitch;
            
                float hue = (float) SpectrogramPanel.frequencyToBin(usable_pitch, 1000) / 1000f;
                float lasthue = colorophone.getColor(0);
                float difference = hue - lasthue;
                float newhue0 = lasthue + Math.min(Math.max(difference, -0.01f), 0.01f);
                float newhue1 = lasthue + Math.min(Math.max(difference, -0.04f), 0.04f);
                float newhue2 = lasthue + Math.min(Math.max(difference, -0.3f), 0.3f);
                float newhue3 = lasthue + Math.min(Math.max(difference, -0.5f), 0.5f);
                colorophone.setColor(0, newhue0);
                colorophone.setColor(1, newhue1);
                colorophone.setColor(2, newhue2);
                colorophone.setColor(3, newhue3);
                colorophone.setColor(4, newhue2);
                colorophone.setColor(5, newhue1);
                colorophone.setColor(6, newhue0);
                //colorophone.setBrightness(3, (float)pitchProbability);
    }

    public static void main(final String... strings) throws InterruptedException,
            InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // ignore failure to set default look en feel;
                }
                JFrame frame = strings.length == 0 ? new Spectrogram(null) : new Spectrogram(strings[0]);
                frame.pack();
                frame.setSize(640, 480);
                frame.setVisible(true);
            }
        });
    }

}
