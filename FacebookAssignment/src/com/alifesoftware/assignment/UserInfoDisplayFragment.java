package com.alifesoftware.assignment;

import java.util.List;

import com.alifesoftware.assignment.model.LoggedInUserData;
import com.alifesoftware.assignment.model.LoggedInUserData.Education;
import com.alifesoftware.assignment.utils.NetworkUtils;
import com.facebook.widget.ProfilePictureView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class is used to implement a Fragment that displays
 * logged-in user's information
 * 
 * This Fragment class extends BaseHeadlessFragment for some common
 * functionality
 * 
 */
public class UserInfoDisplayFragment extends BaseHeadlessFragment {
	// TextView - Name
	private TextView tvName;
	
	// TextView - Location
	private TextView tvLocation;
	
	// ProfilePictureView
	private ProfilePictureView profilePictureView;
	
	// List View for showing Education Details
	private ListView lvEducationDetails;
	
	// Find Friends Button
	private Button btnFindFriends;
	
	// Send Push Note Button
	private Button btnSendPushNote;
	
	// ImageView for Location
	private ImageView ivLocation;
	
	// Education Details ListView Adapter
	private EducationDetailsListAdapter educationDetailsAdapter;
	
	// Logged-in User Data
	private LoggedInUserData loggedInUserData;
	
	// Key for getting LoggedInUserData object from Bundle
	public static final String LOGGED_IN_USER_DATA_KEY = "logged_in_user_data_key";

		
	// Default Constructor
	public UserInfoDisplayFragment() {
		tagFragmentName = "UserInfoDisplayFragment";
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
		RelativeLayout fragmentLayout = (RelativeLayout) inflater.inflate(R.layout.logged_in_user_layout, container, false);
		
		// Set the UI widgets
		tvName = (TextView) fragmentLayout.findViewById(R.id.textViewNameValue);
		tvLocation = (TextView) fragmentLayout.findViewById(R.id.textViewLocationValue);
		profilePictureView = (ProfilePictureView) fragmentLayout.findViewById(R.id.profilePicture);
		lvEducationDetails = (ListView) fragmentLayout.findViewById(R.id.educationDetailsListView);
		
		// Set the list <-> list adapter
		if(educationDetailsAdapter == null) {
			educationDetailsAdapter = new EducationDetailsListAdapter(getActivity(), R.layout.education_list_item_layout);
			lvEducationDetails.setAdapter(educationDetailsAdapter);
		}
		
		// Find Friends button
		btnFindFriends = (Button) fragmentLayout.findViewById(R.id.findFriendsButton);
		btnFindFriends.setOnClickListener(this);
		
		// Send Push Note button
		btnSendPushNote = (Button) fragmentLayout.findViewById(R.id.sendPushNoteButton);
		btnSendPushNote.setOnClickListener(this);
		
		// Set ImageView for Location
		ivLocation = (ImageView) fragmentLayout.findViewById(R.id.imageViewMapLauncher);
		ivLocation.setImageResource(R.drawable.location_icon);
		ivLocation.setOnClickListener(this);
		
		return fragmentLayout;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		Log.d(tagFragmentName, "onResume - Fill th UI with logged-in user data");
		super.onResume();

		// If this Fragment is attached and we have
		// data in fragment bundle has data
		if (fragmentAttached.get() &&
				getArguments() != null) {
			Log.d(tagFragmentName, "Fragment is attached and fragment has valid bundle");
			
			// Get loggedInUserData from Bundle
			loggedInUserData = getArguments().getParcelable(LOGGED_IN_USER_DATA_KEY);

			if (loggedInUserData != null) {
				Log.d(tagFragmentName, "Successfully retrieved loggedInUserData from Fragment Bundle");
				
				tvName.setText(loggedInUserData.getName());
				tvLocation.setText(loggedInUserData.getLocation());
				profilePictureView.setProfileId(loggedInUserData.getUserId());

				// Do not show education details list view if there are no items
				if (loggedInUserData.getEducation() == null
						|| loggedInUserData.getEducation().isEmpty()) {
					lvEducationDetails.setVisibility(View.INVISIBLE);
				} 
				else {
					lvEducationDetails.setVisibility(View.VISIBLE);

					// Update the data
					educationDetailsAdapter.updateData(loggedInUserData
							.getEducation());
				}
			}

			else {
				String errMsg = "Failed to show logged in user's data because Fragment bundle does not have user data";
				Log.e(tagFragmentName, errMsg);
				if(uiUpdater != null) {
					uiUpdater.showAlert("Error", errMsg);
				}
			}
		}
		
		else {
			String errMsg = "Failed to show logged in user's data because Fragment is not attached or fragment does not have user data";
			Log.e(tagFragmentName, errMsg);
			if(uiUpdater != null) {
				uiUpdater.showAlert("Error", errMsg);
			}
		}
	}

