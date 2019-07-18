package com.xiaobu.runtime;

import android.app.Application;
import android.content.Context;

public class AppRuntime {
    private static Application sApplication;
    private static boolean sBuildDebug;
    public static void init(Application application,boolean buildDebug){
        sApplication=application;
        sBuildDebug=buildDebug;
    }

    public static Application getApplication(){
        return sApplication;}

    public  static Context getAppContext(){
        return sApplication;
    }
    public static boolean isBuildDebug(){
        return sBuildDebug;}

}
