package com.example.productiveappjava;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private boolean runTimer = false;

    // For the countdown timer
    int days = 0;
    int hours = 100;
    int minutes = 0;
    int seconds = 0;
    Date targetDate;

    Intent startServiceIntent;
    private BlockerService blockerService;

    @RequiresApi(api = Build.VERSION_CODES.M) // Requires an API of minimum 23
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Change the Action Bar title
        ActionBar actionBar = getSupportActionBar(); // declare the actionBar

        // make sure the action bar doesn't return null
        assert actionBar != null;
        actionBar.setTitle("Home Page"); // set the top title

        // Create the SharedPreferences editor and reader
        Context context = this;
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        final SharedPreferences.Editor prefsEditor = sharedPref.edit();

        // Check permissions and ask for them if they are not granted
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

        // Set the timer counter to constantly display the current time
        final Handler handler = new Handler();
        final Runnable updateTimeTask = new Runnable() {
            public void run() {
                TextView timerText = (TextView)findViewById(R.id.countdownTimer);

                long currentTime = System.currentTimeMillis();
                if(currentTime >= getTimerTime()) {
                    Log.i("Timer", "Timer finished");
                    timerText.setText("");
                    runTimer = false;
                } else if(runTimer) {
                    handler.postDelayed(this, 1000);
                    if(hours == 100) { // Condition defined for when the time remaining has to be calculated completely
                        timeDiffCalculate();
                        Log.i("Timer", days + ":" + hours + ":" + minutes + ":" + seconds);
                    } else {
                        if(seconds == 0) {
                            if(minutes == 0) {
                                if(hours == 0) {
                                    if(days == 0) {
                                        // This should never happen. If it somehow has, good luck fixing it
                                    } else {
                                        days--;
                                        hours = 23;
                                        minutes = 59;
                                        seconds = 59;
                                    }
                                } else {
                                    hours--;
                                    minutes = 59;
                                    seconds = 59;
                                }
                            } else {
                                minutes--;
                                seconds = 59;
                            }
                        } else {
                            seconds--;
                        }
                    }
                    String finalDate = String.format("%02d", days) + ":" + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
                    Log.i("Timer", "Updating timer text: " + finalDate);
                    timerText.setText(finalDate);
                }

            }
        };

        final TextView timerText = (TextView)findViewById(R.id.countdownTimer);
        final ToggleButton blockButton = (ToggleButton) findViewById(R.id.blockActivate);

        // The variables needed to initialize the blocking service
        blockerService = new BlockerService();
        startServiceIntent = new Intent(this, blockerService.getClass());

        // Check if the blocking service has been activated from the start
        if(sharedPref.getBoolean("blockEnable", false)) {
            // If the block is on, check the button and disable it
            blockButton.setChecked(true);
            blockButton.setEnabled(false);
            runTimer = true;
            handler.post(updateTimeTask);

            // Also start the service that checks which app is running in the background
            if (!isMyServiceRunning(blockerService.getClass())) { // but only if it isn't already running
                startService(startServiceIntent);
            }
        } else {
            // Otherwise set it so it's unchecked, enabled, and the timer text is empty
            blockButton.setChecked(false);
            blockButton.setEnabled(true);
            timerText.setText("");
        }

        blockButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // If it's the user that toggled the button and not the SharedPreference listener
                    if(!sharedPref.getBoolean("blockEnable", false)) {
                        // Set the toggle button back to deactivated in case the user decides to cancel
                        blockButton.setChecked(false);
                        // Hence, pull up a dialog so the user can select a date & time
                        // Date picker is called by the date picker dialog after completion
                        showDatePickerDialog();
                    } else {
                        runTimer = true;
                        handler.post(updateTimeTask);
                        Log.i("Timer", "SharedPreference Change started");
                    }
                } else {
                    blockButton.setEnabled(true);
                }
            }
        });
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
            final SharedPreferences.Editor prefsEditor = sharedPref.edit();

            Log.i("Timer", "SharedPreference change detected");
            Log.i("Timer", key);
            if (key.equals("blockEnable")) {
                Log.i("Timer", "'blockEnable' SharedPreference has been changed.");
                ToggleButton blockButton = (ToggleButton) findViewById(R.id.blockActivate);
                if (sharedPref.getBoolean("blockEnable", false)) {
                    // Set the block button to true, and disable it
                    blockButton.setChecked(true);
                    blockButton.setEnabled(false);
                    // Make the current app SharedPref empty to avoid mistakes
                    prefsEditor.putString(String.valueOf(R.string.currentAppKey), "");
                    prefsEditor.commit();
                    // Start the background service
                    startService(startServiceIntent);
                } else {
                    // Set the block button to false
                    blockButton.setChecked(false);
                    // Stop the background service, but only if it is actually running
                    if (isMyServiceRunning(blockerService.getClass())) {
                        stopService(startServiceIntent);
                    }
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Create the SharedPreferences editor and reader
        Context context = this;
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        sharedPref.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Create the SharedPreferences editor and reader
        Context context = this;
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        sharedPref.unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the runnable from working here
        runTimer = false;
        // Set it so that the time needs to be recalculated
        hours = 100;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Function that checks if a service is currently running. Mainly used for the blocker service
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public void confirmSettingsDialog(String message, Intent desiredIntent, Boolean haveNegative) {
        DialogFragment permissionFragment = new ChangeSettingsDialogFragment();
        ((ChangeSettingsDialogFragment) permissionFragment).setSettingDetails(message, desiredIntent, haveNegative);
        permissionFragment.show(getSupportFragmentManager(), "access");
    }

    public void showDatePickerDialog() {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void timeDiffCalculate() {
        long currentTime = System.currentTimeMillis();
        long timerTime = getTimerTime();
        long timeDiff = timerTime - currentTime;
        days = (int) Math.floor(timeDiff / 864000000);
        timeDiff = timeDiff % 864000000;
        hours = (int) Math.floor(timeDiff / 3600000);
        timeDiff = timeDiff % 3600000;
        minutes = (int) Math.floor(timeDiff / 60000);
        timeDiff = timeDiff % 60000;
        seconds = (int) Math.floor(timeDiff / 1000);
    }

    public long getTimerTime() {
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        String dateStr = sharedPref.getInt("chosenDay", 0) + "/" + (sharedPref.getInt("chosenMonth", 0)+1) + "/" + sharedPref.getInt("chosenYear", 0) + " " + sharedPref.getInt("chosenHour", 0) + ":" + sharedPref.getInt("chosenMinute", 0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            targetDate = sdf.parse(dateStr);
            return targetDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
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