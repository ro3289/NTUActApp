package com.example.eventmap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventInfo {
	
	public int 	id;
	public String  name;
	public String  snippet;
	public String  location;
	public String 	url;
	public String  content;
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
	public boolean before(Calendar when){ return cal.before(when);}
	public boolean after(Calendar when){ return cal.after(when);}
	public ArrayList<Integer> getTagList() { return tagList; }
}
