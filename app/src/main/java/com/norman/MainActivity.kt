package com.norman

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rxPermissions = RxPermissions(this);
        val subscribe = rxPermissions
            .requestEach(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE
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

        addButton("...").setOnClickListener {
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
