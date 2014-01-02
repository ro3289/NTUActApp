package com.example.eventmap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        findViews();
        setListeners();
        
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
        
        getActivitiesRecords();
    }
    
    private Button button_get_record;
    
    private void findViews() {
        button_get_record = (Button)findViewById(R.id.get_record);
    }
    
    private void setListeners() {
        button_get_record.setOnClickListener(getDBRecord);
    }
    
    private Button.OnClickListener getDBRecord = new Button.OnClickListener() {
        public void onClick(View v) {
//            getActivitiesRecords();
            Intent myIntent=new Intent(v.getContext(), MapActivity.class);
            startActivity(myIntent);
            finish();
        }
    };

    private void getActivitiesRecords() {
        // TODO Auto-generated method stub
        System.out.println("herehere");
        TableLayout user_list = (TableLayout)findViewById(R.id.user_list);
        user_list.setStretchAllColumns(true);
        TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams view_layout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        try {
            String result = DBConnector.executeQuery("SELECT * FROM activity");
            System.out.println(result);
            /*
                SQL 結果有多筆資料時使用JSONArray
                只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
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
    }
}
