package com.example.eventmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import com.example.pageviewitem.ViewPagerItem;
import com.example.util.Account;
import com.example.util.DBConnector;
import com.example.util.EventInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

//my preference
public class TwitterFragment extends Fragment {

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private GridView gridview1;
	private ArrayList<String> imageSourceList = new ArrayList<String>();
	private String[] imageSource;
	private int layerCount;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("=====>", "TwitterFragment onAttach");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("=====>", "TwitterFragment onCreateView");
		setRetainInstance(true);
		View rootView = inflater.inflate(R.layout.frg_twitter, container, false);
		// ImageLoader configuration
        options = new DisplayImageOptions.Builder()
        	.showStubImage(R.drawable.ic_stub)
        	.showImageForEmptyUri(R.drawable.ic_empty)
        	.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory()
			.cacheOnDisc()
			.build();
        // GridView configuration
        gridview1 = (GridView) rootView.findViewById(R.id.gridView1);
        gridview1.setColumnWidth(GridView.AUTO_FIT);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("=====>", "TwitterFragment onActivityCreated");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext())
    	.defaultDisplayImageOptions(options)
    	.build();
		imageLoader.init(config);
		updatePreferenceEvent();
	}
	
	////////////////////////////////
	/////////// Method /////////////
	////////////////////////////////
	
	// GridView Adapter
		class ItemAdapter extends BaseAdapter {

			private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
			private Context mContext;
		    public ItemAdapter(Context ctx){
		        mContext=ctx;
		    }
			private class ViewHolder {
				public ImageView image1;
				public ImageView image2;
			}

			@Override
			public int getCount() {
				return layerCount;
			}

			@Override
			public Object getItem(int position) {
				return position;
			}
			
			@Override
			public int getViewTypeCount() {
				return 2;
				
			}
			
			public int getItemViewType(int position) {  
			    int type = super.getItemViewType(position);  
			    try {  
			    	if(((imageSource.length % 2 == 1) && (position + 1 == imageSource.length))){
			    		type = 0;
			    	}else{
			    		type = 1;
			    	}
			    }catch(Exception e) {  
			        e.printStackTrace();  
			    }  
			    return type;  
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				View view = convertView;
				final ViewHolder holder;
				
				if (convertView == null) {
					holder = new ViewHolder();
					if(getItemViewType(position)==0)
					{
						view =  LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
						holder.image1 = (ImageView) view.findViewById(R.id.image);	
						holder.image1.setOnClickListener(new OnClickListener(){
							@Override
						    public void onClick(View v) {
								Intent myIntent=new Intent(v.getContext(), ViewPagerItem.class);
				                startActivityForResult(myIntent,1);
						    }
						});
					}else {
						view =  LayoutInflater.from(mContext).inflate(R.layout.list_item2, parent, false);
						holder.image1 = (ImageView) view.findViewById(R.id.image2);
						holder.image2 = (ImageView) view.findViewById(R.id.image3);
					}
					view.setTag(holder);
				}else {
					holder = (ViewHolder) view.getTag();
				}

				if(getItemViewType(position)==0)
				{
					imageLoader.displayImage(imageSource[position], holder.image1, options, animateFirstListener);
				}else {
					imageLoader.displayImage(imageSource[position*2], holder.image1, options, animateFirstListener);
					imageLoader.displayImage(imageSource[position*2+1], holder.image2, options, animateFirstListener);
				}

				return view;
			}
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

	 public void updatePreferenceEvent(){
		imageSourceList.clear();
		int myPreference = Account.getInstance().getMyPreference();
		HashMap<Integer,EventInfo> eventList = MainActivity.getEventList();
	
		for(EventInfo event : eventList.values()){
			if(isMyPreference(event.tagValue, myPreference))
			imageSourceList.add(event.image);
		}
		imageSource = imageSourceList.toArray(new String[imageSourceList.size()]);
		layerCount = ((imageSource.length % 2 == 0)? (imageSource.length)/2 : (imageSource.length+1)/2);
		gridview1.setAdapter(new ItemAdapter(getActivity()));
     }
	 
	 private boolean isMyPreference(int tag, int preference){
		 return ((tag & preference) == preference);
	 }
	
}
