package com.norman.video;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.media.*;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.*;
import java.nio.ByteBuffer;

public class MuxerActivity extends AppCompatActivity {
    public static final String TAG = "VideoRecodeActivity+++:";
    public String mSrcFilePath= Environment.getExternalStorageDirectory() + "/norman/media/" + "sample.mp4";
    public String mDstFilePath= Environment.getExternalStorageDirectory() + "/norman/temp/" + "temp_video.mp4";


    private Activity mActivity;
    TextView mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muxer);
        mActivity = this;
        mTvStatus = findViewById(R.id.tv_status);
    }

    /** 分离视频的视频轨，输入视频 input.mp4，输出视频 output_video.mp4 */
    @SuppressWarnings("RedundantThrows")
    public void onExtractVideoClick(View view) throws IOException {
        final MediaExtractor mediaExtractor = new MediaExtractor();
        MediaMuxer mediaMuxer = null;
        // 设置视频源
        mediaExtractor.setDataSource(mSrcFilePath);
        // 轨道索引 ID
        int videoIndex = -1;
        // 视频轨道格式信息
        //{track-id=1, file-format=video/mp4, level=256, mime=video/avc, profile=8, language=,
        // csd-1=java.nio.HeapByteBuffer[pos=0 lim=9 cap=9], durationUs=23640000, width=640,
        // rotation-degrees=0, max-input-size=230400, frame-rate=25, height=344,
        // csd-0=java.nio.HeapByteBuffer[pos=0 lim=33 cap=33]}
        MediaFormat mediaFormat = null;
        // 数据源的轨道数（一般有视频，音频，字幕等）
        int trackCount = mediaExtractor.getTrackCount();
        // 循环轨道数，找到我们想要的视频轨
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, format.toString());
            // //找到要分离的视频轨
            if (mimeType.startsWith("video/")) {
                videoIndex = i;
                mediaFormat = format;
                break;
            }
        }
        if (mediaFormat == null) {
            return;
        }

        // 最大缓冲区字节数
        int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
//        // 格式类型
//        String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
//        // 视频的比特率
//        int bitRate = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
//        // 视频宽度
//        int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
//        // 视频高度
//        int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
//        // 内容持续时间（以微妙为单位）
//        long duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
//        // 视频的帧率
//        int frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
//        // 视频内容颜色空间
//        int colorFormat = -1;
//        if (mediaFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
//            mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
//        }
//        // 关键之间的时间间隔
//        int iFrameInterval = -1;
//        if (mediaFormat.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL)) {
//            iFrameInterval = mediaFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
//        }
//        //  视频旋转顺时针角度
//        int rotation = -1;
//        if (mediaFormat.containsKey(MediaFormat.KEY_ROTATION)) {
//            rotation = mediaFormat.getInteger(MediaFormat.KEY_ROTATION);
//        }
//        // 比特率模式
//        int bitRateMode = -1;
//        if (mediaFormat.containsKey(MediaFormat.KEY_BITRATE_MODE)) {
//            bitRateMode = mediaFormat.getInteger(MediaFormat.KEY_BITRATE_MODE);
//        }
        //切换视频的轨道
        mediaExtractor.selectTrack(videoIndex);
        mediaMuxer = new MediaMuxer(mDstFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        //将视频轨添加到 MediaMuxer，并返回新的轨道
        final int trackIndex = mediaMuxer.addTrack(mediaFormat);
        final ByteBuffer byteBuffer = ByteBuffer.allocate(maxInputSize);
        final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        // 开始合成
        mediaMuxer.start();
        final int finalVideoIndex = videoIndex;
        final MediaMuxer finalMediaMuxer = mediaMuxer;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 检索当前编码的样本并将其存储在字节缓冲区中
                    int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                    //  如果没有可获取的样本则退出循环
                    if (readSampleSize < 0) {
                        mediaExtractor.unselectTrack(finalVideoIndex);
                        break;
                    }
                    // 设置样本编码信息
                    bufferInfo.size = readSampleSize;
                    bufferInfo.offset = 0;
                    bufferInfo.flags = mediaExtractor.getSampleFlags();
                    bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                    //写入样本数据
                    finalMediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
                    //推进到下一个样本，类似快进
                    mediaExtractor.advance();
                }
            }
        };

        AsyncTask.execute(runnable);


    }
}
