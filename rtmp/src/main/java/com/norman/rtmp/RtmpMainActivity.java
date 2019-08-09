package com.norman.rtmp;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.norman.rtmp.rtmpclient.RTMPMuxer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

// 在支持路由的页面上添加注解(必选)
// 这里的路径需要注意的是至少需要有两级，/xx/xx
@Route(path = "/rtmp/RtmpMainActivity")
public class RtmpMainActivity extends AppCompatActivity {
    public static final String TAG = "RtmpMainActivity+++:";
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_main);
        mActivity = this;
        Disposable subscribe = Observable.just("").subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        publisher1();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });


//        RtmpClient rtmpClient = new RtmpClient();
//        try {
//            //            public native int open(String url, boolean isPublishMode);
//            rtmpClient.open("rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp", false);
////            public native int read(byte[] data, int offset, int size);
//            byte[] data = new byte[1024];
//            int read = rtmpClient.read(data, 1, 1024);
//            Log.d(TAG, "read:" + read);
//            // TODO: 2019-07-23 播放
//            //            public native int write(byte[] data);
////            public native int seek(int seekTime);
////            public native int pause(int pause);
////            public native int close();
////            public native int isConnected();:Call this function to query the connection status. Returns 1 if connected, returns 0 if not connected
//
//        } catch (RtmpClient.RtmpIOException e) {
//            e.printStackTrace();
//        }


    }
    public void publisher1() throws IOException {
        String outputFilePath = mActivity.getExternalFilesDir("audio").getAbsolutePath() + "/codec.aac";//codec
        String outputUrl = "rtmp://172.24.116.103:1935/rtmplive/room";//推流地址

        RTMPMuxer rtmpMuxer = new RTMPMuxer();
        int open = rtmpMuxer.open(outputUrl, 720, 1080);
        boolean connected = rtmpMuxer.isConnected();
        rtmpMuxer.file_open(outputFilePath);
        rtmpMuxer.write_flv_header(true, false);

        String aacFilePath = Environment.getExternalStorageDirectory() + "/norman/media/song.aac";
        FileInputStream fis = new FileInputStream(new File(aacFilePath));
        BufferedInputStream in= new BufferedInputStream(fis);
        int buf_size = 1024;
        byte[] buffer = new byte[buf_size];
        int len = 0;
        while(-1 != (len = in.read(buffer,0,buf_size))){
            rtmpMuxer.writeAudio(buffer, 0, len, 1000);
        }


    }

    /**
     * 把文件推流到服务器
     * public native int open(String url, int width, int height); : First, call this function with the url you plan to publish. Width and height are the width and height of the video. These two parameters are not mandatory. They are optional. They put width and height values into the metadata tag.
     * public native int writeVideo(byte[] data, int offset, int length, int timestamp);: Write h264 nal units with this function
     * public native int writeAudio(byte[] data, int offset, int length, int timestamp);: Write aac frames with this function
     * public native int close();: Call this function to close the publishing.
     * public native int isConnected();: Call this function to query the connection status. Returns 1 if connected, returns 0 if not connected
     */
    public void publisher() throws IOException {
        String outputUrl = "rtmp://172.24.116.103:1935/rtmplive/room";//推流地址

        String outputFilePath= getExternalFilesDir(null) + "/1564038105289980.flv";//输出文件地址
//        String outputFilePath= getExternalFilesDir(null) + "/test_flv_encode.flv";//输出文件地址
        //open
        RTMPMuxer rtmpMuxer = new RTMPMuxer();
        int open = rtmpMuxer.open(outputUrl, 720, 1080);
        boolean connected = rtmpMuxer.isConnected();
        rtmpMuxer.file_open(outputFilePath);
        rtmpMuxer.write_flv_header(true, true);


        //音频解码
        String mp3FilePath = mActivity.getExternalFilesDir("audio").getAbsolutePath() + "/song.mp3";
        String aacFilePath = mActivity.getExternalFilesDir("audio").getAbsolutePath() + "/codec.aac";//codec
//        FileOutputStream fos = new FileOutputStream(new File(aacFilePath));
//        BufferedOutputStream bos = new BufferedOutputStream(fos, 200 * 1024);
        File file = new File(mp3FilePath);
        long fileTotalSize = file.length();
        ArrayList<byte[]> chunkPCMDataContainer = new ArrayList<>();//PCM数据块容器
        //初始化解码器
        MediaExtractor mediaExtractor = new MediaExtractor();//此类可分离视频文件的音轨和视频轨道
//            new File(srcPath).createNewFile();
        mediaExtractor.setDataSource(mp3FilePath);//媒体文件的位置
        MediaCodec mediaDecode = null;
        //遍历媒体轨道 此处我们传入的是音频文件，所以也就只有一条轨道
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio")) {//获取音频轨道
                mediaExtractor.selectTrack(i);//选择此音频轨道
                mediaDecode = MediaCodec.createDecoderByType(mime);//创建Decode解码器
                mediaDecode.configure(format, null, null, 0);
                break;
            }
        }

        if (mediaDecode == null) {
            Log.e(TAG, "create mediaDecode failed");
            return;
        }
        mediaDecode.start();//启动MediaCodec ，等待传入数据
        ByteBuffer[] decodeInputBuffers = mediaDecode.getInputBuffers();//MediaCodec在此ByteBuffer[]中获取输入数据
        ByteBuffer[] decodeOutputBuffers = mediaDecode.getOutputBuffers();//MediaCodec将解码后的数据放到此ByteBuffer[]中 我们可以直接在这里面得到PCM数据
        MediaCodec.BufferInfo  decodeBufferInfo = new MediaCodec.BufferInfo();//用于描述解码得到的byte[]数据的相关信息

        boolean codeOver = false;
        long decodeSize=0;
        while (!codeOver) {
        for (int i = 0; i < decodeInputBuffers.length-1; i++) {
            int inputIndex = mediaDecode.dequeueInputBuffer(-1);//获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
            if (inputIndex < 0) {
                codeOver =true;
                return;
            }

            ByteBuffer inputBuffer = decodeInputBuffers[inputIndex];//拿到inputBuffer
            inputBuffer.clear();//清空之前传入inputBuffer内的数据
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);//MediaExtractor读取数据到inputBuffer中
            if (sampleSize <0) {//小于0 代表所有数据已读取完成
                codeOver=true;
            }else {
                mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);//通知MediaDecode解码刚刚传入的数据
                mediaExtractor.advance();//MediaExtractor移动到下一取样处
                decodeSize+=sampleSize;
            }
        }

        //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
        //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这 等待
        int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);

