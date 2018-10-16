package com.google.android.cameraview;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by drakedan on 17/11/2017.
 */

public class GLOESTextureAdapter {
    private final static String TAG = "GLOESTextureAdapter";
    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	textureCoordinate = inputTextureCoordinate.xy;\n" +
            "	gl_Position = position;\n" +
            "}";

    private static final String CAMERA_INPUT_FRAGMENT_SHADER_OES = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };
    public static final float TEXTURE_NO_ROTATION_flip[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    public static final float TEXTURE_ROTATION_270[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATION_270_flip[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    public static final float TEXTURE_ROTATION_90[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    public static final float TEXTURE_ROTATION_90_flip[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATION_180[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATION_180_flip[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    private final static String POSITION_COORDINATE = "position";
    private final static String TEXTURE_COORDINATE = "inputTextureCoordinate";
    private final static String TEXTURE_UNIFORM = "inputImageTexture";

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mTexCoordBuffer;

    private int mViewPortWidth, mViewPortHeight;

    private int mOESAdapterProgramID = -1;
    private int mOESAdapterProgramPositionAttribLoc;
    private int mOESAdapterProgramTexCoordAttribLoc;
    private int mOESAdapterProgramTextureUniform;

    private int[] mFramebuffers;
    private int[] mFramebufferTextures;

    public GLOESTextureAdapter() {
        //allocate memory legnth * 4(bytes per element)
        mVertexBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //fill the memory
        mVertexBuffer.put(CUBE).position(0);

        mTexCoordBuffer = ByteBuffer.allocateDirect(TEXTURE_ROTATION_270.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexCoordBuffer.put(TEXTURE_ROTATION_90_flip).position(0);
    }

    public void init(int width, int height) {
        if (mViewPortWidth == width && mViewPortHeight == height) {
            return;
        }
        initProgram(CAMERA_INPUT_FRAGMENT_SHADER_OES);
//        initProgram(CAMERA_INPUT_FRAGMENT_SHADER, mArrayPrograms.get(1));
        mViewPortWidth = width;
        mViewPortHeight = height;
        initFrameBuffers(width, height);
    }


    public boolean isInitialzed() {
        return mOESAdapterProgramID != -1;
    }

    public void adjustRotation(Rotation rot, boolean mirror) {
        if (!mirror) {
            switch (rot) {
                case ROTATION_90:
                    mTexCoordBuffer.put(TEXTURE_ROTATION_90).position(0);
                    break;
                case ROTATION_180:
                    mTexCoordBuffer.put(TEXTURE_ROTATION_180).position(0);
                    break;
                case ROTATION_270:
                    mTexCoordBuffer.put(TEXTURE_ROTATION_270).position(0);
                    break;
                default:
                    mTexCoordBuffer.put(TEXTURE_NO_ROTATION).position(0);
                    break;
            }
        } else {
            switch (rot) {
                case ROTATION_90:
                    mTexCoordBuffer.put(TEXTURE_ROTATION_90_flip).position(0);
                    break;
                case ROTATION_180:
                    mTexCoordBuffer.put(TEXTURE_ROTATION_180_flip).position(0);
                    break;
                case ROTATION_270:
                    mTexCoordBuffer.put(TEXTURE_ROTATION_270_flip).position(0);
                    break;
                default:
                    mTexCoordBuffer.put(TEXTURE_NO_ROTATION_flip).position(0);
                    break;
            }
        }
    }

    /**
     * 此函数有三个功能
     * 1. 将OES的纹理转换为标准的GL_TEXTURE_2D格式
     * 2. 将纹理宽高对换，即将wxh的纹理转换为了hxw的纹理??????，并且如果是前置摄像头，则需要有水平的翻转
     * 3. 读取上面两个步骤后纹理的内容到cpu内存，存储为RGBA格式的buffer
     *
     * @param textureId 输入的OES的纹理id
     * @param buffer    输出的RGBA的buffer
     * @return 转换后的GL_TEXTURE_2D的纹理id
     */
    public int preProcess(int textureId, ByteBuffer buffer) {
        if (mFramebuffers == null)
            return -2;

        GLES20.glUseProgram(mOESAdapterProgramID);
//        OpenGlUtils.checkGlError("glUseProgram");

        mVertexBuffer.position(0);
        int glAttribPosition = mOESAdapterProgramPositionAttribLoc;
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mTexCoordBuffer.position(0);
        int glAttribTextureCoordinate = mOESAdapterProgramTexCoordAttribLoc;
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mOESAdapterProgramTextureUniform, 0);
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[0]);
//        OpenGlUtils.checkGlError("glBindFramebuffer");
        GLES20.glViewport(0, 0, mViewPortWidth, mViewPortHeight);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if (buffer != null) {
            GLES20.glReadPixels(0, 0, mViewPortWidth, mViewPortHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        }

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);

        return mFramebufferTextures[0];
    }

    public void destroy() {
        destroyFrameBuffers();
        destroyGLProgram();
        mViewPortWidth = mViewPortHeight = 0;
    }

    public void destroyGLProgram() {
        GLES20.glDeleteProgram(mOESAdapterProgramID);
        mOESAdapterProgramID = -1;
    }

    public void destroyFrameBuffers() {
        if (mFramebufferTextures != null) {
            GLES20.glDeleteTextures(1, mFramebufferTextures, 0);
            mFramebufferTextures = null;
        }
        if (mFramebuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFramebuffers, 0);
            mFramebuffers = null;
        }
    }

    private void initFrameBuffers(int width, int height) {
        destroyFrameBuffers();

        if (mFramebuffers == null) {
            mFramebuffers = new int[1];
            mFramebufferTextures = new int[1];

            GLES20.glGenFramebuffers(1, mFramebuffers, 0);
            GLES20.glGenTextures(1, mFramebufferTextures, 0);

            bindFrameBuffer(mFramebufferTextures[0], mFramebuffers[0], width, height);
//            bindFrameBuffer(mFramebufferTextures[1], mFramebuffers[1], width, height);
        }
    }

    private void initProgram(String fragment) {
        mOESAdapterProgramID = OpenGlUtils.loadProgram(CAMERA_INPUT_VERTEX_SHADER, fragment);
        mOESAdapterProgramPositionAttribLoc =
                GLES20.glGetAttribLocation(mOESAdapterProgramID, POSITION_COORDINATE);
        mOESAdapterProgramTexCoordAttribLoc =
                GLES20.glGetAttribLocation(mOESAdapterProgramID, TEXTURE_COORDINATE);
        mOESAdapterProgramTextureUniform =
                GLES20.glGetUniformLocation(mOESAdapterProgramID, TEXTURE_UNIFORM);

    }

    private void bindFrameBuffer(int textureId, int frameBuffer, int width, int height) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}
