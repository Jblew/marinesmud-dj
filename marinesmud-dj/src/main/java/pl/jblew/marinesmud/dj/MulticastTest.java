/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj;

import com.google.common.util.concurrent.AtomicDouble;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.iot.multicast.MulticastDMXServer;
import pl.jblew.marinesmud.dj.iot.multicast.UDPDeviceDiscovery;

/**
 *
 * @author teofil
 */
public class MulticastTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket serverSocket = new DatagramSocket(5560);
        serverSocket.setSoTimeout(1000/15);
        serverSocket.setTrafficClass(0x04);
                
                /*
                IPTOS_LOWCOST (0x02)
IPTOS_RELIABILITY (0x04)
IPTOS_THROUGHPUT (0x08)
IPTOS_LOWDELAY (0x10)
                */

        boolean bright = true;
        while (true) {
            try {
                byte v = (byte) (bright ? 0x00 : 0xFF);
                bright = !bright;
                byte[] sendData = new byte[]{
                    v,
                };
                InetAddress ip = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, 5562);
                serverSocket.send(sendPacket);
                //System.out.println("Send "+v);

                DatagramPacket buf = new DatagramPacket(new byte[1], 1);
                serverSocket.receive(buf);
                //System.out.println(buf.getData()[0]);

                TimeUnit.MILLISECONDS.sleep(1000/15);
                
            } catch (java.net.SocketTimeoutException ex) {
                //TimeUnit.MILLISECONDS.sleep(100);
            }
        }

        /*final Set<Socket> socketSet = Collections.synchronizedSet(new HashSet<>());
        //ExecutorService executorCache = Executors.newCachedThreadPool();
        ScheduledExecutorService ticker = Executors.newScheduledThreadPool(1);

        AtomicBoolean on = new AtomicBoolean(true);
        ticker.scheduleAtFixedRate(() -> {
            byte valueByte = (byte)(on.get()? 255 : 0);
            on.set(!on.get());

            Socket[] sockets;
            synchronized (socketSet) {
                sockets = socketSet.toArray(new Socket[]{});
            }
            for (Socket s : sockets) {
                //executorCache.execute(() -> {
                    long sTime = System.nanoTime();
                    if (s.isBound() && s.isConnected() && !s.isClosed() && !s.isOutputShutdown()) {
                        try {
                            s.getOutputStream().write(valueByte);
                            s.getOutputStream().flush();
                            int got = -2;//s.getInputStream().read();
                            long timeNs = System.nanoTime()-sTime;
                            System.out.println("got="+got+" in "+(timeNs/1000000)+"ms");
                        } catch (IOException ex) {
                            try {
                                Logger.getLogger(MulticastTest.class.getName()).log(Level.SEVERE, null, ex);
                                s.close();
                                synchronized (socketSet) {
                                    socketSet.remove(s);
                                }
                            } catch (IOException ex1) {
                                Logger.getLogger(MulticastTest.class.getName()).log(Level.SEVERE, null, ex1);
                                synchronized (socketSet) {
                                    socketSet.remove(s);
                                }
                            }
                        }
                    } else {
                        try {
                            s.close();
                            synchronized (socketSet) {
                                socketSet.remove(s);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(MulticastTest.class.getName()).log(Level.SEVERE, null, ex);
                            synchronized (socketSet) {
                                socketSet.remove(s);
                            }
                        }
                    }
                //});
            }
        }, 1000/15, 1000/15, TimeUnit.MILLISECONDS);

        ServerSocket serverSocket = new ServerSocket(5542);

        //int i = 0;
        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientSocket.setKeepAlive(true);
            System.out.println("New client!");
            synchronized (socketSet) {
                socketSet.add(clientSocket);
            }
            /*clientSocket.getOutputStream().write((25*i)%255);
            clientSocket.getOutputStream().flush();
            clientSocket.close();
            i++;* /
        }

        /*MulticastDMXServer mcServer = new MulticastDMXServer();
        mcServer.start();
        try {
            TimeUnit.SECONDS.sleep(90);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        mcServer.stop();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        System.out.println("Should be stopped by now");*/
 /*UDPDeviceDiscovery discovery = new UDPDeviceDiscovery();
        
        discovery.start();
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        discovery.stop();*/
    }
}

/*
cd /Users/teofil/git-repository/marinesmud-dj/marinesmud-dj;
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home "/Applications/NetBeans/NetBeans 8.2.app/Contents/Resources/NetBeans/java/maven/bin/mvn"
"-Dexec.args=-classpath %classpath pl.jblew.marinesmud.dj.MulticastTest"
-Dexec.executable=/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home/bin/java
-Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
Running NetBeans Compile On Save execution. Phase execution is skipped and output directories of dependency projects (with Compile on Save turned on) will be used instead of their jar artifacts.
Scanning for projects...

S
 */
