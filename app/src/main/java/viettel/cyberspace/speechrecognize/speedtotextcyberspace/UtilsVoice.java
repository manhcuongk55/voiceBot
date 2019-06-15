package viettel.cyberspace.speechrecognize.speedtotextcyberspace;

import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.regex.Pattern;

import viettel.cyberspace.speechrecognize.speedtotextcyberspace.audio.Player;

/**
 * Created by Toan on 3/21/2018.
 */

public class UtilsVoice {
    public static final String BASE_PATH = Environment.getExternalStorageDirectory() + "/SoundTest/test.wav";
    private static final String TAG = "UtilsVoice";
    public static final String HOST_DEFAULT = "203.113.152.90";
    public static final int PORT_DEFAULT = 8121;
    public static final int PORT_8kHz = 8122;
    public static final boolean IS_16KHZ = true;
    public static final boolean LIEN_TUC = true;
    public static final boolean PARSE_JSON = true;
    public static final int timeOut = 10000;//time out (ms)
    public static final int PORT_TEXT_CLIENT = 8127;

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    public static void test() {
        AudioTrack audioTrack = Player.getPlayer();
        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/SoundTest/test.wav");
            FileInputStream in = new FileInputStream(f);
            int bytesRead = 0, amount = 0;
            int count = 256 * 1024;
            byte[] bytes = new byte[count];
            int size = 50000;
            audioTrack.play();
            while (bytesRead < size) {
                amount = in.read(bytes, 0, 256 * 1024);
                if (amount != -1) {
                    audioTrack.write(bytes, 0, amount);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String parseText(String text, String fieldName, boolean isDecode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(text, JsonNode.class);
        String v = node.findValue(fieldName).asText();
        if (isDecode) v = URLDecoder.decode(v, "UTF-8");
        Log.d(TAG, "parseText: " + v);
        return v;
    }

    public static boolean isFinal(String text) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(text, JsonNode.class);
        return node.findValue("final").asBoolean();
    }
}
