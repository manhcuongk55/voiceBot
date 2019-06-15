package viettel.cyberspace.speechrecognize.speedtotextcyberspace.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

/**
 * Created by Toan on 3/21/2018.
 */

public class Player {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
    private static int bufferSize;
    private static AudioTrack player;

    static {

        Log.i("duypq3","android.os.Build.VERSION.SDK_INT="+ Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                // For some readon we couldn't obtain a buffer size
                bufferSize = SAMPLE_RATE * CHANNELS * 2;
            }

            player = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(16000)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        } else {
            int outputBufferSize = AudioTrack.getMinBufferSize(16000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            player = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    16000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    outputBufferSize,
                    AudioTrack.MODE_STREAM);
        }
    }

    public int getSAMPLE_RATE() {
        return SAMPLE_RATE;
    }

    public int getCHANNELS() {
        return CHANNELS;
    }

    public static AudioTrack getPlayer() {
        return player;
    }


}
