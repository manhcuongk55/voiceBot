package viettel.cyberspace.speechrecognize.speedtotextcyberspace.myservice.speech2text;

import android.media.AudioRecord;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

import viettel.cyberspace.speechrecognize.speedtotextcyberspace.listeners.ChangeAdapterListener;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.listeners.StopRecordListener;

/**
 * Created by Toan on 4/4/2018.
 */

public abstract class STTService {
    private String TAG = "STTService";
    protected ChangeAdapterListener changeAdapterListener;
    protected StopRecordListener stopRecordListener;
    protected boolean isRecording = false;
    private Thread recordingThread = null;
    protected AudioRecord recorder;
    protected boolean isFirstMess = false;

    public void setStopRecordListener(StopRecordListener stopRecordListener) {
        this.stopRecordListener = stopRecordListener;
    }

    public void setChangeAdapterListener(ChangeAdapterListener changeAdapterListener) {
        this.changeAdapterListener = changeAdapterListener;
    }

    abstract protected CountDownLatch record();

    abstract protected CountDownLatch sendFile(String s);

    public void startRecognize() {
        Log.d(TAG, "startRecognize: ");
        isRecording = true;
        isFirstMess = true;
/*        recordingThread = new Thread(() -> {
            record();
            //sendFile(UtilsVoice.BASE_PATH);
        });*/

        recordingThread = new Thread() {
            @Override
            public void run() {
                try {
                    record();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        recordingThread.start();
    }

    public void stopRecognize() {

        try {
            if (null != recorder && isRecording) {
                isRecording = false;
                recorder.stop();
                recorder.release();
                recordingThread = null;
            }
            Log.d(TAG, "stopRecognize: ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void pauseRecognize() {

        try {
            if (null != recorder && isRecording) {
                isRecording = false;
                recorder.stop();
                recorder.release();
                recordingThread = null;
            }
            Log.d(TAG, "stopRecognize: ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
