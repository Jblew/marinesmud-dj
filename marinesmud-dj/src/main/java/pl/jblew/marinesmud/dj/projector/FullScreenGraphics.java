/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector;

import pl.jblew.marinesmud.dj.projector.effects.GOBOProjector;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.config.StaticConfig;
import pl.jblew.marinesmud.dj.iot.multicast.NetworkDMXSender;

public class FullScreenGraphics {
    private final ProjectorModule projectorModule;
    private final AtomicReference<Projector> selectedProjector = new AtomicReference<>(new GOBOProjector(GOBOLoader.GoboSelector.SMM));

    private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[]{
        new DisplayMode(1024, 768, 32, 0),
        new DisplayMode(1024, 768, 16, 0),
        new DisplayMode(1024, 768, 8, 0)
    };

    private final Frame mainFrame;
    private final GraphicsDevice device;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public FullScreenGraphics(ProjectorModule projectorModule) {
        this.projectorModule = projectorModule;
        
        int numBuffers = 2;
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = env.getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        mainFrame = new Frame(gc);

        try {
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    stop();
                }
            });
            mainFrame.setUndecorated(true);
            mainFrame.setIgnoreRepaint(true);
            device.setFullScreenWindow(mainFrame);
            if (device.isDisplayChangeSupported()) {
                chooseBestDisplayMode(device);
            }
            mainFrame.createBufferStrategy(numBuffers);

            /*Rectangle bounds = mainFrame.getBounds();
            
            BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();
            for (float lag = 2000.0f; lag > 0.00000006f; lag = lag / 1.33f) {
                for (int i = 0; i < numBuffers; i++) {
                    Graphics g = bufferStrategy.getDrawGraphics();
                    if (!bufferStrategy.contentsLost()) {
                        g.setColor(COLORS[i]);
                        g.fillRect(0, 0, bounds.width, bounds.height);
                        bufferStrategy.show();
                        g.dispose();
                    }
                    try {
                        Thread.sleep((int) lag);
                    } catch (InterruptedException e) {
                    }
                }
            }*/
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            stop();
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            device.setFullScreenWindow(null);
        }
    }

    public void start() {
        int tickTimeMs = 1000 / StaticConfig.CLOCK_FREQUENCY_HZ;
        scheduler.scheduleAtFixedRate(() -> {
            paint();
        }, tickTimeMs, tickTimeMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(150, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(FullScreenGraphics.class.getName()).log(Level.SEVERE, "", ex);
        }
        scheduler.shutdownNow();

        mainFrame.setVisible(false);
        device.setFullScreenWindow(null);
        
        projectorModule.stop();
    }

    private void paint() {
        Rectangle bounds = mainFrame.getBounds();
        BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();
        Graphics g = bufferStrategy.getDrawGraphics();
        if (!bufferStrategy.contentsLost()) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, bounds.width, bounds.height);

            Projector p = selectedProjector.get();
            if (p != null) {
                p.paint(g, bounds);
            }

            bufferStrategy.show();
            g.dispose();
        }
    }

    public void setProjector(Projector projector) {
        this.selectedProjector.set(projector);
    }

    private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
        for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
            DisplayMode[] modes = device.getDisplayModes();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
                        && modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
                        && modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()) {
                    return BEST_DISPLAY_MODES[x];
                }
            }
        }
        return null;
    }

    public static void chooseBestDisplayMode(GraphicsDevice device) {
        DisplayMode best = getBestDisplayMode(device);
        if (best != null) {
            device.setDisplayMode(best);
        }
    }

}
