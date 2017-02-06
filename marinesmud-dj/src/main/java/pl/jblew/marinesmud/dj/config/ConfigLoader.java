/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author teofil
 */
public class ConfigLoader {
    private ConfigLoader() {}
    
    public static Config loadConfig() {
        ObjectMapper mapper = new ObjectMapper();
        
        File configFile = new File(StaticConfig.CONFIG_PATH);
        if(configFile.exists() && configFile.canRead()) {
            Config config = new Config();
            
            try {
                config = mapper.readValue(configFile, Config.class);
            } catch (IOException ex) {
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return config;
        }
        else {
            Config defConfig = new Config();
            
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, defConfig);
            } catch (IOException ex) {
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return defConfig;
        }
    }
    
    public static void save(Config config) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        File configFile = new File(StaticConfig.CONFIG_PATH);
        mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
    }
}
