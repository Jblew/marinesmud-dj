/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.graphed;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.graph.GraphScene;
import pl.jblew.marinesmud.dj.scene.SceneSetup;
import pl.jblew.marinesmud.dj.gui.util.GUIUtil;

/**
 *
 * @author teofil
 */
public class DMXFlowChart {
    private final SceneSetup.Current scene;
    
    public DMXFlowChart(SceneSetup.Current scene) {
        this.scene = scene;
    }
    
    public Component createComponent() {
        GUIUtil.assertEDTThread();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(800, 300));
        GraphScene graph = new DMXGraphSceneImpl(scene);
        scrollPane.setViewportView(graph.createView());
        return scrollPane;
    }
}
