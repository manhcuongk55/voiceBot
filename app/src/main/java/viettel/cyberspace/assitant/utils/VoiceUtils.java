package viettel.cyberspace.assitant.utils;

import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.common.primitives.Bytes;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by duy on 29/06/2018.
 */

public class VoiceUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void request(String text) {
        // TODO Auto-generated method stub
        int code = -1;

        String urlString = "http://203.113.152.90/hmm-stream/syn";

        // urlString= urlString+"?data="+text +"&voices=doanngocle.htsvoice&key=K9W6tNTeUuwrkyYARkAmzJ94D9vUR2Qdo5YwVI7D";

        long time1 = System.currentTimeMillis();
        Log.i("duypq33", "time1=" + time1);
        StringBuffer chaine = new StringBuffer("");
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
           /* connection.setRequestProperty("data", text);
            connection.setRequestProperty("voices", "doanngocle.htsvoice");
            connection.setRequestProperty("key", "K9W6tNTeUuwrkyYARkAmzJ94D9vUR2Qdo5YwVI7D");*/


            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("data", text)
                    .appendQueryParameter("voices", "doanngocle.htsvoice")
                    .appendQueryParameter("key", "K9W6tNTeUuwrkyYARkAmzJ94D9vUR2Qdo5YwVI7D");
            String query = builder.build().getEncodedQuery();

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            connection.setRequestMethod("POST");
            connection.connect();

            code = connection.getResponseCode();
            Log.i("duypq33", "code=" + code);
            if (code == 200) {

                InputStream input = connection.getInputStream();
                BufferedInputStream in = new BufferedInputStream(input);
                byte[] byte_buff = new byte[8000];
                byte[] buffer = new byte[0];
                byte[] last_byte = new byte[0];
                boolean last_byte_sent = false;
                int player_buffer_size = MyPlayAudio.getInstance().getBufferSize();
                int nread;
                AudioTrack player;
                player = MyPlayAudio.getInstance().getPlayer();
                player.play();
                Log.i("duypq33", "time1=" + (System.currentTimeMillis() - time1));

                while ((nread = in.read(byte_buff)) != -1) {
                    buffer = Bytes.concat(buffer, Arrays.copyOfRange(byte_buff, 0, nread));
                    if (buffer.length >= player_buffer_size) {
                        if (buffer.length % 2 == 0) {
                            player.write(buffer, 0, buffer.length);
                            buffer = new byte[0];
                            last_byte_sent = true;
                        } else {
                            player.write(buffer, 0, buffer.length - 1);
                            buffer = new byte[]{buffer[buffer.length - 1]};
                            last_byte_sent = true;
                        }

                    } else {
                        last_byte = buffer;
                        last_byte_sent = false;
                    }
                }
                if (!last_byte_sent) {
                    player.write(last_byte, 0, last_byte.length % 2 == 0 ? last_byte.length : last_byte.length - 1);
                }
                in.close();
                input.close();

            }
            connection.disconnect();
        } catch (IOException e) {
            // Writing exception to log
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void stopVoice() {
        AudioTrack player;
        player = MyPlayAudio.getInstance().getPlayer();

        if (player != null)
            player.stop();
    }
}
