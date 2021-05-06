package com.dkanada.gramophone.dialogs.preferences;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.DirectPlayCodecAdapter;
import com.dkanada.gramophone.util.PreferenceUtil;

public class DirectPlayPreferenceDialog extends DialogFragment {
    public static final String TAG = DirectPlayPreferenceDialog.class.getSimpleName();

    public static DirectPlayPreferenceDialog newInstance() {
        return new DirectPlayPreferenceDialog();
    }

    private DirectPlayCodecAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.preference_dialog_direct_play_codecs, null);

        adapter = new DirectPlayCodecAdapter(PreferenceUtil.getInstance(getContext()).getDirectPlayCodecs());

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return new MaterialDialog.Builder(requireActivity())
                .title(R.string.pref_title_direct_play_codecs)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    PreferenceUtil.getInstance(getContext()).setDirectPlayCodecs(adapter.getDirectPlayCodecs());
                    dismiss();
                })
                .build();
    }
}
