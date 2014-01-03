package com.example.eventdialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.example.eventmap.R;


public class EventDialog {
	
	private Activity activity;
	public EventDialog(Activity a)
	{
		activity = a;
	}
	public void showDialog() {
    	LayoutInflater layoutInflater = activity.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.event_dialog, null) ;
    	new AlertDialog.Builder(activity)
        .setTitle("")
        .setView(inflater)
        .setPositiveButton(R.string.ok,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }
        )
        .setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }
        )
        .show();
    }
	
}
