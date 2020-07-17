package com.zhx.videoeffect.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.zhx.videoeffect.GLVideoRenderer;
import com.zhx.videoeffect.R;
import com.zhx.videoeffect.ScaleRenderer;
import com.zhx.videoeffect.ShakeRenderer;
import com.zhx.videoeffect.SoulOutRenderer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GLViewMediaActivity";
    private GLSurfaceView glSurfaceView;
    private GLVideoRenderer glVideoRenderer;
    private SoulOutRenderer soulOutRenderer;
    private ShakeRenderer shakeRenderer;
    private ScaleRenderer scaleRenderer;
    public static final String videoPath = "/sdcard/DCIM/Camera/VID_20200618_174150.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        glSurfaceView = findViewById(R.id.surface_view);

        glSurfaceView.setEGLContextClientVersion(2);
        setGLVideoRenderer(videoPath);
//        setShakeRenderer(videoPath);
//        setScaleRenderer(videoPath);
    }
    public void setGLVideoRenderer(String videoPath){
        glVideoRenderer = new GLVideoRenderer(this, videoPath);//创建renderer
        glSurfaceView.setRenderer(glVideoRenderer);//设置renderer
    }
    public void setSoulOutRenderer(String videoPath){
        soulOutRenderer = new SoulOutRenderer(this, videoPath);//创建renderer
        glSurfaceView.setRenderer(soulOutRenderer);//设置renderer
    }
    public void setShakeRenderer(String videoPath){
        shakeRenderer = new ShakeRenderer(this, videoPath);//创建renderer
        glSurfaceView.setRenderer(shakeRenderer);//设置renderer
    }
    public void setScaleRenderer(String videoPath){
        scaleRenderer = new ScaleRenderer(this, videoPath);//创建renderer
        glSurfaceView.setRenderer(scaleRenderer);//设置renderer
    }
}