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
import com.example.eventmap.FriendPickerApplication;
import com.example.eventmap.InviteFriendsActivity;
import com.example.eventmap.MainActivity;
import com.example.eventmap.R;
import com.facebook.Session;
import com.facebook.model.GraphUser;
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
	private ArrayList<String> inviteList = new ArrayList<String>();
	private ArrayList<String> inviteNameList = new ArrayList<String>();
	private int eventGuests;
	private static final int INVITE_FRIENDS_ACTIVITY = 3;
	
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
		final Button inviteButton = (Button) inflater.findViewById(R.id.invite_button);
		inviteButton.setText("邀請朋友");
		inviteButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened())
				{
				}
				else
				{
			            Intent intent = new Intent(inflater.getContext(), InviteFriendsActivity.class);
			            // Note: The following line is optional, as multi-select behavior is the default for
			            // FriendPickerFragment. It is here to demonstrate how parameters could be passed to the
			            // friend picker if single-select functionality was desired, or if a different user ID was
			            // desired (for instance, to see friends of a friend).
			            
			            InviteFriendsActivity.populateParameters(intent, Account.INSTANCE.getUserID(), true, true);
			            activity.startActivityForResult(intent, INVITE_FRIENDS_ACTIVITY);
				}
			}
			
		});
		final Button inviteListButton = (Button) inflater.findViewById(R.id.invitelist_button);
		inviteListButton.setText("受邀朋友");
		inviteListButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				showInviteList(event);
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
                	getInviteFriends(event);
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
	
	private void showInviteList(EventInfo e){
		FriendPickerApplication application = (FriendPickerApplication) activity.getApplication();
		List<GraphUser> inviteUsers = application.getInviteUsers();
		System.out.println("hi1");
		if (inviteUsers!=null && inviteUsers.size()!=0) {
        	inviteList=new ArrayList<String>();
        	System.out.println("hi2");
    		for(final GraphUser inviteUser:inviteUsers)
        	{
        		
    			String result2;
				try {
					result2 = new DBConnector().execute("SELECT * FROM userlist WHERE ID ="+inviteUser.getId()).get();
				
        		JSONArray jsonArray = new JSONArray(result2);
                for(int index = 0; index < jsonArray.length(); ++index){
                        String userID = jsonArray.getJSONObject(index).getString("ID");
                        inviteList.add(inviteUser.getId());
            			inviteNameList.add(inviteUser.getName());
            			
                }
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
    		System.out.println("hi3");
        	if(inviteList.size()!=0)
            {
        		System.out.println("hi4");
              	for(int index=0;index<inviteList.size();++index)
               	{
              		String result2;
					try {
						result2 = new DBConnector().execute("SELECT * FROM invitation WHERE userID ="+inviteList.get(index)+" AND inviteID ="+Account.INSTANCE.getUserID()+" AND actID = "+e.id).get();
					
            		JSONArray jsonArray = new JSONArray(result2);
                    for(int index2 = 0; index2 < jsonArray.length(); ++index2){
                    	//String result = new DBConnector().execute("UPDATE invitation (userID,inviteID,actID) VALUES ("+inviteUser.getId()+","+	Account.INSTANCE.getUserID()+","+e.id+")").get();
                			
                    }
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ExecutionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
						try {
							String result = new DBConnector().execute("INSERT INTO invitation (userID,inviteID,actID) VALUES ("+inviteList.get(index)+","+	Account.INSTANCE.getUserID()+","+e.id+")").get();
						} catch (InterruptedException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						} catch (ExecutionException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
               	}
            }
        	
        	
    	
		}else {
			System.out.println("no user");
    	
		}
        System.out.println("herehere");
        if(inviteList!=null&&inviteList.size()!=0)
        {
        	String[] getInviteList = inviteList.toArray(new String[inviteList.size()]);
    		ListItemAdapter2 myListItemAdapter = new ListItemAdapter2(activity, R.layout.listview_invite, getInviteList);
    		new AlertDialog.Builder(activity)
    	    // Set the dialog title
    		.setTitle("追蹤此活動的人")
    		.setAdapter( myListItemAdapter, null)
    	    // Specify the list array, the items to be selected by default (null for none),
    	    // and the listener through which to receive callbacks when items are selected
           .show();
        }
        else
        {
        	
        }
		
		
		
		
		
		
		
		

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
	public class ListItemAdapter2 extends ArrayAdapter<String> {
		
		  Context myContext;
		
		  public ListItemAdapter2(Context context, int textViewResourceId, String[] objects) {
			  super(context, textViewResourceId, objects);
			  myContext = context;
		  } 
			  @Override
			  public View getView(int position, View convertView, ViewGroup parent) {
			   
			   LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			   View row = inflater.inflate(R.layout.listview_invite, parent, false);
			   
			   ProfilePictureView profilePictureView = (ProfilePictureView) row.findViewById(R.id.profilePicture);
			   profilePictureView.setProfileId(inviteList.get(position));
			   
			   TextView guestName = (TextView)row.findViewById(R.id.guest_name);
			   guestName.setText(inviteNameList.get(position));
			   
			   return row;
			  }
		}

	private void getInviteFriends(EventInfo e){
		FriendPickerApplication application = (FriendPickerApplication) activity.getApplication();
        List<GraphUser> inviteUsers = application.getInviteUsers();
        if (inviteUsers != null&&inviteUsers.size()!=0) {
        	inviteList=new ArrayList<String>();
        		for(final GraphUser inviteUser:inviteUsers)
            	{
            		
        			String result2;
					try {
						result2 = new DBConnector().execute("SELECT * FROM userlist WHERE ID ="+inviteUser.getId()).get();
					
            		JSONArray jsonArray = new JSONArray(result2);
                    for(int index = 0; index < jsonArray.length(); ++index){
                            String userID = jsonArray.getJSONObject(index).getString("ID");
                            inviteList.add(inviteUser.getId());
                			inviteNameList.add(inviteUser.getName());
                			
                    }
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ExecutionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            	}
            	if(inviteList.size()!=0)
                {
                  	for(int index=0;index<inviteList.size();++index)
                   	{
                  		String result2;
    					try {
    						result2 = new DBConnector().execute("SELECT * FROM invitation WHERE userID ='"+inviteList.get(index)+"' AND inviteID ='"+Account.INSTANCE.getUserID()+"' AND actID = '"+e.id+"'").get();
    					
                		JSONArray jsonArray = new JSONArray(result2);
                        for(int index2 = 0; index2 < jsonArray.length(); ++index2){
                        	//String result = new DBConnector().execute("UPDATE invitation (userID,inviteID,actID) VALUES ("+inviteUser.getId()+","+	Account.INSTANCE.getUserID()+","+e.id+")").get();
                    			
                        }
    					} catch (InterruptedException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} catch (ExecutionException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} catch (JSONException e1) {
    						// TODO Auto-generated catch block
    						//e1.printStackTrace();
    						try {
								String result = new DBConnector().execute("INSERT INTO invitation (userID,inviteID,actID) VALUES ("+inviteList.get(index)+","+	Account.INSTANCE.getUserID()+","+e.id+")").get();
							} catch (InterruptedException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							} catch (ExecutionException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
    					}
                   	}
                }
            	
            	
        	
        }else {
        	System.out.println("no user");
        	
        }
	}
	
}
