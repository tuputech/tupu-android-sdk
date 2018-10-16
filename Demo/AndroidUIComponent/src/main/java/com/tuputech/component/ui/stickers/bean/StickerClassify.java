package com.tuputech.component.ui.stickers.bean;

import com.google.gson.Gson;

import java.util.List;

public class StickerClassify {

    public static final int STICKER_2D = 2;
    public static final int STICKER_3D = 3;
    public static final int STICKER_BEAUTY = 4;
    public static final int STICKER_BIG_EYE = 5;
    public static final int STICKER_SMALL_FACE = 6;


    private String name;
    private String path;

    /**
     * 2d =2
     * 3d =3
     * 大眼=6
     * 瘦脸=5
     * 美颜=4
     */
    private int classify;
    private List<Sticker> stickers;
    private boolean isSelected = false;

    public StickerClassify() {
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public void setStickers(List<Sticker> stickers) {
        this.stickers = stickers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toJSON() {
        final String s = new Gson().toJson(this);
        return s;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getClassify() {
        return classify;
    }

    public void setClassify(int classify) {
        this.classify = classify;
    }

    public class Sticker {
        private String path;
        private String selectedIcon;
        private String unSelectedIcon;
        private String name;
        private boolean isStickerSelected = false;

//        private int type;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            final String s = new Gson().toJson(this);
            return s;
        }

//        public int getType() {
//            return type;
//        }
//
//        public void setType(int type) {
//            this.type = type;
//        }

        public boolean isStickerSelected() {
            return isStickerSelected;
        }

        public void setStickerSelected(boolean stickerSelected) {
            isStickerSelected = stickerSelected;
        }

        public String getSelectedIcon() {
            return selectedIcon;
        }

        public void setSelectedIcon(String selectedIcon) {
            this.selectedIcon = selectedIcon;
        }

        public String getUnSelectedIcon() {
            return unSelectedIcon;
        }

        public void setUnSelectedIcon(String unSelectedIcon) {
            this.unSelectedIcon = unSelectedIcon;
        }
    }
}
