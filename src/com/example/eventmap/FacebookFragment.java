package com.example.eventmap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.util.Account;
import com.example.util.EventDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class FacebookFragment extends ListFragment {

	private MainActivity mainActivity;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private MyListAdapter myListAdapter;
	private String[] myEvent;
    public static final String[] IMAGES = new String[] {
		// 大圖片們
		"http://140.112.18.223/activity1.jpg"
		//"http://140.112.18.223/liver_baby.png",
		// 小圖片們
		
	};
	
	public class MyListAdapter extends ArrayAdapter<String> {
		
	  private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	  Context myContext;
	
	  public MyListAdapter(Context context, int textViewResourceId, String[] objects) {
		  super(context, textViewResourceId, objects);
		  myContext = context;
	  }
	
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	   //return super.getView(position, convertView, parent);
	   
	   LayoutInflater inflater = (LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	   View row = inflater.inflate(R.layout.listview_preference, parent, false);
	   
	   TextView eventName=(TextView)row.findViewById(R.id.event_name);
	   eventName.setText(myEvent[position]);
	   
	   ImageView image=(ImageView)row.findViewById(R.id.image);
	   imageLoader.displayImage(IMAGES[position], image, options, animateFirstListener);
	   
	   return row;
  }
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("=====>", "FacebookFragment onAttach");
		mainActivity = (MainActivity)activity;
		myEvent = Account.getInstance().getEventNameStringArray();
		// Set up image display options
		options = new DisplayImageOptions.Builder()
    	.showStubImage(R.drawable.ic_stub)
    	.showImageForEmptyUri(R.drawable.ic_empty)
    	.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory()
		.cacheOnDisc()
		.build();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("=====>", "FacebookFragment onCreateView");
		return inflater.inflate(R.layout.frg_facebook, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("=====>", "FacebookFragment onActivityCreated");
		/*
		listAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.activity_list_item,month);
		setListAdapter(listAdapter);
		*/
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext())
    	.defaultDisplayImageOptions(options)
    	.build();
		imageLoader.init(config);
		myListAdapter = new MyListAdapter(getActivity(), R.layout.listview_preference, myEvent);
		setListAdapter(myListAdapter);
	}
	
	@Override
	 public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		EventDialog.getInstance().showEventInfoDialog(Account.getInstance().getEvent(position), this);
	 }
	
	// 圖片顯示動畫
		private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

			static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				if (loadedImage != null) {
					ImageView imageView = (ImageView) view;
					boolean firstDisplay = !displayedImages.contains(imageUri);
					if (firstDisplay) {
						FadeInBitmapDisplayer.animate(imageView, 500);
					} else {
						imageView.setImageBitmap(loadedImage);
					}
					displayedImages.add(imageUri);
				}
			}
		}
	
	public void updateEventList(){
		myEvent = Account.getInstance().getEventNameStringArray();
		myListAdapter = new MyListAdapter(getActivity(), R.layout.listview_preference, myEvent);
		setListAdapter(myListAdapter);
	}
	
}
