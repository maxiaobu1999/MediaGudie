package com.norman.audio;

import android.app.Activity;
import android.media.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.io.*;
import java.text.MessageFormat;

/**
 * 音频录制 AudioRecord  音频格式：PCM编码
 * 需要动态权限：Manifest.permission.RECORD_AUDIO
 *
 * 音频播放 AudioTrack
 *
 */
public class AdRecordActivity extends AppCompatActivity {
    public static final String TAG = "AdRecordActivity+++:";
    /** 音频源:使用麦克风作为采集音频的数据源。 */
    private int Audio_Source =MediaRecorder.AudioSource.MIC;
    /**
     * 采样率
     * 一秒钟对声音数据的采样次数，采样率越高，音质越好。
     * 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     * 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
     */
    private int AUDIO_SAMPLE_RATE = 16000;
    /** 音频通道：单声道，双声道等 */
    @SuppressWarnings("deprecation")
    private int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    /** 音频格式:一般选用PCM格式，即原始的音频样本。 */
    private int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 缓冲区大小:音频数据写入缓冲区的总数，可以通过AudioRecord.getMinBufferSize获取最小的缓冲区。
     * （将音频采集到缓冲区中然后再从缓冲区中读取）。
     */
    private int BUFFER_SIZE = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);

    @SuppressWarnings("FieldCanBeLocal")
    private Activity mActivity;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    TextView mTvStatus;

    /** 录制的音频文件路径 */
    private File mTempfile;
    /** 0:IDE  1:正在录制 2:正在播放 */
    private int mCurStatus=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_record);
        mActivity = this;
        mTvStatus = findViewById(R.id.tv_status);

        // /storage/emulated/0/Android/data/com.norman/cache
        File audioDir = mActivity.getExternalCacheDir();
        Log.d(TAG, "audioDir:" + audioDir);
        assert audioDir != null;
        //noinspection ResultOfMethodCallIgnored
        audioDir.mkdirs();
        //创建临时文件,注意这里的格式为.pcm
        try {
            mTempfile = File.createTempFile("tempfile", ".pcm", audioDir);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 开始录制 */
    @SuppressWarnings("RedundantThrows")
    public void onStartClick(View view) throws IOException {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                //开通输出流到指定的文件
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mTempfile)));
                //根据定义好的几个配置，来获取合适的缓冲大小

                //实例化AudioRecord
                AudioRecord record = new AudioRecord(Audio_Source, AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING, BUFFER_SIZE);
                //定义缓冲
                short[] buffer = new short[BUFFER_SIZE];
                //开始录制
                record.startRecording();
                mCurStatus = 1;
                int r = 0; //存储录制进度
                //定义循环，根据mCurStatus的值来判断是否继续录制
                while (mCurStatus == 1) {
                    //从bufferSize中读取字节，返回读取的short个数
                    //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    //循环将buffer中的音频数据写入到OutputStream中
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    r++; //自增进度值
                    emitter.onNext(r); //向UI线程报告当前进度
                }
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mTvStatus.setText(MessageFormat.format("正在录制：process={0}", integer));

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        mCompositeDisposable.add(subscribe);
    }

    /** 停止录制 */
    public void onStopClick(View view) {
        mCurStatus = 0;
    }

    /** 播放 */
    @SuppressWarnings("RedundantThrows")
    public void onPlayClick(View view) throws IOException {
        Disposable subscribe = Observable.just("").subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        int bufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
                        short[] buffer = new short[bufferSize/4];
                        //定义输入流，将音频写入到AudioTrack类中，实现播放
                        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(mTempfile)));
                        //实例AudioTrack
                        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM);
                        //开始播放
                        track.play();
                        mCurStatus = 2;
                        //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                        while(mCurStatus==2 && dis.available()>0){
                            int i = 0;
                            while(dis.available()>0 && i<buffer.length){
                                buffer[i] = dis.readShort();
                                i++;
                            }
                            //然后将数据写入到AudioTrack中
                            track.write(buffer, 0, buffer.length);

                        }
                        //播放结束
                        track.stop();
                        dis.close();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        mCompositeDisposable.add(subscribe);
    }

    /** 停止播放 */
    public void onFinishClick(View view) {
        mCurStatus = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
