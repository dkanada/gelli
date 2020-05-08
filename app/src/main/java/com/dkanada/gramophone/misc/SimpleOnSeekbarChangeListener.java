package com.dkanada.gramophone.misc;

import android.widget.SeekBar;

public abstract class SimpleOnSeekbarChangeListener implements SeekBar.OnSeekBarChangeListener {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
