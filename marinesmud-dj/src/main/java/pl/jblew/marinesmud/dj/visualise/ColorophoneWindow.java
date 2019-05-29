/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.visualise;

import java.awt.Color;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author teofil
 */
public class ColorophoneWindow {
    public static final Color[] COLORS = new Color[]{Color.RED, Color.BLUE, Color.WHITE, Color.MAGENTA};
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final JFrame frame;
    private final JPanel[] bulbs;
    private final float hues[];
    private final float brightnesses[];

    public ColorophoneWindow(int numOfBulbs) {
        frame = new JFrame();
        frame.setSize(250 * numOfBulbs, 250);
        frame.setTitle("Colorophone x" + numOfBulbs);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, numOfBulbs));

        bulbs = new JPanel[numOfBulbs];
        hues = new float[numOfBulbs];
        brightnesses = new float[numOfBulbs];
        for (int i = 0; i < numOfBulbs; i++) {
            bulbs[i] = new JPanel();
            mainPanel.add(bulbs[i]);

            hues[i] = 0.5f;
            brightnesses[i] = 1f;
        }
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    /*public void beat() {
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

     }*/
    public void setColor(final int bulbIndex, final float hue) {
        synchronized (hues) {
            hues[bulbIndex] = hue;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    bulbs[bulbIndex].setBackground(Color.getHSBColor(hue, 1f, 1f));
                    bulbs[bulbIndex].updateUI();
                    //frame.repaint();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setColors(final float[] newhues) {
        synchronized (hues) {
            System.arraycopy(newhues, 0, hues, 0, hues.length);
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    synchronized (hues) {
                        for (int i = 0; i < hues.length; i++) {
                            hues[i] = newhues[i];
                            bulbs[i].setBackground(Color.getHSBColor(hues[i], 1f, 1f));
                            bulbs[i].updateUI();
                        }
                    }
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public float getColor(int bulbIndex) {
        synchronized (hues) {
            return hues[bulbIndex];
        }
    }

    public void setBrightness(final int bulbIndex, final float brightness) {
        brightnesses[bulbIndex] = brightness;
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    synchronized (hues) {
                        bulbs[bulbIndex].setBackground(Color.getHSBColor(hues[bulbIndex], 1f, brightness));
                        bulbs[bulbIndex].updateUI();
                    }
//                    frame.repaint();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setBrightnessForAll(final float brightness) {
        for (int i = 0; i < bulbs.length; i++) {
            brightnesses[i] = brightness;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < bulbs.length; i++) {
                        synchronized (hues) {
                            bulbs[i].setBackground(Color.getHSBColor(hues[i], 1f, brightness));
                            bulbs[i].updateUI();
                        }
                    }
                    //frame.repaint();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SmallColorChanger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public float getBrightness(int bulbIndex) {
        return brightnesses[bulbIndex];
    }

    public int getBulbCount() {
        return bulbs.length;
    }
}
