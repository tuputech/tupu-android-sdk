package com.tuputech.component.ui.stickers.callback;

import android.widget.SeekBar;

public interface SeekBarChangeListener {
    void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser, int classify);
}
