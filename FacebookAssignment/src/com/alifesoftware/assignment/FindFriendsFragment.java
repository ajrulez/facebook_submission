package com.alifesoftware.assignment;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alifesoftware.assignment.model.FriendUserData;
import com.alifesoftware.assignment.utils.NetworkUtils;

/**
 * This class is used to implement a Fragment that displays
 * user's Friend List
 * 
 * This Fragment class extends BaseHeadlessFragment for some common
 * functionality
 * 
 */
public class FindFriendsFragment extends BaseHeadlessFragment {	
	// List View for showing Friends List
	private ListView lvFriendsList;
	
	// Friends ListView Adapter
	private FriendsListAdapter friendsAdapter;
	
	// Key for getting FriendUserData object from Bundle
	public static final String FRIEND_USER_DATA_KEY = "friend_user_data_key";
	
	// Default Constructor
	public FindFriendsFragment() {
		tagFragmentName = "FindFriendsFragment";
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
		RelativeLayout fragmentLayout = (RelativeLayout) inflater.inflate(R.layout.frends_list_fragment_layout, container, false);
		
		// Set the ListView
		lvFriendsList = (ListView) fragmentLayout.findViewById(R.id.friendsListView);
		
		if(friendsAdapter == null) {
			friendsAdapter = new FriendsListAdapter(getActivity(), R.layout.friends_list_item_layout);
			lvFriendsList.setAdapter(friendsAdapter);
		}
		
		return fragmentLayout;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

		// If this Fragment is attached
		if (fragmentAttached.get() &&
				getArguments() != null) {
			
			// Get the friend data from Bundle
			List<FriendUserData> friendData = getArguments().getParcelableArrayList(FRIEND_USER_DATA_KEY);
			
			if (friendData != null && friendData.size() > 0) {
				lvFriendsList.setVisibility(View.VISIBLE);

				// Update the data
				friendsAdapter.updateData(friendData);
			}

			else {
				lvFriendsList.setVisibility(View.INVISIBLE);
				
				String errMsg = "Cannot display friends list because there is no FriendUserData in bundle";
				Log.e(tagFragmentName, errMsg);
				if(uiUpdater != null) {
					uiUpdater.showAlert("Error", errMsg);
				}
			}
		}
		
		else {
			String errMsg = "Cannot show friend's list because either the Fragment is not attached or the bundle is null";
			Log.e(tagFragmentName, errMsg);
			
			if(uiUpdater != null) {
				uiUpdater.showAlert("Error", errMsg);
			}
		}
	}
	
	/**
	 * View Holder Class to be used
	 * in FriendsListAdapter Adapter
	 *
	 */
	static class ViewHolder {
		// Name
		TextView tvName;
		
		// ImageView for Profile Picture
		ImageView profilePictureView;
	}
	
	/**
	 * FriendsListAdapter class extends ArrayAdapter to support custom
	 * list view that displays FriendUserData objects
	 *
	 */
	private class FriendsListAdapter extends ArrayAdapter<FriendUserData> {
		// Data - ArrayList<Education>
		private List<FriendUserData> arrFriendsData;

		
		// Default Constructor
		public FriendsListAdapter(Context context, int resource) {
				super(context, resource);
		}
		
		
		// Update Friends Data
		public synchronized void updateData(final List<FriendUserData> data) {
			Log.i(tagFragmentName, "Updating friends list");
			
			try {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						arrFriendsData = data;
						notifyDataSetChanged();
					}
				});
			}
			
			catch(Exception e) {
				Log.e(tagFragmentName, e.getMessage());
			}
		}
		
		
		// getView
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.friends_list_item_layout, null);
                
                // Create a ViewHolder and store references to the children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.textViewFriendsName);
                holder.profilePictureView = (ImageView) convertView.findViewById(R.id.friendsPictureThumbnailView);
                
                convertView.setTag(holder);
                
                convertView.setOnClickListener(new OnClickListener () {
                	@Override
                	public void onClick(View v) {
            			// Check for Network
            			if(! NetworkUtils.isOnline(getActivity())) {
            				String msg = "Network not available. Cannot process your request to view Friend's Display Picture.";
            				Log.w(tagFragmentName, msg);
            				uiUpdater.showAlert("Network Error", msg);
            			}

						// Launch Friends Profile Picture Activity
						//
						//
						// Get the position of this view in the list to get
						// position of data in the array
						int pos = lvFriendsList.getPositionForView(v);
						
						// Get the URL of Friends Profile Picture
						if(pos < arrFriendsData.size()) {
							String pictureUrl = arrFriendsData.get(pos).getDisplayPictureUrl();
						
							if(pictureUrl != null && 
									pictureUrl.length() > 0 &&
									uiUpdater != null) {
								uiUpdater.showFriendDisplayImage(pictureUrl);
							}
						}
					}
				});
            } 
			
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(arrFriendsData != null && 
					arrFriendsData.size() > 0 &&
					position < arrFriendsData.size()) {
				// Bind the data efficiently with the holder.
				holder.tvName.setText(arrFriendsData.get(position).getName());
				
				// Load the profile bitmap in InmageView
				try {
					Bitmap bmp = FriendUserData.getBitmapCache().get(arrFriendsData.get(position).getId());
					holder.profilePictureView.setImageBitmap(bmp);
				}
				
				catch(Exception e) {
					Log.w(tagFragmentName, "Something went wrong when updating thumbnail image");
				}
			}
           
			return convertView;                   
        }
		
		public int getCount() {
			int nSize = 0;
			
			if(arrFriendsData != null) {
				nSize = arrFriendsData.size();
			}
			
			return nSize;
		}
	}
}
