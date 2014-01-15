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

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ListItemAdapter myListAdapter;
	// Load events and images from Account
	private String[] myEventName;
	private String[] myEventContent;
    private String[] myEventImage;
	
	public class ListItemAdapter extends ArrayAdapter<String> {
		
	  private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	  Context myContext;
	
	  public ListItemAdapter(Context context, int textViewResourceId, String[] objects) {
		  super(context, textViewResourceId, objects);
		  myContext = context;
	  }
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		   
		   LayoutInflater inflater = (LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View row = inflater.inflate(R.layout.listview_my_event, parent, false);
		   
		   TextView eventName = (TextView)row.findViewById(R.id.event_name);
		   eventName.setText(myEventName[position]);
		   
		   TextView eventContent = (TextView)row.findViewById(R.id.event_content);
		   eventContent.setText(myEventContent[position]);
		   
		   ImageView image=(ImageView)row.findViewById(R.id.image);
		   imageLoader.displayImage(myEventImage[position], image, options, animateFirstListener);
		   
		   return row;
		  }
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("=====>", "FacebookFragment onAttach");
		myEventName		= Account.getInstance().getEventNameStringArray();
		myEventContent 	= Account.getInstance().getEventContentStringArray();
		myEventImage 	= Account.getInstance().getEventImageStringArray();
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
		myListAdapter = new ListItemAdapter(getActivity(), R.layout.listview_my_event, myEventName);
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
		myEventName		= Account.getInstance().getEventNameStringArray();
		myEventContent 	= Account.getInstance().getEventContentStringArray();
		myEventImage 	= Account.getInstance().getEventImageStringArray();
		myListAdapter = new ListItemAdapter(getActivity(), R.layout.listview_my_event, myEventName);
		setListAdapter(myListAdapter);
	}
	
}
