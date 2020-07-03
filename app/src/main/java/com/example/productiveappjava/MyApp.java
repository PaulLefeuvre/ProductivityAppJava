package com.example.productiveappjava;

import android.app.AppOpsManager;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class MyApp extends Application {

    private static final String mTAG = "mVariables";

    public MyApp() {
        // this method fires only once per application start.
        // getApplicationContext returns null here

        Log.i("main", "Constructor fired");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("main", "onCreate fired");

        Context context = this;
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();

        prefsEditor.putBoolean("checkPermissions", true);
        prefsEditor.apply();
    }
}
