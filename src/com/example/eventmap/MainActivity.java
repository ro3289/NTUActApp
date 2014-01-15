package com.example.eventmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.util.Account;
import com.example.util.DBConnector;
import com.example.util.EventDialog;
import com.example.util.EventInfo;
import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MainActivity extends FragmentActivity {

	public static HashMap<Integer,EventInfo> eventList = new HashMap<Integer, EventInfo>();
	private AlertDialog facebookLoginDialog;
	private static int MY_EVENT_FRAGMENT = 0;
	private static int HOT_EVENT_FRAGMENT = 1;
	public boolean MY_PREFERENCE = true;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		FragmentTabHost tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		//1
		tabHost.addTab(tabHost.newTabSpec("熱門活動")
			   				  .setIndicator("熱門活動"), 
   					  AppleFragment.class, 
   					  null);
	    //2
		tabHost.addTab(tabHost.newTabSpec("最新活動")
				   			  .setIndicator("最新活動"), 
					  GoogleFragment.class, 
					  null);
		//3
		tabHost.addTab(tabHost.newTabSpec("偏好瀏覽")
					   				  .setIndicator("偏好瀏覽"), 
							  TwitterFragment.class, 
							  null);
	    //4
		tabHost.addTab(tabHost.newTabSpec("我的活動")
				   			  .setIndicator("我的活動"), 
					  FacebookFragment.class, 
				      null);
		//5
		tabHost.addTab(tabHost.newTabSpec("常用連結")
			   				  .setIndicator("常用連結"), 
					  	ConnectFragment.class, 
					  null);
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()  
        .detectDiskReads()  
        .detectDiskWrites()  
        .detectNetwork()  
        .penaltyLog()  
        .build());  
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
        .detectLeakedSqlLiteObjects()   
        .penaltyLog()  
        .penaltyDeath()  
        .build());

        EventDialog.setUpEventDialog(this);
        searchText = (EditText) findViewById(R.id.searchText);
