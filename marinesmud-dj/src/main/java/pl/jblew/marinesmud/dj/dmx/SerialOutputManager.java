/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.dmx;

import gnu.io.RXTXPort;
import gnu.io.CommPortIdentifier;
import gnu.io.RXTXVersion;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.clock.ClockWorker;
import pl.jblew.marinesmud.dj.config.Config;
import static pl.jblew.marinesmud.dj.dmx.SingleByteCommunication.CMD_LED_OFF;
import static pl.jblew.marinesmud.dj.dmx.SingleByteCommunication.CMD_LED_ON;
import pl.jblew.marinesmud.dj.scene.DMXDevice;
import pl.jblew.marinesmud.dj.scene.Scene;

/**
 *
 * @author teofil
 */
public class SerialOutputManager implements OutputManager {
    private final Scene scene;
    private final ClockWorker clock;
    private final PortChangeListener portChangeListener;
    private final AtomicReference<SerialPort> deviceRef = new AtomicReference<>(null);
    private final Lock outputLock = new ReentrantLock();
    private final Condition outputBufferEmptyCondition = outputLock.newCondition();
    private final AtomicBoolean outputBufferEmpty = new AtomicBoolean(true);

    public SerialOutputManager(ClockWorker clock, Scene scene) {
        this.clock = clock;
        this.scene = scene;
        portChangeListener = (String newPortName) -> changeDevice(newPortName);

        clock.setDMXTask(() -> sendDMXSync());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SerialPort port = deviceRef.get();
            if (port != null) {
                try {
                    port.close();
                } catch (Exception e) {
                }
            }
        }));

        System.out.println(RXTXVersion.getVersion());
        System.out.println("DRIVER_AVAILABLE=" + isDriverAvailable());
    }

    @Override
    public String[] listSerialPorts() {
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        ArrayList portList = new ArrayList();
        portList.add("None");
        String portArray[] = null;
        while (ports.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
            if (!port.getName().contains("Bluetooth")) {
                portList.add(port.getName());
            }
        }
        portArray = (String[]) portList.toArray(new String[0]);
        return portArray;
    }

    @Override
    public PortChangeListener getPortChangeListener() {
        return this.portChangeListener;
    }

    private void changeDevice(String newPortName) {
        System.out.println("Port=" + newPortName);
        synchronized (deviceRef) {
            Enumeration ports = CommPortIdentifier.getPortIdentifiers();
            while (ports.hasMoreElements()) {
                CommPortIdentifier portId = (CommPortIdentifier) ports.nextElement();
                if (portId.getName().equals(newPortName)) {
                    if (!portId.isCurrentlyOwned()) {
                        System.out.println("isOwned=" + portId.isCurrentlyOwned()
                                + "; owner=" + portId.getCurrentOwner());
                        initPort(portId);

                    } else {
                        System.out.println("Port is currently owned");
                    }
                    break;
                }
            }
        }
    }

    private void initPort(CommPortIdentifier portId) {
        try {
            SerialPort prevPort = deviceRef.get();
            if (prevPort != null) {
                prevPort.close();
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        try {
            // open serial port, and use class name for the appName.
            SerialPort serialPort = (SerialPort) portId.open(this.getClass().getName(), 200);

            // set port parameters
            serialPort.setSerialPortParams(57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
            System.out.println("Port set pairity, baud=" + serialPort.getBaudRate());

            serialPort.notifyOnOutputEmpty(true);
            serialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent spe) {
                    if (spe.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                        outputBufferEmpty.set(true);
                        outputLock.lock();
                        try {
                            outputBufferEmptyCondition.signalAll();
                        } finally {
                            outputLock.unlock();
                        }
                    }
                }
            });

            // open the streams
            InputStream input = serialPort.getInputStream();
            OutputStream output = serialPort.getOutputStream();

            System.out.println("Checking transmission...");

            int v = 0;
            write(serialPort, new byte[]{(byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v});

            System.out.println("Connection estabilished");

            //this.configDMX(serialPort);
            deviceRef.set(serialPort);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        System.out.println("Finished init");
    }

    /*private void configBreak(SerialPort port) throws IOException {
        try {
            //port.setBaudBase(9600);
            port.setSerialPortParams(this.BREAK_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
        } catch (Exception ex) {
            Logger.getLogger(SerialOutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void configDMX(SerialPort port) throws IOException {
        try {
            //port.setBaudBase(9600);
            port.setSerialPortParams(this.BREAK_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
        } catch (Exception ex) {
            Logger.getLogger(SerialOutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    private void write(SerialPort port, byte[] channelValues) throws InterruptedException, IOException {
        int START_VAL = 0x7E;
        int END_VAL = 0xE7;
        int TX_DMX_PACKET = 6;

        //long sTime = System.currentTimeMillis();
        DataOutputStream os = new DataOutputStream(port.getOutputStream());

        os.write(START_VAL);
        os.write(TX_DMX_PACKET);
        os.write((byte) (channelValues.length & 0xFF));
        os.write((byte) ((channelValues.length >> 8) & 0xFF));
        os.write(channelValues);
        os.write(END_VAL);
        outputBufferEmpty.set(false);
        os.flush();

        TimeUnit.MILLISECONDS.sleep(1);

        if (!outputBufferEmpty.get()) {
            outputLock.lock();
            try {
                outputBufferEmptyCondition.await();
            } finally {
                outputLock.unlock();
            }
        }

        //while (!port.isCTS()) {
        //    TimeUnit.MICROSECONDS.sleep(1);
        //}
        //long eTime = System.currentTimeMillis();
        //long intervalMs = (eTime-sTime);
        //System.out.println("DMX serial send time: "+intervalMs+" ms");
        //send time = 1/57600*1000*10 * bytes.length ms
    }

    private void sendDMXSync() {
        SerialPort device = deviceRef.get();
        if (device != null) {
            //System.out.println("Starting sending DMX... (thread=" + Thread.currentThread().getName() + ")");
            long sTime = System.currentTimeMillis();

            byte[] data = new byte[scene.getMaxAddr() + 1];

            for (DMXDevice d : scene.devices) {
                byte[] values = d.calculateLevels();
                for (int i = 0; i < d.getChannelCount(); i++) {
                    if (values.length > i) {
                        data[d.getStartAddress() + i] = values[i];
                    }
                    //if(values[i] > 0) System.out.println("data["+(d.getStartAddress()+i)+"] = "+values[i]);
                }
            }

            ///data[9] = (byte) 0;//radio
            ///[10] = (byte) 255; // cieply
            ///data[11] = (byte) 0; //zimny
            try {
                write(device, data);
            } catch (IOException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }

            long eTime = System.currentTimeMillis();
            //System.out.println("Sent DMX in " + (eTime - sTime) + " ms");
        }
    }

    /*public void sendDMX(DMXDevice[] devices) {

    }*/
    private static boolean isDriverAvailable() {
        boolean driverAvailable = true;

        try {
            // Load any class that should be present if driver's available
            Class.forName("gnu.io.CommPortIdentifier");
        } catch (ClassNotFoundException e) {
            // Driver is not available
            driverAvailable = false;
        }

        return driverAvailable;
    }
}
