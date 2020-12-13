package com.dkanada.gramophone.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dkanada.gramophone.interfaces.MusicServiceEventListener;
import com.dkanada.gramophone.activities.base.AbsMusicServiceActivity;

public class AbsMusicServiceFragment extends Fragment implements MusicServiceEventListener {
    private AbsMusicServiceActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    @Override
    public void onQueueChanged() {
    }

    @Override
    public void onPlayMetadataChanged() {
    }

    @Override
    public void onPlayStateChanged() {
    }

    @Override
    public void onRepeatModeChanged() {
    }

    @Override
    public void onShuffleModeChanged() {
    }
}
