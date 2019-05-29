/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.iot.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.config.StaticConfig;

/**
 *
 * @author teofil
 */
public class MulticastDMXServer {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService receiveExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService sendExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<DatagramSocket> sendSocket = new AtomicReference<>(null);
    private final AtomicLong counter = new AtomicLong(0);

    public MulticastDMXServer() {
    }

    public void start() {
        if (!isRunning.get()) {
            receiveExecutor.execute(new ReceiveWorker());
            try {
                sendSocket.set(new DatagramSocket(1133));
            } catch (SocketException ex) {
                Logger.getLogger(MulticastDMXServer.class.getName()).log(Level.SEVERE, "", ex);
            }
            sendExecutor.scheduleWithFixedDelay(new SendWorker(), 2000, 2000, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        isRunning.set(false);
        if (sendSocket.get() != null) {
            sendSocket.get().close();
            System.out.println("Closed sendSocket");
        }
        try {
            sendExecutor.shutdown();
            sendExecutor.shutdownNow();
            receiveExecutor.awaitTermination(350, TimeUnit.MILLISECONDS);
            receiveExecutor.shutdownNow();
            System.out.println("Shut down both executors");
        } catch (InterruptedException ex) {
        }
        System.out.println("Stopped");

    }

    public void sendBytes(byte[] buf) {
        try {
            DatagramSocket ds = sendSocket.get();
            if (ds != null) {
                InetAddress group = InetAddress.getByName("235.0.2.1");
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 7134);
                ds.send(packet);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(MulticastDMXServer.class.getName()).log(Level.SEVERE, "", ex);
        } catch (IOException ex) {
            Logger.getLogger(MulticastDMXServer.class.getName()).log(Level.SEVERE, "", ex);
        }
    }

    private final class ReceiveWorker implements Runnable {
        @Override
        public void run() {
            isRunning.set(true);
            Thread.currentThread().setName("ReceiveThread");
            /*try (DatagramSocket socket = new DatagramSocket(3855)) {

            } catch (SocketException ex) {
                Exceptions.printStackTrace(ex);
            }*/
            MulticastSocket socket = null;
            try {
                socket = new MulticastSocket(7134);
                socket.setSoTimeout(200);
                InetAddress group = InetAddress.getByName("235.0.2.1");
                try {
                    socket.joinGroup(group);
                }
                catch(SocketException ex) {
                    ex.printStackTrace();
                    System.err.println("Trying to set interface to en0:");
                    socket.setNetworkInterface(NetworkInterface.getByName("en0"));
                    socket.joinGroup(group);
                    System.out.println("Succesfully connected");
                }
                //socket.joinGroup(new InetSocketAddress(group, 7134), NetworkInterface.getByName("en0"));

                DatagramPacket packet;
                while (isRunning.get()) {
                    try {
                        byte[] buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);

                        String received = new String(packet.getData());
                        System.out.println("Received: \"" + received + "\".");
                        
                        if(!received.startsWith("resp")) {
                            byte [] resp = "resp: Haha".getBytes();
                            DatagramPacket respPacket = new DatagramPacket(resp, resp.length, group, 7134);
                            socket.send(respPacket);
                        }
                    } catch (SocketTimeoutException ex) {
                        //do nothing, iterate to next loop
                    }
                }

                socket.leaveGroup(group);
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                if (socket != null) {
                    socket.close();
                }
            }

            isRunning.set(false);
        }
    }

    private final class SendWorker implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("SendThread");

            byte[] buf = new byte[256];
            buf = ("(" + counter.getAndIncrement() + ")" + new Date().toString()).getBytes();
            sendBytes(buf);
        }

    }
}
