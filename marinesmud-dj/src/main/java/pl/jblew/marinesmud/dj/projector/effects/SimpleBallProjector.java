/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import pl.jblew.marinesmud.dj.projector.Projector;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class SimpleBallProjector extends Projector {
    public SimpleBallProjector() {
    }

    private float dotPosition = 0f;

    @Override
    public void paint(Graphics g, Rectangle bounds) {
        g.setColor(Color.GREEN);
        int dotX = (int) ((1f-dotPosition) * (float) bounds.width);
        int dotY = (int) (dotPosition * (float) bounds.height);
        g.fillOval(dotX - 5, dotY - 5, 10, 10);

        dotPosition += 0.01f;
        if (dotPosition > 1f) {
            dotPosition = 0f;
        }
    }

    @Override
    public void effectsTick() {
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor [] {};
    }

    @Override
    public String getURIName() {
        return "SimpleBallProjector";
    }

}
