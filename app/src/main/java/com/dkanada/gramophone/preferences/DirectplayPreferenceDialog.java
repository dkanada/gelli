package com.dkanada.gramophone.preferences;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.DirectplayCodecAdapter;
import com.dkanada.gramophone.util.PreferenceUtil;

public class DirectplayPreferenceDialog extends DialogFragment {
    public static DirectplayPreferenceDialog newInstance() {
        return new DirectplayPreferenceDialog();
    }

    private DirectplayCodecAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.preference_dialog_directplay_codecs, null);

        adapter = new DirectplayCodecAdapter(PreferenceUtil.getInstance(getContext()).getDirectplayCodecs());

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return new MaterialDialog.Builder(getContext())
                .title(R.string.directplay_codecs)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    PreferenceUtil.getInstance(getContext()).setDirectplayCodecs(adapter.getDirectplayCodecs());
                    dismiss();
                })
                .build();
    }
}
