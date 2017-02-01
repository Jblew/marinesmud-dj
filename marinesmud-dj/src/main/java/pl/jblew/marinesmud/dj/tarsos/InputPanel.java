/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.tarsos;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import pl.jblew.marinesmud.dj.config.Config;
import pl.jblew.marinesmud.dj.dmx.SerialOutputManager;

/**
 *
 * @author teofil
 */
public class InputPanel extends JToolBar {
    private static final long serialVersionUID = 1L;

    Mixer mixer = null;

    public InputPanel() {
        this.setFloatable(false);

        JComboBox inputSelector = new JComboBox(Shared.getMixerInfo(false, true));
        inputSelector.addActionListener((e) -> {
            Object newMixerInfo = inputSelector.getSelectedItem();
            if (newMixerInfo != null) {
                Mixer newValue = AudioSystem.getMixer((Mixer.Info) newMixerInfo);
                this.firePropertyChange("mixer", mixer, newValue);
                this.mixer = newValue;
            }

        });

        this.add(inputSelector);

        JComboBox portSelector = new JComboBox(new SerialOutputManager(null, new Config()).listSerialPorts());

        this.add(portSelector);
        this.addSeparator();
        this.add(new JLabel(" Status: OK"));
    }
}
