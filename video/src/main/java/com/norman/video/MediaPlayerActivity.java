package com.norman.video;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * 姓名：马庆龙 on 2019-08-20 16:55
 * 功能：
 */
public class MediaPlayerActivity extends AppCompatActivity {
    public static final String TAG = "MediaPlayerActivity+++:";
    private Activity mActivity;
//    private String mVideoUrl="https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv";
    private String mVideoUrl="https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4";
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        mActivity = this;

        //初始化mediaPlayer
        mMediaPlayer = new MediaPlayer();
        //设置数据源
        try {
            mMediaPlayer.setDataSource(mVideoUrl);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        //准备
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "准备完成，开始播放");
                //播放
                mMediaPlayer.start();
                setSurfaceView();
            }
        });




        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMedia();
            }
        });
    }

    private void setSurfaceView(){
        //设置surface,可以在播放后再设置
        FrameLayout videoHolder = findViewById(R.id.video_holder);
        final SurfaceView surfaceView = new SurfaceView(mActivity);
        //获取video宽高,设置surface宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                mMediaPlayer.getVideoWidth(),mMediaPlayer.getVideoHeight());
        surfaceView.setLayoutParams(layoutParams);
        videoHolder.addView(surfaceView);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                ////将播放器捕捉的画面展示到SurfaceView画面上
                mMediaPlayer.setDisplay(surfaceHolder);

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    // TODO: 2019-08-21 medie_player 方法补全
    private void initMedia() {
        Log.d(TAG, "mMediaPlayer.getAudioSessionId():" + mMediaPlayer.getAudioSessionId());
        Log.d(TAG, "mMediaPlayer.getCurrentPosition():" + mMediaPlayer.getCurrentPosition());
        Log.d(TAG, "mMediaPlayer.getDuration():" + mMediaPlayer.getDuration());
        Log.d(TAG, "mMediaPlayer.getVideoHeight():" + mMediaPlayer.getVideoHeight());
        Log.d(TAG, "mMediaPlayer.getVideoWidth():" + mMediaPlayer.getVideoWidth());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Log.d(TAG, "mMediaPlayer.getDrmInfo():" + mMediaPlayer.getDrmInfo());
//            Log.d(TAG, "mMediaPlayer.getMetrics():" + mMediaPlayer.getMetrics());
        }



    }
}
