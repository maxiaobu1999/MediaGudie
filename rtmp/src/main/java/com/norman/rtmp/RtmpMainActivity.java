package com.norman.rtmp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RtmpMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_main);
         NativeUtil nativeUtil = new NativeUtil();
        nativeUtil.dynamicRegister("11111111");

    }
}
