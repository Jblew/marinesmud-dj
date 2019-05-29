/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.util;

import javax.swing.JComboBox;

/**
 *
 * @author teofil
 */
public class EnumComboBox<T extends Enum> extends JComboBox {
    public  EnumComboBox(T defaultValue) {
        super(defaultValue.getClass().getEnumConstants());
        this.setSelectedItem(defaultValue);
    }
    
    public T getSelectedEnum() {
        return (T)this.getSelectedItem();
    }
}
