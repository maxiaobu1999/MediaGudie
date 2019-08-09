package com.norman.audio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * 姓名：马庆龙 on 2019-07-29 11:51
 * 功能：使用MediaCodec 解码 音频
 */
public class DeAudioCodec {
    public static final String TAG = "DeAudioCodec+++:";
    /** 源文件路径 */
    public  String mSrcFilePath ;

    /** 初始化成功 */
    private boolean mPrepared;
    /** 解码结束 */
    private boolean mDecodeOver;

    private MediaCodec mMediaCodec;
    /** 输入媒体文件的格式信息 */
    private MediaFormat mMediaFormat;
    /** MediaCodec在此ByteBuffer[]中获取输入数据 */
    private ByteBuffer[] mInputBuffers;
    /** MediaCodec将解码后的数据放到此ByteBuffer[]中 我们可以直接在这里面得到PCM数据 */
    private ByteBuffer[] mOutputBuffers;
    /** 用于描述解码得到的byte[]数据的相关信息 */
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaExtractor mMediaExtractor;

    private OnDecodeListener mOnDecodeListener;
    public interface OnDecodeListener{
       void onDecode(byte[] chunkPCM);
    }

    public void setOnDecodeListener(OnDecodeListener onDecodeListener) {
        mOnDecodeListener = onDecodeListener;
    }


    /**
     * 此类已经过封装
     * 调用prepare方法 会初始化Decode、输入输出流 等一些列操作
     */
    public MediaCodec.BufferInfo prepare() {
        try {
            //此类可分离视频文件的音轨和视频轨道
            mMediaExtractor = new MediaExtractor();
            MediaFormat format;
            mMediaExtractor.setDataSource(mSrcFilePath);//媒体文件的位置
            //遍历媒体轨道 此处我们传入的是音频文件，所以也就只有一条轨道
            for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
                format = mMediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {//获取音频轨道
                    mMediaExtractor.selectTrack(i);//选择此音频轨道
                    //解码器类型，即输入媒体文件的类型。mp3为：audio/mpeg
                    mMediaCodec = MediaCodec.createDecoderByType(mime);//创建Decode解码器
                    mMediaCodec.configure(format, null, null, 0);
                    break;
                }
            }
            mMediaCodec.start();//启动MediaCodec ，等待传入数据
            mInputBuffers =mMediaCodec.getInputBuffers();//MediaCodec在此ByteBuffer[]中获取输入数据
            mOutputBuffers =mMediaCodec.getOutputBuffers();//MediaCodec将解码后的数据放到此ByteBuffer[]中 我们可以直接在这里面得到PCM数据
            mBufferInfo =new MediaCodec.BufferInfo();//用于描述解码得到的byte[]数据的相关信息
            mPrepared = true;
        } catch (Exception e) {
            mPrepared = false;
            e.printStackTrace();
        }
        return mBufferInfo;
    }

    /** 开始解码 */
    public void startDecode() throws IOException {
        if (!mPrepared) {
            Log.d(TAG, "Prepared 未成功");
            return;
        }
        Runnable inputRunnable = new Runnable() {
            @Override
            public void run() {
                while (!mDecodeOver) {
                    for (int i = 0; i < mInputBuffers.length - 1; i++) {
                        int inputIndex = mMediaCodec.dequeueInputBuffer(-1);//获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
                        if (inputIndex < 0) {
                            mDecodeOver = true;
                            return;
                        }

                        ByteBuffer inputBuffer = mInputBuffers[inputIndex];//拿到inputBuffer
                        inputBuffer.clear();//清空之前传入inputBuffeMediaExtractorr内的数据
                        int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);//MediaExtractor读取数据到inputBuffer中
                        if (sampleSize < 0) {//小于0 代表所有数据已读取完成
                            mDecodeOver = true;
                        } else {
                            //通知MediaDecode解码刚刚传入的数据
                            mMediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);
                            mMediaExtractor.advance();//MediaExtractor移动到下一取样处
//                        decodeSize+=sampleSize;
                        }
                    }

                }
            }
        };

        String outFilePath = "/storage/emulated/0/Android/data/com.norman/files/media/song.pcm";
        File file = new File(outFilePath);
        file.createNewFile();
        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        Runnable decodeRunnable = new Runnable() {

            @Override
            public void run() {
                while (!mDecodeOver) {
                    //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
                    //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这 等待
                    int outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
//                    Log.d(TAG, "outputIndex:" + outputIndex);
                    ByteBuffer outputBuffer;
                    byte[] chunkPCM;
                    while (outputIndex >= 0) {//每次解码完成的数据不一定能一次吐出 所以用while循环，保证解码器吐出所有数据
                        outputBuffer = mOutputBuffers[outputIndex];//拿到用于存放PCM数据的Buffer
                        chunkPCM = new byte[mBufferInfo.size];//BufferInfo内定义了此数据块的大小
                        outputBuffer.get(chunkPCM);//将Buffer内的数据取出到字节数组中
                        outputBuffer.clear();//数据取出后一定记得清空此Buffer MediaCodec是循环使用这些Buffer的，不清空下次会得到同样的数据



//                        putPCMData(chunkPCM);//自己定义的方法，供编码器所在的线程获取数据,下面会贴出代码
//                        if (mOnDecodeListener!=null) mOnDecodeListener.onDecode(chunkPCM);
                        try {
                            dos.write(chunkPCM);
                            Log.d(TAG, UtilConversion.byte2hex(chunkPCM));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        mMediaCodec.releaseOutputBuffer(outputIndex, false);//此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
                        outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);//再次获取数据，如果没有数据输出则outputIndex=-1 循环结束
                    }

                }
            }
        };
        new Thread(inputRunnable).start();
        new Thread(decodeRunnable).start();
    }




    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec=null;
        }
    }




}
