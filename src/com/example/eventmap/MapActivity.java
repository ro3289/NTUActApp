package com.example.eventmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
	
	private boolean DATE_FILTER_ACTIVATED = false;
	private boolean TAG_FILTER_ACTIVATED  = false;

    private Button startDateButton, endDateButton, searchButton, resetButton;
    private Calendar calendar;
    private int mYear, mMonth, mDay;
    private String dateText, startDate, endDate;
    private TextView startDateText, endDateText;
    
    private Button tagButton;
    private final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
	private final ArrayList<Marker> dateFilterResult = new ArrayList<Marker>();  
	
	// Test event location stub
	static final LatLng A = new LatLng(23.979548, 120.696745);
	static final LatLng B = new LatLng(24.0, 120.696745);
	static final LatLng C = new LatLng(24.0, 120.706745);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        // map.setOnInfoWindowClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(A, 16));
        
        constructMap();
     //   dateFilter("2014-01-01", "2014-04-01");
        
        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        
        startDate = new String(mYear +"-" + mMonth + "-" + mDay);
        endDate = new String(mYear +"-" + mMonth + "-" + mDay);
        
        startDateText = (TextView)findViewById(R.id.startDateText);
        endDateText = (TextView)findViewById(R.id.endDateText);
        
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
		
		EventInfo event1 = new EventInfo(2014,3,9);
		event1.addTag(2).addTag(0);
		Marker marker1 = map.addMarker(new MarkerOptions().position(B).title("BBB").snippet("MMM"));
		System.out.println(event1.getDate());
		eventHashMap.put(marker1, event1);
		
		EventInfo event2 = new EventInfo(2014,5,8);
		event2.addTag(0).addTag(1);
		Marker marker2 = map.addMarker(new MarkerOptions().position(C).title("CCC").snippet("MMM"));
		System.out.println(event2.getDate());
		eventHashMap.put(marker2, event2);
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
				if((event.getCal().before(end) && event.getCal().after(start))){
					marker.setVisible(true);
					dateFilterResult.add(marker);
				}else{
					marker.setVisible(false);
				}
			}
			TAG_FILTER_ACTIVATED = true;
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
		DATE_FILTER_ACTIVATED = false;
		TAG_FILTER_ACTIVATED  = false;
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
}
