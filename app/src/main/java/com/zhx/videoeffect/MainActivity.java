package com.zhx.videoeffect;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GLViewMediaActivity";
    private GLSurfaceView glView;
    public static final String videoPath = "/sdcard/DCIM/Camera/VID_20200617_193041.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        glView = findViewById(R.id.surface_view);
        glView.setEGLContextClientVersion(2);
        GLVideoRenderer glVideoRenderer = new GLVideoRenderer(this, videoPath);//创建renderer
        glView.setRenderer(glVideoRenderer);//设置renderer

    }
}