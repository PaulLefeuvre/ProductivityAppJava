package com.example.productiveappjava;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

    private Handler timerHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.M) // Requires an API of minimum 23
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

        final TextView timerText = (TextView)findViewById(R.id.countdownTimer);
        final ToggleButton blockButton = (ToggleButton) findViewById(R.id.blockActivate);

        // If the timer block has been activated
        // Display the time remaining countdown
        if(sharedPref.getBoolean("blockEnable", false)) {
            timerText.setText("False");
            blockButton.setChecked(false);
        } else {
            timerText.setText("True");
            blockButton.setChecked(true);
            //timerHandler.removeCallbacks(updateTimeTask);
            //timerHandler.postDelayed(updateTimeTask, 100);
        }

        blockButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // Set the toggle button back to deactivated in case the user decides to cancel
                    blockButton.setChecked(false);
                    // Hence, pull up a dialog so the user can select a date & time
                    // Date picker is called by the date picker dialog after completion
                    showDatePickerDialog();
                } else {
                    prefsEditor.putBoolean("blockEnable", false);
                }
            }
        });
    }

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

    public void confirmSettingsDialog(String message, Intent desiredIntent, Boolean haveNegative) {
        DialogFragment permissionFragment = new ChangeSettingsDialogFragment();
        ((ChangeSettingsDialogFragment) permissionFragment).setSettingDetails(message, desiredIntent, haveNegative);
        permissionFragment.show(getSupportFragmentManager(), "access");
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

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            TextView timerText = (TextView)findViewById(R.id.countdownTimer);

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String dateStr = sharedPref.getInt("chosenDay", 0) + "/" + sharedPref.getInt("chosenMonth", 0) + "/" + sharedPref.getInt("chosenYear", 0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate;
            try {
                startDate = sdf.parse(dateStr);
            } catch (ParseException e) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - startDate.getTime();
            int days = (int) Math.floor(timeDiff / 864000000);
            timeDiff = timeDiff % 864000000;
            int hours = (int) Math.floor(timeDiff / 3600000);
            timeDiff = timeDiff % 3600000;
            int minutes = (int) Math.floor(timeDiff / 60000);
            timeDiff = timeDiff % 60000;
            int seconds = (int) Math.floor(timeDiff / 1000);

            String finalDate = "";
            if (days < 10) {
                finalDate += "0" + days;
            } else {
                finalDate += days;
            }
            if (hours < 10) {
                finalDate += ":0" + hours;
            } else {
                finalDate += ":" + hours;
            }
            if (minutes < 10) {
                finalDate += ":0" + minutes;
            } else {
                finalDate += ":" + minutes;
            }
            if (seconds < 10) {
                finalDate += ":0" + seconds;
            } else {
                finalDate += ":" + seconds;
            }

            Log.d("BlockButton", "Updating timer text...");

            timerText.setText(finalDate);
            timerHandler.postAtTime(this,
                    currentTime + 1000);
        }
    };
}