package pl.jblew.marinesmud.dj;

import com.google.common.util.concurrent.AtomicDouble;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.clock.ClockWorker;
import pl.jblew.marinesmud.dj.config.Config;
import pl.jblew.marinesmud.dj.config.ConfigLoader;
import pl.jblew.marinesmud.dj.dmx.SerialOutputManager;
import pl.jblew.marinesmud.dj.dmx.OutputManager;
import pl.jblew.marinesmud.dj.dmx.SerialOutputManager;
import pl.jblew.marinesmud.dj.effects.PreconfiguredEffects;
import pl.jblew.marinesmud.dj.effects.EmptyEffect;
import pl.jblew.marinesmud.dj.effects.LatentSpectrogramEffect;
import pl.jblew.marinesmud.dj.effects.PitchPreview;
import pl.jblew.marinesmud.dj.effects.SpectrogramPreview;
import pl.jblew.marinesmud.dj.gui.GUI;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import gnu.io.RXTXCommDriver;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JSlider;
import pl.jblew.marinesmud.dj.effects.StaticSliderEffect;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.RGBDevice;

/**
 * Hello world!
 *
 */
public class App {
    private final Config config;
    private final SoundProcessingManager spm;
    private final ClockWorker clock;
    private final PreconfiguredEffects effects;
    private final OutputManager outputManager;
    private final AtomicReference<GUI> currentGUIRef = new AtomicReference<>(null);
    private final AtomicReference<SceneSetup.Current> currentSceneSetupRef = new AtomicReference<>(null);

    public App() {
        this.config = ConfigLoader.loadConfig();
        if (config.setups.isEmpty()) {
            throw new RuntimeException("No scene setups specified");
        }

        this.clock = new ClockWorker();
        this.spm = new SoundProcessingManager();
        this.effects = new PreconfiguredEffects();
        this.outputManager = new SerialOutputManager(clock, config.scene);

        clock.setTask(ClockWorker.TASK_DISPATCH_AUDIO, spm.getDispatchAudioTask());
        clock.setTask(ClockWorker.TASK_PROCESS_AUDIO, spm.getProcessAudioTask());
        clock.setTask(ClockWorker.TASK_PROCESS_EFFECTS, () -> {
            SceneSetup.Current currentSceneSetup = currentSceneSetupRef.get();
            if (currentSceneSetup != null) {
                currentSceneSetup.processEffects(spm);
            }
        });
        clock.setTask(ClockWorker.TASK_PROCESS_VISUALISATIONS,
                () -> {
                    int percentBusy = clock.getPercentBusy();
                    SwingUtilities.invokeLater(() -> {
                        SceneSetup.Current currentSceneSetup = currentSceneSetupRef.get();
                        if (currentSceneSetup != null) {
                            currentSceneSetupRef.get().processVisualisations();
                        }
                        GUI gui = currentGUIRef.get();
                        if (gui != null) {
                            gui.updateStatus(percentBusy);
                        }
                    });
                }
        );

        clock.start();

        reloadWithNewSceneSetup(config.setups.get(0));
    }

    public void reloadWithNewSceneSetup(SceneSetup newSetup) {
        SceneSetup.Current prevCurrentSceneSetup = currentSceneSetupRef.get();
        if (prevCurrentSceneSetup != null) {
            prevCurrentSceneSetup.stop(spm);
        }

        SceneSetup.Current newCurrentSceneSetup = new SceneSetup.Current(newSetup, config.scene);
        currentSceneSetupRef.set(newCurrentSceneSetup);

        GUI prevGui = currentGUIRef.get();
        if (prevGui != null) {
            try {
                if (SwingUtilities.isEventDispatchThread()) {
                    prevGui.silentClose();
                } else {
                    SwingUtilities.invokeAndWait(() -> {
                        prevGui.silentClose();
                    });
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        GUI gui = new GUI(spm.getMixerChangeListener(), outputManager.getPortChangeListener(), effects, spm, currentSceneSetupRef.get(), outputManager, config, this);
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                gui.show();
            } else {
                SwingUtilities.invokeAndWait(() -> {
                    gui.show();
                });
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        currentGUIRef.set(gui);
        newCurrentSceneSetup.init(spm);
    }

    public static void main(String... args) {
        new App();
    }

    public GUI getGUI() {
        return currentGUIRef.get();
    }
}
