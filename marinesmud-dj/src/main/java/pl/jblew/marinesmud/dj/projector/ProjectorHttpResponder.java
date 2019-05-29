/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.projector;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.iot.multicast.NetworkDMXSender;

/**
 *
 * @author teofil
 */
public class ProjectorHttpResponder {
    private final ProjectorModule projectorModule;
    private final String template;

    public ProjectorHttpResponder(ProjectorModule projectorModule) {
        this.projectorModule = projectorModule;
        
        String template_;
        try {
            URL templateUrl = ProjectorHttpResponder.class.getResource("template.html");
            if(templateUrl == null) throw new RuntimeException("Could not find template.html");
            template_ = Resources.toString(templateUrl, Charset.forName("UTF-8"));
        } catch (Exception ex) {
            Logger.getLogger(ProjectorHttpResponder.class.getName()).log(Level.SEVERE, "", ex);
            template_ = ex.toString()+": "+ex.getMessage();
        }
        template = template_;
    }
    
    public String getResponse(String url) {
        String statusLine = "";
        
        String [] urlParts = (url.charAt(0) == '/'? url.substring(1) : url).split("/");
        if(urlParts.length > 1) {
            statusLine += "urlParts[0]="+urlParts[0]+"; ";
            if(urlParts[0].equals("selectEffect")) {
                String newEffectName = urlParts[1];
                for(Projector p : Projector.PROJECTORS) {
                    if(p.getURIName().equals(newEffectName)) {
                        projectorModule.setProjector(p);
                        statusLine += "Effect changed to "+p.getURIName()+";";
                        break;
                    }
                }
            }
        }
        
        String effectSelector = "Select projector: ";
        
        for(Projector p : Projector.PROJECTORS) {
            if(p == projectorModule.getProjector()) {
                effectSelector += "&nbsp;<b>"+p.getURIName()+"</b>&nbsp;";
            }
            else {
                effectSelector += " &nbsp;<a href=\"/selectEffect/"+p.getURIName()+"\">"+p.getURIName()+"</a>&nbsp; ";
            }
        }
        
        String projectorProperties = projectorModule.getProjector().respondToHttp(url);
        
        String content = "<p>"+effectSelector+"</p><br />"
                + "<hr />"+projectorProperties
                + "<br /><hr /><i>"+statusLine+"</i>&nbsp;URL: "+url+"; parts.length="+urlParts.length;
        return template.replace("{{content}}", content);
    }
}
