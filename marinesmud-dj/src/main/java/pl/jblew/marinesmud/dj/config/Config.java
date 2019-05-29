/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.config;

import java.util.ArrayList;
import java.util.List;
import pl.jblew.marinesmud.dj.scene.Scene;
import pl.jblew.marinesmud.dj.scene.SceneSetup;

/**
 *
 * @author teofil
 */
public class Config {
    public Scene scene = new Scene();
    
    public List<SceneSetup> setups = new ArrayList<SceneSetup>();
    
    {
        setups.add(new SceneSetup());
    }
}
