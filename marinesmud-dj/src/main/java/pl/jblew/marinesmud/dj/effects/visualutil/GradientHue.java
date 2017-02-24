/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects.visualutil;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import pl.jblew.marinesmud.dj.effects.RomanticColorWheel;

/**
 *
 * @author teofil
 */
public final class GradientHue {
    private final Object sync = new Object();
    private BufferedImage gradientImg;
    private Gradient gradient = null;

    public GradientHue(Gradient initial) {
        loadGradient(initial);
    }

    public void loadGradient(Gradient gradient) {
        synchronized (sync) {
            this.gradient = gradient;
            if (!gradient.path.isEmpty()) {
                try {
                    gradientImg = ImageIO.read(GradientHue.class.getResource(gradient.path));
                    System.out.println("Loaded image " + gradient.path + "; Width=" + gradientImg.getWidth());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public float getHue(float position) {
        return getHue(position, 0);
    }

    public float getHue(float position, int line) {
        Color c = getColor(position, line);
        return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
    }

    public Color getColor(float position, int line) {
        synchronized (sync) {
            Color color = Color.getHSBColor(position, 1f, 1f);
            if (gradientImg != null && gradientImg.getWidth() > 0) {
                int positionI = (int) ((float) gradientImg.getWidth() * position);
                color = new Color(gradientImg.getRGB(positionI, line % gradientImg.getHeight()));
            }
            return color;
        }
    }

    public Gradient getGradient() {
        synchronized (sync) {
            return gradient;
        }
    }

    public static enum Gradient {
        HSV(""),
        COLORFUL("colorful.png"),
        GROUND("ground.png"),
        SOFT_PINKS("soft_pinks.png"),
        MORE_PINKS("more_pinks.png"),
        WARM("cieple.jpg"),
        NATURAL_COLORWHEEL("natural.jpg"),
        DOUBLE_NATURAL("double_natural.jpg"),
        QUADRO_NATURAL("quadro_natural.jpg");

        public final String path;

        private Gradient(String path) {
            this.path = path;
        }
    }
}
