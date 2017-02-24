/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.config;

/**
 *
 * @author teofil
 */
public class StaticConfig {
    public static final int CLOCK_FREQUENCY_HZ = 50;
    public static final String CONFIG_PATH = "config.json";
    public static final float SAMPLE_RATE = 44100;
    public static final int BUFFER_SIZE = 1024 * 4;
    public static final int OVERLAP = 768 * 4;
    
    private StaticConfig() {}
}
