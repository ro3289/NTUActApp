package com.example.eventmap;

import java.awt.Event;
import java.util.ArrayList;




import com.example.eventdialog.EventDialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Account{
	
	public static Account INSTANCE = new Account();
	private MainActivity activity;
	private String username;
	private int userID;
	private int myPreference;
	private ArrayList<Integer> selectedItems = new ArrayList<Integer>();
	private boolean[] checkedItems = new boolean[8]; 
	private ArrayList<EventInfo> myEventList = new ArrayList<EventInfo>();

	public static void updateAccount(MainActivity a, int id, String name, String pwd, int preference)
	{
		getInstance().activity 		= a;
		getInstance().userID			= id;
		getInstance().username 		= name;
		getInstance().myPreference 	= preference;
		getInstance().parceMyPreference();
		getInstance().myEventList.clear();
	}
	
	private void parceMyPreference() {
		for(int i = 0; i < 7; ++i){
			if(((myPreference >> i) & 1) == 1){
				selectedItems.add(i);
			}
		}
	}

	public void showMyPreference()
	{
		// Convert item id to boolean check bit
		for(int i = 0; i < 7; ++i){
			checkedItems[i] = ((selectedItems.contains(i))? true : false);
		}
		new AlertDialog.Builder(activity)
	    // Set the dialog title
	    .setTitle(R.string.set_preference)
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
       .setMultiChoiceItems(R.array.tags, checkedItems,
                  new DialogInterface.OnMultiChoiceClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which, boolean isChecked) {
               if (isChecked) {
                   // If the user checked the item, add it to the selected items
            	   selectedItems.add(which);
               } else if (selectedItems.contains(which)) {
                   // Else, if the item is already in the array, remove it 
            	   selectedItems.remove(Integer.valueOf(which));
               }
           }
       })
       // Set the action buttons
       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
               // User clicked OK, so save the mSelectedItems results somewhere
               // or return them to the component that opened the dialog
        	   updateMyPreference();
           }
       })
       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {

           }
       }).show();
	}
	
	private void updateMyPreference()
	{
		int newPreference = 0;
		for(Integer i : selectedItems){
			newPreference = (int) Math.pow(2, i.intValue());
		}
		myPreference = newPreference;
	}

	public void addMyEvent(EventInfo event)
	{
		myEventList.add(event);
	}
	
	public void showMyEvent()
	{
		ArrayList<String> myEventItemsList = new ArrayList<String>();
		for(EventInfo event : myEventList)
		{
			myEventItemsList.add(event.name);
		}
		String[] myEventItems = myEventItemsList.toArray(new String[myEventItemsList.size()]);
    	new AlertDialog.Builder(activity)
        .setTitle(R.string.my_events)
        .setItems(myEventItems, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EventDialog.getInstance().showDialog(myEventList.get(which));
			}
		})
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .show();
	}
	
	public static Account getInstance(){
		return INSTANCE;
	}
}
