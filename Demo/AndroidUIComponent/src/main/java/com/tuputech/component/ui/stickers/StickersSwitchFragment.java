package com.tuputech.component.ui.stickers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuputech.component.ui.R;
import com.tuputech.component.ui.stickers.adapter.StickerSwitchClassifyAdapter;
import com.tuputech.component.ui.stickers.adapter.StickerSwitchItemAdapter;
import com.tuputech.component.ui.stickers.bean.StickerClassify;
import com.tuputech.component.ui.stickers.callback.SeekBarChangeListener;
import com.tuputech.component.ui.stickers.callback.StickerSwitchListener;
import com.tuputech.component.ui.stickers.util.StringUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class StickersSwitchFragment extends Fragment implements StickerSwitchClassifyAdapter.OnClassifyClickListener, StickerSwitchItemAdapter.onItemClickListener, SeekBar.OnSeekBarChangeListener {
    RecyclerView mRyStickerSwitchClassify;

    RecyclerView mRyStickerSwitchList;

    AppCompatSeekBar sbRatio;


    private StickerSwitchClassifyAdapter stickerSwitchClassifyAdapter;
    private StickerSwitchItemAdapter stickerSwitchItemAdapter;
    private StickerSwitchListener stickerSwitchListener;
    private SeekBarChangeListener seekBarChangeListener;
    private ArrayList<StickerClassify> stickerClassifies;
    private int classify;
    private int beautyLevel;
    private int bigEyeLevel;
    private int smallFaceLevel;

    public static StickersSwitchFragment newInstance() {
        return new StickersSwitchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        initMenuData();

        View view = inflater.inflate(R.layout.fragment_stickers_switch, container, false);
        mRyStickerSwitchClassify = view.findViewById(R.id.ry_stickers_classify);
        mRyStickerSwitchList = view.findViewById(R.id.ry_stickers_list);
        sbRatio = view.findViewById(R.id.sb_ratio);
        stickerSwitchClassifyAdapter = new StickerSwitchClassifyAdapter();
        stickerSwitchClassifyAdapter.setData(stickerClassifies);
        mRyStickerSwitchClassify.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        mRyStickerSwitchClassify.setItemAnimator(new DefaultItemAnimator());
        mRyStickerSwitchClassify.setAdapter(stickerSwitchClassifyAdapter);
        stickerSwitchClassifyAdapter.setOnItemClickListener(this);

        stickerSwitchItemAdapter = new StickerSwitchItemAdapter(getActivity());
        stickerSwitchItemAdapter.setData(new ArrayList<>(), -1);
        mRyStickerSwitchList.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        mRyStickerSwitchList.setItemAnimator(new DefaultItemAnimator());
        mRyStickerSwitchList.setAdapter(stickerSwitchItemAdapter);
        stickerSwitchItemAdapter.setOnItemClickListener(this);
        sbRatio.setOnSeekBarChangeListener(this);

        return view;
    }

    private void initMenuData() {
        stickerClassifies = new ArrayList<>();
        String str = StringUtil.getString(getActivity(), "sticker.json");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<StickerClassify>>() {}.getType();
        stickerClassifies = gson.fromJson(str, type);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setStickerSwitchListener(StickerSwitchListener stickerSwitchListener) {
        this.stickerSwitchListener = stickerSwitchListener;

    }

    public void setSeekBarChangeListener(SeekBarChangeListener seekBarChangeListener) {
        this.seekBarChangeListener = seekBarChangeListener;
    }

    @Override
    public void onClassifyClick(View view, List<StickerClassify> stickerClassifys, int position) {
        for (int i = 0; i < stickerClassifys.size(); i++) {
            if (i == position) {
                boolean isChecked = !stickerClassifys.get(i).isSelected();
                stickerClassifys.get(i).setSelected(isChecked);
                if (isChecked) {
                    mRyStickerSwitchList.setVisibility(View.VISIBLE);
                } else {
                    mRyStickerSwitchList.setVisibility(View.GONE);
                }
                this.classify = stickerClassifys.get(i).getClassify();
                if (stickerClassifys.get(i).getClassify() == StickerClassify.STICKER_BEAUTY && isChecked) {
                    sbRatio.setVisibility(View.VISIBLE);
                    sbRatio.setProgress(beautyLevel);
                } else if (stickerClassifys.get(i).getClassify() == StickerClassify.STICKER_BIG_EYE && isChecked) {
                    sbRatio.setVisibility(View.VISIBLE);
                    sbRatio.setProgress(bigEyeLevel);
                } else if (stickerClassifys.get(i).getClassify() == StickerClassify.STICKER_SMALL_FACE && isChecked) {
                    sbRatio.setVisibility(View.VISIBLE);
                    sbRatio.setProgress(smallFaceLevel);
                } else {
                    sbRatio.setVisibility(View.GONE);
                }
                stickerSwitchItemAdapter.setData(stickerClassifys.get(i).getStickers(), stickerClassifys.get(i).getClassify());
            } else {
                stickerClassifys.get(i).setSelected(false);
            }
        }
        stickerSwitchClassifyAdapter.notifyDataSetChanged();
        stickerSwitchItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, List<StickerClassify.Sticker> stickers, int position, int classify) {
        for (int i = 0; i < stickers.size(); i++) {
            if (i == position) {
                stickers.get(i).setStickerSelected(!stickers.get(i).isStickerSelected());
            } else {
                stickers.get(i).setStickerSelected(false);
            }
        }
        stickerSwitchListener.onStickerSwitchClick(stickers.get(position), classify);
        stickerSwitchItemAdapter.notifyDataSetChanged();
        stickerSwitchClassifyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (classify == StickerClassify.STICKER_BEAUTY) {
            beautyLevel = progress;
        } else if (classify == StickerClassify.STICKER_SMALL_FACE) {
            smallFaceLevel = progress;
        } else if (classify == StickerClassify.STICKER_BIG_EYE) {
            bigEyeLevel = progress;
        }
        seekBarChangeListener.onProgressChanged(seekBar, progress, fromUser, classify);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
