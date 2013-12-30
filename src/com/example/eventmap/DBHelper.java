package com.example.eventmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
	private final static int DBVersion = 1; 
	private final static String DBName = "Event.db";  
	private final static String tableName = "EventInfo"; 


	public DBHelper(Context context) {
		super(context, DBName, null, DBVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String SQL = "CREATE TABLE IF NOT EXISTS " + 
							tableName + 
							"( " +
							"EventID INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"Name VARCHAR(50), " +
							"_CONTENT TEXT," +
							"_KIND VARCHAR(10)" +
							");";
		db.execSQL(SQL);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			final String SQL = "DROP TABLE " + tableName;
			db.execSQL(SQL);       
	}

}
