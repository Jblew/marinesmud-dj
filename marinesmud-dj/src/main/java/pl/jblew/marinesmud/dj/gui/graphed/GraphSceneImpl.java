/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.gui.graphed;

import java.awt.Color;
import java.awt.Point;
import javax.swing.ImageIcon;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;

public class GraphSceneImpl extends GraphScene<String, String> {
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private LayerWidget interactionLayer;

    public GraphSceneImpl() {
        mainLayer = new LayerWidget(this);
        connectionLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(interactionLayer);

        Widget w1 = addNode("1. Hammer");
        w1.setPreferredLocation(new Point(10, 100));
        Widget w2 = addNode("2. Saw");
        w2.setPreferredLocation(new Point(100, 250));
        Widget w3 = addNode("Nail");
        w3.setPreferredLocation(new Point(250, 250));
        Widget w4 = addNode("Bolt");
        w4.setPreferredLocation(new Point(250, 350));
        
        getActions().addAction(ActionFactory.createZoomAction());
    }

    @Override
    protected Widget attachNodeWidget(String arg) {
        IconNodeWidget widget = new IconNodeWidget(this);
        if (arg.startsWith("1")) {
            widget.setImage(IconFontSwing.buildImage(GoogleMaterialDesignIcons.ANDROID, 60, Color.RED.darker()));
        } else if (arg.startsWith("2")) {
            widget.setImage(IconFontSwing.buildImage(GoogleMaterialDesignIcons.HDR_WEAK, 60, Color.GREEN.darker()));
        } else {
            widget.setImage(IconFontSwing.buildImage(GoogleMaterialDesignIcons.PHONE_IN_TALK, 60, Color.BLUE.darker()));
        }
        widget.getActions().addAction(
            ActionFactory.createExtendedConnectAction(
            connectionLayer, new MyConnectProvider()));
        widget.getActions().addAction(
                ActionFactory.createAlignWithMoveAction(
                        mainLayer, interactionLayer,
                        ActionFactory.createDefaultAlignWithMoveDecorator()));
        widget.setLabel(arg);
        ImageIcon i;
        mainLayer.addChild(widget);
        System.out.println("Made widget");
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeSourceAnchor(String arg0, String arg1, String arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeTargetAnchor(String arg0, String arg1, String arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class MyConnectProvider implements ConnectProvider {

        public boolean isSourceWidget(Widget source) {
            return source instanceof IconNodeWidget && source != null ? true : false;
        }

        public ConnectorState isTargetWidget(Widget src, Widget trg) {
            return src != trg && trg instanceof IconNodeWidget ? ConnectorState.ACCEPT : ConnectorState.REJECT;
        }

        public boolean hasCustomTargetWidgetResolver(Scene arg0) {
            return false;
        }

        public Widget resolveTargetWidget(Scene arg0, Point arg1) {
            return null;
        }

        public void createConnection(Widget source, Widget target) {
            ConnectionWidget conn = new ConnectionWidget(GraphSceneImpl.this);
            conn.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
            conn.setTargetAnchor(AnchorFactory.createRectangularAnchor(target));
            conn.setSourceAnchor(AnchorFactory.createRectangularAnchor(source));
            connectionLayer.addChild(conn);
        }

    }
}
