package com.example.productiveappjava;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private int minHour = -1, minMinute = -1, maxHour = 100, maxMinute = 100;

    private int currentHour, currentMinute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        timeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() { // Supposed to work every time the cancel button is pressed
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d("BlockButton", "Time dialog cancelled"); // Never logged when cancel button pressed
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefsEditor = sharedPref.edit();
                prefsEditor.putBoolean("blockEnable", false);
                prefsEditor.apply();
            }
        });

        timeDialog.setCanceledOnTouchOutside(true);

        // Create a new instance of TimePickerDialog and return it
        return timeDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putInt("chosenHour", hourOfDay);
        prefsEditor.putInt("chosenMinute", minute);
        prefsEditor.putBoolean("blockEnable", true);
        prefsEditor.apply();
    }
}
