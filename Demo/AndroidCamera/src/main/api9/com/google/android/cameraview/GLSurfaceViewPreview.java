package com.google.android.cameraview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Roger on 2017/11/7.
 */

public class GLSurfaceViewPreview extends PreviewImpl implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private final GLSurfaceView mGLSurfaceView;

    private SurfaceTexture mSurfaceTexture;
    private RendererCallback mRendererCallback;
    private CameraOesTextureFilter mCameraOesTextureFilter;
    private int mExternalTextureId;
    @CameraView.DEVICE_TYPE
    private int mType;
    @CameraView.ORIENTATION
    private int mOrientation;

    public GLSurfaceViewPreview(Context context, ViewGroup viewGroup, @CameraView.DEVICE_TYPE int device_type, @CameraView.ORIENTATION int orientation) {
        View inflate = View.inflate(context, R.layout.gl_surface_view, viewGroup);
        mGLSurfaceView = inflate.findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mCameraOesTextureFilter = new CameraOesTextureFilter();
        mType = device_type;
        mOrientation = orientation;
    }

    @Override
    Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    @Override
    View getView() {
        return mGLSurfaceView;
    }

    @Override
    Class getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
//        switch (displayOrientation) {
//            case 0:
//                mGLRenderer.setRotation(Rotation.NORMAL);
//                break;
//            case 90:
//                mGLRenderer.setRotation(Rotation.ROTATION_90);
//                break;
//            case 180:
//                mGLRenderer.setRotation(Rotation.ROTATION_270);
//                break;
//            case 270:
//                mGLRenderer.setRotation(Rotation.ROTATION_270);
//                break;
//            default:
//                break;
//        }
    }

    @Override
    public void enqueueRenderingThread(Runnable r) {
        this.mGLSurfaceView.queueEvent(r);
    }

    @Override
    public void setRendererCallback(RendererCallback rendererCallback) {
        mRendererCallback = rendererCallback;
    }

    @Override
    boolean isReady() {
        return mSurfaceTexture != null;
    }


    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }


    @Override
    void onFacingChanged(int facing) {
        Matrix.setIdentityM(mCameraOesTextureFilter.getMatrix(), 0);
        switch (mType) {
            case CameraView.ADVERTISING:
                if (facing == CameraView.FACING_FRONT) {
                    Matrix.rotateM(mCameraOesTextureFilter.getMatrix(), 0, mOrientation == CameraView.LANDSCAPE ? 0 : 270, 0, 0, 1);
                    Matrix.scaleM(mCameraOesTextureFilter.getMatrix(), 0, -1, 1, 1);
                } else {
                    Matrix.rotateM(mCameraOesTextureFilter.getMatrix(), 0, mOrientation == CameraView.LANDSCAPE ? 0 : 270, 0, 0, 1);
//                    Matrix.scaleM(mCameraOesTextureFilter.getMatrix(), 0, -1, 1, 1);
                }
                break;
            case CameraView.PHONE:
                if (facing == CameraView.FACING_FRONT) {
                    Matrix.rotateM(mCameraOesTextureFilter.getMatrix(), 0, mOrientation == CameraView.LANDSCAPE ? 0 : 90, 0, 0, 1);
                } else {
                    Matrix.rotateM(mCameraOesTextureFilter.getMatrix(), 0, 270, 0, 0, 1);
                    Matrix.scaleM(mCameraOesTextureFilter.getMatrix(), 0, -1, 1, 1);
                }
                break;
        }


    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        GLES20.glClearColor(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 0);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mExternalTextureId = createExternalOesTexture();
        mSurfaceTexture = new SurfaceTexture(mExternalTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        setSize(640, 480);
        mCameraOesTextureFilter.onInit();
        dispatchSurfaceChanged();
        if (mRendererCallback != null) {
            mRendererCallback.onGLSurfaceCreate(gl, config);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        mCameraOesTextureFilter.onOutputSizeChanged(width, height);
        if (mRendererCallback != null) {
            mRendererCallback.onGLSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        mCameraOesTextureFilter.onDraw(mExternalTextureId);
        if (mRendererCallback != null) {
            mRendererCallback.onDrawFrame(gl, mExternalTextureId);
        }
    }


    private int createExternalOesTexture() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);


        mExternalTextureId = texture[0];
        return mExternalTextureId;
    }


}
