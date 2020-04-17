package com.example.productiveappjava;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.fragment.app.DialogFragment;

public class ConfirmTimeDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the date difference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor prefsEditor = sharedPref.edit();
        String dateStr = sharedPref.getInt("chosenDay", 0) + "/" + (sharedPref.getInt("chosenMonth", 0)+1) + "/" + sharedPref.getInt("chosenYear", 0) + " " + sharedPref.getInt("chosenHour", 0) + ":" + sharedPref.getInt("chosenMinute", 0);
        Log.i("timerPrint", String.valueOf(sharedPref.getInt("chosenDay", 0)));
        Log.i("timerPrint", String.valueOf(sharedPref.getInt("chosenMonth", 0)));
        Log.i("timerPrint", String.valueOf(sharedPref.getInt("chosenYear", 0)));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date timerDate;
        String durationStr = "";
        try {
            timerDate = sdf.parse(dateStr);
            long currentTime = System.currentTimeMillis();
            Log.i("timerPrint", "The current time is " + currentTime);
            Log.i("timerPrint", "The timer time is " + timerDate);
            long timeDiff = timerDate.getTime() - currentTime;
            Log.i("timerPrint", "the time difference is " + timeDiff);
            if((timeDiff / 86400000) > 300) {
                int years = (int) Math.rint((timeDiff/86400000.0)/365.25);
                if(years == 1) {
                    durationStr = "about a year";
                } else {
                    durationStr = "about " + years + " years";
                }
            } else if ((timeDiff / 3600000) > 20) {
                int days = (int) Math.rint(timeDiff/86400000.0);
                if(days == 1) {
                    durationStr = "about a day";
                } else {
                    durationStr = "about " + days + " days";
                }
            } else if ((timeDiff / 60000) > 50) {
                int hours = (int) Math.rint(timeDiff/3600000.0);
                if(hours == 1) {
                    durationStr = "about an hour";
                } else {
                    durationStr = "about " + hours + " hours";
                }
            } else if ((timeDiff / 1000) > 50) {
                int minutes = (int) Math.rint(timeDiff/60000.0);
                if(minutes == 1) {
                    durationStr = "about a minute";
                } else {
                    durationStr = "about " + minutes + " minutes";
                }
            } else {
                int seconds = (int) Math.rint(timeDiff/1000.0);
                if(seconds == 1) {
                    durationStr = "about a second (that's not a very long amount of time)";
                } else {
                    durationStr = "about " + seconds + " seconds";
                }
            }
        } catch (ParseException e) {
            durationStr = "a very long time";
        }

        builder.setMessage("Are you sure you want to continue? Once activated, this block will last for " + durationStr + ". This action cannot be reversed while the timer is running.")
                .setPositiveButton("Activate timer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        prefsEditor.putBoolean("blockEnable", true);
                        prefsEditor.apply();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefsEditor.putBoolean("blockEnable", false);
                        prefsEditor.apply();
                    }
                });

        return builder.create();
    }
}
