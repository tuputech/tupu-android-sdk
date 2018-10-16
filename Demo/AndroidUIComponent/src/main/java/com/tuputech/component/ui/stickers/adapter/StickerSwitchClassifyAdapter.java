package com.tuputech.component.ui.stickers.adapter;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tuputech.component.ui.R;
import com.tuputech.component.ui.stickers.bean.StickerClassify;

import java.util.ArrayList;
import java.util.List;

public class StickerSwitchClassifyAdapter extends RecyclerView.Adapter<StickerSwitchClassifyAdapter.StickersViewHolder> {

    private ArrayList<StickerClassify> stickerClassifies;

    private OnClassifyClickListener mOnClassifyClickListener;


    public StickerSwitchClassifyAdapter() {

    }

    public void setData(ArrayList<StickerClassify> stickerClassifies) {
        this.stickerClassifies = stickerClassifies;
    }

    @NonNull
    @Override
    public StickerSwitchClassifyAdapter.StickersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_classify, parent, false);


        return new StickersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerSwitchClassifyAdapter.StickersViewHolder holder, int position) {
        holder.mStickerName.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.btn_sticker_menu));
        holder.mStickerName.setSelected(stickerClassifies.get(position).isSelected());
        holder.mStickerName.setText(stickerClassifies.get(position).getName());
        holder.itemView.setOnClickListener(v -> {
            if (mOnClassifyClickListener != null) {
                mOnClassifyClickListener.onClassifyClick(v, stickerClassifies, position);
            }
        });
    }

    public void setOnItemClickListener(OnClassifyClickListener onClassifyClick) {
        this.mOnClassifyClickListener = onClassifyClick;
    }

    @Override
    public int getItemCount() {
        return stickerClassifies.size();
    }

    public interface OnClassifyClickListener {
        void onClassifyClick(View view, List<StickerClassify> stickerClassifys, int position);
    }

    public static class StickersViewHolder extends RecyclerView.ViewHolder {


        private AppCompatTextView mStickerName;

        public StickersViewHolder(View itemView) {
            super(itemView);
            mStickerName = itemView.findViewById(R.id.tv_sticker_classify);
        }
    }
}
