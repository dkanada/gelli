package com.dkanada.gramophone.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.login.LoginActivity;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.util.NavigationUtil;

import org.jellyfin.apiclient.interaction.EmptyResponse;

public class ConfirmLogoutDialog extends DialogFragment {
    @NonNull
    public static ConfirmLogoutDialog create() {
        return new ConfirmLogoutDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(requireActivity())
                .title(R.string.logout)
                .content(R.string.confirm_logout)
                .positiveText(R.string.logout)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    App.getApiClient().Logout(new EmptyResponse());
                    MusicPlayerRemote.clearQueue();

                    NavigationUtil.goToLogin(requireContext());
                })
                .build();
    }
}
