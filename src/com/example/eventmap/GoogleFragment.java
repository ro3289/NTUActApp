package com.example.eventmap;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;



import com.example.pageviewitem.ViewPagerItem;
import com.example.util.DBConnector;
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
import android.widget.TextView;


public class GoogleFragment extends Fragment {

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private GridView gridview1;
	private static final int NUMBER_OF_DAY = 5;
	private static final int NUMBER_OF_EVENTS = 3;
	//private ArrayList<String> imageSourceList = new ArrayList<String>();
	private ArrayList<ArrayList<String>> imageDayList=new ArrayList<ArrayList<String>>();
	private ArrayList<String> imageDay=new ArrayList<String>();
	//private int layerCount; 
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("=====>", "AppleFragment onAttach");
		updateHotEvent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Log.d("=====>", "AppleFragment onCreateView");
		View rootView = inflater.inflate(R.layout.frg_google, container, false);
		setRetainInstance(true);
		// ImageLoader configuration
        options = new DisplayImageOptions.Builder()
        	.showStubImage(R.drawable.ic_stub)
        	.showImageForEmptyUri(R.drawable.ic_empty)
        	.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory()
			.cacheOnDisc()
			.build();
        // GridView configuration
        gridview1 = (GridView) rootView.findViewById(R.id.gridView2);
        //gridview1.setColumnWidth(GridView.AUTO_FIT);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext())
    	.defaultDisplayImageOptions(options)
    	.build();
		imageLoader.init(config);
		gridview1.setAdapter(new ItemAdapter(getActivity()));
	}
	// GridView Adapter
	class ItemAdapter extends BaseAdapter {

		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
		private Context mContext;
	    public ItemAdapter(Context ctx){
	        mContext=ctx;
	    }
		private class ViewHolder {
			public TextView text;
			public ImageView image1;
			public ImageView image2;
			public ImageView image3;
		}

		@Override
		public int getCount() {
			return imageDayList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return 3;
			
		}
		
		public int getItemViewType(int position) {  
		    int type = super.getItemViewType(position);  
		    try {  
		    	type=imageDayList.get(position).size()-1;
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
					view =  LayoutInflater.from(mContext).inflate(R.layout.gridview_newevent1, parent, false);
					holder.text = (TextView) view.findViewById(R.id.text);
					holder.image1 = (ImageView) view.findViewById(R.id.image);	
					holder.image1.setOnClickListener(new OnClickListener(){
						@Override
					    public void onClick(View v) {
							
					    }
					});
				}else if(getItemViewType(position)==1){
					view =  LayoutInflater.from(mContext).inflate(R.layout.gridview_newevent2, parent, false);
					holder.text = (TextView) view.findViewById(R.id.text);
					holder.image1 = (ImageView) view.findViewById(R.id.image);
					holder.image2 = (ImageView) view.findViewById(R.id.image2);
				}else{
					view =  LayoutInflater.from(mContext).inflate(R.layout.gridview_newevent3, parent, false);
					holder.text = (TextView) view.findViewById(R.id.text);
					holder.image1 = (ImageView) view.findViewById(R.id.image);
					holder.image2 = (ImageView) view.findViewById(R.id.image2);
					holder.image3 = (ImageView) view.findViewById(R.id.image3);
				}
				
				view.setTag(holder);
			}else {
				holder = (ViewHolder) view.getTag();
			}

			if(getItemViewType(position)==0)
			{
				imageLoader.displayImage(imageDayList.get(position).get(0), holder.image1, options, animateFirstListener);
			}else if(getItemViewType(position)==1){
				imageLoader.displayImage(imageDayList.get(position).get(0), holder.image1, options, animateFirstListener);
				imageLoader.displayImage(imageDayList.get(position).get(1), holder.image2, options, animateFirstListener);
			}else{
				imageLoader.displayImage(imageDayList.get(position).get(0), holder.image1, options, animateFirstListener);
				imageLoader.displayImage(imageDayList.get(position).get(1), holder.image2, options, animateFirstListener);
				imageLoader.displayImage(imageDayList.get(position).get(2), holder.image3, options, animateFirstListener);
			}
			holder.text.setText(imageDay.get(position));
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
	
	public void updateHotEvent(){
    	try {
			String result = new DBConnector().execute("SELECT id, Name, Time FROM "+ DBConnector.table_activity +" WHERE NOW()<Time ORDER BY Time").get();
			System.out.println(result);
			JSONArray jsonArray = new JSONArray(result);
			int upperbound = jsonArray.length();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        
			int index=0;
			int index3=0;
			if(index3!=upperbound)
			{
				ArrayList<String> imageSourceList = new ArrayList<String>();
				int    ID 	 = jsonArray.getJSONObject(index3).getInt("id");
				String imageQuery = new DBConnector().execute("SELECT img FROM " + DBConnector.table_image + " WHERE event_id = " + ID).get();
				JSONArray imageJsonArray = new JSONArray(imageQuery);
				String name  = jsonArray.getJSONObject(index3).getString("Name");
				String image = DBConnector.image_pre_url + imageJsonArray.getJSONObject(0).getString("img");
				String date_ini=jsonArray.getJSONObject(index3).getString("Time");
				date_ini=sdf.format(sdf.parse(date_ini));	
				System.out.println(date_ini);
				//imageSourceList.add(image);
				String date="";
				index3++;
				imageSourceList.add(image);
				int check=0;
			while(index<NUMBER_OF_DAY)
			{
				if(index3==upperbound)
				{
					if(check==0)
					{
					imageDayList.add(imageSourceList);
					imageDay.add(date_ini);
					System.out.println(imageDayList.size());
					System.out.println(imageSourceList.size());
					System.out.println(imageDay.size());
					break;
					}
					else
						break;
				}
				
				for(int index2 = 1; index2 < NUMBER_OF_EVENTS; ++index2){
					if(index3==upperbound)
					{
						imageDayList.add(imageSourceList);
						imageDay.add(date_ini);
						check=1;
						break;
					}
					
					ID 	 = jsonArray.getJSONObject(index3).getInt("id");
					name  = jsonArray.getJSONObject(index3).getString("Name");
					image = DBConnector.image_pre_url + imageJsonArray.getJSONObject(0).getString("img");
					date  = jsonArray.getJSONObject(index3).getString("Time");
					date = sdf.format(sdf.parse(date));
					System.out.println(date);
					index3++;
					if(!date.equals(date_ini)){
						++index;
						imageDay.add(date_ini);
						date_ini=date.toString();
						//index3++;
						imageDayList.add(imageSourceList);

						imageSourceList = new ArrayList<String>();
						imageSourceList.add(image);
						
						break;
					}
					imageSourceList.add(image);
				}
			}
			}
			//imageSource = imageSourceList.toArray(new String[imageSourceList.size()]);
			//layerCount = ((imageSource.length % 2 == 0)? (imageSource.length+2)/2 : (imageSource.length+1)/2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
