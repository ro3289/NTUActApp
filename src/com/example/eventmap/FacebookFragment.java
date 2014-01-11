package com.example.eventmap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class FacebookFragment extends ListFragment {

	private MainActivity mainActivity;
	private String value = "";
	private ListView listView;
	private ArrayAdapter<String> listAdapter;
	private String[] month = {"January", "February", "March", "April", "May", "June", 
	   		 "July",   "August", "September", "October",  "November",  "December" };
	
	public class MyListAdapter extends ArrayAdapter<String> {
		  
		  Context myContext;

		  public MyListAdapter(Context context, int textViewResourceId,
		    String[] objects) {
		   super(context, textViewResourceId, objects);
		   myContext = context;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		   //return super.getView(position, convertView, parent);
		   
		   LayoutInflater inflater = (LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View row = inflater.inflate(R.layout.listview_preference, parent, false);
		   
		   TextView eventName=(TextView)row.findViewById(R.id.month);
		   eventName.setText(month[position]);
		   
		   ImageView icon=(ImageView)row.findViewById(R.id.icon);
		   //Customize your icon here
		   icon.setImageResource(R.drawable.ic_launcher);
		   
		   return row;
		  }
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("=====>", "FacebookFragment onAttach");
		mainActivity = (MainActivity)activity;
		value = mainActivity.getFacebookData();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("=====>", "FacebookFragment onCreateView");
		return inflater.inflate(R.layout.frg_facebook, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("=====>", "FacebookFragment onActivityCreated");
		/*
		listAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.activity_list_item,month);
		setListAdapter(listAdapter);
		*/
		MyListAdapter myListAdapter = new MyListAdapter(getActivity(), R.layout.listview_preference, month);
		setListAdapter(myListAdapter);
	}
	
}
