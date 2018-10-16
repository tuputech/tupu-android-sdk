package com.tuputech.mvp.main;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.google.android.cameraview.CameraView;
import com.tuputech.component.ui.stickers.StickersSwitchFragment;
import com.tuputech.component.ui.stickers.bean.StickerClassify;
import com.tuputech.component.ui.stickers.callback.SeekBarChangeListener;
import com.tuputech.component.ui.stickers.callback.StickerSwitchListener;
import com.tuputech.mvp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by RogerOu on 2018/5/25.
 */

@RuntimePermissions
public class MainFragment extends android.support.v4.app.Fragment implements MainViewInterface, StickerSwitchListener, SeekBarChangeListener {


    @BindView(R.id.camera)
    CameraView mCamera;
    @BindView(R.id.tv_like)
    AppCompatTextView mTvLike;
    @BindView(R.id.logo)
    AppCompatImageView mLogo;
    MainPresenter mMainPresenter;
    StickersSwitchFragment mStickersSwitchFragment;
    Unbinder unbinder;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, inflate);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainPresenter = new MainPresenter(this, getActivity());
        mStickersSwitchFragment = StickersSwitchFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.stickers_switch_fragment, mStickersSwitchFragment);
        fragmentTransaction.commit();
        mStickersSwitchFragment.setStickerSwitchListener(this);
        mStickersSwitchFragment.setSeekBarChangeListener(this);
        mCamera.setRendererCallback(mMainPresenter.getRenderCallback());
        mCamera.addCallback(mMainPresenter.getCameraViewCallback());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onResume() {
        super.onResume();
        MainFragmentPermissionsDispatcher.startCameraWithPermissionCheck(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMainPresenter.destroy();
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    @Override
    public void startCamera() {
        mCamera.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @Override
    public void runOnRenderingThread(Runnable runnable) {
        mCamera.enqueueRenderingThread(runnable);
    }


    @Override
    public int getDeviceType() {
        return mCamera.getDeviceType();
    }

    @Override
    public int getFacing() {
        return mCamera.getFacing();
    }

    @Override
    public void onStickerSwitchClick(StickerClassify.Sticker sticker, int classify) {
        mMainPresenter.switchSticker(sticker, classify);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser, int classify) {
        mMainPresenter.onProgressChanged(seekBar, progress, fromUser, classify);
    }


}
