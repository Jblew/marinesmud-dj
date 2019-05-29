/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.sound;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.iot.multicast.NetworkDMXSender;

/**
 *
 * @author teofil
 */
public class NoteDetector {
    private static final Map<String, Float> FREQUENCIES = new HashMap<>();

    private NoteDetector() {

    }

    public static String detectNote(float frequencyHz) {
        try {
            Map<String, Float> frequencies = loadFrequencies();
        } catch (IOException ex) {
            Logger.getLogger(NoteDetector.class.getName()).log(Level.SEVERE, "", ex);
        }
        return null;
    }

    private static Map<String, Float> loadFrequencies() throws IOException {
        synchronized (FREQUENCIES) {
            if (FREQUENCIES.isEmpty()) {
                ObjectMapper om = new ObjectMapper();
                FREQUENCIES.putAll(om.readValue(
                        NoteDetector.class.getResource("note_table_440hz.json"), FREQUENCIES.getClass()));
            }

            return FREQUENCIES;
        }
    }

    public static class NoteDetectionResult {
        public final String note;
        public final float distance;

        public NoteDetectionResult(String note, float distance) {
            this.note = note;
            this.distance = distance;
        }
    }
}
