/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.util;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 *
 * @author teofil
 */
public class ColoredToggleButton extends JToggleButton {
    public ColoredToggleButton(String text, Icon icon, boolean state) {
        super(text, icon, state);
    }
    @Override
    public void paintComponent(Graphics g) {
        Color bg;
        if (isSelected()) {
            bg = Color.GREEN.darker();
        } else {
            bg = Color.DARK_GRAY;
        }
        setBackground(bg);
        super.paintComponent(g);
    }
}
