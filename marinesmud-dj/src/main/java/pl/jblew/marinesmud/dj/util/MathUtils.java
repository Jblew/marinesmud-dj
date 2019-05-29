/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.util;

/**
 *
 * @author teofil
 */
public class MathUtils {
    private MathUtils() {}
    
    public static float map(float v, float a, float b, float c, float d) {
        return (v-a)/(b-a) * (d-c) + c;
    }
}
