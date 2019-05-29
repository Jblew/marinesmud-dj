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
import pl.jblew.marinesmud.dj.sound.processors.PitchProcessor;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.tarsos.SpectrogramPanel;

/**
 *
 * @author teofil
 */
public class PitchLinerProjector extends Projector {
    private int pitchWidth = 20;

    private float[] circularBuffer = new float[500];
    private int bufferPos = 0;

    public PitchLinerProjector() {

    }

    @Override
    public void paint(Graphics g, Rectangle bounds) {
        g.setColor(Color.WHITE);
        int i = 0;
        for (int x = bounds.width; x > 0; x -= pitchWidth) {
            float v = getFromBuffer(i);
            if(v > 0) {
                int y = (int)((1f-v)*(float)bounds.height);
                g.fillRect(x, y, pitchWidth, 10);
            }
            i++;
        }
        
    }

    @Override
    public void effectsTick() {
        float pitch = -1f;
        float mostProbablePitch = -1f;
        float maxProbability = -10f;
        for (Object res_ : PitchProcessor.getInstance().getResults()) {
            PitchProcessor.Result result = (PitchProcessor.Result) res_;
            if (result.pitch > 0) {
                if (result.pitchProbability > maxProbability) {
                    mostProbablePitch = result.pitch;
                    maxProbability = result.pitchProbability;
                }
            }
        }
        pitch = mostProbablePitch;
        float v = pitch < 0 ? -1f : (float) SpectrogramPanel.frequencyToBin(pitch, 10000, 50, 2000) / 10000f;
        addToBuffer(v);
    }

    private void addToBuffer(float v) {
        circularBuffer[bufferPos] = v;
        bufferPos++;
        if (bufferPos >= circularBuffer.length) {
            bufferPos = 0;
        }
    }

    private float getFromBuffer(int lagToHead) {
        int realPos = bufferPos - 1 - lagToHead;
        if (realPos < 0) {
            realPos += circularBuffer.length;
        }
        if (realPos < circularBuffer.length && realPos > 0) {
            return circularBuffer[realPos];
        } else {
            return -1f;
        }
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor [] {};
    }

    @Override
    public String getURIName() {
        return "PitchLiner";
    }
}
