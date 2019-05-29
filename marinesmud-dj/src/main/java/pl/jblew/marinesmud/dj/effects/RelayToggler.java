/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.scene.RGBDevice;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.PitchProcessor;
import pl.jblew.marinesmud.dj.sound.processors.Processor;
import pl.jblew.marinesmud.dj.tarsos.SpectrogramPanel;
import pl.jblew.marinesmud.dj.util.Listener;

/**
 *
 * @author teofil
 */
public class RelayToggler implements Effect {
    public boolean enabled = true;
    public boolean[] states = new boolean[0];

    @JsonIgnore
    private final Object sync = new Object();

    public RelayToggler() {

    }

    @Override
    @JsonIgnore
    public String getName() {
        return "Relay toggler";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    @JsonIgnore
    public Processor[] getRequiredProcessors() {
        return new Processor[]{};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }

    @Override
    public RelayToggler deriveEffect() {
        synchronized (sync) {
            RelayToggler clone = new RelayToggler();
            clone.enabled = this.enabled;
            clone.states = this.states;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;

            int numOfKnobs = deviceGroup.getDevices().length;

            synchronized (sync) {
                if (states == null) {
                    states = new boolean[numOfKnobs];
                } else if (states.length < numOfKnobs) {
                    boolean[] newstates = new boolean[numOfKnobs];
                    for (int i = 0; i < numOfKnobs; i++) {
                        newstates[i] = (i < states.length ? states[i] : false);
                    }
                    states = newstates;
                }
            }
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return RelayToggler.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            synchronized (sync) {
                MyEffectPanel p = new MyEffectPanel(deviceGroup, states);
                myPanelRef.set(p);
                return p;
            }
        }

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            boolean _enabled;
            boolean[] _states;
            synchronized (sync) {
                _enabled = enabled;
                _states = states;
            }
            if (_enabled) {
                MyEffectPanel myPanel = myPanelRef.get();
                if (myPanel != null) {
                    DMXDevice[] devices = deviceGroup.getDevices();
                    for (int i = 0; i < devices.length; i++) {

                        final int index = i;

                        _states[i] = myPanel.togglers[i].isSelected();

                        devices[i].setCommonLevel(_states[i] ? 1f : 0f);

                        String newLabel = (_states[i] ? "ON" : "OFF");
                        if (!myPanel.togglers[i].getText().equals(newLabel)) {
                            SwingUtilities.invokeLater(() -> {
                                myPanel.togglers[index].setText(newLabel);
                            });
                        }

                    }
                }
            }
            synchronized (sync) {
                states = _states;
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                RelayToggler.this.enabled = enabled;
            }
        }

        @Override
        public boolean isEnabled() {
            synchronized (sync) {
                return enabled;
            }
        }

    }

    private static class MyEffectPanel extends EffectPanel {
        private final JToggleButton[] togglers;
        private final JLabel[] togglerLabels;

        public MyEffectPanel(DeviceGroup deviceGroup, boolean[] states) {
            setLayout(new GridLayout(0, 2));

            DMXDevice[] devices = deviceGroup.getDevices();
            togglers = new JToggleButton[devices.length];
            togglerLabels = new JLabel[togglers.length];

            char letter = 'A';
            for (int i = 0; i < togglers.length; i++) {
                togglers[i] = new JToggleButton("OFF");
                togglerLabels[i] = new JLabel("CHAN_" + (letter + i));

                if (i < states.length) {
                    togglers[i].setSelected(states[i]);
                }

                this.add(togglerLabels[i]);
                this.add(togglers[i]);
            }
        }
    }

}
