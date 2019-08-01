package com.norman.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.FloatBuffer;

/**
 * 绘制三角形
 */
public class TriangleRenderer implements GLSurfaceView.Renderer {
    public static final String TAG = "TriangleRenderer+++:";
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 aPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * aPosition;" +
                    "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform vec4 uColor;" +
                    "void main() {" +
                    "  gl_FragColor = uColor;" +
                    "}";
    // 三角颜色{红，绿，蓝，透明度}
    private static final float[] COLORS = {0.1f, 0.5f, 0.3f, 1.0f};
    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;
    // 坐标：按逆时针顺序:
    private static final float[] COORDS = {
            0, 0.6f, // top
            -0.6f, -0.3f, // bottom left
            0.6f, -0.3f, // bottom right
    };
    private FloatBuffer mVertexBuffer;
    private int mProgram;
    private float[] mMvpMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private int mColorHandle;
    private int mMvpMatrixHandle;
    private int mPositionHandle;

    /** 当界面第一次被创建时调用，如果我们失去界面上下文并且之后由系统重建，也会被调用。 */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        // 设置清空屏幕用到的颜色，参数是 Red、Green、Blue、Alpha，范围 [0, 1]。这里我们使用白色背景。
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        mVertexBuffer = GLESUtils.createFloatBuffer(COORDS);
        int vertexShader = GLESUtils.createVertexShader(VERTEX_SHADER);
        int fragmentShader = GLESUtils.createFragmentShader(FRAGMENT_SHADER);
        mProgram = GLESUtils.createProgram(vertexShader, fragmentShader);
        // get handle to fragment shader's uColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
        // get handle to vertex shader's aPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        // get handle to shape's transformation matrix
        mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    /** 每当界面改变时被调用；例如，从纵屏切换到横屏，在创建界面后也会被调用。 */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged:width="+width+"height="+height);
        // 设置视口的尺寸，告诉 OpenGL 可以用来渲染的 surface 大小。
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2.5f, 6);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMvpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    /** 每当绘制新帧时被调用。 */
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame");
        // 清空屏幕，擦除屏幕上的所有颜色，并用之前 glClearColor 定义的颜色填充整个屏幕。
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 将程序添加到OpenGL ES环境 Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        // Enable a handle to the triangle vertices？
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, COLORS, 0);
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mMvpMatrix, 0);
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, COORDS.length / COORDS_PER_VERTEX);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glUseProgram(0);
    }
}
