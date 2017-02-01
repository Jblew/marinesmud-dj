/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.util;

import javax.swing.SwingUtilities;
import pl.jblew.marinesmud.dj.util.GUIProcessingOutsideEDTThreadException;

/**
 *
 * @author teofil
 */
public class GUIUtil {
    private GUIUtil() {}
    
    public static void assertEDTThread() {
        if(!SwingUtilities.isEventDispatchThread()) throw new GUIProcessingOutsideEDTThreadException();
    }
}
