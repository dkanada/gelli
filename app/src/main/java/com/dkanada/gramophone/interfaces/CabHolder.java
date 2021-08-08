package com.dkanada.gramophone.interfaces;

import androidx.annotation.NonNull;

import com.afollestad.materialcab.MaterialCab;

public interface CabHolder {
    MaterialCab openCab(final int menuRes, final MaterialCab.Callback callback);
}
