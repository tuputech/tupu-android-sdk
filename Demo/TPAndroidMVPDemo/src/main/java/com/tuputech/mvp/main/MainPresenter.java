package com.tuputech.mvp.main;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.widget.SeekBar;

import com.google.android.cameraview.CameraView;
import com.google.android.cameraview.GLOESTextureAdapter;
import com.google.android.cameraview.PassThroughFilter;
import com.google.android.cameraview.RendererCallback;
import com.google.android.cameraview.Rotation;
import com.tuputech.authentication.common.FrameDegree;
import com.tuputech.authentication.common.FrameType;
import com.tuputech.common.TupuTech;
import com.tuputech.component.ui.stickers.bean.StickerClassify;
import com.tuputech.component.ui.visitor.Yuv2BitmapConverter;
import com.tuputech.sdk.face.TPFaceAPI;
import com.tuputech.sdk.gesture.TPGestureDetectAPI;
import com.tuputech.tpgraphics.TPGraphicsFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by RogerOu on 2018/5/24.
 */
public class MainPresenter {


    public static final String ACTION_SHOW_UP = "ACTION_SHOW_UP";
    public static final String ACTION_HIDDEN = "ACTION_HIDDEN";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_GESTURE_SHOW = "ACTION_GESTURE_SHOW";
    private final MainViewInterface mMainView;
    private final Yuv2BitmapConverter mYuv2BitmapConverter;
    private int mWidth, mHeight;
    private AtomicBoolean isGestureRunning;
    private CameraView.Callback mCallback;
    private RendererCallback mRendererCallback;
    private GLOESTextureAdapter mOESAdapter;
    private TPGraphicsFilter mTPGraphicsFilter;
    private PassThroughFilter mPassThroughRenderer;
    private int mTextureWidth, mTextureHeight;
    private int mFaceSize;
    private boolean isPlayingFullScreenGestureAnimation;
    private Context mContext;
    private StickerClassify.Sticker mCurrentSticker;
    private int mCurrentStickerClassify;


    public MainPresenter(MainViewInterface mainViewInterface, Context context) {
        mMainView = mainViewInterface;
        mContext = context;
        IntentFilter intentFilter = new IntentFilter(ACTION_SHOW_UP);
        intentFilter.addAction(ACTION_HIDDEN);
        intentFilter.addAction(ACTION_UPDATE);
        intentFilter.addAction(ACTION_GESTURE_SHOW);
        mOESAdapter = new GLOESTextureAdapter();
        mTPGraphicsFilter = new TPGraphicsFilter(mContext);
        mYuv2BitmapConverter = new Yuv2BitmapConverter(mContext);
        isGestureRunning = new AtomicBoolean(false);
        mPassThroughRenderer = new PassThroughFilter();
    }

    private float[][] faceResult2Raw(TupuTech.FaceResult result) {

        if (result.getArrayCount() == 0) {
            return null;
        }

        float[][] ret = new float[result.getArrayCount() * 3][];
        int id = 0;
        for (TupuTech.Face f :
                result.getArrayList()) {
            float[] points = new float[166];
            int i = 0;
            for (TupuTech.Point point : f.getLandmark().getPointsList()) {
                points[i] = point.getX();
                points[i + 1] = point.getY();
                i += 2;
            }

            TupuTech.Rect r = f.getRect();
            float[] box = {
                    r.getLeft(),
                    r.getTop(),
                    r.getRight(),
                    r.getBottom()};
            float[] pose = {
                    f.getLandmark().getPitch(),
                    f.getLandmark().getYaw(),
                    f.getLandmark().getRoll()};

            ret[id] = points;
            ret[id + 1] = box;
            ret[id + 2] = pose;
            id += 3;
        }
        return ret;
    }