	/**
	 * View Holder Class to be used
	 * in EducationDetailsList Adapter
	 *
	 */
	static class ViewHolder {
		// Type
		TextView tvType;
		
		// SchoolName
		TextView tvSchoolName;
		
		// Year
		TextView tvYear;
		
		// Concentration(s) - Comma-separated Strings, each String representing a concentration
		TextView tvConcentrations;
	}
	
	/**
	 * EducationDetailsListAdapter class extends ArrayAdapter to support custom
	 * list view that displays Education objects
	 *
	 */
	private class EducationDetailsListAdapter extends ArrayAdapter<Education> {
		// Data - ArrayList<Education>
		private List<Education> arrEducationDetails;

		
		// Default Constructor
		public EducationDetailsListAdapter(Context context, int resource) {
				super(context, resource);
		}
		
		
		// Update Education Data
		public synchronized void updateData(final List<Education> data) {
			Log.i(tagFragmentName, "Updating education details list");
			
			try {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						arrEducationDetails = data;
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
                convertView = inflater.inflate(R.layout.education_list_item_layout, null);
                
                // Create a ViewHolder and store references to the children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.tvType = (TextView) convertView.findViewById(R.id.tvEducationType);
                holder.tvType.setTextColor(getColor(R.color.CustomGreen));
                
                holder.tvYear = (TextView) convertView.findViewById(R.id.tvEducationYear);
                holder.tvYear.setTextColor(getColor(R.color.Black));
                
                holder.tvSchoolName = (TextView) convertView.findViewById(R.id.tvSchoolName);
                holder.tvSchoolName.setTextColor(getColor(R.color.DarkRed));
                // We may want to make School Name auto-scroll horizontally
                // as some school names are too long
                holder.tvSchoolName.setSelected(true);
                
                holder.tvConcentrations = (TextView) convertView.findViewById(R.id.tvConcentration);
                holder.tvConcentrations.setTextColor(getColor(R.color.CustomBlue));
					
                convertView.setTag(holder);
            } 
			
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(arrEducationDetails != null && 
					arrEducationDetails.size() > 0 &&
					position < arrEducationDetails.size()) {
				// Bind the data efficiently with the holder.
				holder.tvType.setText(arrEducationDetails.get(position).getType());
				holder.tvYear.setText(arrEducationDetails.get(position).getYear());
				holder.tvSchoolName.setText(arrEducationDetails.get(position).getSchool());
				
				List<String> concentrationList = arrEducationDetails.get(position).getConcentration();
				if(concentrationList != null &&
						concentrationList.isEmpty() == false) {
					if(concentrationList.size() == 1) {
						holder.tvConcentrations.setText(concentrationList.get(0));
					}
					
					// Make a comma-separated String of all concentrations
					else {
						StringBuilder builder = new StringBuilder();
						
						for(int nCount = 0; nCount < concentrationList.size(); nCount++) {
							builder.append(concentrationList.get(nCount));
							if(nCount != concentrationList.size() - 1) {
								builder.append(", ");
							}
						}
						
						holder.tvConcentrations.setText(builder.toString());
					}
				}
			}
           
			return convertView;                   
        }
		
		public int getCount() {
			int nSize = 0;
			
			if(arrEducationDetails != null) {
				nSize = arrEducationDetails.size();
			}
			
			return nSize;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.alifesoftware.assignment.BaseHeadlessFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// If user clicked Find Friends button
		if(v == btnFindFriends &&
				uiUpdater != null) {
			Log.d(tagFragmentName, "Find Friends button clicked");
			
			// Check for Network
			if(! NetworkUtils.isOnline(getActivity())) {
				String msg = "Network not available. Cannot process your request to find Friends.";
				Log.w(tagFragmentName, msg);
				uiUpdater.showAlert("Network Error", msg);
			}
			uiUpdater.showFriendsList();
		}
		
		// If user clicked Send Push Note button
		else if(v == btnSendPushNote && 
				uiUpdater != null) {
			Log.d(tagFragmentName, "Send Push Note button clicked");
			
			// Check for Network
			if(! NetworkUtils.isOnline(getActivity())) {
				String msg = "Network not available. Cannot process your request to send Push Note.";
				Log.w(tagFragmentName, msg);
				uiUpdater.showAlert("Network Error", msg);
			}
			uiUpdater.showPushNoteDialog();
		}
		
		// If user clicked Location Launcher
		else if(v == ivLocation &&
				uiUpdater != null) {
			Log.d(tagFragmentName, "Launch User Location Map");
			
			// Check for Network
			if(! NetworkUtils.isOnline(getActivity())) {
				String msg = "Network not available. Cannot process your request to locate user on Map.";
				Log.w(tagFragmentName, msg);
				uiUpdater.showAlert("Network Error", msg);
			}
			uiUpdater.showLocation(loggedInUserData.getLocation());
		}
	}
}
