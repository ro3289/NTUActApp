package com.example.eventmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

public class MainActivity extends FragmentActivity {

	public static HashMap<Integer,EventInfo> eventList = new HashMap<Integer, EventInfo>();
	
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
		tabHost.addTab(tabHost.newTabSpec("我的活動")
				   			  .setIndicator("我的活動"), 
					  FacebookFragment.class, 
				      null);
	    //4
		tabHost.addTab(tabHost.newTabSpec("帳號管理")
			   				  .setIndicator("帳號管理"), 
					  TwitterFragment.class, 
					  null);
		//4
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
        this.showLoginDialog();
        

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
	        FacebookFragment eventFragment = (FacebookFragment)getSupportFragmentManager().findFragmentByTag("最新活動");
	        if(eventFragment != null) eventFragment.updateEventList();
	    }else if (requestCode == 1){
	    	AppleFragment eventFragment = (AppleFragment)getSupportFragmentManager().findFragmentByTag("熱門活動");
	        if(eventFragment != null) eventFragment.updateHotEvent();
	    }
	}
}
