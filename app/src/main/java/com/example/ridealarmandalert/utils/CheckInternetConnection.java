package com.example.ridealarmandalert.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is responsible for internet status checking
 */
public class CheckInternetConnection {

    /**
     * Check internet status
     *
     * @param context
     * @return: internet connection status
     */
    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
    }
}
