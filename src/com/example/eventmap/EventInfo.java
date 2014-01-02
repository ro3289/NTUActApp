package com.example.eventmap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;

import com.google.android.gms.maps.model.Marker;

public class EventInfo {
	
	private int 	id;
	private String  name;
	private String  location;
	private String 	url;
	private String  content;
	private Calendar cal;
	private ArrayList<Integer> tagList;

	public EventInfo (int y, int m, int d)
	{
		cal = Calendar.getInstance();
		cal.set(y, m-1, d, 18, 30);
		tagList = new ArrayList<Integer>();
	}
	
	public EventInfo(int ID, String Name, String Location, String u, String Content, Date date)
	{
		id = ID;
		name = Name;
		location = Location;
		url = u;
		content = Content;
		cal = Calendar.getInstance();
		cal.setTime(date);
		tagList = new ArrayList<Integer>();
	}
	public EventInfo id(int i)
	{
		id = i;
		return this;
	};
	
	public EventInfo url(String u)
	{
		url = u;
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
