package com.example.productiveappjava;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

public class TimeReselectDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Unfortunately the time you have set is not valid. This application does not offer the possibility of blocking in the past, only the present and future.")
            .setPositiveButton("Select new time", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    showTimePickerDialog();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("BlockButton", "Cancel button pressed");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefsEditor = sharedPref.edit();
                prefsEditor.putBoolean("blockEnable", false);
                prefsEditor.apply();
            }
        });
        return builder.create();
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }
}
