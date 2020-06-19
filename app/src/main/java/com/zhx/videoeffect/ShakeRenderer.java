package com.zhx.videoeffect;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShakeRenderer implements GLSurfaceView.Renderer
        , SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnVideoSizeChangedListener  {

    private static final String TAG = "SoulOutRenderer";
    private Context context;

    //顶点坐标
    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f,-1f,0f,
            -1f,-1f,0f,
            1f,1f,0f,
            -1f,1f,0f
    };

    //纹理坐标
    private FloatBuffer textureVertexBuffer;
    private final float[] textureVertexData = {
            1f,0f,
            0f,0f,
            1f,1f,
            0f,1f
    };

    //Matrix
    private float[] mMvpMatrix =new float[16];
    private float[] mTexMatrix = new float[16];

    private float mProgress = 0.0f;             //当前动画进度
    private int mFrames = 0;                    //当前地帧数
    private static final int mMaxFrames = 30;   //动画最大帧数
    private static final int mSkipFrames = 10;   //动画完成后跳过的帧数

    //shader uniform location
    private int aPositionLocation;
    private int aTextureCoordLocation;
    private int uMvpMatrixLocation;
    private int uTexMatrixLocation;
    private int uTextureLocation;//fragment
    private int uAlphaLocation;//fragment new for soulout
    private int uTextureCoordOffsetLocation;//fragment new for shake

    private int programID;
    private int textureId;

    private SurfaceTexture surfaceTexture;
    private MediaPlayer mediaPlayer;

    private boolean updateSurface;
    private boolean playerPrepared;

    private int screenWidth,screenHeight;

    public ShakeRenderer(Context context,String videoPath) {
        this.context = context;
        playerPrepared=false;
        synchronized(this) {
            updateSurface = false;
        }
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        //初始化MediaPlayer
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, Uri.parse(videoPath));
        } catch (IOException e){
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //set shader
        String vertexShader = ShaderUtils.readRawTextFile(context, R.raw.shake_vertex_shader);
        String fragmentShader= ShaderUtils.readRawTextFile(context, R.raw.shake_fragment_shader);
        programID = ShaderUtils.createProgram(vertexShader,fragmentShader);
        aPositionLocation = GLES20.glGetAttribLocation(programID,"aPosition");
        aTextureCoordLocation = GLES20.glGetAttribLocation(programID,"aTexCoord");
        uMvpMatrixLocation = GLES20.glGetUniformLocation(programID,"uMvpMatrix");
        uTexMatrixLocation = GLES20.glGetUniformLocation(programID, "uTexMatrix");
        uTextureLocation = GLES20.glGetUniformLocation(programID,"uTexture");
        uAlphaLocation = GLES20.glGetUniformLocation(programID,"uAlpha");
        uTextureCoordOffsetLocation = GLES20.glGetUniformLocation(programID,"uTextureCoordOffset");

        //set texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        ShaderUtils.checkGlError("glBindTexture mTextureID");
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        //set SurfaceTexture 作为MediaPlayer的输出
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);//监听是否有新的一帧数据到来
        Surface surface = new Surface(surfaceTexture);
        mediaPlayer.setSurface(surface);
        surface.release();

        //set MediaPlayer
        if (!playerPrepared){
            try {
                mediaPlayer.prepare();
                playerPrepared = true;
            } catch (IOException t) {
                Log.e(TAG, "media player prepare failed");
            }
            mediaPlayer.start();
            playerPrepared = true;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: "+width+" "+height);
        screenWidth=width; screenHeight=height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        mProgress = (float) mFrames / mMaxFrames;
        if (mProgress > 1f) {
            mProgress = 0f;
        }
        mFrames++;
        if (mFrames > mMaxFrames + mSkipFrames) {
            mFrames = 0;
        }

        GLES20.glUseProgram(programID);

        float scale = 1.0f + 0.2f * mProgress;
        Matrix.setIdentityM(mMvpMatrix, 0);
        //设置放大的百分比
        Matrix.scaleM(mMvpMatrix, 0, scale, scale, 1.0f);
        GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mMvpMatrix, 0);

        //设置色值偏移的量
        float textureCoordOffset = 0.01f * mProgress;
        GLES20.glUniform1f(uTextureCoordOffsetLocation, textureCoordOffset);

//        //底层图层的透明度
//        float backAlpha = 1f;
//        //放大图层的透明度
//        float alpha = 0f;
//        if (mProgress > 0f) {
//            alpha = 0.2f - mProgress * 0.2f;
//            backAlpha = 1 - alpha;
//        }
//        GLES20.glUniform1f(uAlphaLocation, backAlpha);
        GLES20.glUniformMatrix4fv(uTexMatrixLocation, 1, false, mTexMatrix, 0);
        synchronized (this){
            if (updateSurface){
                surfaceTexture.updateTexImage();//获取新数据
                surfaceTexture.getTransformMatrix(mTexMatrix);//让新的纹理和纹理坐标系能够正确的对应,mSTMatrix的定义是和projectionMatrix完全一样的。
                updateSurface = false;
            }
        }
//        GLES20.glUseProgram(programID);
//        GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mMvpMatrix, 0);
//        GLES20.glUniformMatrix4fv(uTexMatrixLocation, 1, false, mTexMatrix, 0);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false,
                12, vertexBuffer);

        textureVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        GLES20.glVertexAttribPointer(aTextureCoordLocation,2,GLES20.GL_FLOAT,false,8,textureVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

//        //绘制底部原图
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        if (mProgress > 0f) {
//            //这里绘制放大图层
//            GLES20.glUniform1f(uAlphaLocation, alpha);
//            float scale = 1.0f + 1f * mProgress;
//            Matrix.scaleM(mMvpMatrix, 0, scale, scale, scale);
//            GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mMvpMatrix, 0);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurface = true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d(TAG, "onVideoSizeChanged: "+width+" "+height);
        updateProjection(width,height);
    }

    private void updateProjection(int videoWidth, int videoHeight){
        float screenRatio=(float)screenWidth/screenHeight;
        float videoRatio=(float)videoWidth/videoHeight;
        if (videoRatio>screenRatio){
            Matrix.orthoM(mMvpMatrix,0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);
        }else Matrix.orthoM(mMvpMatrix,0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}


