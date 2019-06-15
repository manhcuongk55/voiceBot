package viettel.cyberspace.speechrecognize.speedtotextcyberspace.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

/**
 * Created by Toan on 3/21/2018.
 */

public class Recorder {
    private static final String TAG = "Recorder";
    private static int SAMPLE_RATE;
    private static final int CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static int bufferSize;

    public static int getBufferSize() {
        return bufferSize;
    }

    public static AudioRecord getAudioRecorder(boolean is16kHz) {
        AudioRecord audioRecord = null;
        if (is16kHz) {
            SAMPLE_RATE = 16000;
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNELS,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                // For some readon we couldn't obtain a buffer size
                bufferSize = SAMPLE_RATE * CHANNELS * 2;
            }
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, CHANNELS, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        } else {
            SAMPLE_RATE = 8000;
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNELS,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                // For some readon we couldn't obtain a buffer size
                bufferSize = SAMPLE_RATE * CHANNELS * 2;
            }
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, CHANNELS, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }
        Log.i("duy8k", "SAMPLE_RATE=" + SAMPLE_RATE + "|bufferSize=" + bufferSize);
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(audioRecord.getAudioSessionId());
        }
        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
        }
        return audioRecord;
    }
}
