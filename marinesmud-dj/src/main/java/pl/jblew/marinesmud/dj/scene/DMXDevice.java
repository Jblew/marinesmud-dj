/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.scene;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.awt.Component;

/**
 *
 * @author teofil
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface DMXDevice {
    public String getName();
    public int getStartAddress();
    public int getChannelCount();
    public Component newComponent();
}
