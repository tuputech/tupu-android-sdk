package com.tuputech.mvp;

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.squareup.leakcanary.LeakCanary;
import com.tuputech.sdk.face.TPFaceAPI;
import com.tuputech.sdk.gesture.TPGestureDetectAPI;


/**
 * Created by RogerOu on 2018/4/16.
 */
public class MVPApplication extends Application {

    // 图普测试专用 AppKey 和 AppSecret
    private String app_key ="59cb844ecb2c9492c8e03bb517f96ede";
    private String app_secret ="5fc28b2f1df7b6a294e15e1048ebd7a350c3266f";


    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        Fresco.initialize(this);


        TPFaceAPI.init(this, app_key, app_secret, (success, description) -> {
            Log.d("MVPApplication：Face", description);
            TPGestureDetectAPI.init(this, app_key, app_secret, (success1, description1) -> {
                Log.d("MVPApplication：gesture", description1);
            });

        });

    }
}
