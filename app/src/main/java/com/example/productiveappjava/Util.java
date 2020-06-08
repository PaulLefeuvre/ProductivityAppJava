package com.example.productiveappjava;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class Util {
    private static Intent serviceIntent = null;

    private void setServiceIntent(Context context) {
        if (serviceIntent == null) {
            serviceIntent = new Intent(context, Service.class);
        }
    }

    public void launchService(Context context) {
        if (context == null) {
            return;
        }
        setServiceIntent(context);
        // depending on the version of Android we either launch the simple service (version<O)
        // or we start a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
