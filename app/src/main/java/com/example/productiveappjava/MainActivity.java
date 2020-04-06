package com.example.productiveappjava;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PermissionLog";



    @RequiresApi(api = Build.VERSION_CODES.M) // Requires an API of 22 or higher

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Change the Action Bar title
        ActionBar actionBar = getSupportActionBar(); // declare the actionBar
        getSupportActionBar().setTitle("Home Page"); // set the top title

        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();

        if(sharedPref.getBoolean("checkPermissions", true)) {
            prefsEditor.putBoolean(getString(R.string.check_permission_key), false);
            prefsEditor.commit();
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
/*
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
        if(!Settings.canDrawOverlays(this)){
            // ask for setting
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }*/

        // Stuff that doesn't work. Get back to this later
/*        final WindowManager mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initiate toggle button's
        ToggleButton blockActivate = (ToggleButton) findViewById(R.id.blockActivate);
        blockActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private View blockView;

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mWindowManager.addView(blockView, mLayoutParams);
                } else {
                    mWindowManager.removeView(blockView);
                }
            }

        });*/
    }

    public void confirmSettingsDialog(String message, Intent desiredIntent, Boolean haveNegative) {
        DialogFragment newFragment = new ChangeSettingsDialogFragment();
        ((ChangeSettingsDialogFragment) newFragment).setSettingDetails(message, desiredIntent, haveNegative);
        newFragment.show(getSupportFragmentManager(), "access");
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