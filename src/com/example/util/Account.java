package com.example.util;


import java.util.ArrayList;

import com.example.eventmap.MainActivity;
import com.example.eventmap.R;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class Account{
	
	public static Account INSTANCE = new Account();
	
	private MainActivity activity;
	private String username;
	private int userID;
	private int myPreference;
	
	private ArrayList<Integer> originalSelectedItems = new ArrayList<Integer>();
	private ArrayList<Integer> newSelectedItems		 = new ArrayList<Integer>();
	private ArrayList<Integer> selectedItems 		 = new ArrayList<Integer>();
	private boolean[] checkedItems = new boolean[8]; 
	
	private ArrayList<EventInfo> myEventList 		 = new ArrayList<EventInfo>();

	public static void updateAccount(MainActivity a, int id, String name, String pwd, int preference)
	{
		getInstance().activity 		= a;
		getInstance().userID		= id;
		getInstance().username 		= name;
		getInstance().myPreference 	= preference;
		getInstance().myEventList	.clear();
		getInstance().selectedItems .clear();
		getInstance().parceMyPreference();
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
		// Temp arrayList
		// Convert item id to boolean check bit
		originalSelectedItems = new ArrayList<Integer>();
		newSelectedItems      = new ArrayList<Integer>();
		for(int i = 0; i < 7; ++i){
			if(selectedItems.contains(i)){
				checkedItems[i] = true;
				originalSelectedItems.add(i);
				newSelectedItems     .add(i);
			}else{
				checkedItems[i] = false;
			}
			
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
            	   newSelectedItems.add(which);
               } else if (selectedItems.contains(which)) {
                   // Else, if the item is already in the array, remove it 
            	   newSelectedItems.remove(Integer.valueOf(which));
               }
           }
       })
       // Set the action buttons
       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
               // User clicked OK, so save the mSelectedItems results somewhere
               // or return them to the component that opened the dialog
        	   selectedItems = newSelectedItems;
        	   updateMyPreference();
           }
       })
       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
        	   selectedItems = originalSelectedItems;
           }
       }).show();
	}
	
	private void updateMyPreference()
	{
		int newPreference = 0;
		for(Integer i : selectedItems){
			newPreference += (int) Math.pow(2, i.intValue());
		}
		myPreference = newPreference;
		// Update database!!
		new DBConnector().execute("UPDATE userlist SET Preference = " + myPreference + " WHERE ID = " + userID);
	}

	public void addMyEvent(EventInfo event)
	{
		myEventList.add(event);
	}
	
	public void showMyEvent()
	{
		String[] myEventItems = getEventNameStringArray();
		
    	new AlertDialog.Builder(activity)
        .setTitle(R.string.my_events)
        .setItems(myEventItems, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EventDialog.getInstance().showEventInfoDialog(myEventList.get(which));
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
	
	public String[] getEventNameStringArray(){
		ArrayList<String> myEventItemsList = new ArrayList<String>();
		for(EventInfo event : myEventList)
		{
			myEventItemsList.add(event.name);
		}
		return myEventItemsList.toArray(new String[myEventItemsList.size()]);
	}
	
	public EventInfo getEvent(int which){
		return myEventList.get(which);
	}
}
