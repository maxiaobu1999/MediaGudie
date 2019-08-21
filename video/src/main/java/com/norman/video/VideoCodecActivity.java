package com.norman.video;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoCodecActivity extends AppCompatActivity {
    public static final String TAG = "VideoRecodeActivity+++:";
    public String mSrcFilePath= Environment.getExternalStorageDirectory() + "/norman/media/" + "sample.mp4";
    public String mDstFilePath= Environment.getExternalStorageDirectory() + "/norman/temp/" + "temp_video.mp4";


    private Activity mActivity;
    TextView mTvStatus;
    private ExecutorService mExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_codec);
        mActivity = this;
        mTvStatus = findViewById(R.id.tv_status);
        mExecutorService = Executors.newCachedThreadPool();
    }

    /** 分离视频的视频轨，输入视频 input.mp4，输出视频 output_video.mp4 */
    @SuppressWarnings("RedundantThrows")
    public void onExtractVideoClick(View view) throws IOException {
        final VideoDecodeCodec videoDecodeCodec = new VideoDecodeCodec();
        videoDecodeCodec.srcPath = mSrcFilePath;
        final SurfaceView surfaceView = new SurfaceView(mActivity);
        LinearLayout viewById = findViewById(R.id.container);
        viewById.addView(surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                videoDecodeCodec.surfaceView = surfaceView;
                videoDecodeCodec.prepare();


                mExecutorService.execute(videoDecodeCodec.mDecodeTask);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
//                    byte[] byteBuffer = videoDecodeCodec.getByteBuffer();
//                    if (null!=byteBuffer)
//                    Log.d(TAG, UtilConversion.byte2hex(byteBuffer));
                        }
                    }
                };

                mExecutorService.execute(runnable);

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });





    }
}
