package com.norman.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

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
        final CameraMan cameraMan = new CameraMan();

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable");
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


        findViewById(R.id.ugc_album_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //拍照
                File picParent = mActivity.getExternalFilesDir("pic");
                picParent.mkdirs();
                File pic = new File(picParent, "temp.jpg");
                cameraMan.takePicture(pic);
            }
        });

        findViewById(R.id.record_normal_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始录制视频
                File picParent = mActivity.getExternalFilesDir("video");
                picParent.mkdirs();
                File pic = new File(picParent, "video.mp4");
                cameraMan.startRecorder(mActivity,pic);
            }
        });

        findViewById(R.id.cancel_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止录制视频
                cameraMan.stopRecoder();
            }
        });


    }
}
