package com.norman.audio.audiotrack;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.norman.audio.R;

public class AudioTrackActivity extends AppCompatActivity implements View.OnClickListener {
    public static String TAG = "AudioTrackActivity+++:";
    private Activity mActivity;
    private AudioTrackHelper mAudioTrackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track);
        mActivity = this;
        findViewById(R.id.btn_start_play_audio).setOnClickListener(this);
        findViewById(R.id.btn_stop_play_audio).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_play_audio) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    playPCM();
                }
            }).start();
        } else if (v.getId() == R.id.btn_stop_play_audio) {
            try {
                mAudioTrackManager.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void playPCM() {
        mAudioTrackManager = new AudioTrackHelper();
        mAudioTrackManager.prepare();
        String pcmFilePath = Environment.getExternalStorageDirectory() + "/norman/media/"+"sample.pcm";
        mAudioTrackManager.play(pcmFilePath);



    }

}
