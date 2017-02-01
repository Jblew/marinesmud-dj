/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.Dimension;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import pl.jblew.marinesmud.dj.dmx.OutputManager;
import pl.jblew.marinesmud.dj.tarsos.Shared;

public class NorthToolbar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Mixer mixer = null;
        String portName = "";
	
	public NorthToolbar(OutputManager outputManager){
                this.setFloatable(false);
                

                JComboBox inputSelector = new JComboBox(Shared.getMixerInfo(false, true));
                inputSelector.setPreferredSize(new Dimension(250, 20));
                inputSelector.addActionListener((e) -> {
                    Object newMixerInfo = inputSelector.getSelectedItem();
                    if(newMixerInfo != null) {
                        Mixer newValue = AudioSystem.getMixer((Mixer.Info)newMixerInfo);
                        this.firePropertyChange("mixer", mixer, newValue);
			this.mixer = newValue;
                    }
					
                });
                
               this.add(inputSelector);
               
               JComboBox portSelector = new JComboBox(outputManager.listSerialPorts());
               portSelector.setPreferredSize(new Dimension(250, 20));
               portSelector.addActionListener((e) -> {
                    Object newPortName_ = portSelector.getSelectedItem();
                    if(newPortName_ != null && !((String)newPortName_).isEmpty()) {
                        String newPortName = (String)newPortName_;
                        this.firePropertyChange("port", portName, newPortName);
			this.portName = newPortName;
                    }
					
                });
               
               this.add(portSelector);
               this.addSeparator();
               this.add(new JLabel(" Status: OK"));
	}

}