/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author teofil
 */
public class ConfigLoader {
    private static boolean preventSavingConfig = false;

    private ConfigLoader() {
    }

    public static Config loadConfig() {
        ObjectMapper mapper = new ObjectMapper();

        File configFile = new File(StaticConfig.CONFIG_PATH);
        if (configFile.exists() && configFile.canRead()) {
            Config config = new Config();

            try {
                config = mapper.readValue(configFile, Config.class);
            } catch (IOException ex) {
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                preventSavingConfig = true;

                backupBrokenConfig(configFile);
            }

            return config;
        } else {
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
        if (preventSavingConfig) {
            System.out.println("Cannot save config! (Previous broken config backup could not be saved!)");
        } else {
            ObjectMapper mapper = new ObjectMapper();

            File configFile = new File(StaticConfig.CONFIG_PATH);
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
        }
    }

    private static void backupBrokenConfig(File configFile) {
        int bakNum = 0;
        File nextConfigBak = new File(configFile.getAbsolutePath() + "_" + bakNum + ".bak");
        while (nextConfigBak.exists()) {
            bakNum++;
            nextConfigBak = new File(configFile.getAbsolutePath() + "_" + bakNum + ".bak");
        }

        try {
            Files.copy(configFile, nextConfigBak);
            preventSavingConfig = false;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
