package com.example.eventmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.eventdialog.EventDialog;

public class MainActivity extends FragmentActivity {

	private Account me;
	public ArrayList<EventInfo> eventList = new ArrayList<EventInfo>();
	
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
        this.getEventInfo();
        this.getUserInfo();

        Button mapActivity = (Button)findViewById(R.id.map_activity);
        mapActivity.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent=new Intent(v.getContext(), MapActivity.class);
                startActivity(myIntent);
                finish();
            }
        });
        
        // Test for dialog
        Button infoDialog = (Button) findViewById(R.id.get_info);
        infoDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// me.showMyPreference();
				me.showMyEvent();
			}
		});
    }
   
    
    private void getUserInfo() {
    	// User information and preference
    	try {
			String accountData = new DBConnector().execute("SELECT * FROM userlist WHERE ID = 0" ).get();
			JSONArray jsonArray = new JSONArray(accountData);
			if(jsonArray.length() != 0)
			{
				int    id 	      = jsonArray.getJSONObject(0).getInt("ID");
				String username   = jsonArray.getJSONObject(0).getString("Username");
				String password   = jsonArray.getJSONObject(0).getString("Password");
				int    preference = jsonArray.getJSONObject(0).getInt("Preference");
				me = new Account(this, id, username, password, preference);
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
    	// Tracing events
    	try {
			String myEventData = new DBConnector().execute("SELECT * FROM user_act WHERE UserID = 0").get();
			JSONArray jsonArray = new JSONArray(myEventData);
			for(int index = 0; index < jsonArray.length(); ++index)
			{
				int eventID = jsonArray.getJSONObject(index).getInt("ActID");
				me.addMyEvent(eventList.get(eventID));
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
				String url 		= jsonArray.getJSONObject(index).getString("url");
				String content 	= jsonArray.getJSONObject(index).getString("Content");
				String date 	= jsonArray.getJSONObject(index).getString("Time");
				int    tag 	    = jsonArray.getJSONObject(index).getInt("Tag");
				try {
					eventList.add(ID, new EventInfo(ID, name, location, url, content, sdf.parse(date), tag));
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
  /*  
    private void getActivitiesRecords() {

        TableLayout user_list = (TableLayout)findViewById(R.id.user_list);
        user_list.setStretchAllColumns(true);
        TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams view_layout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        try {
            String result = new DBConnector().execute("SELECT * FROM activity").get();
            System.out.println(result);
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                        只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
    /*
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                TableRow tr = new TableRow(MainActivity.this);
                tr.setLayoutParams(row_layout);
                tr.setGravity(Gravity.CENTER_HORIZONTAL);
                
                TextView user_acc = new TextView(MainActivity.this);
                user_acc.setText(jsonData.getString("Name"));
                user_acc.setLayoutParams(view_layout);
                
                TextView user_pwd = new TextView(MainActivity.this);
                user_pwd.setText(jsonData.getString("Time"));
                user_pwd.setLayoutParams(view_layout);
                
                tr.addView(user_acc);
                tr.addView(user_pwd);
                user_list.addView(tr);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        }
    }*/
    
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
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	EventDialog.getInstance().setUpEventDialog(this);
    }
}
