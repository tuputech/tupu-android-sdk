package com.tuputech.component.ui.stickers.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tuputech.component.ui.R;
import com.tuputech.component.ui.stickers.bean.StickerClassify;

import java.util.List;

public class StickerSwitchItemAdapter extends RecyclerView.Adapter<StickerSwitchItemAdapter.StickersViewHolder> {

    private List<StickerClassify.Sticker> stickers;

    private onItemClickListener mOnItemClickListener;

    private Context context;
    private int classify;

    public StickerSwitchItemAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<StickerClassify.Sticker> stickers, int classify) {
        this.stickers = stickers;
        this.classify = classify;
    }

    @NonNull
    @Override
    public StickerSwitchItemAdapter.StickersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stickers, parent, false);


        return new StickersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerSwitchItemAdapter.StickersViewHolder holder, int position) {
        String loadIcon;
        if (stickers.get(position).isStickerSelected()) {
            loadIcon = "asset:///" + stickers.get(position).getSelectedIcon();
        } else {
            loadIcon = "asset:///" + stickers.get(position).getUnSelectedIcon();
        }

        holder.mStickerImage.setImageURI(Uri.parse(loadIcon));


        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, stickers, position, classify);
            }
        });
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return stickers == null ? 0 : stickers.size();
    }

    public interface onItemClickListener {
        void onItemClick(View view, List<StickerClassify.Sticker> stickers, int position, int classify);
    }

    public static class StickersViewHolder extends RecyclerView.ViewHolder {


        private SimpleDraweeView mStickerImage;

        public StickersViewHolder(View itemView) {
            super(itemView);
            mStickerImage = itemView.findViewById(R.id.iv_sticker);
        }
    }
}
