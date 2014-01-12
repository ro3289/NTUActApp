package com.example.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.eventmap.FacebookFragment;
import com.example.eventmap.R;


public class EventDialog {
	
	private static final EventDialog Instance = new EventDialog();
	public Activity activity;
	
	public static void setUpEventDialog(Activity a){
		getInstance().activity = a;
	}
	
	public void showEventInfoDialog(final EventInfo event, final FacebookFragment eventListFragment) {
    	LayoutInflater layoutInflater = activity.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.event_dialog, null) ;
		((TextView) inflater.findViewById(R.id.event_name)).setText("活動名稱： " + event.name);
		((TextView) inflater.findViewById(R.id.event_content)).setText("活動敘述： " + event.content);
		// Set track event button
		final Button trackEventButton = (Button) inflater.findViewById(R.id.track_event_button);
		if(Account.getInstance().containEvent(event)){
			trackEventButton.setText("追蹤中");
		}else{
			trackEventButton.setText("追蹤活動");
		}
		trackEventButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(trackEventButton.getText().toString().equals("追蹤活動")){
					trackEventButton.setText("追蹤中");
					Account.getInstance().addMyEvent(event);
					new DBConnector().execute("INSERT INTO user_act (UserID, ActID) VALUES (" + Account.getInstance().getUserID() + ", " + event.id + ")");
				}else{
					trackEventButton.setText("追蹤活動");
					Account.getInstance().deleteMyEvent(event);
					new DBConnector().execute("DELETE FROM user_act WHERE UserID = " + Account.getInstance().getUserID() + " AND ActID = " + event.id);
				}
			}
		});
    	new AlertDialog.Builder(activity)
        .setTitle(event.name)
        .setView(inflater)
        .setPositiveButton(R.string.visit,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	
                }
            }
        )
        .setNegativeButton(R.string.exit,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	if(eventListFragment != null){
                		eventListFragment.updateEventList();
                	}
                }
            }
        )
        .show();
    }
	
	public static EventDialog getInstance(){
		return Instance;
	}
	
}
