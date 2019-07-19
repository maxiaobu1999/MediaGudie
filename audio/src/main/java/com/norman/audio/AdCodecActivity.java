package com.norman.audio;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 姓名：马庆龙 on 2019-07-19 11:38
 * 功能：MediaCodec 音频编解码的实现——转码

 */
public class AdCodecActivity extends AppCompatActivity {
    public static final String TAG = "AdCodecActivity+++:";
    private static String MP3_URL = "http://54.183.236.104:8080/audio/jiuguan.mp3";

    String mFilePath ;///storage/emulated/0/Android/data/com.norman/files/audio/song.mp3
    String mMp3FileName = "song.mp3";


    private Activity mActivity;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextView mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_codec);
        mActivity = this;
        mTvStatus = findViewById(R.id.tv_status);

        mFilePath = mActivity.getExternalFilesDir("audio").getAbsolutePath()+"/";
        //noinspection ResultOfMethodCallIgnored
        new File(mFilePath).mkdirs();
        

    }

    /** 下载mp3文件 */
    public void onDownloadClick(View view) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(MP3_URL).build();
                Response response = okHttpClient.newCall(request).execute();
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                is = response.body().byteStream();
                long total = response.body().contentLength();
                File file = new File(mFilePath+mMp3FileName);
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
    /** 解码：mp3转aac */
    public void onMp3ToAacClick(View view) throws IOException {
        final AudioCodec audioCodec=AudioCodec.newInstance();
        audioCodec.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC);
        audioCodec.setIOPath( mFilePath + mMp3FileName,mFilePath + "codec.aac");
        audioCodec.prepare();
        audioCodec.startAsync();
        audioCodec.setOnCompleteListener(new AudioCodec.OnCompleteListener() {
            @Override
            public void completed() {
                audioCodec.release();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
