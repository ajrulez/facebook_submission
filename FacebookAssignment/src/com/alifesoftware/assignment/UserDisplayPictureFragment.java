package com.alifesoftware.assignment;

import com.alifesoftware.assignment.interfaces.IBitmapReceiver;
import com.alifesoftware.assignment.tasks.UrlToBitmapTask;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * This class implements the fragment to show
 * display picture of selected friend
 * 
 * This class implements IBitmapReceiver interface
 * to receive a callback when display picture URL is
 * converted to a Bitmap object.
 * 
 */
public class UserDisplayPictureFragment extends BaseHeadlessFragment 
										implements IBitmapReceiver {
	
	/**
	 * Note: I am using fitCenter in ImageView (in the Layout XML)
	 * because I want to maintain the aspect ratio of the app. 
	 * 
	 * I can show the image Full Screen - by replacing fitCenter
	 * with fitXY - but then the image does not look very nice as
	 * it loses the aspect ratio. 
	 * 
	 */
	
	// ImageView to display user's display pic
	private ImageView displayPicView;
	
	// Selected Friend's display pic URL
	private String selectedFriendDisplayUrl;
	
	// Key for getting friend's display pic URL from Bundle
	public static final String SELECTED_FRIEND_DISPLAY_URL_KEY = "selected_friend_display_url_key";
	
	
	// Default Constructor
	public UserDisplayPictureFragment() {
		tagFragmentName = "UserDisplayPictureFragment";
		selectedFriendDisplayUrl = "";
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.alifesoftware.assignment.BaseHeadlessFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, 
								ViewGroup container, Bundle savedInstanceState) {
		// Inflate the Fragment layout
		LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(R.layout.user_display_picture_layout, container, false);
		
		// Set the ImageVive
		displayPicView = (ImageView) fragmentLayout.findViewById(R.id.imageViewDisplayPic);
		displayPicView.setVisibility(View.INVISIBLE);
		
		return fragmentLayout;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(tagFragmentName, "onResume called. Get the selected friend's display pic URL from Bundle");
		
		if(fragmentAttached.get() && 
				getArguments() != null) {
			selectedFriendDisplayUrl = getArguments().getString(SELECTED_FRIEND_DISPLAY_URL_KEY);
			
			if(selectedFriendDisplayUrl != null &&
					selectedFriendDisplayUrl.length() > 0) {
				Log.d(tagFragmentName, "Selected friend's display picture URL is non-null. Proceed with UI update");
				
				UrlToBitmapTask task = new UrlToBitmapTask(getActivity(), this);
				String[] params = {selectedFriendDisplayUrl};
				task.execute(params);
			}
			
			else {
				String errMsg = "Unable to retrieve selected friend's display picture URL. Cannot update the view";
				Log.e(tagFragmentName, errMsg);
				
				if(uiUpdater != null) {
					uiUpdater.showAlert("Error", errMsg);
				}
			}
		}
		
		else {
			Log.e(tagFragmentName, "Fragment is not attached to the activity or the Fragment Bundle is null. Cannot show user's display picture");
			if(uiUpdater != null) {
				uiUpdater.showAlert("Error", "Cannot display friend's profile picutre because the fragment is not attached or fragment bundle is null");
			}
			
			return;
		}
	}

	/**
	 * Callback that is invoked when selected friend's display
	 * picture URL is converted to a Bitmap file
	 *  
	 *  @param bitmap - Converted Bitmap object
	 */
	@Override
	public void onConversionComplete(Bitmap bitmap) {
		Log.d(tagFragmentName, "onConversionComplete - Bitmap conversion callback invoked");
		
		if(bitmap != null) {
			displayPicView.setImageBitmap(bitmap);
			displayPicView.setVisibility(View.VISIBLE);
		}
		
		else {
			String errMessage = "Unable to display selected friend's display picture because we do not have a valid Bitmap file";
			Log.e(tagFragmentName, errMessage);
			
			if(uiUpdater != null) {
				uiUpdater.showAlert("Error", errMessage);
			}
		}
	}

}
