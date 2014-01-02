package com.example.eventdialog;


import com.example.eventmap.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EventDialog extends DialogFragment{
	
	static EventDialog newInstance(){
		return new EventDialog();
	}

	@Override 
	public void onCreate(Bundle savedInstanceState){
		
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.eventdialog_layout, container, false);
        TextView tv = (TextView) inflater.findViewById(R.id.testtext);
        setText("This is an instance of MyDialogFragment");
        return v;
    }
	
}
