/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.effects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import pl.jblew.marinesmud.dj.effects.visualutil.GradientHue;
import pl.jblew.marinesmud.dj.gui.EffectPanel;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.DeviceGroup;
import pl.jblew.marinesmud.dj.sound.SoundProcessingManager;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public class KeyboardSpiker implements Effect {
    public boolean enabled = true;
    public String[] keys = new String[]{"a", "s", "d", "f"};

    @JsonIgnore
    private final Object sync = new Object();

    public KeyboardSpiker() {
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Processor[] getRequiredProcessors() {
        return new Processor[]{};
    }

    @Override
    public EffectWorker newWorker(DeviceGroup initialDeviceGroup) {
        return new MyWorker(initialDeviceGroup);
    }

    @Override
    public KeyboardSpiker deriveEffect() {
        synchronized (sync) {
            KeyboardSpiker clone = new KeyboardSpiker();
            clone.enabled = this.enabled;
            clone.keys = this.keys;
            return clone;
        }
    }

    private class MyWorker extends EffectWorker {
        private final DeviceGroup deviceGroup;
        private final AtomicReference<MyEffectPanel> myPanelRef = new AtomicReference<>(null);

        public MyWorker(DeviceGroup deviceGroup) {
            this.deviceGroup = deviceGroup;
        }

        @Override
        public void reload() {
        }

        @Override
        public Effect getEffect() {
            return KeyboardSpiker.this;
        }

        @Override
        public EffectPanel createEffectPanel() {
            MyEffectPanel p = new MyEffectPanel(KeyboardSpiker.this.keys);
            myPanelRef.set(p);
            return p;
        }

        @Override
        public void process(SoundProcessingManager spm, boolean isFirstInChain) {
            boolean _enabled;
            synchronized (sync) {
                _enabled = enabled;
            }
            if (_enabled) {
                MyEffectPanel panel = myPanelRef.get();
                if (panel != null) {
                    DMXDevice [] devices = deviceGroup.getDevices();
                    for (int i = 0;i < devices.length && i < panel.keyStates.length;i++) {
                        devices[i].setCommonLevel((panel.keyStates[i].get() ? 1f : 0f));
                    }
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            synchronized (sync) {
                KeyboardSpiker.this.enabled = enabled;
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
        private final String[] keys;
        private final AtomicBoolean[] keyStates;

        public MyEffectPanel(String[] keys) {
            this.keys = keys;
            this.add(new JLabel("Press keys: "
                    + Arrays.stream(keys).collect(Collectors.joining(", "))));

            keyStates = new AtomicBoolean[keys.length];
            for (int i = 0; i < keys.length; i++) {
                keyStates[i] = new AtomicBoolean(false);
            }

            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    for (int i = 0; i < keys.length; i++) {
                        if (e.getKeyChar() == keys[i].charAt(0)) {
                            if (e.getID() == KeyEvent.KEY_PRESSED) {
                                keyStates[i].set(true);
                            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                                keyStates[i].set(false);
                            }

                        }
                    }
                    return false;
                }
            });

        }
    }

}
