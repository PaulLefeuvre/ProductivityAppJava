package com.example.productiveappjava;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public class ChangeSettingsDialogFragment extends DialogFragment {

    String message = "";
    Intent currentIntent;
    Boolean activateNegativeButton = true;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
            .setPositiveButton(R.string.settingsAcknowledge, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(currentIntent);
                }
            });
        if(activateNegativeButton) {
            builder.setNegativeButton(R.string.settingsCancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Create function here that takes a string input and saves it to the message variable so it can be used when creating the dialog
    public void setSettingDetails(String dialogMessage, Intent settingsIntent, Boolean haveNegative) {
        message = dialogMessage;
        currentIntent = settingsIntent;
        activateNegativeButton = haveNegative;
        return;
    }
}
