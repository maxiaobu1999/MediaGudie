package com.norman.video;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.alibaba.android.arouter.facade.annotation.Route;

@Route(path = "/video/VideoMainActivity")
public class VideoMainActivity extends AppCompatActivity {
    public static final String TAG = "VideoMainActivity+++:";
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_main);
        mActivity = this;

//        addButton("音频录制 AudioRecord").setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(mActivity, AdRecordActivity.class));
//            }
//        });
//
//        addButton("音频编解码 MediaCodec").setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(mActivity, AdCodecActivity.class));
//            }
//        });
//
//        addButton("音频播放 AudioTrack").setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(mActivity, AudioTrackActivity.class));
//            }
//        });
    }

    private Button addButton(String string) {
        Button button = new Button(mActivity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        params.topMargin = 50;
        button.setLayoutParams(params);
        button.setText(string);
        LinearLayout ll = findViewById(R.id.ll_container);
        ll.addView(button);
        return button;
    }
}
