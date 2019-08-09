package com.norman.audio.audiotrack;

import android.media.*;
import android.os.Handler;
import android.util.Log;
import com.norman.audio.Status;

import java.io.*;

public class AudioTrackHelper {
    public static final String TAG = "AudioTrackManager+++:";
    private Status mStatus = Status.IDEL;
    /**
     * 采样率
     * 一秒钟对声音数据的采样次数，采样率越高，音质越好。
     * 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     * 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
     */
    private int AUDIO_SAMPLE_RATE = 44100;
    /** 音频通道：单声道，双声道等 必须对应 */
    private int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    /** 音频格式:一般选用PCM格式，即原始的音频样本。 */
    private int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private Handler mWorkderHandler;
    public AudioTrack mAudioTrack;

    public PipedInputStream mInstream;
    /** 缓冲区字节大小 */
    private int mBufferSize;
    private Thread mThread;

    public void prepare() {
        //一秒钟buffer的大小：根据采样率，采样精度，单双声道来得到frame的大小。
        mBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
        //实例AudioTrack
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE,
                CHANNEL_CONFIG, AUDIO_ENCODING, mBufferSize, AudioTrack.MODE_STREAM);
        //开始播放
        mAudioTrack.play();
        mStatus = Status.READY;

    }

    public void play(final InputStream inputStream) {
        mStatus = Status.PLAYING;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = new byte[mBufferSize];
                    int length;
                    while (mStatus==Status.PLAYING&&(length = inputStream.read(bytes)) != -1) {
                        mAudioTrack.write(bytes, 0, bytes.length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mStatus = Status.IDEL;
            }
        });
        mThread.start();

    }

    MediaExtractor mediaExtractor;

    public void play(String pcmFilePath) {


        mStatus = Status.PLAYING;
        try {
            File pcmFile = new File(pcmFilePath);
            final DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(pcmFile)));
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = new byte[mBufferSize];
                        int length;
                        while (mStatus==Status.PLAYING&&(length = inputStream.read(bytes)) != -1) {
                            mAudioTrack.write(bytes, 0, bytes.length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mStatus = Status.IDEL;
                }
            });
            mThread.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }



    public void stop(){
        mStatus = Status.IDEL;
        mAudioTrack.stop();//停止播放
    }

    public void release() {
        mStatus = Status.IDEL;
        mAudioTrack.stop();//停止播放
        mAudioTrack.release();//释放底层资源。
    }


}
