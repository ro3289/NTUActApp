package com.example.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventmap.FacebookFragment;
import com.example.eventmap.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class EventDialog {
	
	private static final EventDialog Instance = new EventDialog();
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private DisplayImageOptions options;
	public Activity activity;
	
	public static void setUpEventDialog(Activity a){
		getInstance().activity = a;
		
	}
	
	public void showEventInfoDialog(final EventInfo event, final FacebookFragment eventListFragment) {
		
		LayoutInflater layoutInflater = activity.getLayoutInflater();
		final View inflater = layoutInflater.inflate(R.layout.event_dialog, null) ;
		((TextView) inflater.findViewById(R.id.event_name)).setText("活動名稱： " + event.name);
		((TextView) inflater.findViewById(R.id.event_content)).setText("活動敘述： " + event.content);
		ImageView image = (ImageView) inflater.findViewById(R.id.dialog_image);
		
		options = new DisplayImageOptions.Builder()
    	.showStubImage(R.drawable.ic_stub)
    	.showImageForEmptyUri(R.drawable.ic_empty)
    	.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory()
		.cacheOnDisc()
		.build();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity.getApplicationContext())
    	.defaultDisplayImageOptions(options)
    	.build();
		imageLoader.init(config);

		imageLoader.displayImage(event.image, image, options, animateFirstListener);
		
		// Set track event button
		final Button trackEventButton = (Button) inflater.findViewById(R.id.track_event_button);
		if(Account.getInstance().containEvent(event)){
			trackEventButton.setText("追蹤中");
		}else{
			trackEventButton.setText("追蹤活動");
		}
		trackEventButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(trackEventButton.getText().toString().equals("追蹤活動")){
					trackEventButton.setText("追蹤中");
					Account.getInstance().addMyEvent(event);
					new DBConnector().execute("INSERT INTO user_act (UserID, ActID) VALUES (" + Account.getInstance().getUserID() + ", " + event.id + ")");
				}else{
					trackEventButton.setText("追蹤活動");
					Account.getInstance().deleteMyEvent(event);
					new DBConnector().execute("DELETE FROM user_act WHERE UserID = " + Account.getInstance().getUserID() + " AND ActID = " + event.id);
				}
			}
		});
    	new AlertDialog.Builder(activity)
        .setView(inflater)
        .setPositiveButton(R.string.visit,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	
                }
            }
        )
        .setNegativeButton(R.string.exit,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	if(eventListFragment != null){
                		eventListFragment.updateEventList();
                	}
                }
            }
        )
        .show();
    }
	
	public static EventDialog getInstance(){
		return Instance;
	}
	
	
	// Image loader method
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
	
}
