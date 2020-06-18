package com.zhx.videoeffect;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GLViewMediaActivity";
    private GLSurfaceView glSurfaceView;
    private GLVideoRenderer glVideoRenderer;
    public static final String videoPath = "/sdcard/DCIM/Camera/VID_20200617_193041.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        glSurfaceView = findViewById(R.id.surface_view);

        glSurfaceView.setEGLContextClientVersion(2);
        glVideoRenderer = new GLVideoRenderer(this, videoPath);//创建renderer
        glSurfaceView.setRenderer(glVideoRenderer);//设置renderer

    }
}