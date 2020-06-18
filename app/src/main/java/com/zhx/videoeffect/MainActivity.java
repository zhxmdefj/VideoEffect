package com.zhx.videoeffect;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GLViewMediaActivity";
    private GLSurfaceView glSurfaceView;
    private GLVideoRenderer glVideoRenderer;
    private SoulOutRenderer soulOutRenderer;
    public static final String videoPath = "/sdcard/DCIM/Camera/VID_20200618_174150.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        glSurfaceView = findViewById(R.id.surface_view);

        glSurfaceView.setEGLContextClientVersion(2);
        soulOutRenderer = new SoulOutRenderer(this, videoPath);//创建renderer
        glSurfaceView.setRenderer(soulOutRenderer);//设置renderer

    }
}