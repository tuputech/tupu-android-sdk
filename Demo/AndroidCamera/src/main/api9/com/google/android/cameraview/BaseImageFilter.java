
package com.google.android.cameraview;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public class BaseImageFilter {
    public static final String NO_FILTER_VERTEX_SHADER =
            "attribute vec4 vPosition;\n" +
                    "attribute vec4 inputTextureCoordinate;\n" +
                    "uniform mat4 vMatrix;\n" +
                    " \n" +
                    "varying vec2 textureCoordinate;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = vMatrix * vPosition;\n" +
                    "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                    "}";
    public static final String NO_FILTER_FRAGMENT_SHADER =
            "varying mediump vec2 textureCoordinate;\n" +
                    " \n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "}";

    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private final String mVertexShader;
    private final String mFragmentShader;
    protected int mGLProgId;
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;
    private int mOutputWidth;
    private int mOutputHeight;
    boolean mIsInitialized;
    protected FloatBuffer mCoordinatesBuffer;
    protected FloatBuffer mCubeBuffer;
    //变换矩阵句柄
    protected int mLocationMatrix;
    protected float[] mMatrix = new float[16];
    protected int[] mBufferId;


    public BaseImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }


    public BaseImageFilter(String fragmentShader) {
        this(NO_FILTER_VERTEX_SHADER, fragmentShader);
    }

    public BaseImageFilter(final String vertexShader, final String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        mCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        Matrix.setIdentityM(mMatrix, 0);
        mCubeBuffer.put(CUBE).position(0);
        setRotation(Rotation.NORMAL, false, false);
        mBufferId = new int[2];
    }


    public void onInit() {
        mGLProgId = OpenGlUtils.loadProgram(mVertexShader, mFragmentShader);
        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "vPosition");
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate");
        mLocationMatrix = GLES20.glGetUniformLocation(mGLProgId, "vMatrix");
        onInitialized();
        mIsInitialized = true;
    }


    public void onDraw(int textureId) {
        GLES20.glUseProgram(mGLProgId);
        if (!mIsInitialized) {
            return;
        }
        setExpandData();
        onDrawBuffers();
        onBindTexture(textureId);
        onDrawArrays();
        onUnbind();
    }

    protected void onUnbind() {
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    protected void onDrawBuffers() {
        onGenerateVBO();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId[0]);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId[1]);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                0);
    }

    protected void onGenerateVBO() {
        if (mBufferId[0] == 0 || mBufferId[1] == 0) {
            GLES20.glGenBuffers(2, mBufferId, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mCubeBuffer.capacity() * 4, mCubeBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId[1]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mCoordinatesBuffer.capacity() * 4, mCoordinatesBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }
    }

    protected void onDrawArrays() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onDestroy() {
        mIsInitialized = false;
        GLES20.glDeleteBuffers(mBufferId.length, mBufferId, 0);
        GLES20.glDeleteProgram(mGLProgId);
        if (mCubeBuffer != null) {
            mCubeBuffer.clear();
            mCubeBuffer = null;
        }
        if (mCoordinatesBuffer != null) {
            mCoordinatesBuffer.clear();
            mCoordinatesBuffer = null;
        }
    }


    public void onInitialized() {
    }


    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;

    }


    protected void onBindTexture(int textureId) {
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
    }


//    protected void runPendingOnDrawTasks() {
//        while (!mRunOnDraw.isEmpty()) {
//            mRunOnDraw.removeFirst().run();
//        }
//    }

    public void setExpandData() {
        GLES20.glUniformMatrix4fv(mLocationMatrix, 1, false, getMatrix(), 0);
    }

    public void setMatrix(float[] matrix) {
        this.mMatrix = matrix;
    }

    public float[] getMatrix() {
        return mMatrix;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }

    public int getProgram() {
        return mGLProgId;
    }

    public int getAttribPosition() {
        return mGLAttribPosition;
    }

    public int getAttribTextureCoordinate() {
        return mGLAttribTextureCoordinate;
    }

    public int getUniformTexture() {
        return mGLUniformTexture;
    }


    public void setRotation(Rotation rotation, boolean isFlipHorizontal, boolean isFlipVertical) {
        float[] buffer = TextureRotationUtil.getRotation(rotation, isFlipHorizontal, isFlipVertical);
        ByteBuffer bBuffer = ByteBuffer.allocateDirect(buffer.length * 4).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        mCoordinatesBuffer = fBuffer;
        mCoordinatesBuffer.position(0);
    }

//    protected void runOnDraw(final Runnable runnable) {
//        synchronized (mRunOnDraw) {
//            mRunOnDraw.addLast(runnable);
//        }
//    }

    public void setTextureBuffer(FloatBuffer textureBuffer) {
        mCoordinatesBuffer = textureBuffer;
    }


    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        mCubeBuffer = vertexBuffer;
    }
}
