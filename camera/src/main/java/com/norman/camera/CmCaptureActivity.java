package com.norman.camera;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CmCaptureActivity extends AppCompatActivity {
    public static final String TAG = "CmCaptureActivity+++:";
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cm_capture);
        mActivity = this;

    }
}