//        this.showLoginDialog();
        options = new DisplayImageOptions.Builder()
    	.showStubImage(R.drawable.ic_stub)
    	.showImageForEmptyUri(R.drawable.ic_empty)
    	.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory()
		.cacheOnDisc()
		.build();
        Button mapActivity = (Button)findViewById(R.id.map_activity);
        mapActivity.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent=new Intent(v.getContext(), MapActivity.class);
                startActivityForResult(myIntent, MY_EVENT_FRAGMENT);
            }
        });
        
        // Test for dialog
        Button setPreference = (Button) findViewById(R.id.set_preference);
        setPreference.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Account.getInstance().showMyPreference();
				//Account.getInstance().showMyEvent();
			}
		});
        
        Button pickFriendButton = (Button) findViewById(R.id.pick_friend);
        pickFriendButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	startPickFriendsActivity();
            }
        });
        
        final Button switchButton = (Button) findViewById(R.id.preference_friend_switch);
        switchButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	if(switchButton.getText().equals("朋友活動")){
            		switchButton.setText("我的偏好");
            		MY_PREFERENCE = false;
            		updateMyFriendEvent();
            	} else {
            		switchButton.setText("朋友活動");
            		MY_PREFERENCE = true;
            		updatePreferenceEvent();
            	}
            }
        });
        
        // Facebook Login setting
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        setUpFacebookLoginDialog();
        facebookLoginDialog.show();
    }
   
    private void getEventInfo()
    {
    	try {
			String resultData = new DBConnector().execute("SELECT * FROM activity").get();
			JSONArray jsonArray = new JSONArray(resultData);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for(int index = 0; index < jsonArray.length(); ++index)
			{
				int    ID 	    = jsonArray.getJSONObject(index).getInt("ID");
				String name 	= jsonArray.getJSONObject(index).getString("Name");
				String location = jsonArray.getJSONObject(index).getString("Location");
				String url 		= jsonArray.getJSONObject(index).getString("Url");
				String image	= jsonArray.getJSONObject(index).getString("ImageUrl");
				String content 	= jsonArray.getJSONObject(index).getString("Content");
				String date 	= jsonArray.getJSONObject(index).getString("Time");
				int    tag 	    = jsonArray.getJSONObject(index).getInt("Tag");
				double lat 		= jsonArray.getJSONObject(index).getDouble("Latitude");
				double lng 		= jsonArray.getJSONObject(index).getDouble("Longitude");
				try {
					eventList.put(ID, new EventInfo(ID, name, location, new LatLng(lat,lng) ,url, image, content, sdf.parse(date), tag));
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
    
    private void getUserEvent(){
    	// Tracing events
    	try {
			String myEventData = new DBConnector().execute("SELECT * FROM user_act WHERE UserID ='" + Account.getInstance().getUserID() + "'").get();
			JSONArray jsonArray = new JSONArray(myEventData);
			Account.getInstance().clearEvent();
			for(int index = 0; index < jsonArray.length(); ++index)
			{
				int eventID = jsonArray.getJSONObject(index).getInt("ActID");
				Account.getInstance().addMyEvent(eventList.get(eventID));
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

    
	public boolean checkAccountExsistence(String name){
		try {
			String result = new DBConnector().execute("SELECT Username FROM userlist WHERE Username = " + "'" + name + "'").get();
			JSONArray jsonArray = new JSONArray(result);
			return ((jsonArray.length() == 0)? false : true);
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
		return false;
	}
	
	public void registerAccount(String name, String pwd){
		new DBConnector().execute("INSERT INTO userlist (Username, Password) VALUES (" + "'" + name + "'," + "'" + pwd + "')");
	}
	
	public static HashMap<Integer,EventInfo> getEventList(){
		return eventList;
	}

	public void updatePreferenceEvent(){
		TwitterFragment eventFragment = (TwitterFragment) getSupportFragmentManager().findFragmentByTag("偏好瀏覽");
        if(eventFragment != null) eventFragment.updatePreferenceEvent();
	}
	
	private void updateMyFriendEvent(){
		TwitterFragment eventFragment = (TwitterFragment) getSupportFragmentManager().findFragmentByTag("偏好瀏覽");
        if(eventFragment != null) eventFragment.updateMyFriendEvent();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == MY_EVENT_FRAGMENT) {
	    	EventDialog.setUpEventDialog(this);
	        getUserEvent();
	        FacebookFragment eventFragment = (FacebookFragment)getSupportFragmentManager().findFragmentByTag("我的活動");
	        if(eventFragment != null) eventFragment.updateEventList();
	    }else if (requestCode == HOT_EVENT_FRAGMENT){
	    	AppleFragment eventFragment = (AppleFragment)getSupportFragmentManager().findFragmentByTag("熱門活動");
	        if(eventFragment != null) eventFragment.updateHotEvent();
	    }else if (requestCode == PICK_FRIENDS_ACTIVITY){
	    	updateMyFriendEvent();
	    }
	    uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}
	
	///////////////////////////
	//////////Facebook/////////
	///////////////////////////
	
    private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

    private LoginButton loginButton;
    private PendingAction pendingAction = PendingAction.NONE;
    private GraphUser user;
    private GraphPlace place;
    private List<GraphUser> tags;
	private boolean pickFriendsWhenSessionOpened;
	private ProfilePictureView profilePictureView;
	private static final int PICK_FRIENDS_ACTIVITY = 2;

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.cancel)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
    }


    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                break;
            case POST_STATUS_UPDATE:
                break;
        }
    }
    
    private void setUpFacebookLoginDialog() {
    	LayoutInflater layoutInflater = this.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.layout_facebook_login, null) ;
		loginButton = (LoginButton) inflater.findViewById(R.id.login_button);
	    loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                MainActivity.this.user = user;
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                checkLogin();
                handlePendingAction();
            }
	    });
    	facebookLoginDialog = new AlertDialog.Builder(this)
    	.setTitle("NTUAct")
    	.setView(inflater)
        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int whichButton) {
            	System.exit(0);
            }
        })
        .create();
    	facebookLoginDialog.setCanceledOnTouchOutside(false);
    }
    
    private void checkLogin(){
        if ( ensureOpenSession() && user != null) {
        	updateUserInfo(user.getName(), user.getId());
        	getEventInfo();
        	getUserEvent();
        	facebookLoginDialog.dismiss();
        } else {
        	facebookLoginDialog.show();
        }
    }
    
    public void updateUserInfo(String name, String id){
    	// User information and preference
    	try {
			String accountData = new DBConnector().execute("SELECT * FROM userlist WHERE ID =" + "'" + id + "'").get();
			JSONArray jsonArray = new JSONArray(accountData);
			if(jsonArray.length() != 0)
			{
				String userID 	  = jsonArray.getJSONObject(0).getString("ID");
				String username   = jsonArray.getJSONObject(0).getString("Username");
				int    preference = jsonArray.getJSONObject(0).getInt("Preference");
				Account.updateAccount(this, userID, username, preference);
				return;
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
    	// Create new account if not
		new DBConnector().execute("INSERT INTO userlist (Username, ID) VALUES ('" + name + "','" + id + "')");
		try {
			String newAccountData = new DBConnector().execute("SELECT * FROM userlist WHERE ID =" + "'" + id + "'").get();
			JSONArray newJsonArray = new JSONArray(newAccountData);
			if(newJsonArray.length() != 0)
			{
				String userID     = newJsonArray.getJSONObject(0).getString("ID");
				String username   = newJsonArray.getJSONObject(0).getString("Username");
				int    preference = newJsonArray.getJSONObject(0).getInt("Preference");
				Account.updateAccount(this, userID, username, preference);
				return;
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
		updateUserInfo(name, id);
    }
    
    private boolean ensureOpenSession() {
        if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
            Session.openActiveSession(this, true, new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            });
            return false;
        }
        return true;
    }
    
    private void startPickFriendsActivity() {
        if (ensureOpenSession()) {
            Intent intent = new Intent(this, PickFriendsActivity.class);
            // Note: The following line is optional, as multi-select behavior is the default for
            // FriendPickerFragment. It is here to demonstrate how parameters could be passed to the
            // friend picker if single-select functionality was desired, or if a different user ID was
            // desired (for instance, to see friends of a friend).
            
            PickFriendsActivity.populateParameters(intent, user.getId(), true, true);
            startActivityForResult(intent, PICK_FRIENDS_ACTIVITY);
        } else {
            pickFriendsWhenSessionOpened = true;
        }
    }
    
    
    public void parseMyFacebookEvent() {
    	String requestIdsText = "/" + user.getId() +"/events";
    	String[] requestIds = requestIdsText.split(",");
    	Session session = Session.getActiveSession();
    	Session.NewPermissionsRequest newPermissionsRequest = new Session
  		      .NewPermissionsRequest(this, Arrays.asList("user_events"));
    	session.requestNewPublishPermissions(newPermissionsRequest);
        List<Request> requests = new ArrayList<Request>();
        for (final String requestId : requestIds) {
            requests.add(new Request(Session.getActiveSession(), requestId, null, null, new Request.Callback() {
                public void onCompleted(Response response) {
                    GraphObject graphObject = response.getGraphObject();
                    FacebookRequestError error = response.getError();
                    String s = "";
                    if (graphObject != null) {
                        JSONObject jsonObject = graphObject.getInnerJSONObject();
                        try {
	                         JSONArray array = jsonObject.getJSONArray("data");
	                         for (int i = 0; i < array.length(); i++) {
	                             JSONObject object = (JSONObject) array.get(i);
	                             System.out.println(object.get("id"));
	                          }
                        } catch (JSONException e) {
                        	e.printStackTrace();
	                    }
                    }
                }
            }));
        }
        //pendingRequest = false;
        Request.executeBatchAndWait(requests);
    }
    
    ///////////////////////////
    /////////Search////////////
    ///////////////////////////
    
    private String[] myEventName;
	private String[] myEventContent;
	private String[] myEventImage;
	protected EditText searchText;
	private ArrayList<Integer> searchEventIdList = new ArrayList<Integer>();
	private DisplayImageOptions options;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	
	
    public void searchEvent(View view){
    	searchEventIdList.clear();
    	try {
			if(searchText.getText().toString().equals("")) {
				System.out.println("thisline");
			} else {
	    		String myEventData = new DBConnector().execute("SELECT * FROM activity WHERE Name LIKE '%"+searchText.getText().toString()+"%' OR Content LIKE '%"+searchText.getText().toString()+"%'").get();
				JSONArray jsonArray = new JSONArray(myEventData);
				ArrayList<String> myEventNameList 	 = new ArrayList<String>();
				ArrayList<String> myEventContentList = new ArrayList<String>();
				ArrayList<String> myEventImageList 	 = new ArrayList<String>();
				for(int index = 0; index < jsonArray.length(); ++index) {
					int eventID = jsonArray.getJSONObject(index).getInt("ID");
					searchEventIdList.add(eventID);
					myEventNameList.add(eventList.get(eventID).name);
					myEventContentList.add(eventList.get(eventID).content);
					myEventImageList.add(eventList.get(eventID).image);
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
				EventDialog.getInstance().showEventInfoDialog(eventList.get(searchEventIdList.get(which)), null);
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
}
