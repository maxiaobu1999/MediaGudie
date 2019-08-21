package com.norman.audio.codec;

import android.app.Activity;
import android.media.*;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.norman.audio.*;
import com.norman.audio.audiotrack.AudioTrackHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * 姓名：马庆龙 on 2019-07-19 11:38
 * 功能：MediaCodec 音频编解码的实现——转码

 */
public class AdCodecActivity extends AppCompatActivity {
    public static final String TAG = "AdCodecActivity+++:";
    private static String MP3_URL = "http://54.183.236.104:8080/audio/jiuguan.mp3";

    String mMp3FilePath;

    private String mediaDirPath;//"/storage/emulated/0/Android/data/com.norman/files/media/"


    private Activity mActivity;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextView mTvStatus;
    private String mPcmFilePath;
    private String mAacFilePath;
    private  String mTempDirPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_codec);
        mActivity = this;
        mTvStatus = findViewById(R.id.tv_status);

        mediaDirPath = mActivity.getExternalFilesDir("media").getAbsolutePath() + "/";
        //noinspection ResultOfMethodCallIgnored
        new File(mediaDirPath).mkdirs();
        //"/storage/emulated/0/Android/data/com.norman/files/media/song.mp3"
        mMp3FilePath = Environment.getExternalStorageDirectory() + "/norman/media/" + "sample.mp3";
        mPcmFilePath = Environment.getExternalStorageDirectory() + "/norman/media/" + "sample.pcm";
        mAacFilePath = Environment.getExternalStorageDirectory() + "/norman/media/" + "sample.aac";
        mTempDirPath = Environment.getExternalStorageDirectory() + "/norman/temp/" ;
        new File(mTempDirPath).mkdirs();


    }

    /** 下载mp3文件 */
    public void onDownloadClick(View view) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(MP3_URL).build();
                Response response = okHttpClient.newCall(request).execute();
                InputStream is;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos;
                is = response.body().byteStream();
                long total = response.body().contentLength();
                File file = new File(mMp3FilePath);
                file.createNewFile();
                fos = new FileOutputStream(file);
                long sum = 0;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    // 下载中
                    emitter.onNext(progress);
                }
                fos.flush();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mTvStatus.setText("正在下载mp3：progress="+integer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                    }
                });
        mCompositeDisposable.add(subscribe);
    }


    /** mp3解码 sample.mp3 装成 temp.pcm*/
    public void onMp3ToPCMClick(View view) throws IOException {
        AudioCodecHelper helper = new AudioCodecHelper();
        helper.decodeToPCM(mMp3FilePath,mTempDirPath+"/temp.pcm");
    }

    /** pcm解码成aac */
    public void onPcmToAacClick(View view) throws IOException {
        DeAudioCodec deAudioCodec = new DeAudioCodec();
        deAudioCodec.mSrcFilePath = mMp3FilePath;
        deAudioCodec.prepare();
        deAudioCodec.startDecode();
    }
    /** 编解码：mp3转aac */
    public void onMp3ToAacClick(View view) throws IOException {
        final AudioCodec audioCodec=AudioCodec.newInstance();
        audioCodec.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC);
        audioCodec.setIOPath( mMp3FilePath , mediaDirPath + "song.aac");
        audioCodec.prepare();
        audioCodec.startAsync();
        audioCodec.setOnCompleteListener(new AudioCodec.OnCompleteListener() {
            @Override
            public void completed() {
//                Toast.makeText(mActivity, "转码完成", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "转码完成");
                audioCodec.release();
            }
        });
    }

    /** 编解码：mp3转aac */
    public void onPlayPCMClick(View view) throws IOException {
        play();
    }


    /** 从mp3文件中获取一帧数据 */
    public void onGetOneFrameFormMp3Click(View view) throws IOException {


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }


    private void play() throws IOException {
        AudioTrackHelper mAudioTrackManager = new AudioTrackHelper();
        mAudioTrackManager.prepare();
        String pcmFilePath = Environment.getExternalStorageDirectory() + "/norman/media/"+"media.pcm";
        mAudioTrackManager.play(mTempDirPath+"temp.pcm");

    }
}
