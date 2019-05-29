/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import pl.jblew.marinesmud.dj.effects.visualutil.GradientHue;

/**
 *
 * @author teofil
 */
public class GOBOLoader {
    private final Object sync = new Object();
    private BufferedImage goboImg;
    private GoboSelector gobo = null;

    public GOBOLoader(GoboSelector initial) {
        loadGobo(initial);
    }

    public void loadGobo(GoboSelector gobo) {
        synchronized (sync) {
            this.gobo = gobo;
            if (!gobo.path.isEmpty()) {
                try {
                    goboImg = ImageIO.read(GOBOLoader.class.getResource(gobo.path));
                    System.out.println("Loaded image " + gobo.path + "; Width=" + goboImg.getWidth());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    

    public Image getGoboImage() {
        synchronized (sync) {
            if (goboImg != null && goboImg.getWidth() > 0) {
                return goboImg;
            }
            return null;
        }
    }

    public GoboSelector getSelectedGobo() {
        synchronized (sync) {
            return gobo;
        }
    }

    public static enum GoboSelector {
        DOTS("dots.png"), HEARTS("hearts.png"), SMM("smm-gobo.png");

        public final String path;

        private GoboSelector(String path) {
            this.path = path;
        }
    }
}
