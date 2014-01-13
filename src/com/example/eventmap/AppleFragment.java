package com.example.eventmap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import com.example.pageviewitem.ViewPagerItem;;

public class AppleFragment extends Fragment {

	ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	GridView gridview1;
	GridView gridview2;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("=====>", "AppleFragment onAttach");
		//myActivity=activity;
		//MainActivity mainActivity = (MainActivity)activity;
		//value = mainActivity.getAppleData();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("=====>", "AppleFragment onCreateView");
		View rootView = inflater.inflate(R.layout.frg_apple, container, false);
		setRetainInstance(true);

		// ImageLoader configuration
        options = new DisplayImageOptions.Builder()
        	.showStubImage(R.drawable.ic_stub)
        	.showImageForEmptyUri(R.drawable.ic_empty)
        	.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory()
			.cacheOnDisc()
			.build();
     // gridview configuration
        gridview1 = (GridView) rootView.findViewById(R.id.gridView1);
        gridview1.setColumnWidth(GridView.AUTO_FIT);
        //gridview2 = (GridView) rootView.findViewById(R.id.gridView1);
     	
        
		return rootView;
	}

	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("=====>", "AppleFragment onActivityCreated");
		//imageView=((MainActivity) myActivity).setImage();
		//TextView txtResult = (TextView) this.getView().findViewById(R.id.textView1);
		//txtResult.setText(value);
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext())
    	.defaultDisplayImageOptions(options)
    	.build();
		imageLoader.init(config);
		gridview1.setAdapter(new ItemAdapter(getActivity()));
     	gridview1.setOnItemClickListener(new OnItemClickListener(){
	     	@Override
	     	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	     		// TODO
	     	}
     	});
	}
	// gridview Adapter
	class ItemAdapter extends BaseAdapter {

		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
		private Context mContext;
	    public ItemAdapter(Context ctx){
	        mContext=ctx;
	    }
		private class ViewHolder {
			public ImageView image;
			public ImageView image2;
		}

		@Override
		public int getCount() {
			return (IMAGES2.length+1)/2;
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
		        type = (position>0)?1:0;
		    } catch (Exception e) {  
		        e.printStackTrace();  
		    }  
		    System.out.println("getItemViewType::" + position + " is " + type);  
		    return type;  
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			System.out.println(position);
			View view = convertView;
			final ViewHolder holder;
			
			if (convertView == null) {
				//if(position==0)
				holder = new ViewHolder();
				if(getItemViewType(position)==0)
				{
					view =  LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
					holder.image = (ImageView) view.findViewById(R.id.image);	
					holder.image.setOnClickListener(new OnClickListener(){
						@Override
					    public void onClick(View v) {
							Intent myIntent=new Intent(v.getContext(), ViewPagerItem.class);
			                startActivityForResult(myIntent,0);
					    }
					});
				}
				else
				{
					view =  LayoutInflater.from(mContext).inflate(R.layout.list_item2, parent, false);
					holder.image = (ImageView) view.findViewById(R.id.image2);
					holder.image2 = (ImageView) view.findViewById(R.id.image3);
				}
				
				/*else
				view =  LayoutInflater.from(mContext).inflate(R.layout.list_item2, parent, false);*/
					
				
				//holder.text = (TextView) view.findViewById(R.id.text);
				//if(position==0)
					
				/*else
				{System.out.println(position+":)");
				
				}*/
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			//holder.text.setText("�o�O�� "+ (position+1) +" ��");
			/*if(number==1)
				imageLoader.displayImage(IMAGES1[position], holder.image, options, animateFirstListener);
			else*/
			if(getItemViewType(position)==0)
			{
			imageLoader.displayImage(IMAGES2[position], holder.image, options, animateFirstListener);
			}
			else
			{System.out.println(position+":)");
				imageLoader.displayImage(IMAGES2[(position-1)*2+1], holder.image, options, animateFirstListener);
				imageLoader.displayImage(IMAGES2[(position-1)*2+2], holder.image2, options, animateFirstListener);
			}

			return view;
		}
	}

	// �Ϥ���ܰʵe
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
    
    public static final String[] IMAGES2 = new String[] {
		// �j�Ϥ���
		"http://140.112.18.223/activity2.jpg",
		"http://140.112.18.223/activity3.jpg",
		"http://140.112.18.223/activity1.jpg",
		"http://140.112.18.223/activity4.jpg",
		"http://140.112.18.223/activity5.gif",
		"http://140.112.18.223/activity6.jpg",
		"http://140.112.18.223/activity6.jpg",
		// �p�Ϥ���
		
	};
}
