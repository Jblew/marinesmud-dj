/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.iot.multicast.NetworkDMXSender;
import pl.jblew.marinesmud.dj.iot.multicast.UDPDeviceDiscovery;

/**
 *
 * @author teofil
 */
public class SendDMX {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
        /*int v = 0;
        for(int i = 0;i < 200;i++) {
            v+= 10;
            System.out.println("V="+v);
            sendValues(v % 1023, (v+128) % 1023, (v+256) % 1023, (v+384) % 1023, (v+512) % 1023, (v+640) % 1023);
            TimeUnit.MILLISECONDS.sleep(10);
            
        }*/
        
        /*DatagramSocket sendSocket = new DatagramSocket();
        InetAddress sendGroup = InetAddress.getByName("235.0.2.3");
        
        UDPDeviceDiscovery discovery = new UDPDeviceDiscovery();
        discovery.start();
        
        datagramSendValues(0, sendSocket, sendGroup, 0, 0, 1023, 512, 790, 12);
        TimeUnit.SECONDS.sleep(1);
        //datagramSendValues(0, sendSocket, sendGroup, 25, 0, 1023, 512, 790, 12);
        //TimeUnit.SECONDS.sleep(1);
        //datagramSendValues(0, sendSocket, sendGroup, 250, 0, 1023, 512, 790, 12);
        
        
        int v = 0;
        for(int i = 0;i < 100;i++) {
            v+= 50;
            System.out.println("V="+v);
            datagramSendValues(i, sendSocket, sendGroup, v % 1023, (v+128) % 1023, (v+256) % 1023, (v+384) % 1023, (v+512) % 1023, (v+640) % 1023);
            TimeUnit.MILLISECONDS.sleep(50);
            
        }
        
        discovery.stop();*/
        
        NetworkDMXSender nds = new NetworkDMXSender();
        nds.start();
        TimeUnit.SECONDS.sleep(1);
        
        double speed = 0.025d;
        
        double vd = 0d;
        for(int i = 0;i < 800;i++) {
            vd += 0.05d;
            int v =  (int)Math.floor((Math.sin(vd)+1d)/2d*1023d);
            
            long stime = System.currentTimeMillis();
            nds.sendDMXToDevice("MMDJ_ES2", valuesToMsg(i, v%1023, (v+128) % 1023, (v+512)%1023));
            //nds.sendDMXToDevice("MMDJ_ES1", valuesToMsg(i, v%1023, (v+128) % 1023, (v+512)%1023));
            System.out.println("Sent "+v+" in "+(System.currentTimeMillis()-stime)+"ms");
            TimeUnit.MILLISECONDS.sleep(20);
            
        }
        nds.stop();
        TimeUnit.MILLISECONDS.sleep(500);
    }
    
    private static void sendValues(int v1, int v2, int v3, int v4, int v5, int v6) {
        long stime = System.currentTimeMillis();
        try {
            Socket s = new Socket(InetAddress.getByName("192.168.43.121"), 80);
            s.setSoTimeout(110);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
            pw.println(String.format("%04d", v1)
                    +String.format("%04d", v2)
                    +String.format("%04d", v3)
                    +String.format("%04d", v4)
                    +String.format("%04d", v5)
                    +String.format("%04d", v6));
            pw.println();
            pw.println();
            pw.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println("Got: "+br.readLine());
            pw.close();
            br.close();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(SendDMX.class.getName()).log(Level.SEVERE, "", ex);
        }
        System.out.println("Request time: "+(System.currentTimeMillis()-stime)+"ms");
        
    }
    
    private static void datagramSendValues(int packetNum, DatagramSocket sendSocket, InetAddress sendGroup, int v1, int v2, int v3, int v4, int v5, int v6) {
        long stime = System.currentTimeMillis();
        try {
            String msg = ""
                    + String.format("%2s", Integer.toString(v1, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v2, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v3, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v4, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v5, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v6, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(packetNum % 1023, 32)).replace(' ', '0')
                    + "\r\n";
            
            byte [] resp = msg.getBytes();
                            DatagramPacket respPacketS = new DatagramPacket(resp, resp.length, sendGroup, 7134);
                            sendSocket.send(respPacketS);
        } catch (IOException ex) {
            Logger.getLogger(SendDMX.class.getName()).log(Level.SEVERE, "", ex);
        }
        System.out.println("Request time: "+(System.currentTimeMillis()-stime)+"ms");
        
    }
    
    public static String valuesToMsg(int packetNum, int v1, int v2, int v3) {
        String msg = ""
                    + String.format("%2s", Integer.toString(v1, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v2, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v3, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v1, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v2, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(v3, 32)).replace(' ', '0')
                    + String.format("%2s", Integer.toString(packetNum % 1023, 32)).replace(' ', '0')
                    + "\r\n";
        return msg;
    }
    
}
