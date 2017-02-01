/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.graphed;

import java.awt.*;
import javax.swing.*;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import org.netbeans.api.visual.graph.GraphScene;

/**
 *
 * @author teofil
 */
public class GraphEditorPanel extends JPanel {
    public GraphEditorPanel() {
        initComponents();
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
            
            JFrame frame = new JFrame();
            frame.setMinimumSize(new Dimension(500, 400));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GraphEditorPanel());
            frame.pack();
            frame.setVisible(true);
        });

    }

    private void initComponents() {
        //Set the layout:
        setLayout(new BorderLayout());
        //Create a JScrollPane:
        JScrollPane scrollPane = new JScrollPane();
        //Add the JScrollPane to the JPanel:
        add(scrollPane, BorderLayout.CENTER);
        //Create the GraphSceneImpl:
        GraphScene scene = new GraphSceneImpl();
        //Add it to the JScrollPane:
        scrollPane.setViewportView(scene.createView());
        //Add the SatellitView to the scene:
        add(scene.createSatelliteView(), BorderLayout.WEST);
    }
}
