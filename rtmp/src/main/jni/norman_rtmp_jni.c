//
// Created by v_maqinglong on 2019-07-22.
//

#include <jni.h>

#include <string.h>
#include "util/log.h"
#include <assert.h>



//动态注册对应的native方法
JNIEXPORT void JNICALL Jni_dynamicRegister (JNIEnv *env, jobject thiz,jstring j_str) {
   const char *c_str = NULL;
   c_str = (*env)->GetStringUTFChars(env, j_str, NULL);
   LOGD( "Jni_dynamicRegister方法执行，参数：j_str=%s", c_str);
}
//当Android的VM(Virtual Machine)执行到C组件(即*so档)里的System.loadLibrary()函数时，首先会去执行C组件里的JNI_OnLoad()函数。
//JavaVM *vm  JavaVM是虚拟机在JNI中的表示，一个JVM中只有一个JavaVM对象，这个对象是线程共享的
//void *reserved:??保留的
//return：告诉VM此C组件使用那一个JNI版本。
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved){
    LOGD( "JNI_OnLoad执行开始");
    //JNIEnv类型是一个指向全部JNI方法的指针。该指针只在创建它的线程有效，不能跨线程传递
    //使用GetEnv 获取 JNIEnv
    JNIEnv* env = NULL;
    //GetEnv()返回当前线程所在的JNIEnv*
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);

    jclass clazz = (*env)->FindClass(env,"com/norman/rtmp/NativeUtil");
    //Java和JNI函数的绑定表:二维数组，代表着这个class里的每一个native方法所对应的实现的方法
    JNINativeMethod gMethods[] = {
        {"dynamicRegister", "(Ljava/lang/String;)V", (void*)Jni_dynamicRegister}
        };
    //参数size代表要指定的native的数量
    int size = ((int) (sizeof(gMethods) / sizeof(gMethods[0])));
    //注册native方法
    (*env)->RegisterNatives(env,clazz, gMethods, size);
    LOGD( "JNI_OnLoad执行结束");
  return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    LOGD( "JNI_OnUnload 执行");

}

