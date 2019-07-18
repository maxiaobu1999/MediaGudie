package com.norman

import android.app.Application
import com.xiaobu.runtime.AppRuntime

class App :Application() {
    override fun onCreate() {
        super.onCreate()
        AppRuntime.init(this,BuildConfig.DEBUG)

    }
}