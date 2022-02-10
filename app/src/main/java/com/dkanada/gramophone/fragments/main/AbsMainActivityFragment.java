package com.dkanada.gramophone.fragments.main;

import androidx.fragment.app.Fragment;

import com.dkanada.gramophone.activities.MainActivity;

public abstract class AbsMainActivityFragment extends Fragment {
    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
