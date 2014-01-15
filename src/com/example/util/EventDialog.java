package com.example.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventmap.FacebookFragment;
import com.example.eventmap.R;
import com.facebook.widget.ProfilePictureView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class EventDialog {
	
	private static final EventDialog Instance = new EventDialog();
	
	public Activity activity;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private DisplayImageOptions options;
	private ArrayList<String> eventGuestsList = new ArrayList<String>();
	private ArrayList<String> eventGuestsNameList = new ArrayList<String>();
	private int eventGuests;
	
	public static void setUpEventDialog(Activity a){
		getInstance().activity = a;
	}
	
	public void showEventInfoDialog(final EventInfo event, final FacebookFragment eventListFragment) {
		
		LayoutInflater layoutInflater = activity.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.dialog_event, null) ;
		((TextView) inflater.findViewById(R.id.event_name)).setText("活動名稱： " + event.name);
		((TextView) inflater.findViewById(R.id.event_content)).setText("活動敘述： " + event.content);
		ImageView image = (ImageView) inflater.findViewById(R.id.dialog_image);
		
		options = new DisplayImageOptions.Builder()
    	.showStubImage(R.drawable.ic_stub)
    	.showImageForEmptyUri(R.drawable.ic_empty)
    	.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory()
		.cacheOnDisc()
		.build();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity.getApplicationContext())
    	.defaultDisplayImageOptions(options)
    	.build();
		
		eventGuests = getEventGuests(event);
		
		imageLoader.init(config);
		imageLoader.displayImage(event.image, image, options, animateFirstListener);
		
		final Button guestsButton = (Button) inflater.findViewById(R.id.guests_button);
		guestsButton.setText("有" + eventGuests + "人追蹤中");
		guestsButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				showGuestsList();
			}
		});
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
					new DBConnector().execute("INSERT INTO user_act (UserID, UserName, ActID, ActName) VALUES "
							+ "('" + Account.getInstance().getUserID() 
							+ "','"+ Account.getInstance().getUserName() 
							+ "'," + event.id 
							+ ",'" + event.name 
							+"')");
					new DBConnector().execute("UPDATE activity SET Follower = Follower + 1 WHERE ID = " + event.id);
				}else{
					trackEventButton.setText("追蹤活動");
					Account.getInstance().deleteMyEvent(event);
					new DBConnector().execute("DELETE FROM user_act WHERE UserID = '" + Account.getInstance().getUserID() + "'" + " AND ActID = " + event.id);
					new DBConnector().execute("UPDATE activity SET Follower = Follower - 1 WHERE ID = " + event.id);
				}
				eventGuests = getEventGuests(event);
				guestsButton.setText("有" + eventGuests + "人追蹤中");
			}
		});
		AlertDialog eventDialog = new AlertDialog.Builder(activity)
        .setView(inflater)
        .setPositiveButton(R.string.visit,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	if(!event.url.equals("")){
	                	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.url));
	    	        	activity.startActivity(browserIntent);
                	}else{
                		Toast.makeText(activity.getApplicationContext(), "此活動目前無專頁",
                				   Toast.LENGTH_SHORT).show();
                	}
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
        ).create();
		eventDialog.setCanceledOnTouchOutside(false);
		eventDialog.show();
    }
	
	public static EventDialog getInstance(){
		return Instance;
	}
	
	// Image loader method
	// 圖片顯示動畫
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
	
	private int getEventGuests(EventInfo event){
		eventGuestsNameList.clear();
		eventGuestsList.clear();
		try {
			String result = new DBConnector().execute("SELECT UserID, UserName FROM user_act WHERE ActID = " + event.id).get();
			JSONArray jsonArray = new JSONArray(result);
			for(int index = 0; index < jsonArray.length(); ++index){
				String guestID   = jsonArray.getJSONObject(index).getString("UserID");
				String guestName = jsonArray.getJSONObject(index).getString("UserName");
				eventGuestsList.add(guestID);
				eventGuestsNameList.add(guestName);
			}
			return eventGuestsList.size();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return 0;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private void showGuestsList(){
		String[] guestsList = eventGuestsNameList.toArray(new String[eventGuestsNameList.size()]);
		ListItemAdapter myListItemAdapter = new ListItemAdapter(activity, R.layout.listview_event_guests, guestsList);
		new AlertDialog.Builder(activity)
	    // Set the dialog title
		.setTitle("追蹤此活動的人")
		.setAdapter( myListItemAdapter, null)
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
       .show();
	}
	
	public class ListItemAdapter extends ArrayAdapter<String> {
		
	  Context myContext;
	
	  public ListItemAdapter(Context context, int textViewResourceId, String[] objects) {
		  super(context, textViewResourceId, objects);
		  myContext = context;
	  } 
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		   
		   LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View row = inflater.inflate(R.layout.listview_event_guests, parent, false);
		   
		   ProfilePictureView profilePictureView = (ProfilePictureView) row.findViewById(R.id.profilePicture);
		   profilePictureView.setProfileId(eventGuestsList.get(position));
		   
		   TextView guestName = (TextView)row.findViewById(R.id.guest_name);
		   guestName.setText(eventGuestsNameList.get(position));
		   
		   return row;
		  }
	}

}
