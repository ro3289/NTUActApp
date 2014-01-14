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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		FragmentTabHost tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		//1
		tabHost.addTab(tabHost.newTabSpec("Apple")
			   				  .setIndicator("Apple"), 
   					  AppleFragment.class, 
   					  null);
	    //2
		tabHost.addTab(tabHost.newTabSpec("Google")
				   			  .setIndicator("Google"), 
					  GoogleFragment.class, 
					  null);
	    //3
		tabHost.addTab(tabHost.newTabSpec("Facebook")
				   			  .setIndicator("Facebook"), 
					  FacebookFragment.class, 
				      null);
	    //4
		tabHost.addTab(tabHost.newTabSpec("Twitter")
			   				  .setIndicator("Twitter"), 
					  TwitterFragment.class, 
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
                startActivityForResult(myIntent,0);
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
        
        // Facebook Login setting
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        showFacebookLoginDialog();
        
    }
   
    
    private boolean getUserInfo(String name, String pwd) {
    	// User information and preference
    	try {
			String accountData = new DBConnector()
			.execute("SELECT * FROM userlist WHERE Username =" + "'" + name + "'"
					+ "AND Password =" + "'" + pwd + "'")
			.get();
			JSONArray jsonArray = new JSONArray(accountData);
			if(jsonArray.length() != 0)
			{
				int    id 	      = jsonArray.getJSONObject(0).getInt("ID");
				String username   = jsonArray.getJSONObject(0).getString("Username");
				String password   = jsonArray.getJSONObject(0).getString("Password");
				int    preference = jsonArray.getJSONObject(0).getInt("Preference");
				Account.updateAccount(this, id, username, password, preference);
				return true;
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
    	return false;
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
			String myEventData = new DBConnector().execute("SELECT * FROM user_act WHERE UserID =" + Account.getInstance().getUserID()).get();
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
    
    private void showLoginDialog(){
    	LayoutInflater layoutInflater = this.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.dialog_login, null) ;
		((TextView) inflater.findViewById(R.id.username_text)).setText("使用者名稱");
		((TextView) inflater.findViewById(R.id.password_text)).setText("使用者密碼");
    	AlertDialog loginDialog = new AlertDialog.Builder(this)
    	.setTitle("NTUAct")
    	.setView(inflater)
        .setPositiveButton(R.string.log_in, new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int whichButton) {
				String username = ((EditText) inflater.findViewById(R.id.username_edit)).getText().toString();
				String password = ((EditText) inflater.findViewById(R.id.password_edit)).getText().toString();
        		if(getUserInfo(username, password)){
        			getEventInfo();
        			getUserEvent();
        		}else{
            		Toast.makeText(getApplicationContext(), "請重新輸入", Toast.LENGTH_SHORT).show();
            		showLoginDialog();
        		}
            }
        })
        .setNeutralButton(R.string.sign_up, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String username = ((EditText) inflater.findViewById(R.id.username_edit)).getText().toString();
				String password = ((EditText) inflater.findViewById(R.id.password_edit)).getText().toString();
				if(!checkAccountExsistence(username)){
					registerAccount(username, password);
					getEventInfo();
					getUserInfo(username, password);
				}else{
					Toast.makeText(getApplicationContext(), "名稱已經有人使用", Toast.LENGTH_SHORT).show();
					showLoginDialog();
				}
			}
        })
        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int whichButton) {
            	System.exit(0);
            }
        })
        .create();
		loginDialog.setCanceledOnTouchOutside(false);
		loginDialog.show();
    }
    
	public String getAppleData(){
		return "Apple 123";
	}

	public String getGoogleData(){
		return "Google 456";
	}
	
	public String getFacebookData(){
		return "Facebook 789";
	}
	
	public String getTwitterData(){
		return "Twitter abc";
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == 0) {
	        // Make sure the request was successful
	    	EventDialog.setUpEventDialog(this);
	        getUserEvent();
	        FacebookFragment eventFragment = (FacebookFragment)getSupportFragmentManager().findFragmentByTag("Facebook");
	        if(eventFragment != null) eventFragment.updateEventList();
	    }else if (requestCode == 1){
	    	AppleFragment eventFragment = (AppleFragment)getSupportFragmentManager().findFragmentByTag("Apple");
	        if(eventFragment != null) eventFragment.updateHotEvent();
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
    
    
    private void showFacebookLoginDialog(){
    	LayoutInflater layoutInflater = this.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.facebook_layout, null) ;
		loginButton = (LoginButton) inflater.findViewById(R.id.login_button);
	    loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                MainActivity.this.user = user;
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                handlePendingAction();
                if(MainActivity.this.user != null){
                	
                }
            }
	    });
    	AlertDialog loginDialog = new AlertDialog.Builder(this)
    	.setTitle("NTUAct")
    	.setView(inflater)
        .setPositiveButton(R.string.log_in, new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int whichButton) {
				
            }
        })
        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int whichButton) {
            	System.exit(0);
            }
        })
        .create();
		loginDialog.setCanceledOnTouchOutside(false);
		loginDialog.show();
    }
}
