package com.norman.audio.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import com.norman.audio.UtilConversion;
import okhttp3.internal.Util;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AudioCodecHelper {
    private static final String TAG = "AudioCodecHelper+++:";
    /** 编码格式 */
    private String encodeType;
    /** 原文件路径 */
    private String mSrcPath;
    /** 编码后新的文件路径 */
    private String mDstPath;
    /** 解码器 */
    private MediaCodec mediaDecode;
    /** 编码器 */
    private MediaCodec mediaEncode;
    /** 提取数据源中的媒体数据，如：分离视频文件的音轨和视频轨道 */
    private MediaExtractor mediaExtractor;
    /** 解码 */
    private ByteBuffer[] decodeInputBuffers;
    private ByteBuffer[] decodeOutputBuffers;
    /** 编码 */
    private ByteBuffer[] encodeInputBuffers;
    private ByteBuffer[] encodeOutputBuffers;
    private MediaCodec.BufferInfo decodeBufferInfo;
    private MediaCodec.BufferInfo encodeBufferInfo;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private ArrayList<byte[]> chunkPCMDataContainer;//PCM数据块容器
//    private OnCompleteListener onCompleteListener;
//    private OnProgressListener onProgressListener;
    private long fileTotalSize;
    private long decodeSize;
    /**
     * 初始化解码器
     */
    private void initMediaDecode() {
        try {
            mediaExtractor=new MediaExtractor();//此类可分离视频文件的音轨和视频轨道
//            new File(mSrcPath).createNewFile();
            mediaExtractor.setDataSource(mSrcPath);//媒体文件的位置
            //遍历媒体轨道 此处我们传入的是音频文件，所以也就只有一条轨道
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {//获取音频轨道
                    mediaExtractor.selectTrack(i);//选择此音频轨道
                    mediaDecode = MediaCodec.createDecoderByType(mime);//创建Decode解码器
                    format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                    mediaDecode.configure(format, null, null, 0);
                    Log.d(TAG, "format:" + format);
                    //format:{max-bitrate 比特率（码率）一个音频流中每秒钟能通过的数据量=320000, sample-rate 采样频率（hz）：采样次数 =44100, track-id=1, file-format=audio/mpeg,
                    // durationUs=251480812持续时间, mime=audio/mpeg, channel-count=2双声道, bitrate比特率=128000, language=, max-input-size=3000}
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mediaDecode == null) {
            Log.e(TAG, "create mediaDecode failed");
            return;
        }
        mediaDecode.start();//启动MediaCodec ，等待传入数据
        decodeInputBuffers=mediaDecode.getInputBuffers();//MediaCodec在此ByteBuffer[]中获取输入数据
        decodeOutputBuffers=mediaDecode.getOutputBuffers();//MediaCodec将解码后的数据放到此ByteBuffer[]中 我们可以直接在这里面得到PCM数据
        decodeBufferInfo=new MediaCodec.BufferInfo();//用于描述解码得到的byte[]数据的相关信息
        Log.d(TAG, "decodeBufferInfo.size:" + decodeBufferInfo.size);
        Log.d(TAG, "decodeBufferInfo.flags:" + decodeBufferInfo.flags);
        Log.d(TAG, "decodeBufferInfo.offset:" + decodeBufferInfo.offset);
        Log.d(TAG, "decodeBufferInfo.presentationTimeUs:" + decodeBufferInfo.presentationTimeUs);

    }



    private boolean codeOver = false;
    /**
     * 音频文件转PCM
     * @param filePath 源文件
     * @param  pcmFilePath 生成pcm存放路径
     */
    public void decodeToPCM(String filePath,String pcmFilePath) throws IOException {
        mSrcPath = filePath;
        mDstPath = pcmFilePath;
        initMediaDecode();

        File pcmFile = new File(pcmFilePath);
        pcmFile.createNewFile();
        final FileOutputStream fileOutputStream = new FileOutputStream(pcmFile);


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (!codeOver) {
                    //1\获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
                    int inputIndex = mediaDecode.dequeueInputBuffer(10000);
//                    int inputIndex = mediaDecode.dequeueInputBuffer(-1);
                    if (inputIndex < 0) {
                        codeOver = true;
                        Log.d(TAG, "解码结束");
                        return;
                    }
                    ByteBuffer dstBuf = decodeInputBuffers[inputIndex];//拿到inputBuffer
                    int sampleSize = mediaExtractor.readSampleData(dstBuf, 0);//向buffer中写数据
                    if (sampleSize < 0) {
                        //小于0 代表所有数据已读取完成
                        codeOver=true;
                        mediaDecode.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                        mediaExtractor.advance();
                    }



                    //取解码后的数据
                    //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
                    //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这 等待
                    int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);
                    ByteBuffer outputBuffer;
                    byte[] chunkPCM;
                    if (outputIndex >= 0) {//每次解码完成的数据不一定能一次吐出 所以用while循环，保证解码器吐出所有数据
                        outputBuffer = decodeOutputBuffers[outputIndex];//拿到用于存放PCM数据的Buffer
                        outputBuffer.position(decodeBufferInfo.offset);
                        outputBuffer.limit(decodeBufferInfo.offset + decodeBufferInfo.size);
                        chunkPCM = new byte[decodeBufferInfo.size];//BufferInfo内定义了此数据块的大小
                        outputBuffer.get(chunkPCM);//将Buffer内的数据取出到字节数组中
                        outputBuffer.clear();//数据取出后一定记得清空此Buffer MediaCodec是循环使用这些Buffer的，不清空下次会得到同样的数据

                        //自己定义的方法，供编码器所在的线程获取数据,下面会贴出代码
                        try {
                            //打印剧耗时
//                            Log.d(TAG, UtilConversion.byte2hex(chunkPCM));
                            fileOutputStream.write(chunkPCM);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mediaDecode.releaseOutputBuffer(outputIndex, false);//此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
                    }
                }
            }
        };

        new Thread(runnable).start();

    }




}
