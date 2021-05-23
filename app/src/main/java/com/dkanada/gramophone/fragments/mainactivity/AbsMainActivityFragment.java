package com.dkanada.gramophone.fragments.mainactivity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.dkanada.gramophone.activities.MainActivity;

public abstract class AbsMainActivityFragment extends Fragment {
    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
