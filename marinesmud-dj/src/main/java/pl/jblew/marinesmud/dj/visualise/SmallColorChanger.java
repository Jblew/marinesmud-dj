/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.visualise;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author teofil
 */
public class SmallColorChanger {
    public static final Color[] COLORS = new Color[]{Color.RED, Color.BLUE, Color.WHITE, Color.MAGENTA};
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final JFrame frame;
    private final JPanel colorPanel;

    public SmallColorChanger() {
        frame = new JFrame();
        frame.setSize(200, 200);
        frame.setTitle("SmallColorChanger");

        colorPanel = new JPanel();
        frame.setContentPane(colorPanel);
        frame.setVisible(true);
    }

    public void beat() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if (currentIndex.incrementAndGet() >= COLORS.length) {
                        currentIndex.set(0);
                    }
                    colorPanel.setBackground(COLORS[currentIndex.get()]);
                    colorPanel.updateUI();
                    frame.repaint();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
