package com.norman.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 姓名：马庆龙 on 2019-07-19 16:43
 * 功能：3. 在 Android 平台使用 Camera API 进行视频的采集，
 * 分别使用 SurfaceView、TextureView 来预览 Camera 数据，
 * 取到 NV21 的数据回调
 */
public class CmCaptureActivity extends AppCompatActivity {
    public static final String TAG = "CmCaptureActivity+++:";
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cm_capture);
        mActivity = this;

        final TextureView textureView = new TextureView(mActivity);

        textureView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout flSurfaceHolder = findViewById(R.id.fl_surface_holder);
        flSurfaceHolder.addView(textureView);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable");
                CameraMan cameraMan = new CameraMan();
                int cameraId = cameraMan.getCameraId(0);
                cameraMan.openCamera(cameraId);

                cameraMan.startPreview(mActivity, textureView.getSurfaceTexture());
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });




    }
}
