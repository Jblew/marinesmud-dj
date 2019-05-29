/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class ProjectorModule {
    private final FullScreenGraphics fsg;
    private final WebServer webServer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<Projector> selectedProjector = new AtomicReference<>(Projector.PROJECTORS[0]);

    protected ProjectorModule() {
        running.set(true);
        fsg = new FullScreenGraphics(this);
        fsg.setProjector(selectedProjector.get());

        webServer = new WebServer(new ProjectorHttpResponder(this));
        fsg.start();
    }

    public void stop() {
        if (running.get()) {
            running.set(false);
            fsg.stop();
            webServer.stop();
        }
        
        Factory.dispose();
    }
    
    public void effectsTick() {
        Projector p = getProjector();
        if(p != null) {
            p.effectsTick();
        }
    }
    
    public Projector getProjector() {
        return selectedProjector.get();
    }
    
    public void setProjector(Projector newProjector) {
        this.selectedProjector.set(newProjector);
        fsg.setProjector(newProjector);
    }

    public Processor [] getRequiredProcessors() {
        return selectedProjector.get().getRequiredProcessors();
    }
    
    public static class Factory {
        private static final AtomicReference<ProjectorModule> ref = new AtomicReference<>(null);
        
        private Factory() {}
        
        public static ProjectorModule create() {
            ProjectorModule currentPm = ref.get();
            if(currentPm != null) {
                throw new RuntimeException("ProjectorModule is already created!");
            }
            currentPm = new ProjectorModule();
            ref.set(currentPm);
            return currentPm;
        }
        
        public static void dispose() {
            ref.set(null);
        }
        
        public static ProjectorModule get() {
            return ref.get();
        }
    }
}
