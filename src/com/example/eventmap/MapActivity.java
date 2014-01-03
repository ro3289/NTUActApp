package com.example.eventmap;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements OnInfoWindowClickListener{

	private GoogleMap 	map;
	private HashMap<Marker,EventInfo> eventHashMap = new HashMap<Marker,EventInfo>();

    private Button startDateButton, endDateButton, searchButton, resetButton;
    private Calendar calendar;
    private int mYear, mMonth, mDay;
    private String dateText, startDate, endDate;
    private TextView startDateText, endDateText;
    
    private Button tagButton;
    private final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
	private final ArrayList<Marker> dateFilterResult = new ArrayList<Marker>();  
	
	// Test event location stub
	static final LatLng A = new LatLng(25.179548, 121.396745);
	static final LatLng B = new LatLng(25.1, 121.396745);
	static final LatLng C = new LatLng(25.1, 121.406745);
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnInfoWindowClickListener(this);
        setCalendar();
        constructMap();
        setButton();
	}

	// Method
	
	private void constructMap()
	{
		// Fetch data from remote server
		// TODO
		// Construct events
		
		EventInfo event = new EventInfo(2014,2,3);
		event.addTag(1).addTag(2);
		Marker marker = map.addMarker(new MarkerOptions().position(A).title("AAA").snippet("MMM"));
		System.out.println(event.getDate());
		eventHashMap.put(marker,event);
		dateFilterResult.add(marker);
		
		EventInfo event1 = new EventInfo(2014,3,9);
		event1.addTag(2).addTag(0);
		Marker marker1 = map.addMarker(new MarkerOptions().position(B).title("BBB").snippet("MMM"));
		System.out.println(event1.getDate());
		eventHashMap.put(marker1, event1);
		dateFilterResult.add(marker1);
		
		EventInfo event2 = new EventInfo(2014,5,8);
		event2.addTag(0).addTag(1);
		Marker marker2 = map.addMarker(new MarkerOptions().position(C).title("CCC").snippet("MMM"));
		System.out.println(event2.getDate());
		eventHashMap.put(marker2, event2);
		dateFilterResult.add(marker2);
		
		try {
			String resultData = new DBConnector().execute("SELECT * FROM activity").get();
			JSONArray jsonArray = new JSONArray(resultData);
			System.out.println("JSONArray length = " + jsonArray.length());
			for(int index = 0; index < jsonArray.length(); ++index)
			{
				int    ID 	    = jsonArray.getJSONObject(index).getInt("ID");
				String name 	= jsonArray.getJSONObject(index).getString("Name");
				String location = jsonArray.getJSONObject(index).getString("Location");
				String url 		= jsonArray.getJSONObject(index).getString("url");
				String content 	= jsonArray.getJSONObject(index).getString("Content");
				String date 	= jsonArray.getJSONObject(index).getString("Time");
				double lat 		= jsonArray.getJSONObject(index).getDouble("Latitude");
				double lng 		= jsonArray.getJSONObject(index).getDouble("Longitude");
				createEvent(ID, name, location, url, content, lat, lng, date);
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createEvent(int ID, String Name, String Location, String url, String Content, double lat, double lng, String date) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			EventInfo event = new EventInfo(ID, Name, Location, url, Content, sdf.parse(date));
			Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(Name));
			eventHashMap.put(marker, event);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10));
			dateFilterResult.add(marker);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	// Date & Tag filters should be considered at the same time!!!!
	private void dateFilter(String s, String e)
	{
		// Initialize date format
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		// Set start time
		Calendar start = Calendar.getInstance();
		try {
			start.setTime(sdf.parse(s));
			System.out.println(start.getTime());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		// Set end time
		Calendar end   = Calendar.getInstance();
		try {
			end.setTime(sdf.parse(e));
			System.out.println(end.getTime());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		if(end.after(start) || end.equals(start))
		{
			dateFilterResult.clear();
			// Run all event
			for(Marker marker : eventHashMap.keySet())
			{
				EventInfo event = eventHashMap.get(marker);
				if((event.before(end) && event.after(start))){
					marker.setVisible(true);
					dateFilterResult.add(marker);
				}else{
					marker.setVisible(false);
				}
			}
		}else{
			// Show alert dialog
		}
		
	}
	
	public void tagFilter(int t)
	{
		for(Marker marker : dateFilterResult)
		{
			// Set marker invisible if not in the tag list
			if(!eventHashMap.get(marker).getTagList().contains(t)){
				marker.setVisible(false);
			}
		}
	}
	
	public void resetFilter()
	{
		dateFilterResult.clear();
		for(Marker marker : eventHashMap.keySet())
		{
			marker.setVisible(true);
			dateFilterResult.add(marker);
		}
	}
	
	// Listener
	@Override
	public void onInfoWindowClick(Marker marker) {
		showDialog(eventHashMap.get(marker));
	}
	
	 void showDialog(EventInfo event) {
    	LayoutInflater layoutInflater = getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.event_dialog, null) ;
		((TextView) inflater.findViewById(R.id.eventname)).setText(event.name);
		((TextView) inflater.findViewById(R.id.content)).setText(event.content);
    	new AlertDialog.Builder(this)
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
	
	public void dateSelectDialog(final boolean setEnd) {
	    Calendar c = Calendar.getInstance();
		new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
						                 @Override  
						                 public void onDateSet(DatePicker view,  int  year, int  monthOfYear,  int  dayOfMonth) 
						                 {  
						                     if(setEnd) {
						                    	 endDate = String.valueOf(year) + "-" + String.valueOf(monthOfYear+1) + "-" + String.valueOf(dayOfMonth);
						                    	 endDateText.setText(endDate);
						                     }else{
						                    	 startDate = String.valueOf(year) + "-" + String.valueOf(monthOfYear+1) + "-" + String.valueOf(dayOfMonth);
						                    	 startDateText.setText(startDate);
						                     }
						                 }  
							     	}
             // 設置初始日期  
             , c.get(Calendar.YEAR) , c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
	     .show();  
	}
	private void setCalendar()
	{
		calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        
        startDate = new String(mYear +"-" + mMonth + "-" + mDay);
        endDate = new String(mYear +"-" + mMonth + "-" + mDay);
        
        startDateText = (TextView)findViewById(R.id.startDateText);
        endDateText = (TextView)findViewById(R.id.endDateText);
	}
	private void setButton()
	{
		startDateButton = (Button)findViewById(R.id.startDateButton);
        startDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dateSelectDialog(false);
            }
        });
        endDateButton = (Button)findViewById(R.id.endDateButton);
        endDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	dateSelectDialog(true);
               
            }
        });
        searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	dateFilter(startDate, endDate);
            }
        });
        resetButton = (Button)findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	resetFilter();
            }
        });
        
        tagButton = (Button)findViewById(R.id.tagButton);
        tagButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	
        	    new AlertDialog.Builder(MapActivity.this)
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
	                       mSelectedItems.add(which);
	                   } else if (mSelectedItems.contains(which)) {
	                       // Else, if the item is already in the array, remove it 
	                       mSelectedItems.remove(Integer.valueOf(which));
	                   }
	               }
	           })
	           // Set the action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // User clicked OK, so save the mSelectedItems results somewhere
	                   // or return them to the component that opened the dialog
	            	   if(!mSelectedItems.isEmpty()){
		            	   for(Marker m: dateFilterResult){
		            		   m.setVisible(true);
		            	   }
		            	   for(int s: mSelectedItems){
		            		   tagFilter(s);
		            		   System.out.println(s);
		            	   }
		            	   mSelectedItems.clear();
	            	   }
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	               }
	           }).show();
            }
        });
	}
	
	@Override
	public void onBackPressed() {
	    Intent myIntent=new Intent(MapActivity.this, MainActivity.class);
        startActivity(myIntent);
        finish();
	}
}
