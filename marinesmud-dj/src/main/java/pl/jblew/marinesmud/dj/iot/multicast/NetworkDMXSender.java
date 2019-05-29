/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.iot.multicast;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author teofil
 */
public class NetworkDMXSender {
    private final UDPDeviceDiscovery discovery;
    private final Map<String, Socket> sockets = Collections.synchronizedMap(new HashMap<>());

    public NetworkDMXSender() {
        discovery = new UDPDeviceDiscovery();
    }

    public void sendDMXToDevice(String name, String msg) {
        Socket s = null;
        if (sockets.containsKey(name)) {
            s = sockets.get(name);
            if (!s.isConnected() || s.isClosed()) {
                s = null;
                System.out.println("Socket of " + name + " lost");
            }
        }

        if (s == null) {
            String ip = discovery.getServiceIpByName(name);
            if (ip != null) {
                try {
                    s = new Socket(ip, 90);
                    s.setTcpNoDelay(true);
                    s.setTrafficClass(0x10);
                    s.setSendBufferSize(msg.getBytes().length*3+3);
                    s.setSoTimeout(150);
                    System.out.println("Socket of " + name + " connected");

                } catch (IOException ex) {
                    Logger.getLogger(NetworkDMXSender.class.getName()).log(Level.SEVERE, "", ex);
                    s = null;
                    System.out.println("Socket of " + name + " lost");
                }
            }
        }

        if (s != null) {
            try {
                s.getOutputStream().write(msg.getBytes());
                s.getOutputStream().flush();
            } catch (IOException ex) {
                Logger.getLogger(NetworkDMXSender.class.getName()).log(Level.SEVERE, "", ex);
                s = null;
                System.out.println("Socket of " + name + " lost");
            }
        }

        if (s != null) {
            sockets.put(name, s);
        } else {
            sockets.remove(name);
        }
    }

    public void start() {
        discovery.start();
    }

    public void stop() {
        discovery.stop();

        for (Socket s : sockets.values()) {
            try {
                s.close();
            } catch (IOException ex) {
                Logger.getLogger(NetworkDMXSender.class.getName()).log(Level.SEVERE, "", ex);
            }
        }
    }

}
