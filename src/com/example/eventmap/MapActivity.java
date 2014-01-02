package com.example.eventmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


import android.app.Activity;
import android.app.DatePickerDialog;
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

    private Button startDateButton, endDateButton, searchButton;
    private Calendar calendar;
    private int mYear, mMonth, mDay;
    private String dateText, startDate, endDate;
    private TextView startDateText, endDateText;
    private DatePickerDialog datePickerDialog;

	    
	// Test event stub
	static final LatLng A = new LatLng(23.979548, 120.696745);
	static final LatLng B = new LatLng(24.0, 120.696745);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        // map.setOnInfoWindowClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(A, 16));
        constructMap();
        dateFilter("2014-01-01", "2014-04-01");
        
        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        
        startDate = new String(mYear +"-" + mMonth + "-" + mDay);
        endDate = new String(mYear +"-" + mMonth + "-" + mDay);
        
        startDateText = (TextView)findViewById(R.id.startDateText);
        endDateText = (TextView)findViewById(R.id.endDateText);
        
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
	}

	// Method
	
	private void constructMap()
	{
		// Fetch data from remote server
		// TODO
		// Construct events
		
		EventInfo event = new EventInfo(2014,2,3);
		Marker marker = map.addMarker(new MarkerOptions().position(A).title("HHH").snippet("MMM"));
		System.out.println(event.getDate());
		eventHashMap.put(marker,event);
		
		EventInfo event2 = new EventInfo(2014,5,8);
		Marker marker2 = map.addMarker(new MarkerOptions().position(B).title("HHH").snippet("MMM"));
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
		
		// Run all event
		for(Marker marker : eventHashMap.keySet())
		{
			EventInfo event = eventHashMap.get(marker);
			if(event.getCal().before(end) && event.getCal().after(start)){
				marker.setVisible(true);
			}else{
				marker.setVisible(false);
			}
		}
	}
	
	public void resetDateFilter()
	{
		for(Marker marker : eventHashMap.keySet())
		{
			marker.setVisible(true);
		}
	}
	// Listener
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		
	}
	
	public void dateSelectDialog(final boolean setEnd) {
	     
		new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
	    	 
						                 @Override  
						                 public void onDateSet(DatePicker view,  int  year, int  monthOfYear,  int  dayOfMonth) 
						                 {  
						                     if(setEnd) {
						                    	 endDate = String.valueOf(year) + "-" + String.valueOf(monthOfYear) + "-" + String.valueOf(dayOfMonth);
						                    	 endDateText.setText(endDate);
						                     }else{
						                    	 startDate = String.valueOf(year) + "-" + String.valueOf(monthOfYear) + "-" + String.valueOf(dayOfMonth);
						                    	 startDateText.setText(startDate);
						                     }
						                 }  
							     	}
             // 設置初始日期  
             , 0000 , 0, 0)
	     .show();  
	}
}
