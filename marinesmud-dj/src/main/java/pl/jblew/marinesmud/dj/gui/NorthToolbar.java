/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Date;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import pl.jblew.marinesmud.dj.App;
import pl.jblew.marinesmud.dj.config.Config;
import pl.jblew.marinesmud.dj.config.ConfigLoader;
import pl.jblew.marinesmud.dj.dmx.OutputManager;
import pl.jblew.marinesmud.dj.projector.ProjectorModule;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.tarsos.Shared;

public class NorthToolbar extends JToolBar {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    Mixer mixer = null;
    String portName = "";

    public NorthToolbar(OutputManager outputManager, Config config, App app, SceneSetup currentSceneSetup) {
        this.setFloatable(false);

        JButton openProjectorButton = new JButton("Open projector");
        openProjectorButton.addActionListener((evt) -> {
            ProjectorModule.Factory.create();
        });
        this.add(openProjectorButton);
        
        JComboBox inputSelector = new JComboBox(Shared.getMixerInfo(false, true));
        inputSelector.setPreferredSize(new Dimension(250, 18));
        inputSelector.addActionListener((e) -> {
            Object newMixerInfo = inputSelector.getSelectedItem();
            if (newMixerInfo != null) {
                Mixer newValue = AudioSystem.getMixer((Mixer.Info) newMixerInfo);
                this.firePropertyChange("mixer", mixer, newValue);
                this.mixer = newValue;
            }

        });

        this.add(inputSelector);

        JComboBox portSelector = new JComboBox(outputManager.listSerialPorts());
        portSelector.setPreferredSize(new Dimension(250, 18));
        portSelector.addActionListener((e) -> {
            Object newPortName_ = portSelector.getSelectedItem();
            if (newPortName_ != null && !((String) newPortName_).isEmpty()) {
                String newPortName = (String) newPortName_;
                this.firePropertyChange("port", portName, newPortName);
                this.portName = newPortName;
            }

        });

        this.add(portSelector);

        Object[] configSetupModel = new Object[config.setups.size() + 1];
        for (int i = 0; i < config.setups.size(); i++) {
            configSetupModel[i] = config.setups.get(i);
        }
        configSetupModel[configSetupModel.length - 1] = new String("Utwórz nową grupę ustawień");

        JComboBox configSelector = new JComboBox(configSetupModel);
        configSelector.setPreferredSize(new Dimension(250, 18));
        configSelector.addActionListener((e) -> {
            Object _config = configSelector.getSelectedItem();
            if (_config != null && _config instanceof SceneSetup) {
                if(_config != currentSceneSetup) app.reloadWithNewSceneSetup((SceneSetup) _config);
            } else if (_config != null && _config instanceof String) {
                String newConfigName = (String) JOptionPane.showInputDialog(
                        app.getGUI().getFrame(),
                        "Please specify a name of new SceneSetup",
                        "Customized Dialog",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "Config " + (new Date()).toGMTString());
                SceneSetup newSceneSetup = new SceneSetup();
                newSceneSetup.setupName = newConfigName;
                config.setups.add(newSceneSetup);
                app.reloadWithNewSceneSetup(newSceneSetup);
                try {
                    ConfigLoader.save(config);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(app.getGUI().getFrame(), ex.toString(), "Error while writing file", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        configSelector.setSelectedItem(currentSceneSetup);

        this.add(configSelector);

        JButton saveButton = new JButton("Zapisz");
        saveButton.addActionListener(e -> {
            try {
                ConfigLoader.save(config);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(app.getGUI().getFrame(), ex.toString(), "Error while writing file", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.add(saveButton);

        //this.addSeparator();
        //this.add(new JLabel(" Status: OK"));
    }

}
