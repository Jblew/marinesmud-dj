/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.util;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author teofil
 */
public class Knob<A extends JComponent> extends JPanel {
    private final JLabel preLabel;
    private final A knob;
    private final JLabel unitLabel;
    
    public Knob(String labelText, A knob, String unitText) {
        this.setLayout(new BorderLayout());
        
        preLabel = new JLabel(labelText);
        this.add(preLabel, BorderLayout.WEST);
        
        this.knob = knob;
        this.add(knob, BorderLayout.CENTER);
        
        unitLabel = new JLabel(unitText);
        this.add(unitLabel, BorderLayout.EAST);
    }

    public JLabel getPreLabel() {
        return preLabel;
    }

    public A getKnob() {
        return knob;
    }

    public JLabel getUnitLabel() {
        return unitLabel;
    }
}
