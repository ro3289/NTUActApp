package com.example.eventdialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.eventmap.EventInfo;
import com.example.eventmap.R;


public class EventDialog {
	
	private static final EventDialog Instance = new EventDialog();
	
	public Activity activity;
	
	public static void setUpEventDialog(Activity a){
		getInstance().activity = a;
	}
	
	public void showDialog(EventInfo event) {
    	LayoutInflater layoutInflater = activity.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.event_dialog, null) ;
		((TextView) inflater.findViewById(R.id.eventname)).setText(event.name);
		((TextView) inflater.findViewById(R.id.content)).setText(event.content);
    	new AlertDialog.Builder(activity)
        .setTitle(event.name)
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
	
	public static EventDialog getInstance(){
		return Instance;
	}
	
}
