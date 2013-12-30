package com.example.eventmap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;

import com.google.android.gms.maps.model.Marker;

public class EventInfo {
	
	private String 	ID;
	private String 	URL;
	private Calendar cal;
	private ArrayList<Integer> tagList;

	public EventInfo (int y, int m, int d)
	{
		cal = Calendar.getInstance();
		cal.set(y, m-1, d, 18, 30);
		tagList = new ArrayList<Integer>();
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
	
	public EventInfo addTag(int i)
	{
		tagList.add(i);
		return this;
	}
	
	public Date getDate() { return cal.getTime();}
	public Calendar getCal() { return cal;}
	public ArrayList<Integer> getTagList() { return tagList; }
	
	
}
