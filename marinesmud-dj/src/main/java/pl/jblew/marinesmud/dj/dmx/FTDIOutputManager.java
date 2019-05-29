/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
package pl.jblew.marinesmud.dj.dmx;

import com.ftdi.DeviceStatus;
import com.ftdi.FTD2XXException;
import com.ftdi.FTDevice;
import com.ftdi.Parity;
import com.ftdi.StopBits;
import com.ftdi.WordLength;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.clock.ClockWorker;


public class FTDIOutputManager implements OutputManager {
    private static final long DMX_BAUD = 250000;
    private static final long BREAK_BAUP = 100000;
    private final ClockWorker clock;
    private final PortChangeListener portChangeListener;
    private final AtomicReference<FTDevice> deviceRef = new AtomicReference<>(null);

    public FTDIOutputManager(ClockWorker clock) {
        this.clock = clock;
        portChangeListener = (String newPortName) -> changeDevice(newPortName);
        
        clock.setDMXTask(() -> sendDMXSync());
    }

    @Override
    public String[] listSerialPorts() {
        ArrayList<String> names = new ArrayList<>();

        try {
            List<FTDevice> fTDevices;
            fTDevices = FTDevice.getDevices();
            for (FTDevice fTDevice : fTDevices) {
                names.add(fTDevice.getDevDescription() + " " + fTDevice.getDevSerialNumber());
                /*fTDevice.open();
                fTDevice.setBaudRate(57600);
                fTDevice.setDataCharacteristics(WordLength.BITS_8,
                        StopBits.STOP_BITS_1, Parity.PARITY_NONE);
                fTDevice.setTimeouts(1000, 1000);

                System.out.println(fTDevice.read());
                fTDevice.close();*
            }

        } catch (FTD2XXException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return names.toArray(new String[]{});
    }

    @Override
    public PortChangeListener getPortChangeListener() {
        return this.portChangeListener;
    }

    private void changeDevice(String newPortName) {
        synchronized (deviceRef) {
            try {

                System.out.println("New port = " + newPortName);
                FTDevice newDevice = FTDevice.getDevices().stream().sequential()
                        .filter(d -> (d.getDevDescription() + " " + d.getDevSerialNumber()).equals(newPortName))
                        .findFirst().orElse(null);

                if (newDevice != null && !newDevice.isOpened()) {
                    //newDevice.open();
                    //if (newDevice.isOpened()) {
                    //    newDevice.close();
                    //    System.out.println("Device could be opened and is reassigned");
                        deviceRef.set(newDevice);
                    //}
                    //else System.out.println("Device could not be opened");
                }
                else if(newDevice == null) System.out.println("Device is null");
                else System.out.println("Device is already opened");

            } catch (FTD2XXException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void configBreak(FTDevice fTDevice) throws FTD2XXException {
        fTDevice.setBaudRate(this.BREAK_BAUP);
        fTDevice.setDataCharacteristics(WordLength.BITS_8, StopBits.STOP_BITS_1, Parity.PARITY_EVEN);//8E1
        fTDevice.setTimeouts(1000, 1000);
    }

    private void configDMX(FTDevice fTDevice) throws FTD2XXException {
        fTDevice.setBaudRate(this.DMX_BAUD);
        fTDevice.setDataCharacteristics(WordLength.BITS_8, StopBits.STOP_BITS_2, Parity.PARITY_NONE);//8N2
        fTDevice.setTimeouts(1000, 1000);
    }

    private void write(FTDevice d, byte[] channelValues) throws FTD2XXException, InterruptedException {
        configBreak(d);
        d.write(new byte[]{0});

        while (d.getDeviceStatus().contains(DeviceStatus.CTS)) {
            TimeUnit.MICROSECONDS.sleep(1);
        }

        configDMX(d);
        d.write(new byte[]{0});

        d.write(channelValues);

        while (d.getDeviceStatus().contains(DeviceStatus.CTS)) {
            TimeUnit.MICROSECONDS.sleep(1);
        }
    }
    
    private void sendDMXSync() {
        FTDevice device = deviceRef.get();
        if(device != null) {
            long sTime = System.currentTimeMillis();
        
            byte [] data = new byte [511];
            for(int i = 0;i < data.length;i++) {
                data[i] = 20;
            }
            
            try {
                device.open();
                System.out.println("Device opened");
                //write(device, data);
            } catch (FTD2XXException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            //} catch (InterruptedException ex) {
            //    Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    device.close();
                } catch (FTD2XXException ex) {
                    Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
            long eTime = System.currentTimeMillis();
            System.out.println("Sent DMX in "+(eTime-sTime)+" ms");
        }
    }

}
*/