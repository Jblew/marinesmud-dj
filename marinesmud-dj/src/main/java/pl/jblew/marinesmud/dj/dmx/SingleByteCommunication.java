/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.dmx;

//import gnu.
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.awt.Color;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleByteCommunication implements SerialPortEventListener {

    private static final byte[] TEST_MESSAGE = new byte[]{(byte) 0x12, (byte) 0x13, (byte) 0x15, (byte) 0x21, (byte) 0xff, (byte) 0xab, (byte) 0x3c, (byte) 0x0, (byte) 0x11};
    public static final byte CMD_ECHO = 0x01;
    public static final byte CMD_LED_ON = 0x02;
    public static final byte CMD_LED_OFF = 0x03;
    public static final byte CMD_SEND = 0x04;
    public static final byte RESP_OK_NO_DATA = 0x02;
    public static final byte RESP_OK_DATA = 0x03;
    public static final byte RESP_WRONG_CMD = 0x04;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyUSB0", // Linux
        "/dev/ttyUSB1", // Linux
        "COM6", // Windows
        "/dev/tty.usbmodem1411" // Mac OS X El Capitan
    };
    /**
     * Buffered input stream from the port
     */
    private InputStream input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 57600;

    public SingleByteCommunication() {
    }

    public boolean initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // iterate through, looking for the port
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }

        if (portId == null) {
            System.out.println("Could not find COM port.");
            return false;
        } else {
            System.out.println("Found your port");
            System.out.println("PortId="+portId.getName());
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            System.out.println("Port set pairity");

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            System.out.println("Checking transmission...");
            /*byte[] result = sendCommandAndAwaitResult(CMD_ECHO, TEST_MESSAGE);
            for (int i = 0; i < TEST_MESSAGE.length; i++) {
                if (result[i] != TEST_MESSAGE[i]) {
                    System.out.println("Echo does not match");
                    close();
                    return false;
                }
            }*/
            connected.set(true);
            System.out.println("Connection estabilished");
            //eBus.post(new SystemConnectedEvent());

            sendCommandWithoutResponse(CMD_LED_ON);
            TimeUnit.MILLISECONDS.sleep(200);
            sendCommandWithoutResponse(CMD_LED_OFF);
            TimeUnit.MILLISECONDS.sleep(200);
            sendCommandWithoutResponse(CMD_LED_ON);
            TimeUnit.MILLISECONDS.sleep(200);
            sendCommandWithoutResponse(CMD_LED_OFF);
            System.out.println("Written leds");

            // add event listeners
            //serialPort.addEventListener(this);
            //serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        System.out.println("Finished init");
        return true;
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * This Method can be called to print a single byte to the serial connection
     */
    public void sendSingleByte(byte myByte) {
        try {
            output.write(myByte);
            output.flush();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This Method is called when Serialdata is recieved
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int myByte = input.read();
                int value = myByte & 0xff;//byte to int conversion:0...127,-127...0 -> 0...255
                if (value >= 0 && value < 256) {//make shure everything is ok
                    //System.out.print((char)myByte);
                    //sendSingleByte((byte) myByte);
                }
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public byte[] sendCommandAndAwaitResult(byte cmd, byte[] TEST_MESSAGE) throws IOException {
        sendSingleByte(cmd);
        sendSingleByte((byte) TEST_MESSAGE.length);
        for (int i = 0; i < TEST_MESSAGE.length; i++) {
            sendSingleByte(TEST_MESSAGE[i]);
        }
        System.out.println("Awaiting response...");
        int statusCode = input.read() & 0xff;
        System.out.println("Got status!");
        if (statusCode == RESP_OK_DATA) {
            int length = input.read() & 0xff;
            byte[] out = new byte[length];
            for (int i = 0; i < length; i++) {
                out[i] = (byte) (input.read() & 0xff);
            }
            return out;
        } else {
            throw new BadResponseException(statusCode, input.read() & 0xff);
        }
        //input.read();
    }

    public void sendCommandWithoutResponse(byte cmd) throws IOException {
        sendSingleByte(cmd);
        //int statusCode = input.read() & 0xff;
        //if (statusCode != RESP_OK_NO_DATA) {
        //    throw new BadResponseException(statusCode, input.read() & 0xff);
        //}
    }

    /*@Subscribe
    public void eventSendSerialCmd(SendSerialCmd cmd) {
        if (connected.get()) {
            try {
                sendCommandWithoutResponse(cmd.getCmd());
            } catch (IOException ex) {
                Logger.getLogger(SingleByteCommunication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else throw new NotConnectedException();
    }

    public static class SendSerialCmd implements Loggable {

        private final byte cmd;

        public SendSerialCmd(byte cmd) {
            this.cmd = cmd;
        }

        public byte getCmd() {
            return cmd;
        }

        @Override
        public String toString() {
            return "Sending serial command: " + cmd;
        }
    }

    public static class SystemConnectedEvent implements Loggable {

        @Override
        public String toString() {
            return "System Connected Event";
        }
    }*/

    public static class BadResponseException extends RuntimeException {

        private final int code;
        private final int attachment;

        public BadResponseException(int code, int attachment) {
            this.code = code;
            this.attachment = attachment;
        }

        @Override
        public String toString() {
            return "BadResponseException{" + "code=" + code + ", attachment=" + attachment + '}';
        }
    }
    
    public static class NotConnectedException extends RuntimeException{}
}
