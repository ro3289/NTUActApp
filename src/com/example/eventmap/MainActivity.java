package com.example.eventmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.util.Account;
import com.example.util.DBConnector;
import com.example.util.EventDialog;
import com.example.util.EventInfo;
import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

public class MainActivity extends FragmentActivity {

	public static HashMap<Integer,EventInfo> eventList = new HashMap<Integer, EventInfo>();
	private AlertDialog facebookLoginDialog;
	private static int MY_EVENT_FRAGMENT = 0;
	private static int HOT_EVENT_FRAGMENT = 1;
	
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
//        this.showLoginDialog();

        Button mapActivity = (Button)findViewById(R.id.map_activity);
        mapActivity.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent=new Intent(v.getContext(), MapActivity.class);
                startActivityForResult(myIntent, MY_EVENT_FRAGMENT);
            }
        });
        
        // Test for dialog
        Button infoDialog = (Button) findViewById(R.id.get_info);
        infoDialog.setOnClickListener(new View.OnClickListener() {
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
				try {
					eventList.put(ID, new EventInfo(ID, name, location, url, image, content, sdf.parse(date), tag));
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
	    	
	    }
	    uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}
	
	/* 
	 * 
	 * 
	 * Facebook activity
	 * 
	 * 
	 */
    private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

    private LoginButton loginButton;
    private PendingAction pendingAction = PendingAction.NONE;
    private GraphUser user;
    private GraphPlace place;
    private List<GraphUser> tags;
	private boolean pickFriendsWhenSessionOpened;
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
		final View inflater = layoutInflater.inflate(R.layout.facebook_layout, null) ;
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
}
