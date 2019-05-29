/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector;

import pl.jblew.marinesmud.dj.projector.effects.SimpleBallProjector;
import pl.jblew.marinesmud.dj.projector.effects.GOBOProjector;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import pl.jblew.marinesmud.dj.projector.effects.PitchLinerProjector;
import pl.jblew.marinesmud.dj.sound.processors.Processor;

/**
 *
 * @author teofil
 */
public abstract class Projector {
    public static final Projector[] PROJECTORS = new Projector[]{
        new GOBOProjector(GOBOLoader.GoboSelector.SMM),
        new SimpleBallProjector(),
        new GOBOProjector(GOBOLoader.GoboSelector.DOTS),
        new GOBOProjector(GOBOLoader.GoboSelector.HEARTS),
        new PitchLinerProjector()
    };
    
    private final Map<String, Property> properties = new HashMap<>();
    
    public abstract String getURIName();

    public abstract Processor[] getRequiredProcessors();

    public abstract void paint(Graphics g, Rectangle bounds);

    public abstract void effectsTick();

    public String respondToHttp(String url) {
        String[] urlParts = (url.charAt(0) == '/' ? url.substring(1) : url).split("/");
        if (urlParts.length > 2) {
            if (urlParts[0].equals("set")) {
                String name = urlParts[1];
                String newValue = urlParts[2];
                if (properties.containsKey(name)) {
                    properties.get(name).valueChanged(newValue);
                }
            }
        }

        String out = "<table border=\"0\">";
        for (String name : properties.keySet()) {
            out += "<tr><td>" + name + "</td><td>" + properties.get(name).getSetterHtml() + "</td></tr>";
        }
        out += "</table>";
        return out;
    }

    protected void registerProperty(String name, Property property) {
        properties.put(name, property);
    }

    public static interface Property {
        public String getSetterHtml();

        public void valueChanged(String newValue);

        @FunctionalInterface
        public static interface SetterHtmlGetter {
            public String getSetterHtml();
        }

        @FunctionalInterface
        public static interface ValueChangedListener {
            public void valueChanged(String newValue);
        }

        public static Property builder(SetterHtmlGetter shg, ValueChangedListener vcl) {
            return new Property() {
                @Override
                public String getSetterHtml() {
                    return shg.getSetterHtml();
                }

                @Override
                public void valueChanged(String newValue) {
                    try {
                        vcl.valueChanged(newValue);
                    } catch (RuntimeException e) {
                    }
                }

            };
        }
    }

    public static class PropertyUtils {
        public static final AtomicLong ID = new AtomicLong(0);

        private PropertyUtils() {
        }

        public static String getFloatSliderHtml(String propertyName, int defaultValue) {
            String sliderId = "range-slider-" + ID.incrementAndGet();
            return "<input type=\"range\" name=\"points\" min=\"0\" max=\"1000\" value=\"" + defaultValue + "\" id=\"" + sliderId + "\">"
                    + "<script>$(document).ready(function() {"
                    + "     $(\"#" + sliderId + "\").on('input', function () {"
                    + "         $.ajax({url: \"/set/" + propertyName + "/\"+$(\"#" + sliderId + "\").val()});"
                    + "     });"
                    + "});</script>";
        }
    }
}