//        showLog("decodeOutIndex:" + outputIndex);
        ByteBuffer outputBuffer;
        byte[] chunkPCM=null;
        while (outputIndex >= 0) {//每次解码完成的数据不一定能一次吐出 所以用while循环，保证解码器吐出所有数据
            outputBuffer = decodeOutputBuffers[outputIndex];//拿到用于存放PCM数据的Buffer
            chunkPCM = new byte[decodeBufferInfo.size];//BufferInfo内定义了此数据块的大小
            outputBuffer.get(chunkPCM);//将Buffer内的数据取出到字节数组中
            outputBuffer.clear();//数据取出后一定记得清空此Buffer MediaCodec是循环使用这些Buffer的，不清空下次会得到同样的数据
//            putPCMData(chunkPCM);//自己定义的方法，供编码器所在的线程获取数据,下面会贴出代码
            mediaDecode.releaseOutputBuffer(outputIndex, false);//此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
            outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);//再次获取数据，如果没有数据输出则outputIndex=-1 循环结束
        }

        //     * public native int writeAudio(byte[] data, int offset, int length, int timestamp);: Write aac frames with this function
            if (chunkPCM!=null&&chunkPCM.length>0)
        rtmpMuxer.writeAudio(chunkPCM, 0, chunkPCM.length, 10000000);



        }

    }
}
