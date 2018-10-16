package com.tuputech.mvp.main;

import com.google.android.cameraview.CameraView;


/**
 * Created by RogerOu on 2018/5/24.
 */
public interface MainViewInterface {

    void startCamera();

    void runOnRenderingThread(Runnable runnable);

    @CameraView.DEVICE_TYPE
    int getDeviceType();

    @CameraView.Facing
    int getFacing();
}
