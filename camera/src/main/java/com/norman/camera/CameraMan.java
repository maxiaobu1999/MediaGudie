package com.norman.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

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



}
