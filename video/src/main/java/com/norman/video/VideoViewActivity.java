package com.norman.video;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
/**
 * 姓名：马庆龙 on 2019-08-20 16:32
 * 功能：VideoView使用
 * VideoView，用于播放一段视频媒体，它继承了SurfaceView，位于"android.widget.VideoView"，是一个视频控件。
 * 接口： MediaController.MediaPlayerControl
 * int getCurrentPosition()：获取当前播放的位置。
 * int getDuration()：获取当前播放视频的总长度。
 * isPlaying()：当前VideoView是否在播放视频。
 * void pause()：暂停
 * void seekTo(int msec)：从第几毫秒开始播放。
 * void resume()：重新播放。
 * void setVideoPath(String path)：以文件路径的方式设置VideoView播放的视频源。
 * void setVideoURI(Uri uri)：以Uri的方式设置VideoView播放的视频源，可以是网络Uri或本地Uri。
 * void start()：开始播放。
 * void stopPlayback()：停止播放。
 * setMediaController(MediaController controller)：设置MediaController控制器。
 * setOnCompletionListener(MediaPlayer.onCompletionListener l)：监听播放完成的事件。
 * setOnErrorListener(MediaPlayer.OnErrorListener l)：监听播放发生错误时候的事件。
 * setOnPreparedListener(MediaPlayer.OnPreparedListener l)：：监听视频装载完成的事件。
 */
public class VideoViewActivity extends AppCompatActivity {
    public static final String TAG = "VideoViewActivity+++:";
    private Activity mActivity;
    private String mVideoUrl="https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv";
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        mActivity = this;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickFab();
            }
        });

        mVideoView = findViewById(R.id.video_view);
        mVideoView.setVideoPath(mVideoUrl);
        mVideoView.start();

    }

    private void clickFab() {
        MediaController.MediaPlayerControl mediaPlayerControl = mVideoView;
        //视频时长
        Log.d(TAG, "mediaPlayerControl.getDuration():" + mediaPlayerControl.getDuration());
        //当前播放时长
        Log.d(TAG, "mediaPlayerControl.getCurrentPosition():" + mediaPlayerControl.getCurrentPosition());
        //正在播放
        Log.d(TAG, "mediaPlayerControl.isPlaying():" + mediaPlayerControl.isPlaying());
        //当前缓冲区百分比
        Log.d(TAG, "mediaPlayerControl.getBufferPercentage():" + mediaPlayerControl.getBufferPercentage());
        //可以停止
        Log.d(TAG, "mediaPlayerControl.canPause():" + mediaPlayerControl.canPause());
        Log.d(TAG, "mediaPlayerControl.canSeekBackward():" + mediaPlayerControl.canSeekBackward());
        Log.d(TAG, "mediaPlayerControl.canSeekForward():" + mediaPlayerControl.canSeekForward());
        //音频会话ID 错误为0
        Log.d(TAG, "mediaPlayerControl.getAudioSessionId():" + mediaPlayerControl.getAudioSessionId());
    }
}
