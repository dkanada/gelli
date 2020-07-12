package com.dkanada.gramophone.helper;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkConnectionHelper {
    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
