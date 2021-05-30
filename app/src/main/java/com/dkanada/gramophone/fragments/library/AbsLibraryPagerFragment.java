package com.dkanada.gramophone.fragments.library;

import android.os.Bundle;

import com.dkanada.gramophone.fragments.AbsMusicServiceFragment;
import com.dkanada.gramophone.fragments.main.LibraryFragment;

public class AbsLibraryPagerFragment extends AbsMusicServiceFragment {
    public LibraryFragment getLibraryFragment() {
        return (LibraryFragment) getParentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
