package com.example.productiveappjava;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M) // Requires an API of 23 or higher

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Log.i("BlockButton", "SharedPreference change detected");
            Log.i("BlockButton", key);
            if (key.equals("blockEnable")) {
                Log.i("BlockButton", "'blockActivate' SharedPreference has been changed.");
                ToggleButton blockButton = (ToggleButton) findViewById(R.id.blockActivate);
                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("blockEnable", false)) {
                    Log.i("BlockButton", "Setting the block button to true");
                    blockButton.setChecked(true);
                } else {
                    Log.i("BlockButton", "Setting the block button to false");
                    blockButton.setChecked(false);
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Change the Action Bar title
        ActionBar actionBar = getSupportActionBar(); // declare the actionBar
        actionBar.setTitle("Home Page"); // set the top title

        // Create the SharedPreferences editor and reader
        Context context = this;
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor prefsEditor = sharedPref.edit();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        if(sharedPref.getBoolean("checkPermissions", true)) {
            prefsEditor.putBoolean(getString(R.string.check_permission_key), false);
            prefsEditor.apply();
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
            if (mode != AppOpsManager.MODE_ALLOWED) { // Ask for setting permission if not already granted (Improve message later)
                confirmSettingsDialog("You have not granted permission for us to access your usage stats, please grant this.",
                        new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        false);
            }
            if(!Settings.canDrawOverlays(this)){
                confirmSettingsDialog("You have not granted permission for us to draw over other apps, please grant this.",
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                        true);
            }
        }

        final ToggleButton blockButton = (ToggleButton) findViewById(R.id.blockActivate);
        blockButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // Set the toggle button back to deactivated in case the user decides to cancel
                    prefsEditor.putBoolean("blockEnable", true);
                    // Hence, pull up a dialog so the user can select a date & time
                    // Time dialog is called by the date picker dialog after completion
                    showDatePickerDialog();
                } else {
                    // The toggle is disabled
                }
            }
        });
    }

    public void confirmSettingsDialog(String message, Intent desiredIntent, Boolean haveNegative) {
        DialogFragment newFragment = new ChangeSettingsDialogFragment();
        ((ChangeSettingsDialogFragment) newFragment).setSettingDetails(message, desiredIntent, haveNegative);
        newFragment.show(getSupportFragmentManager(), "access");
    }

    public void showDatePickerDialog() {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // Whenever something is selected from the Action Bar
        switch (item.getItemId()) {
            case R.id.settings_id:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.chart_id:
                startActivity(new Intent(this, ChartActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}

/*
class CheckRunningActivity extends Thread{

    public List CheckRunningActivity(Context con){
        Context context = con;
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 10000*10000, time);
        return appList;
    }

    public void run(){
        Looper.prepare();

        while(true){
            List appList = CheckRunningActivity()
            if (appList != null && appList.size() == 0) {
                Log.d("Executed app", "######### NO APP FOUND ##########" );
            }
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    Log.d("Executed app", "usage stats executed : " +usageStats.getPackageName() + "\t\t ID: ");
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    String currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();

                }
            }

            if (currentRunningActivityName.equals("PACKAGE_NAME.ACTIVITY_NAME")) {
                // show your activity here on top of PACKAGE_NAME.ACTIVITY_NAME
            }
        }
        Looper.loop();
    }
}*/