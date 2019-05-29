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
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author teofil
 */
public class UDPDeviceDiscovery {
    private static final int DEVICE_TIMEOUT_MS = 5 * 1000;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService receiveExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, NetworkDevice> devices = Collections.synchronizedMap(new HashMap<String, NetworkDevice>());
    
    public UDPDeviceDiscovery() {
    }
    
    public String getServiceIpByName(String name) {
        NetworkDevice nd =  devices.get(name);
        return (nd == null? null : nd.getIp());
    }

    public void start() {
        if (!isRunning.get()) {
            receiveExecutor.execute(new ReceiveWorker());

        }
    }

    public void stop() {
        isRunning.set(false);
        try {

            receiveExecutor.awaitTermination(350, TimeUnit.MILLISECONDS);
            receiveExecutor.shutdownNow();
        } catch (InterruptedException ex) {
        }

    }

    private void processHeartbeat(String ip, String name) {
        synchronized (devices) {
            if (devices.containsKey(name)) {
                String prevIp = devices.get(name).getIp();
                devices.get(name).gotPacket(ip);
                if (!ip.equals(prevIp)) {
                    System.out.println("Device ip changed: " + name + ": " + prevIp + " --> " + ip);
                }
            } else {
                devices.put(name, new NetworkDevice(name, ip));
                System.out.println("Device appeared: " + name + "@" + ip);
            }
        }
    }

    private void removeIdleDevices() {
        synchronized (devices) {
            List<String> namesToRemove = new LinkedList<>();
            for (String name : devices.keySet()) {
                if (devices.get(name).getIdleTimeMs() > DEVICE_TIMEOUT_MS) {
                    namesToRemove.add(name);
                    System.out.println("Device lost: " + name);
                }
            }
            for (String name : namesToRemove) {
                devices.remove(name);
            }
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
            DatagramSocket sendSocket;
            try {
                socket = new MulticastSocket(7134);
                socket.setSoTimeout(200);
                sendSocket = new DatagramSocket();
                InetAddress group = InetAddress.getByName("235.0.2.1");
                InetAddress sendGroup = InetAddress.getByName("235.0.2.1");
                try {
                    socket.joinGroup(group);
                } catch (SocketException ex) {
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
                        byte[] buf = new byte[128];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(packet.getData()).trim();
                        if (received.startsWith("iam")) {
                            String[] parts = received.split(" ");
                            if (parts.length > 1) {
                                String[] idElems = parts[1].split("@");
                                if (idElems.length > 0) {
                                    String ip = idElems[0];
                                    String name = idElems[1];
                                    processHeartbeat(ip, name);
                                }

                            }
                        }
                        System.out.println("Received: \"" + received + "\".");

                        if (received.startsWith("iam")) {
                            byte[] resp = "resp: got_iam".getBytes();
                            DatagramPacket respPacket = new DatagramPacket(resp, resp.length, group, 7134);
                            socket.send(respPacket);
                            
                            byte [] resp2 = "002501250225032504500750123 0".getBytes();
                            DatagramPacket respPacketS = new DatagramPacket(resp2, resp2.length, sendGroup, 7134);
                            sendSocket.send(respPacketS);
                        }
                    } catch (SocketTimeoutException ex) {
                        //do nothing, iterate to next loop
                    }
                    removeIdleDevices();
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

    public static class NetworkDevice {
        private final String name;
        private final AtomicReference<String> ipRef = new AtomicReference<>("");
        private final AtomicLong lastPacketMs = new AtomicLong(0);

        public NetworkDevice(String name, String ip) {
            this.name = name;
            this.ipRef.set(ip);
            lastPacketMs.set(System.currentTimeMillis());
        }

        public void gotPacket(String ip) {
            this.ipRef.set(ip);
            lastPacketMs.set(System.currentTimeMillis());
        }

        public int getIdleTimeMs() {
            return (int) (System.currentTimeMillis() - lastPacketMs.get());
        }

        public String getIp() {
            return ipRef.get();
        }
    }
}
