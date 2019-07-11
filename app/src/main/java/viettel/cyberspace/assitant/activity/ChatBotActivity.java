package viettel.cyberspace.assitant.activity;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.cloud.android.speech.MessageDialogFragment;
import com.google.cloud.android.speech.R;
import com.google.cloud.android.speech.RecognizeCommands;
import com.google.cloud.android.speech.SettingActivity;
import com.google.cloud.android.speech.SpeechService;
import com.viettel.speech.tts.Synthesizer;
import com.viettel.speech.tts.Voice;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import chatview.data.Message;
import chatview.data.MessageHistory;
import chatview.widget.ChatView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import viettel.cyberspace.assitant.model.QuestionExperts;
import viettel.cyberspace.assitant.model.ResponseAnswer;
import viettel.cyberspace.assitant.model.ResponseQuestionExperts;
import viettel.cyberspace.assitant.model.User;
import viettel.cyberspace.assitant.rest.ApiClient;
import viettel.cyberspace.assitant.rest.ApiInterface;
import viettel.cyberspace.assitant.rest.ApiVoiceClient;
import viettel.cyberspace.assitant.rest.ApiVoiceInterface;
import viettel.cyberspace.assitant.storage.StorageManager;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.MySharePreferenceVoice;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.listeners.ChangeAdapterListener;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.listeners.StopRecordListener;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.myservice.speech2text.STTService;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.myservice.speech2text.VoiceClient;

import static com.activeandroid.Cache.getContext;

public class ChatBotActivity extends AppCompatActivity implements StopRecordListener, ChangeAdapterListener {

    private STTService voiceClient;
    ChatView chatView;
    List<Uri> mSelected;
    MaterialRippleLayout micMRL;
    MaterialRippleLayout avi;
    MaterialRippleLayout settings;
    ImageView imageVolume;
    TextView tvVoice;
    public static
    int LIMIT_QUERY_HISTORY = 20;
    public static long currentTimeStamp = Long.MAX_VALUE;
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    ApiInterface apiService;
    ApiVoiceInterface apiVoiceInterface;
    private Synthesizer m_syn;
    Animation myAnim;
    TextView tvNotification;
    public boolean isRecording = false;
    private boolean mIsLienTuc;


    // you are running your own model.
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private static final long AVERAGE_WINDOW_DURATION_MS = 500;
    private static final float DETECTION_THRESHOLD = 0.95f;
    private static final int SUPPRESSION_MS = 1500;
    private static final int MINIMUM_COUNT = 3;
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 30;
    private static final String LABEL_FILENAME = "file:///android_asset/conv_kws_labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/conv_kws.pb";
    private static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    private static final String SAMPLE_RATE_NAME = "decoded_sample_data:1";
    private static final String OUTPUT_SCORES_NAME = "labels_softmax";

    // Working variables.
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    int recordingOffset = 0;
    boolean shouldContinue = true;
    private Thread recordingThread;
    boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private TensorFlowInferenceInterface inferenceInterface;
    private List<String> labels = new ArrayList<String>();
    private List<String> displayedLabels = new ArrayList<>();
    private RecognizeCommands recognizeCommands = null;
    private static final int REQUEST_RECORD_AUDIO = 13;

    private boolean nhandangoffline = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActiveAndroid.initialize(this);
        setContentView(R.layout.activity_main);

        myAnim = AnimationUtils.loadAnimation(this, R.anim.buttom_check);

        messageAnswering.setMessageType(Message.MessageType.LeftSimpleMessage);
        messageAnswering.setAnswer(true);
        apiService = ApiClient.getClient().create(ApiInterface.class);
        apiVoiceInterface = ApiVoiceClient.getClient().create(ApiVoiceInterface.class);
        mIsLienTuc = MySharePreferenceVoice.isLienTuc(getApplicationContext());


