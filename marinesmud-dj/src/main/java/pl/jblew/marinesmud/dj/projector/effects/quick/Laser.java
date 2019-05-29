/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector.effects.quick;

import pl.jblew.marinesmud.dj.projector.effects.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import pl.jblew.marinesmud.dj.effects.visualutil.GradientHue;
import pl.jblew.marinesmud.dj.projector.GOBOLoader;
import pl.jblew.marinesmud.dj.projector.Projector;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.util.MathUtils;

/**
 *
 * @author teofil
 */
public class Laser extends Projector {
    private final GOBOLoader goboLoader;
    private final GradientHue gradientHue;
    private final Object sync = new Object();

    private float rotation = 0f;
    private float hue = 0f;
    private float rotationSpeed = 0.02f;
    private float hueSpeed = 0.005f;

    public Laser() {
        this.goboLoader = new GOBOLoader(GOBOLoader.GoboSelector.HEARTS);
        this.gradientHue = new GradientHue(GradientHue.Gradient.NATURAL_COLORWHEEL);

        super.registerProperty("rotationSpeed", Property.builder(() -> {
            synchronized (sync) {
                return Projector.PropertyUtils.getFloatSliderHtml("rotationSpeed",
                        (int) MathUtils.map(rotationSpeed, -0.1f, 0.1f, 0, 1000));
            }
        }, (newValue) -> {
            int unscaled = Integer.parseInt(newValue);
            synchronized (sync) {
                rotationSpeed = MathUtils.map(unscaled, 0, 1000, -0.1f, 0.1f);
            }
        }));
        
        super.registerProperty("hueSpeed", Property.builder(() -> {
            synchronized (sync) {
                return Projector.PropertyUtils.getFloatSliderHtml("hueSpeed",
                        (int) MathUtils.map(hueSpeed, 0, 0.02f, 0, 1000));
            }
        }, (newValue) -> {
            int unscaled = Integer.parseInt(newValue);
            synchronized (sync) {
                hueSpeed = MathUtils.map(unscaled, 0, 1000, 0f, 0.02f);
            }
        }));
    }

    @Override
    public void paint(Graphics g_, Rectangle bounds) {
        Graphics2D g = (Graphics2D) g_;
        /*g.setColor(Color.RED);
        int dotX = (int) ((dotPosition) * (float) bounds.width);
        int dotY = (int) (dotPosition * (float) bounds.height);
        g.fillOval(dotX - 5, dotY - 5, 10, 10);

        dotPosition += 0.01f;
        if (dotPosition > 1f) {
            dotPosition = 0f;
        }*/

        g.rotate(rotation, bounds.width / 2, bounds.height / 2);

        Image img = goboLoader.getGoboImage();
        if (img != null) {
            int imgHeight = img.getHeight(null);
            g.setColor(gradientHue.getColor(hue, 1));
            g.fillOval(bounds.width / 2 - imgHeight / 2 + 2, bounds.height / 2 - imgHeight / 2 + 2, imgHeight - 4, imgHeight - 4);

            g.drawImage(img, bounds.width / 2 - imgHeight / 2, bounds.height / 2 - imgHeight / 2, null);
        }

        synchronized (sync) {
            rotation += rotationSpeed;
            if (rotation > Math.PI * 2f) {
                rotation = 0f;
            }
            if (rotation < 0) {
                rotation = (float) Math.PI * 2f;
            }

            hue += hueSpeed;
            if (hue > 1f) {
                hue = 0;
            }
        }
    }

    @Override
    public void effectsTick() {
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor[]{};
    }

    @Override
    public String getURIName() {
        return "Laser";
    }

}
