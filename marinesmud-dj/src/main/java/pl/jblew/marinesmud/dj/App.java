package pl.jblew.marinesmud.dj;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.config.Config;
import pl.jblew.marinesmud.dj.config.ConfigLoader;
import pl.jblew.marinesmud.dj.effects.Effects;
import pl.jblew.marinesmud.dj.effects.EmptyEffect;
import pl.jblew.marinesmud.dj.effects.LatentSpectrogramEffect;
import pl.jblew.marinesmud.dj.effects.PitchPreview;
import pl.jblew.marinesmud.dj.effects.SpectrogramPreview;
import pl.jblew.marinesmud.dj.gui.GUI;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;

/**
 * Hello world!
 *
 */
public class App {
    public App() {
        Config config = ConfigLoader.loadConfig();
        if (config.setups.length == 0) {
            throw new RuntimeException("No scene setups specified");
        }
        SceneSetup.Current currentSceneSetup = new SceneSetup.Current(config.setups[0]);

        SoundProcessingManager spm = new SoundProcessingManager(currentSceneSetup);

        Effects effects = new Effects();
        effects.registerEffect(new EmptyEffect(spm));
        effects.registerEffect(new SpectrogramPreview(spm));
        effects.registerEffect(new PitchPreview(spm));
        effects.registerEffect(new LatentSpectrogramEffect(1, spm));
        effects.registerEffect(new LatentSpectrogramEffect(3, spm));
        effects.registerEffect(new LatentSpectrogramEffect(5, spm));

        try {
            SwingUtilities.invokeAndWait(() -> {
                GUI gui = new GUI(spm.getMixerChangeListener(), effects, spm, currentSceneSetup);
                gui.show();
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String... args) {
        new App();
    }
}