    public CameraView.Callback getCameraViewCallback() {

        if (mCallback == null) {
            mCallback = new CameraView.Callback() {
                @Override
                public void onNV21FrameCallback(byte[] data, Camera camera, int width, int height) {
                    if (width != mWidth) {
                        mWidth = width;
                        mHeight = height;

                        mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.scale((float) mHeight / (float) mTextureWidth));
                    }

                    FrameDegree frameDegree = mMainView.getFacing() == CameraView.FACING_FRONT ? FrameDegree.Rotate270 : FrameDegree.Rotate90;

                    boolean mirror = mMainView.getDeviceType() == CameraView.ADVERTISING || mMainView.getFacing() == CameraView.FACING_FRONT;

                    final TupuTech.FaceResult result = TPFaceAPI.onFaceFrame(data, width, height, mirror, frameDegree, FrameType.NV21);

                    float[][] raw = faceResult2Raw(result);
                    if (raw != null) {
                        mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.onNewFacelandmarkResult(raw));
                    } else {
                        mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.onNewFacelandmarkResult(new float[][]{}));
                    }
                    if (!isGestureRunning.get()) {
                        isGestureRunning.getAndSet(true);
                        detectGesture(data, width, height, mirror, frameDegree);
                    }

                }
            };
        }
        return mCallback;

    }

    private float[][] GestureResult2Raw(TupuTech.GestureResult gesture, TupuTech.GestureEntry entry) {
        float[][] ret = new float[gesture.getArrayCount()][];
        int i = 0;
        for (TupuTech.Gesture g : gesture.getArrayList()) {
            TupuTech.Rect r = g.getRect();
            float[] t = {
                    r.getLeft(),
                    r.getTop(),
                    r.getRight(),
                    r.getBottom(),
                    entry.getType().getNumber(),
                    entry.getThreshold()};
            ret[i] = t;
            i++;
        }
        return ret;
    }

    private void detectGesture(byte[] data, int width, int height, boolean mirror, FrameDegree degree) {
        Disposable subscribe = Observable.<Boolean>create(emitter -> {
            Bitmap bitmap = mYuv2BitmapConverter.convertNv21ToRGBA(data, width, height);
            TupuTech.GestureResult gestureResult = TPGestureDetectAPI.onGestureImage(bitmap, mirror, degree);

            if (gestureResult == null || gestureResult.getErrCode() != TupuTech.ErrorCode.Success || gestureResult.getArrayCount() == 0) {
                emitter.onComplete();
                return;
            }


            List<TupuTech.GestureEntry> gestures = new ArrayList<>(gestureResult.getArrayList().get(0).getGestureList());

            Collections.sort(gestures, (a, b) -> Float.compare(b.getThreshold(), a.getThreshold()));

            if (gestures.get(0).getType() == TupuTech.GestureType.Heart) {
                float[][] gesture_raw = GestureResult2Raw(gestureResult, gestures.get(0));
                if (gesture_raw != null && !isPlayingFullScreenGestureAnimation) {
                    isPlayingFullScreenGestureAnimation = true;
                    mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.onNewGestureResult(gesture_raw));
                    gestureAnimation(3000);
                }
                emitter.onNext(true);
            }
            emitter.onComplete();

        }).delay(1, TimeUnit.SECONDS)//delay 1 sec between each gesture detection.subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())//schedule the 'delay' promise on the main thread otherwise the animation won't play
                .subscribe(aBoolean -> {
                        },
                        throwable -> isGestureRunning.getAndSet(false),
                        () -> isGestureRunning.getAndSet(false));
    }


    public void gestureAnimation(long millseconds) {
        mTPGraphicsFilter.load2DStickerAsync("gestures/loveyou", nativeSticker -> mTPGraphicsFilter.render2DSticker(nativeSticker));
        Observable.timer(millseconds, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> switchSticker(mCurrentSticker, mCurrentStickerClassify)).subscribeOn(Schedulers.io()).subscribe();

    }

    public RendererCallback getRenderCallback() {

        if (mRendererCallback == null) {
            mRendererCallback = new RendererCallback() {
                @Override
                public void onGLSurfaceCreate(GL10 unused, EGLConfig config) {

                    // 初始化
                    mTPGraphicsFilter.setupRenderContext();
                    mTPGraphicsFilter.activeRenderContext();
                    mTPGraphicsFilter
                            .startWithTextureInput()
                            .add3DStickerFilter("sticker3D/head.obj")
                            .addCircleFaceIndicator(30, 238, 249)
                            .add2DStickerFilter()
                            .addBeautyFilter()
                            .addDeformFilter("deforms/deform.json") // 大眼和瘦脸配置
                            .endWithTextureOutput();

                    mTPGraphicsFilter.setSmallFaceDegree(0f);
                    mTPGraphicsFilter.setBigEyeDegree(0f);
                    mTPGraphicsFilter.setBeautyDegree(0f);
                }

                @Override
                public void onGLSurfaceChanged(GL10 gl, int width, int height) {
                    mTextureWidth = width;
                    mTextureHeight = height;
                    mOESAdapter.init(width, height);
                    mPassThroughRenderer.onInit();
                }

                @Override
                public void onDrawFrame(GL10 gl, int oesTexture) {


                    // 旋转角度
                    Rotation rot = (mMainView.getFacing() == CameraView.FACING_FRONT) ?
                            Rotation.ROTATION_270 : Rotation.ROTATION_90;

                    // 如果是广告机，旋转角度为90
                    if (mMainView.getDeviceType() == CameraView.ADVERTISING) {
                        rot = Rotation.ROTATION_90;
                    }

                    // 水平翻转(手机的前置相机需要翻转)
                    boolean mirror = mMainView.getDeviceType() != CameraView.ADVERTISING &&
                            mMainView.getFacing() != CameraView.FACING_FRONT;

                    mOESAdapter.adjustRotation(rot, mirror);

                    // * 预处理
                    int normalTexture = mOESAdapter.preProcess(oesTexture, null);

                    // * 把数据喂给滤镜链的第一个成员 textureInput, 滤镜链开始工作
                    mTPGraphicsFilter.processTexture(normalTexture, mTextureWidth, mTextureHeight, 0);
                    // * 获得滤镜链处理后的纹理
                    int textureAfterProcessing = mTPGraphicsFilter.getOutputTexture();
                    // * 切回默认的FrameBuffer
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                    // * 渲染结果
                    mPassThroughRenderer.onDraw(textureAfterProcessing);
                    mTPGraphicsFilter.recycleTexture();
                }
            };


        }
        return mRendererCallback;

    }


    public void switchSticker(StickerClassify.Sticker sticker, int classify) {
        if (mTPGraphicsFilter == null) return;
        this.mCurrentSticker = sticker;
        this.mCurrentStickerClassify = classify;
        isPlayingFullScreenGestureAnimation = false;
        if (classify == StickerClassify.STICKER_2D && sticker.isStickerSelected()) {
            // 设置2D贴纸
            mTPGraphicsFilter.load2DStickerAsync(sticker.getPath(), stickerNativePtr -> mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.render2DSticker(stickerNativePtr)));
        } else if (classify == StickerClassify.STICKER_3D && sticker.isStickerSelected()) {
            // 设置3D贴纸
            mTPGraphicsFilter.load3DStickerAsync(sticker.getPath(), stickerNativePtr -> mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.render3DSticker(stickerNativePtr)));
        } else if (classify == StickerClassify.STICKER_3D && !sticker.isStickerSelected()) {
            // 取消3D贴纸
            mTPGraphicsFilter.render3DSticker(0);
        } else if (classify == StickerClassify.STICKER_2D && !sticker.isStickerSelected()) {
            // 取消2D贴纸
            mTPGraphicsFilter.render2DSticker(0);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser, int classify) {
        if (mTPGraphicsFilter == null) return;
        float ratio = (float) progress * 0.01f;
        if (classify == StickerClassify.STICKER_BEAUTY) {
            // 设置美颜程度
            mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.setBeautyDegree(ratio));
        } else if (classify == StickerClassify.STICKER_BIG_EYE) {
            // 设置大眼程度
            mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.setBigEyeDegree(ratio));
        } else if (classify == StickerClassify.STICKER_SMALL_FACE) {
            // 设置瘦脸程度
            mMainView.runOnRenderingThread(() -> mTPGraphicsFilter.setSmallFaceDegree(ratio));
        }
    }


    public void destroy() {
        mPassThroughRenderer.onDestroy();
        mOESAdapter.destroy();
    }

}
