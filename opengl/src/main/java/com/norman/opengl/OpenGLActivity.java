package com.norman.opengl;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class OpenGLActivity extends AppCompatActivity {
    public static final String TAG = "OpenGLActivity+++:";
    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        //检测openGL版本
        int glVersion = GLESUtils.getSupportGLVersion(this);
        String msg = "支持 GLES " + glVersion;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        mGlSurfaceView = findViewById(R.id.gl_surface);
        // 求一个OpenGL ES 2.0兼容的上下文
        mGlSurfaceView.setEGLContextClientVersion(2);
        GLSurfaceView.Renderer renderer = null;
        renderer = new TriangleRenderer();
        //设置渲染器
        mGlSurfaceView.setRenderer(renderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGlSurfaceView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGlSurfaceView.onPause();
    }
}
