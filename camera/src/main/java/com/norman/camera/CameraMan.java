package com.norman.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraMan {
    public static final String TAG = "CameraMan+++:";

    /** 摄像头方向 */
    static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    static final SparseIntArray FRONT_VIDEO_ORIENTATIONS = new SparseIntArray();
    // Camera1前摄像头预览方向，后摄像头预览方向，后置摄像头的录制方向。
    // Camera2的后置摄像头的类型1录制方向
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // Camera2的后置摄像头的类型2的录制方向
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    // Camera1和Camera2的前置摄像头的视频录制方向
    static {
        FRONT_VIDEO_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        FRONT_VIDEO_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        FRONT_VIDEO_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        FRONT_VIDEO_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /** 宽度：保存的图片或视频 */
    public static final int TARGET_WIDTH = 720;
    /** 高度：保存的图片或视频 */
    public static final int TARGET_HEIGHT = 1080;
    /** 宽度：预览 */
    public static final int PREVIEW_WIDTH = 720;
    /** 高度：预览 */
    public static final int PREVIEW_HEIGHT = 1080;

    private Camera mCamera;
//    private int mCameraId;
    /** 是否开启闪光灯 */
    boolean mIsFlashMode;
    private MediaRecorder mMediaRecorder;

    /**
     * @param whichCamera 开启哪个摄像头 0后置像头 1前置像头
     * @return 摄像头id
     */
    public int getCameraId(int whichCamera) {
       int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras > 0) {
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (whichCamera==1 && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                    break;
                } else if (whichCamera==0 && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraId = i;
                    break;
                }
            }
        }
        return cameraId;
    }

    public void configCamera(Context context, SurfaceTexture surfaceTexture) {

    }

    /**
     * 打开相机
     */
    public Camera openCamera(int cameraId)  {
        mCamera = Camera.open(cameraId);
        return mCamera;
    }

    public void startPreview(Context context, SurfaceTexture surfaceTexture) {
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(getCameraId(0), cameraInfo);
        //获取相机参数
        final Camera.Parameters cameraParams = mCamera.getParameters();

        //摄像机图像的方向。 该值是摄像机图像需要顺时针旋转的角度，因此它在显示屏上以其自然方向正确显示。 它应该是0,90,180或270。
        int orientation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "orientation:" + orientation);
        mCamera.setDisplayOrientation(DEFAULT_ORIENTATIONS.get(orientation));

        // 设置连续自动对焦
        List<String> focusModes = cameraParams.getSupportedFocusModes();
        if (focusModes != null) {
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else {
//                mIsContinueAutoFocus = false;
            }
        }
        // 设置闪光模式
        if (isSupportFlashMode(context)) {
            if (mIsFlashMode) {
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }

        //获取支持的预览大小。
        List<Camera.Size> previewSizeList = mCamera.getParameters().getSupportedPreviewSizes();
        Camera.Size similarPreviewSize = getSimilarSize(previewSizeList, TARGET_WIDTH, TARGET_HEIGHT);
        //设置预览大小
        cameraParams.setPreviewSize(similarPreviewSize.width, similarPreviewSize.height);

        //启用和禁用视频稳定功能。
        cameraParams.setVideoStabilization(true);

        //设置相机参数
        mCamera.setParameters(cameraParams);

        //设置surfaceHolder
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开启预览
        mCamera.startPreview();

    }

    public void stopPreview() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void takePicture(final File outputPath) {
        //在拍照的瞬间被回调，这里通常可以播放"咔嚓"这样的拍照音效。
        Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        };

        //返回未经压缩的图像数据。
        Camera.PictureCallback raw = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

            }
        };

        //返回postview类型的图像数据
        Camera.PictureCallback postview = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

            }
        };

        //返回经过JPEG压缩的图像数据。一般用的就是这个最后一个
        Camera.PictureCallback jpeg = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //存储返回的图像数据
                final File pictureFile = outputPath;
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions.");
                    return;
                }
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                } catch (FileNotFoundException error) {
                    Log.e(TAG, "File not found: " + error.getMessage());
                } catch (IOException error) {
                    Log.e(TAG, "Error accessing file: " + error.getMessage());
                } catch (Throwable error) {
                    Log.e(TAG, "Error saving file: " + error.getMessage());
                }
            }
        };

        mCamera.takePicture(shutter,raw,postview,jpeg);
    }


    /**
     * 判断是否支持闪光灯
     *
     * @return
     */
    public boolean isSupportFlashMode(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            List<String> list = mCamera.getParameters().getSupportedFlashModes();
            return list != null && list.contains(Camera.Parameters.FLASH_MODE_TORCH);
        }
        return false;
    }



    /**
     * 根据目标尺寸，从list中返回一个相近的尺寸
     */
    private Camera.Size getSimilarSize(List<Camera.Size> list, int targetWidth, int targetHeight) {
        Camera.Size equalSize = null;
        Camera.Size similarSize = null;

        if (list != null) {
            int targetPixelNum = targetWidth * targetHeight;
            int minDiff = Integer.MAX_VALUE;
            for (Camera.Size size : list) {
                if (size == null) {
                    continue;
                }
//                // 查找和目标完全相同的
//                if (mCallback.getWidth(size) == targetWidth
//                        && mCallback.getHeight(size) == targetHeight) {
//                    equalSize = size;
//                    break;
//                } else {
                    // 查找尺寸相似的
                    int pixelNum = targetWidth * targetHeight;
                    int diff = Math.abs(targetPixelNum - pixelNum);
                    if (diff < minDiff) {
                        similarSize = size;
                        minDiff = diff;
                    }
//                }
            }
        }
        if (equalSize != null) {
            return equalSize;
        } else if (similarSize != null) {
            return similarSize;
        } else {
            return null;
        }
    }


    // TODO: 2019-07-20 配置优化
    public void startRecorder(Context context, File output){
        mMediaRecorder = new MediaRecorder();
//        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            //输出格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //视频帧率
            mMediaRecorder.setVideoFrameRate(30);
            //视频宽高
            mMediaRecorder.setVideoSize(TARGET_WIDTH, TARGET_HEIGHT);
            //视频比特率
            mMediaRecorder.setVideoEncodingBitRate((int) (2.0 * TARGET_WIDTH * TARGET_HEIGHT));
            //视频编码器
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            //音频编码率
            mMediaRecorder.setAudioEncodingBitRate(44100*2);
            //音频声道
            mMediaRecorder.setAudioChannels(2);
            //音频采样率
            mMediaRecorder.setAudioSamplingRate(44100);
            //音频编码器
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            File outputFile = output;
            String outputFilePath = outputFile.toString();
            //输出路径
            mMediaRecorder.setOutputFile(outputFilePath);

//            //设置视频输出的最大尺寸
//            if (mCameraConfigProvider.getVideoFileSize() > 0) {
//                mediaRecorder.setMaxFileSize(mCameraConfigProvider.getVideoFileSize());
//                mediaRecorder.setOnInfoListener(this);
//            }
//
//            //设置视频输出的最大时长
//            if (mCameraConfigProvider.getVideoDuration() > 0) {
//                mediaRecorder.setMaxDuration(mCameraConfigProvider.getVideoDuration());
//                mediaRecorder.setOnInfoListener(this);
//            }
            // 视频方向调整
            int orientation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
//            if (mIsCameraFront) {
//                mediaRecorder.setOrientationHint(FRONT_VIDEO_ORIENTATIONS.get(orientation));
//            } else {
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(orientation));
//            }

            //准备
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        } catch (IllegalStateException error) {
//            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + error.getMessage());
//        } catch (IOException error) {
//            Log.e(TAG, "IOException preparing MediaRecorder: " + error.getMessage());
//        } catch (Throwable error) {
//            Log.e(TAG, "Error during preparing MediaRecorder: " + error.getMessage());
//        }
        //MediaRecorder初始化完成

        mMediaRecorder.start();
//        isVideoRecording = true;
//        uiHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                videoListener.onVideoRecordStarted(videoSize);
//            }
//        });
    }


    public void stopRecoder(){
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        // 释放资源
        mMediaRecorder.release();
        mMediaRecorder = null;
    }
}
