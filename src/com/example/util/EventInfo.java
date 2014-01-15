package com.example.util;

import java.util.Calendar;
import java.util.Date;

public class EventInfo {
	
	public int 		id;
	public String  	name;
	public String  	snippet;
	public String  	location;
	public String 	url;
	public String 	image;
	public String  	content;
	private Calendar cal;
	public int 	tagValue;
	
	public EventInfo(int ID, String Name, String Location, String u, String i, String Content, Date date, int tag)
	{
		id = ID;
		name = Name;
		location = Location;
		url = u;
		image = i;
		content = Content;
		cal = Calendar.getInstance();
		cal.setTime(date);
		tagValue = tag;
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
	
	public Date getDate() { return cal.getTime();}
	public boolean before(Calendar when){ return cal.before(when);}
	public boolean after(Calendar when){ return cal.after(when);}
	public int getTagValue() { return tagValue; }
}
