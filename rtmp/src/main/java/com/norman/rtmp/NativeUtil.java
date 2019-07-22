package com.norman.rtmp;
public class NativeUtil {

    static {
        // 加载 JNI 库
        System.loadLibrary("rtmp-jni");
    }

    //动态注册native   方法
    public  native void dynamicRegister(String s);

}
