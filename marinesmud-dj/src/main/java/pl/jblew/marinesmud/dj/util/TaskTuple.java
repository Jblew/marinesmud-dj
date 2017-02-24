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
@FunctionalInterface
public interface TaskTuple<A, B> {
    public void process(A a, B b);
}
