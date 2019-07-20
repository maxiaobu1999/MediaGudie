package com.norman.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class Camera2Manager {
    public static final String TAG = "CameraManager+++:";


//    private CameraManager mCameraManager;
    private CameraDevice mcameraDevice;
    private String mCameraId;

    /**
     * @param whichCamera 开启哪个摄像头 0后置像头 1前置像头
     * @return 摄像头id
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String getCameraId(Context context, int whichCamera) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        final String[] ids = cameraManager.getCameraIdList();
        for (String id : ids) {
            final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
            final int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (orientation == CameraCharacteristics.LENS_FACING_FRONT) {
                //前置摄像头
                if (whichCamera == 1)
                    mCameraId = id;
            } else {
                //后置摄像头
                if (whichCamera == 0)
                    mCameraId = id;
            }
        }
        return mCameraId;
    }


    /**
     * 打开相机
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera(Context context,CameraDevice.StateCallback stateCallback )  {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId, stateCallback, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * 支持Camera2
     * 注：事实上，在各个厂商的的Android设备上，Camera2的各种特性并不都是可用的，需要通过characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)方法
     * 来根据返回值来获取支持的级别，具体说来：
     *
     * INFO_SUPPORTED_HARDWARE_LEVEL_FULL：全方位的硬件支持，允许手动控制全高清的摄像、支持连拍模式以及其他新特性。
     * INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED：有限支持，这个需要单独查询。
     * INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY：所有设备都会支持，也就是和过时的Camera API支持的特性是一致的。
     *
     * 利用这个INFO_SUPPORTED_HARDWARE_LEVEL参数，我们可以来判断是使用Camera还是使用Camera2，具体方法如下：
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean supportCamera2(Context mContext) {
        if (mContext == null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false;
        try {
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            boolean notFull = true;
            if (idList.length == 0) {
                notFull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notFull = false;
                        break;
                    }
                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);

                    final int supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notFull = false;
                        break;
                    }
                }
            }
            return notFull;
        } catch (Throwable ignore) {
            return false;
        }
    }

    /** 关闭相机 */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void closeCamera(CameraDevice cameraDevice) {
        cameraDevice.close();
    }

    public void startPreview(Context context,SurfaceHolder surfaceHolder) {
//        try {
//            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//            Camera.getCameraInfo(mCameraId, cameraInfo);
//            int cameraRotationOffset = cameraInfo.orientation;
//
//            //获取相机参数
//            final Camera.Parameters parameters = mCameraManager.getParameters();
//            //设置对焦模式
////            setAutoFocus(mCameraManager, parameters);
//            //设置闪光模式
////            setFlashMode(mCameraConfigProvider.getFlashMode());
//
////            if (mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
////                    || mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)
////                turnPhotoCameraFeaturesOn(camera, parameters);
////            else if (mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO)
////                turnVideoCameraFeaturesOn(camera, parameters);
//
//            final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
//            int degrees = 0;
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                    degrees = 0;
//                    break; // Natural orientation
//                case Surface.ROTATION_90:
//                    degrees = 90;
//                    break; // Landscape left
//                case Surface.ROTATION_180:
//                    degrees = 180;
//                    break;// Upside down
//                case Surface.ROTATION_270:
//                    degrees = 270;
//                    break;// Landscape right
//            }
//
////            //根据前置与后置摄像头的不同，设置预览方向，否则会发生预览图像倒过来的情况。
////            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
////                displayRotation = (cameraRotationOffset + degrees) % 360;
////                displayRotation = (360 - displayRotation) % 360; // compensate
////            } else {
////                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
////            }
////            mCameraManager.setDisplayOrientation(displayRotation);
//
////            if (Build.VERSION.SDK_INT > 13
////                    && (mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO
////                    || mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)) {
//////                parameters.setRecordingHint(true);
////            }
////
////            if (Build.VERSION.SDK_INT > 14
////                    && parameters.isVideoStabilizationSupported()
////                    && (mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO
////                    || mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)) {
////                parameters.setVideoStabilization(true);
////            }
//
//            //设置预览大小
////            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
////            parameters.setPictureSize(photoSize.getWidth(), photoSize.getHeight());
//            parameters.setPreviewSize(300, 500);
//            parameters.setPictureSize(200, 400);
//
//            //设置相机参数
//            mCameraManager.setParameters(parameters);
//
//
//
//            //设置surfaceHolder
//            mCameraManager.setPreviewDisplay(surfaceHolder);
//            //开启预览
//            mCameraManager.startPreview();
//
//        }
////        catch (IOException error) {
////            Log.d(TAG, "Error setting camera preview: " + error.getMessage());
////        }
//        catch (Exception ignore) {
//            Log.d(TAG, "Error starting camera preview: " + ignore.getMessage());
//        }
    }


}
