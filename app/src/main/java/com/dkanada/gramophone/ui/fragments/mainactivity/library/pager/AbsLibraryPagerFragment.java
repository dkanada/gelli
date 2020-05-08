package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import com.dkanada.gramophone.ui.fragments.AbsMusicServiceFragment;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.LibraryFragment;

public class AbsLibraryPagerFragment extends AbsMusicServiceFragment {
    public LibraryFragment getLibraryFragment() {
        return (LibraryFragment) getParentFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