        chatView = findViewById(R.id.chatView);
        mSelected = new ArrayList<>();
        micMRL = findViewById(R.id.micMRL2);
        avi = findViewById(R.id.avi2);
        imageVolume = findViewById(R.id.imageVolume);
        settings = findViewById(R.id.setting);
        tvNotification = findViewById(R.id.tvNotification);


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // logout();
                Intent i = new Intent(ChatBotActivity.this, SettingActivity.class);
                startActivityForResult(i, 1);
            }
        });
        avi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnableVoidButton(true);
                stopVoiceRecorder();
            }
        });
        tvVoice = findViewById(R.id.tvVoice);
        micMRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate(50);
                if (isNetworkConnected()) {
                    // Start listening to voices
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (!isRecording) {
                            stopRecording();

                            isRecording = true;
                            setEnableVoidButton(false);
                            startVoiceRecorder();
                        } else {

                            isRecording = false;
                            startRecording();

                            setEnableVoidButton(true);
                            stopVoiceRecorder();
                        }

                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(ChatBotActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {
                        showPermissionMessageDialog();
                    } else {
                        ActivityCompat.requestPermissions(ChatBotActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_RECORD_AUDIO_PERMISSION);
                    }
                } else {
                    toastError();
                }
            }
        });


        if (getString(R.string.api_key).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        } else {
            if (m_syn == null) {
                // Create Text To Speech Synthesizer.
                m_syn = new Synthesizer(getString(R.string.api_key));
            }
        }


        String actualFilename = LABEL_FILENAME.split("file:///android_asset/")[1];
        Log.i("duypq31", "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }


        recognizeCommands =
                new RecognizeCommands(
                        labels,
                        AVERAGE_WINDOW_DURATION_MS,
                        DETECTION_THRESHOLD,
                        SUPPRESSION_MS,
                        MINIMUM_COUNT,
                        MINIMUM_TIME_BETWEEN_SAMPLES_MS);

        // Load the TensorFlow model.
        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILENAME);

        // Start the recording and recognition threads.
        requestMicrophonePermission();

        if (nhandangoffline)
            startRecording();

    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }


    @Override
    public void stop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setEnableVoidButton(true);
                String t1 = tvVoice.getText().toString();
                Log.i("duypq3", "stop= " + t1);
                tvVoice.setText("");
                micMRL.setEnabled(true);
                micMRL.setAlpha(1.0f);

            }
        });
    }

    @Override
    public void change(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("duypq3", "change= " + text);
                tvVoice.setText(text);
            }
        });
    }

    @Override
    public void finish(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("duypq3", "finish= " + text);
                tvVoice.setText(text);
                String t1 = tvVoice.getText().toString();
                if (!t1.equals("")) {
                    getTextFromVoice(t1);
                    tvVoice.setText("");
                }

                if (!mIsLienTuc) {
                    setEnableVoidButton(true);
                    stopVoiceRecorder();
                }

                if(t1.equals("Kết thúc")||t1.equals("kết thúc")){
                    stopVoiceRecorder();
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentTimeStamp = Long.MAX_VALUE;
    }

    @Override
    protected void onStart() {
        try {
            voiceClient = null;
            voiceClient = new VoiceClient("123.31.18.120",
                    50051,
                    MySharePreferenceVoice.isParseJson(getApplicationContext()),
                    mIsLienTuc,
                    MySharePreferenceVoice.is16kHz(getApplicationContext()));
            voiceClient.setStopRecordListener(this);
            voiceClient.setChangeAdapterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopVoiceRecorder();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else if (chatView.onBackpress()) return;
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_information:

                return true;

            case R.id.notification:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public String getTime() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        String time = mdformat.format(calendar.getTime());
        return time;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void setEnableVoidButton(boolean isEnable) {
        if (isEnable) {
            avi.setVisibility(View.GONE);
            isRecording = false;
            stopVoiceRecorder();
            //usDetectVoice = false;
        } else {
            avi.setVisibility(View.VISIBLE);
            //usDetectVoice = true;
        }
    }


    public void getTextFromVoice(String text) {
        Message message = new Message();
        message.setMessageType(Message.MessageType.RightSimpleImage);
        message.setTime(getTime());
        message.setTimeStamp(System.currentTimeMillis());
        message.setBody(text);
        message.setUserIcon(Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
        chatView.addMessage(message);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        }, new IntentFilter("NewsFromServer"));

    }

    private void startVoiceRecorder() {

        m_syn.stopSound();
        voiceClient.startRecognize();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void stopVoiceRecorder() {
        if (voiceClient != null)
            voiceClient.stopRecognize();

        startRecording();

    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }


    public static Message messageAnswering = new Message();


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void vibrate(int time) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    private void toastError() {
        Toast.makeText(getApplicationContext(), "No Network Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                Log.v("duypq3", "onActivityResult changer host port 16kHz=" + MySharePreferenceVoice.is16kHz(getApplicationContext()));
                try {
                    voiceClient = null;
                    mIsLienTuc = MySharePreferenceVoice.isLienTuc(getApplicationContext());
                    voiceClient = new VoiceClient(MySharePreferenceVoice.getHost(getApplicationContext()),
                            MySharePreferenceVoice.getPort(getApplicationContext()),
                            MySharePreferenceVoice.isParseJson(getApplicationContext()),
                            mIsLienTuc,
                            MySharePreferenceVoice.is16kHz(getApplicationContext()));
                    voiceClient.setStopRecordListener(this);
                    voiceClient.setChangeAdapterListener(this);
                } catch (Exception e) {
                    // showDialog("Host or port is wrong. Please check carefully");
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();

        startRecognition();
    }

    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;

        stopRecognition();
    }

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("duypq31", "Audio Record can't initialize!");
            return;
        }

        record.startRecording();

        Log.v("duypq31", "Start recording");

        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;
            } finally {
                recordingBufferLock.unlock();
            }
        }

        record.stop();
        record.release();
    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;
    }

    private void recognize() {
        Log.v("duypq31", "Start recognition");

        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[] floatInputBuffer = new float[RECORDING_LENGTH];
        float[] outputScores = new float[labels.size()];
        String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};
        int[] sampleRateList = new int[]{SAMPLE_RATE};

        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            // The recording thread places data in this round-robin buffer, so lock to
            // make sure there's no writing happening and then copy it to our own
            // local version.
            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingBufferLock.unlock();
            }

            // We need to feed in float values between -1.0f and 1.0f, so divide the
            // signed 16-bit inputs.
            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i] = inputBuffer[i] / 32767.0f;
            }

            // Run the model.
            inferenceInterface.feed(SAMPLE_RATE_NAME, sampleRateList);
            inferenceInterface.feed(INPUT_DATA_NAME, floatInputBuffer, RECORDING_LENGTH, 1);
            inferenceInterface.run(outputScoresNames);
            inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);

            // Use the smoother to figure out if we've had a real recognition event.
            long currentTime = System.currentTimeMillis();
            final RecognizeCommands.RecognitionResult result =
                    recognizeCommands.processLatestResults(outputScores, currentTime);

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // If we do have a new command, highlight the right list entry.
                            Log.i("duypq3", "result=" + result.isNewCommand + "|" + result.foundCommand);
                            if (!result.foundCommand.startsWith("_") && result.isNewCommand) {

                                Log.v("duypq31", "Tim thay ket qua...................");

                                stopRecording();

                                isRecording = true;
                                setEnableVoidButton(false);
                                startVoiceRecorder();

                            }
                        }
                    });
            try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        Log.v("duypq31", "End recognition");
    }


}
