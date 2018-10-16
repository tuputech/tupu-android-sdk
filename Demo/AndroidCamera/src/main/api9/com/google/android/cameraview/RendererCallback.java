package com.google.android.cameraview;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Roger on 2017/11/7.
 */

public interface RendererCallback {


    void onGLSurfaceCreate(GL10 unused, EGLConfig config);

    void onGLSurfaceChanged(GL10 gl, final int width, final int height);

    void onDrawFrame(GL10 gl, int oesTexture);
}
