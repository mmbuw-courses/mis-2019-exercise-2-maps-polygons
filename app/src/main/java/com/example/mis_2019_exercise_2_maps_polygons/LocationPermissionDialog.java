package com.example.mis_2019_exercise_2_maps_polygons;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

// https://developer.android.com/guide/topics/ui/dialogs.html
public class LocationPermissionDialog extends DialogFragment
{
    public AlertDialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_location_permission)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // grant permission
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // denied permission
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
