/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

/**
 *
 * @author teofil
 */
public class AddEffectPanel extends JPanel {
    private final JButton addButton;
    private final ActionListener listener;
    
    public AddEffectPanel(ActionListener listener) {
        this.listener = listener;
        
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(40,40,40,40));
        
        addButton = new JButton("Add new effect to chain", IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD_CIRCLE_OUTLINE, 60, Color.GREEN.darker()));
        addButton.setHorizontalTextPosition(JButton.CENTER);
        addButton.setVerticalTextPosition(JButton.BOTTOM);
        
        this.add(addButton, BorderLayout.CENTER);
        addButton.addActionListener(listener);
    }
    
    
}
