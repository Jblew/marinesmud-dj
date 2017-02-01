/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.dmx;

/**
 *
 * @author teofil
 *
public class NRSerialOutputManager implements OutputManager {
    private static final int DMX_BAUD = 250000;
    private static final int BREAK_BAUD = 100000;
    private final ClockWorker clock;
    private final PortChangeListener portChangeListener;
    //private final AtomicReference<NRSerialPort> deviceRef = new AtomicReference<>(null);

    public NRSerialOutputManager(ClockWorker clock) {
        this.clock = clock;
        portChangeListener = (String newPortName) -> changeDevice(newPortName);

        clock.setDMXTask(() -> sendDMXSync());

    }

    @Override
    public String[] listSerialPorts() {
        return NRSerialPort.getAvailableSerialPorts().stream().toArray(String[]::new);
    }

    @Override
    public PortChangeListener getPortChangeListener() {
        return this.portChangeListener;
    }

    private void changeDevice(String newPortName) {
        synchronized (deviceRef) {
            NRSerialPort lastPort = deviceRef.get();
            if(lastPort != null && lastPort.isConnected()) lastPort.disconnect();
            
            NRSerialPort port = new NRSerialPort(newPortName, 11520);
            port.connect();
            deviceRef.set(port);
        }
    }

    private void configBreak(NRSerialPort port) throws IOException {
        try {
            port.setBaud(BREAK_BAUD);
            //port.setSerialPortParams(this.BREAK_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
        } catch (Exception ex) {
            Logger.getLogger(NRSerialOutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void configDMX(NRSerialPort port) throws IOException {
        try {
            port.setBaud(DMX_BAUD);
            //port.setSerialPortParams(this.BREAK_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
        } catch (Exception ex) {
            Logger.getLogger(NRSerialOutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void write(NRSerialPort port, byte[] channelValues) throws InterruptedException, IOException {
        configBreak(port);
        while (!port.getSerialPortInstance().isCTS()) {
            TimeUnit.MICROSECONDS.sleep(1);
        }

        OutputStream os = port.getOutputStream();
        os.write(0);

        while (!port.getSerialPortInstance().isCTS()) {
            TimeUnit.MICROSECONDS.sleep(1);
        }

        configDMX(port);
        while (!port.getSerialPortInstance().isCTS()) {
            TimeUnit.MICROSECONDS.sleep(1);
        }

        os = port.getOutputStream();
        os.write(0);

        os.write(channelValues);

        while (!port.getSerialPortInstance().isCTS()) {
            TimeUnit.MICROSECONDS.sleep(1);
        }
    }

    private void sendDMXSync() {
        NRSerialPort device = deviceRef.get();
        if (device != null) {
            long sTime = System.currentTimeMillis();

            byte[] data = new byte[511];
            for (int i = 0; i < data.length; i++) {
                data[i] = 20;
            }

            try {
                write(device, data);
            } catch (IOException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }

            long eTime = System.currentTimeMillis();
            System.out.println("Sent DMX in " + (eTime - sTime) + " ms");
        }
    }

}*/
