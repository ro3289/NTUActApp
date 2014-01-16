package com.example.eventmap;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.util.DBConnector;
import com.example.util.EventDialog;
import com.example.util.EventInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MapActivity extends Activity implements OnInfoWindowClickListener{

	private GoogleMap 	map;
	private HashMap<Marker,EventInfo> eventHashMap = new HashMap<Marker,EventInfo>();
	private HashMap<Integer, EventInfo> eventList = new HashMap<Integer, EventInfo>();
	
    private Button startDateButton, endDateButton, searchButton, resetButton;
    private Calendar calendar;
    private int mYear, mMonth, mDay;
    private String startDate, endDate;
    private TextView startDateText, endDateText;
    
    private Button tagButton;
	private ArrayList<Integer> originalSelectedItems = new ArrayList<Integer>();
	private ArrayList<Integer> newSelectedItems		 = new ArrayList<Integer>();
	private ArrayList<Integer> selectedItems 		 = new ArrayList<Integer>();
	private boolean[] checkedItems = new boolean[8]; 
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
        searchText = (EditText) findViewById(R.id.searchText);
        EventDialog.setUpEventDialog(this);
	}
	
	////////////////////////////////
	/////////// Method//////////////
	////////////////////////////////
	
	private void constructMap()
	{
		try {
			// Fetch data from remote server
			String resultData = new DBConnector().execute("SELECT * FROM " + DBConnector.table_activity).get();
			String imageQuery = new DBConnector().execute("SELECT * FROM " + DBConnector.table_image).get();
			JSONArray jsonArray = new JSONArray(resultData);
			JSONArray imageJsonArray = new JSONArray(imageQuery);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for(int index = 0; index < jsonArray.length(); ++index)
			{
				int    ID 	    = jsonArray.getJSONObject(index).getInt("id");
				String name 	= jsonArray.getJSONObject(index).getString("Name");
				String location = jsonArray.getJSONObject(index).getString("Location");
				String url 		= jsonArray.getJSONObject(index).getString("Url");
				String content 	= jsonArray.getJSONObject(index).getString("Content");
				String date 	= jsonArray.getJSONObject(index).getString("Time");
				int    tag 	    = jsonArray.getJSONObject(index).getInt("Tag");
				double lat 		= jsonArray.getJSONObject(index).getDouble("Latitude");
				double lng 		= jsonArray.getJSONObject(index).getDouble("Longitude");
				String image	= DBConnector.image_pre_url + imageJsonArray.getJSONObject(index).getString("img");
				// Construct events
				try {
					createEvent(ID, name, location, url, image, content, new LatLng(lat, lng), sdf.parse(date), tag);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	private void createEvent(int id, String name, String location, String url, String image, String content, LatLng position, Date date, int tag) 
	{
		EventInfo event = new EventInfo(id, name, location, position, url, image, content, date, tag);
		Marker marker = map.addMarker(new MarkerOptions().position(position).title(name));
		eventList.put(event.id, event);
		eventHashMap.put(marker, event);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
		dateFilterResult.add(marker);
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
			// Show alert dialog and reset search dates
		}
		
	}
	
	public void tagFilter()
	{
		// Compute tag value
		int tag = 0;
		for(Integer i: selectedItems)
		{
			tag += Math.pow(2,i.intValue());
			System.out.println(tag);
		}
		for(Marker marker : dateFilterResult)
		{
			if((eventHashMap.get(marker).getTagValue() & tag) == tag)
			{
				marker.setVisible(true);
			}else{
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
		EventDialog.getInstance().showEventInfoDialog(eventHashMap.get(marker), null);
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
             // Default by current date
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
        
        startDateText = (TextView)findViewById(R.id.start_date_text);
        endDateText = (TextView)findViewById(R.id.end_date_text);
	}
	
	private void setButton()
	{
		startDateButton = (Button)findViewById(R.id.start_date_button);
        startDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dateSelectDialog(false);
            }
        });
        endDateButton = (Button)findViewById(R.id.end_date_button);
        endDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	dateSelectDialog(true);
               
            }
        });
        searchButton = (Button)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	dateFilter(startDate, endDate);
            }
        });
        resetButton = (Button)findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	resetFilter();
            }
        });
        
        tagButton = (Button)findViewById(R.id.tag_button);
        tagButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
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
        	    new AlertDialog.Builder(MapActivity.this)
        	    // Set the dialog title
        	    .setTitle(R.string.select_tags)
        	    // Specify the list array, the items to be selected by default (null for none),
        	    // and the listener through which to receive callbacks when items are selected
	           .setMultiChoiceItems(R.array.tags, checkedItems,
	                      new DialogInterface.OnMultiChoiceClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int which, boolean isChecked) {
	                   if (isChecked) {
	                       // If the user checked the item, add it to the selected items
	                       newSelectedItems.add(which);
	                   } else if (newSelectedItems.contains(which)) {
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
	            	   tagFilter();
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   selectedItems = originalSelectedItems;
	               }
	           }).show();
            }
        });
	}
	
	private String[] myEventName;
	private String[] myEventContent;
	private String[] myEventImage;
	private ArrayList<Integer> searchEventIdList = new ArrayList<Integer>();
	protected EditText searchText;
	private DisplayImageOptions options;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	
	
    public void searchEvent(View view){
    	searchEventIdList.clear();
    	try {
			if(searchText.getText().toString().equals("")) {
				System.out.println("thisline");
			} else {
	    		String myEventData = new DBConnector().execute("SELECT * FROM "+ DBConnector.table_activity + " WHERE Name LIKE '%"+searchText.getText().toString()+"%' OR Content LIKE '%"+searchText.getText().toString()+"%'").get();
				JSONArray jsonArray = new JSONArray(myEventData);
				ArrayList<String> myEventNameList 	 = new ArrayList<String>();
				ArrayList<String> myEventContentList = new ArrayList<String>();
				ArrayList<String> myEventImageList 	 = new ArrayList<String>();
				for(int index = 0; index < jsonArray.length(); ++index) {
					int eventID = jsonArray.getJSONObject(index).getInt("id");
					myEventNameList.add(eventList.get(eventID).name);
					myEventContentList.add(eventList.get(eventID).content);
					myEventImageList.add(eventList.get(eventID).image);
					searchEventIdList.add(eventID);
				}
			
				myEventName = myEventNameList.toArray(new String[myEventNameList.size()]);
				myEventContent = myEventContentList.toArray(new String[myEventContentList.size()]);
				myEventImage = myEventImageList.toArray(new String[myEventImageList.size()]);
				showSearchList();
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
    private void showSearchList(){
		ListItemAdapter myListItemAdapter = new ListItemAdapter(this, R.layout.listview_search, myEventName);
		new AlertDialog.Builder(this)
	    // Set the dialog title
		.setTitle("Search")
		.setAdapter( myListItemAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// EventDialog.getInstance().showEventInfoDialog(eventList.get(searchEventIdList.get(which)), null);
				EventInfo event = eventList.get(searchEventIdList.get(which));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(event.point, 16));
				
			}
		})
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
       .show();
	}
    
    public class ListItemAdapter extends ArrayAdapter<String> {
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
  	  Context myContext;
  	
  	  public ListItemAdapter(Context context, int textViewResourceId, String[] objects) {
  		  super(context, textViewResourceId, objects);
  		  myContext = context;
  	  } 
  		  @Override
  		  public View getView(int position, View convertView, ViewGroup parent) {
  		   
  		   LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  		   View row = inflater.inflate(R.layout.listview_search, parent, false);
  		   TextView eventName = (TextView)row.findViewById(R.id.event_name);
		   eventName.setText(myEventName[position]);
		   
		   TextView eventContent = (TextView)row.findViewById(R.id.event_content);
		   eventContent.setText(myEventContent[position]);
		   
		   ImageView image=(ImageView)row.findViewById(R.id.image);
		   imageLoader.displayImage(myEventImage[position], image, options, animateFirstListener);

  		   return row;
  		  }
  	}
    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
				} else {
					imageView.setImageBitmap(loadedImage);
				}
				displayedImages.add(imageUri);
			}
		}
	}
	
	@Override
	public void onBackPressed() {
	    Intent myIntent = new Intent(MapActivity.this, MainActivity.class);
	    setResult(0,myIntent);
        finish();
	}
	
	
}
