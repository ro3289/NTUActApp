package com.example.eventmap;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

public class Account{
	
	private Activity activity;
	private String username;
	private int myPreference;
	private ArrayList<Integer> selectedItems = new ArrayList<Integer>();
	private Builder preferenceDialog;

	public Account(Activity a, String name, int preference)
	{
		activity 		= a;
		username 		= name;
		myPreference 	= preference;
		preferenceDialog = new AlertDialog.Builder(activity)
	    // Set the dialog title
	    .setTitle(R.string.select_tags)
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
       .setMultiChoiceItems(R.array.tags, null,
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
        	   selectedItems.clear();
           }
       })
       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
        	   selectedItems.clear();
           }
       });
	}
	
	public void showMyPreference()
	{
		preferenceDialog.show();
	}
	
	private void updateMyPreference()
	{
		int newPreference = 0;
		for(Integer i : selectedItems){
			newPreference = (int) Math.pow(2, i.intValue());
		}
		myPreference = newPreference;
	}
}
