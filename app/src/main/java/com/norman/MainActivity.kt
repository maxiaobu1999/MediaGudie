package com.norman

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rxPermissions = RxPermissions(this);
        val subscribe = rxPermissions
            .requestEach(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            .subscribe { // will emit 2 Permission objects
                    permission ->
                if (permission.granted) {
                    // `permission.name` is granted !
                } else if (permission.shouldShowRequestPermissionRationale) {
                    // Denied permission without ask never again
                } else {
                    // Denied permission with ask never again
                    // Need to go to the settings
                }
            }

        addButton("音频相关").setOnClickListener {
            ARouter.getInstance().build("/ad/AdMainActivity").navigation();
        }
        addButton("视频相关").setOnClickListener {
            ARouter.getInstance().build("/video/VideoMainActivity").navigation();
        }

        addButton("相机相关").setOnClickListener {
            ARouter.getInstance().build("/cm/CmMainActivity").navigation();
        }

        addButton("rtmp推流协议").setOnClickListener {
            // 1. 应用内简单的跳转(通过URL跳转在'进阶用法'中)
            ARouter.getInstance().build("/rtmp/RtmpMainActivity").navigation();
        }
        addButton("opengl使用").setOnClickListener {
            // 1. 应用内简单的跳转(通过URL跳转在'进阶用法'中)
            ARouter.getInstance().build("/opengl/OpenglMainActivity").navigation();
        }

    }

    private fun addButton(string: String): Button {
        val button = Button(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
        params.topMargin = 50
        button.layoutParams = params
        button.text = string
        val ll = findViewById<LinearLayout>(R.id.ll_container)
        ll.addView(button)
        return button
    }
}
