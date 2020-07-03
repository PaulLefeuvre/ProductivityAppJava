package com.example.productiveappjava;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

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
                final SharedPreferences sharedPref = getActivity().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
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
        final SharedPreferences sharedPref = getActivity().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Calendar currentTime = Calendar.getInstance();
        if(currentTime.get(Calendar.DATE) == sharedPref.getInt("chosenDay", 50)
            && currentTime.get(Calendar.MONTH) == sharedPref.getInt("chosenMonth", 20)
            && currentTime.get(Calendar.YEAR) == sharedPref.getInt("chosenYear", 1000)
            && currentTime.get(Calendar.HOUR) >= hourOfDay
            && currentTime.get(Calendar.MINUTE) >= minute)  {
            confirmTimeReselectDialog();
        } else {
            prefsEditor.putInt("chosenHour", hourOfDay);
            prefsEditor.putInt("chosenMinute", minute);
            prefsEditor.apply();
            confirmTimeConfirmDialog();
        }
    }

    public void confirmTimeReselectDialog() {
        DialogFragment timeReselectFragment = new TimeReselectDialogFragment();
        timeReselectFragment.show(getActivity().getSupportFragmentManager(), "reselectTime");
    }

    public void confirmTimeConfirmDialog() {
        DialogFragment timeConfirmFragment = new ConfirmTimeDialogFragment();
        timeConfirmFragment.show(getActivity().getSupportFragmentManager(), "confirmTime");
    }
}
