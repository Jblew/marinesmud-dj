/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj;

/*import com.ftdi.FTD2XXException;
import com.ftdi.FTDevice;
import com.ftdi.Parity;
import com.ftdi.StopBits;
import com.ftdi.WordLength;*/
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import pl.jblew.marinesmud.dj.dmx.SingleByteCommunication;

/**
 *
 * @author teofil
 */
public class Test {
    public static void main(String... args) throws UnsupportedEncodingException {
        
        /*try {
            List<FTDevice> fTDevices;
            fTDevices = FTDevice.getDevices(true);
            for (FTDevice fTDevice : fTDevices) {

                System.out.println(fTDevice);
                System.out.println(fTDevice.getDevType());
                System.out.println(fTDevice.getDevID());
                System.out.println(fTDevice.getDevLocationID());
                fTDevice.open();
                fTDevice.setBaudRate(250000);
                fTDevice.setDataCharacteristics(WordLength.BITS_8,
                        StopBits.STOP_BITS_1, Parity.PARITY_NONE);
                fTDevice.setTimeouts(1000, 1000);

                //System.out.println(fTDevice.read());
                fTDevice.close();
            }

        } catch (FTD2XXException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        /*long sTime = System.currentTimeMillis();
        int [] channels = new int [510];
        channels[1] = 255;
        channels[2] = 255;
        channels[3] = 255;
        channels[9] = 255;
        channels[10] = 255;
        channels[11] = 255;
        channels[12] = 255;
        
        StringBuilder sb = new StringBuilder();
        for(int v : channels) {
            sb.append(v);
            sb.append(",");
        }
        sb.append(0);
        //String channelsStr = Arrays.stream(channels).mapToObj(i -> ((Integer) i).toString()).collect(Collectors.joining(","));
        String device = "/dev/tty.usbmodem1411";
        String json = "{\"device\":\""+device+"\",\"channels\":["+sb.toString()+"]}";
        String base64 = Base64.getUrlEncoder().encodeToString(json.getBytes("UTF-8"));
        
        System.out.println(base64);
        //System.out.println("");
        
        
        
        String cmd = "python send-dmx.py \""+base64+"\"";
        try {
            Process p = Runtime.getRuntime().exec(new String []{"python", "send-dmx.py", base64});
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*System.out.println("===Error stream===");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            reader.lines().forEach(line -> System.out.println(line));
            System.out.println("");
            reader.close();
            
            System.out.println("===Output stream===");
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            reader.lines().forEach(line -> System.out.println(line));
            System.out.println("");
            reader.close();*
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        long eTime = System.currentTimeMillis();
        long intervalMs = (eTime-sTime);
        System.out.println("DMX external send time: "+intervalMs+" ms");*/
        
        /*SingleByteCommunication sbc = new SingleByteCommunication();
        sbc.initialize();
        sbc.close();
        System.out.println("Done");*/
        

        /*byte [] dmxFrame = new byte [512];
        byte l1 = (byte)(dmxFrame.length & 0xFF);
        byte l2 = (byte)((dmxFrame.length >> 8) & 0xFF);
        System.out.println(l1+" "+l2);*/
        
       // IconFontSwing.buildImage(GoogleMaterialDesignIcons.ANDROID, 60, Color.RED.darker());
       
       float [] levels = new float [] {0f, 0f, 0f, 0f};
       
       
       byte v = (byte) 0x00;
       if(levels[0] > 0.5f) v |= 1 << 0;
       if(levels[1] > 0.5f) v |= 1 << 1;
       if(levels[2] > 0.5f) v |= 1 << 2;
       if(levels[3] > 0.5f) v |= 1 << 3;
       
       boolean a =  (v & (1 << 0)) != 0;
       boolean b =  (v & (1 << 1)) != 0;
       boolean c =  (v & (1 << 2)) != 0;
       boolean d =  (v & (1 << 3)) != 0;
       
        System.out.println(""+(a? "A": ".")+(b? "B": ".")+(c? "C": ".")+(d? "D": "."));
    }
}
//{"device":"/dev/tty.usbmodem1411","channels":[255,123,20,30,23,210,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}