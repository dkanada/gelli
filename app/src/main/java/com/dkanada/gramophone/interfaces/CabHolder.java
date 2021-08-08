package com.dkanada.gramophone.interfaces;

import android.view.MenuItem;

import com.afollestad.materialcab.attached.AttachedCab;

public interface CabHolder {
    default void onCreateCab(AttachedCab cab) {}

    default void onSelectionCab(MenuItem item) {}

    default void onDestroyCab(AttachedCab cab) {}
}
