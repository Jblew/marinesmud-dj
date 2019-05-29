/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import jiconfont.IconCode;

/**
 *
 * @author teofil
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class DMXDevice {
    private final List<DeviceGroup> groups = new ArrayList<>();
    
    public abstract Object getSync();
    public abstract String getName();
    public abstract int getStartAddress();
    public abstract int getChannelCount();
    public abstract JComponent newPreviewComponent();
    public abstract void processValues();
    public abstract void updatePreview();
    public abstract byte [] calculateLevels();
    public abstract IconCode getIconCode();
    public abstract int getLevelsCount();
    public abstract float [] getLevels();
    public abstract void setLevels(float [] levels);

    public void setCommonLevel(float level) {
        synchronized(getSync()) {
            float [] levels = getLevels();
            for(int i = 0;i < levels.length;i++) {
                levels[i] = level;
            }
            setLevels(levels);
        }
    }
    
    public void multiplyLevels(float factor) {
        synchronized(getSync()) {
            float [] levels = getLevels();
            for(int i = 0;i < levels.length;i++) {
                levels[i] = levels[i]*factor;
            }
            setLevels(levels);
        }
    }
    
    public void multiplyLevels(float [] factorArr) {
        synchronized(getSync()) {
            float [] levels = getLevels();
            for(int i = 0;i < levels.length && i < factorArr.length;i++) {
                levels[i] = levels[i]*factorArr[i];
            }
            setLevels(levels);
        }
    }
    
    public void registerGroup(DeviceGroup g) {
        groups.add(g);
    }
}
