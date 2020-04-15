package com.example.productiveappjava;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // If the user presses cancel when selecting the date
                // Deactivate the button and don't proceed with the timer
                // This is done by changing the SharedPreference value 'blockActivate'
                Log.i("BlockButton", "Date Dialog cancelled");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefsEditor = sharedPref.edit();
                prefsEditor.putBoolean("blockEnable", false);
                prefsEditor.apply();
            }
        });

        dateDialog.setCanceledOnTouchOutside(true);

        // Return the instance of DatePickerDialog
        return dateDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putInt("chosenYear", year);
        prefsEditor.putInt("chosenMonth", month);
        prefsEditor.putInt("chosenDay", day);
        prefsEditor.apply();

        showTimePickerDialog();
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }
}
