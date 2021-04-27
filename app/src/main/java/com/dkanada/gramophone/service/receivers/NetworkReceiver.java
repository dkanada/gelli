package com.dkanada.gramophone.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dkanada.gramophone.service.LoginService;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        // network info will be null in airplane mode
        if (netInfo != null && netInfo.isConnected()) {
            context.sendBroadcast(new Intent(LoginService.STATE_ONLINE));
        } else {
            context.sendBroadcast(new Intent(LoginService.STATE_OFFLINE));
        }
    }
}
