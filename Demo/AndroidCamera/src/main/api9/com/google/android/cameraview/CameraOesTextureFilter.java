package com.google.android.cameraview;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * Created by Roger on 2017/11/7.
 */

public class CameraOesTextureFilter extends BaseImageFilter {


    private final static String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "varying mediump vec2 textureCoordinate;\n" +
                    " \n" +
                    "uniform samplerExternalOES inputImageTexture;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "}";


    public CameraOesTextureFilter() {
        super(FRAGMENT_SHADER);
    }


    @Override
    protected void onBindTexture(int textureId) {
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
    }
}
