package viettel.cyberspace.speechrecognize.speedtotextcyberspace.myservice.speech2text;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import service.StreamVoiceGrpc;
import service.TextReply;
import service.VoiceRequest;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.UtilsVoice;
import viettel.cyberspace.speechrecognize.speedtotextcyberspace.audio.Recorder;

public class VoiceClient extends STTService {
    public final String TAG = "VoiceClient";
    private final StreamVoiceGrpc.StreamVoiceStub asyncStub;
    private final ManagedChannel channel;
    private int timeOut;
    private boolean isParse;
    private boolean mIsLienTuc;
    private boolean mIs16kHz;
    private CountDownLatch finishLatch; //nếu lỗi là do cái này

    public VoiceClient(String host, int port, boolean isParse, boolean isLienTuc, boolean is16kHz) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build(), is16kHz);
        this.isParse = isParse;
        this.mIsLienTuc = isLienTuc;
        this.mIs16kHz = is16kHz;
    }

    VoiceClient(ManagedChannel channel, boolean is16kHz) {
        this.channel = channel;
        Metadata header = new Metadata();
        header.put(Metadata.Key.of("channels", Metadata.ASCII_STRING_MARSHALLER), "1");
        header.put(Metadata.Key.of("rate", Metadata.ASCII_STRING_MARSHALLER), is16kHz ? "16000" : "8000");
        //header.put(Metadata.Key.of("rate", Metadata.ASCII_STRING_MARSHALLER), "16000");

        header.put(Metadata.Key.of("format", Metadata.ASCII_STRING_MARSHALLER), "S16LE");
        header.put(Metadata.Key.of("single_sentence", Metadata.ASCII_STRING_MARSHALLER), mIsLienTuc + "");
        asyncStub = MetadataUtils.attachHeaders(StreamVoiceGrpc.newStub(channel), header);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    protected CountDownLatch record() {
        timeOut = UtilsVoice.timeOut / 250;

        recorder = Recorder.getAudioRecorder(mIs16kHz);
        recorder.startRecording();
        finishLatch = new CountDownLatch(1);

        int sizeData = 0;

        if (mIs16kHz) {
            sizeData = 4000;
        } else {
            sizeData = 2000;
        }

        Log.d("duy8k", "record: is16kHz= " + mIs16kHz);
        try {
            //dùng để khởi tạo 1 phiên ASR ở server
            StreamObserver<VoiceRequest> request = asyncStub.sendVoice(new StreamObserverImpl());
            short[] byte_buff_short = new short[sizeData];
            int byteRead = 0;
            while (recorder != null && (byteRead = recorder.read(byte_buff_short, 0, byte_buff_short.length)) != -1 && isRecording) {
                byte[] byte_buff = short2byte(byte_buff_short);
                Log.d(TAG, "record: sending message " + byteRead);
                //fos.write(byte_buff);
                request.onNext(VoiceRequest.newBuilder().setByteBuff(ByteString.copyFrom(byte_buff)).build());
                timeOut--;
                Log.d(TAG, "record: " + timeOut);
                if (timeOut == 0) {
                    stopRecordListener.stop();
                    stopRecognize();
                }
            }
            request.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return finishLatch;
    }

    public CountDownLatch sendFile(String file) {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        try {
            BufferedInputStream bi = new BufferedInputStream(new FileInputStream(file));
            StreamObserver<VoiceRequest> request = asyncStub.sendVoice(new StreamObserverImpl());
            Log.d("duy8k", "sendFile: is16kHz= " + mIs16kHz);
            byte[] byte_buff = null;
            int sizeDataSend = 0;
            if (mIs16kHz) {
                byte_buff = new byte[8000];
                sizeDataSend = 8000;
            } else {
                byte_buff = new byte[4000];
                sizeDataSend = 4000;
            }
            boolean start_sent = false;
            //dùng để khởi tạo 1 phiên ASR ở server
            while (bi.read(byte_buff, 0, byte_buff.length) != -1) {
                request.onNext(VoiceRequest.newBuilder().setByteBuff(ByteString.copyFrom(byte_buff)).build());
                Thread.sleep(250);
            }
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(5000);
                    request.onNext(VoiceRequest.newBuilder().setByteBuff(ByteString.copyFrom(new byte[sizeDataSend])).build());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            request.onCompleted();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return finishLatch;
    }

    //TODO::   class impl text response master server trả về
    public class StreamObserverImpl implements StreamObserver<TextReply> {
        @Override
        public void onNext(TextReply textReply) {
            //nhận được onNext thì tiếp tục chờ
            timeOut = UtilsVoice.timeOut / 250;
            //nhận được tín hiệu kết thúc câu
            if (textReply == null || !isRecording) return;

            changeAdapterListener.change(textReply.getResult().getHypotheses(0).getTranscriptNormed());

            if (textReply.getResult().getFinal()) {
                Log.d(TAG, "onNext: final signal");
                //  stopRecognize();
                changeAdapterListener.finish(textReply.getResult().getHypotheses(0).getTranscriptNormed());

                return;
            }


            Log.d(TAG, "onNext: text = " + textReply.getResult().getHypotheses(0).getTranscriptNormed());

            Log.i("duypq3", "onNext: text = " + textReply.getResult().getHypotheses(0).getTranscriptNormed());
            // changeAdapterListener.change(textReply.getResult().getHypotheses(0).getTranscriptNormed());

        }

        @Override
        public void onError(Throwable throwable) {
            Status status = Status.fromThrowable(throwable);
            Log.d(TAG, "onError: sendvoice fail+ " + status);
            finishLatch.countDown();
        }

        @Override
        public void onCompleted() {
            Log.d(TAG, "onCompleted: finish send voice ");
            finishLatch.countDown();
            stopRecordListener.stop();
        }
    }

    //TODO: convert short arr to byte arr recorded
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            sData[i] = (short) Math.min((int) (sData[i] * 2.0), (int) Short.MAX_VALUE);
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

}
