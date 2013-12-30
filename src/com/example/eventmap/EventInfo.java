package com.example.eventmap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;

import com.google.android.gms.maps.model.Marker;

public class EventInfo {
	
	private String 	ID;
	private String 	URL;
	private Date 	date;
	private Time 	time;
	private Calendar cal;
	private ArrayList<String> tag;

	public EventInfo (int y, int m, int d)
	{
		cal = Calendar.getInstance();
		cal.set(y, m, d, 18, 30);
	}
	public EventInfo id(String id)
	{
		ID = id;
		return this;
	};
	
	public EventInfo url(String url)
	{
		URL = url;
		return this;
	}

	public EventInfo date(Date d)
	{
		date = d;
		return this;
	}
	
	public EventInfo time(Time t)
	{
		time = t;
		return this;
	}
	
	public EventInfo tag()
	{
		// TODO
		return null;
	}
	
	public Date getDate() { return cal.getTime();}
	public Time getTime() { return time;}
	public Calendar getCal() { return cal;}
	
}
