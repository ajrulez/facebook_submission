package com.alifesoftware.assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alifesoftware.assignment.interfaces.IGeocodingResponseReceiver;
import com.alifesoftware.assignment.interfaces.IPushNotesRegistrationReceiver;
import com.alifesoftware.assignment.interfaces.IPushNotesSendResultReceiver;
import com.alifesoftware.assignment.interfaces.IUserIntefaceUpdater;
import com.alifesoftware.assignment.model.FriendUserData;
import com.alifesoftware.assignment.model.LoggedInUserData;
import com.alifesoftware.assignment.model.LoggedInUserData.Education;
import com.alifesoftware.assignment.tasks.GcmRegistrationTask;
import com.alifesoftware.assignment.tasks.GeocodingTask;
import com.alifesoftware.assignment.tasks.PushNoteSenderTask;
import com.alifesoftware.assignment.utils.NetworkUtils;
import com.alifesoftware.assignment.R;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * LauncherFragmentActivity is the Main Activity of this
 * application and it extends FragmentActivity.
 * 
 * This class also extends IUserIntefaceUpdater interface
 * that provides a couple of helper methods to change
 * fragments
 * 
 * @author asaluja
 */
public class LauncherFragmentActivity extends FragmentActivity 
									  implements IUserIntefaceUpdater,
									  IPushNotesRegistrationReceiver,
									  IPushNotesSendResultReceiver,
									  IGeocodingResponseReceiver {

	// Use LoginButton widget provided by Facebook to get an
	// official look and feel
	private LoginButton loginButton;

	// Use Facebook provided UILifecycleHelper to maintain Active Session
	private UiLifecycleHelper uiHelper;

	// Maintain a current session object to use for Requests
	// or Use Session.getActiveSession();
	private Session currentSession;

	// LoggedInUserData Object
	private LoggedInUserData loggedInUserData = new LoggedInUserData();

	// Friends List Data
	private List<FriendUserData> friendsListData;

	// Fragment Container
	protected RelativeLayout fragmentContainer;

	// Saved Instance Bundle
	protected Bundle mainBundle;

	// Current Fragment
	protected BaseHeadlessFragment currentFragment;
	
	// Display Pic URL for Selected Friend
	protected String selectedFriendDisplayUrl;
	
	// GoogleCloudMessaging Object
	private GoogleCloudMessaging gcm;
	
	// Registration ID for Push Notes
    private String registrationId;
    
    // Key that indicates that App is being launched by Push Notes
    public static final String ACTIVITY_LAUNCHED_FROM_PUSHNOTES_KEY = "activity_launched_from_pushnotes_key";

    // User Location Lat
    private String userLocationLat = "";
    
    // User Location Lng
    private String userLocationLng = "";

	// Enums for UI States
	public static enum UI_STATE {
		SESSION_OPENED,
		SESSION_CLOSED,
		SHOW_LOGGED_IN_USER_DATA,
		FIND_FRIENDS,
		SHOW_DISPLAY_IMAGE,
		SHOW_USER_LOCATION_MAP,
		UNKNOWN
	}

	// Logging tag
	private static final String TAG = "LauncherFragmentActivity";

	// Callback that is invoked whenever there is a change in
	// session
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			Log.v(TAG, "Session.StatusCallback invoked");
			
			// Update current session
			updateSession(session, state, exception);
		}
	};

	// Must have FacebookDialog callback to receive status of a pending
	// Facebook UI operation (we use it for Login UI callback)
	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d(TAG, String.format("FacebookDialog.Callback reported an Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d(TAG, "FacebookDialog.Callback reported Success");
		}
	};

	/**
	 * Method to override default Activity Lifecycle
	 * onCreate
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Do not show title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Set the Main Bundle
		mainBundle = savedInstanceState;

		// Force run Portrait for now
		// If there's time, I will create layout for Landscape view
		// and remove this
		// TODO
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// UI Lifecycle Helper hooks to Android Lifecycle events
		// Set the callback and call onCreate
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		// Set the layout
		setContentView(R.layout.launcher_activity_layout);

		// Set the Fragment Container
		fragmentContainer = (RelativeLayout) findViewById(R.id.fragmentLayout);

		// Based on requirement #3, we need to get logged in user's
		// education history, location, name, and picture. While
		// name and picture are part of public profile, education history
		// and location are not. So we need specified permissions for education
		// history and location
		//
		// Create list of permission Strings
		ArrayList<String> permissions = new ArrayList<String>();
		permissions.add("user_education_history"); // For education history
		permissions.add("user_location"); // For location
		permissions.add("user_friends"); // For friends
		
		// Set up the LoginButton widget with permissionss
		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setReadPermissions(permissions);
		loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				if(currentSession == null) {
					// Get the ActiveSession in temp
					Session temp = Session.getActiveSession();
					
					// Check temp for Opened Category
					// and if we have a valid AccessToken
					// Setthe currentSession object to ActiveSession
					if(temp != null && 
							temp.isOpened() && 
							temp.getAccessToken() != null &&
							temp.getAccessToken().length() > 0) {
						Log.d(TAG, "Setting currentSession in loginButton Callback");
						currentSession = Session.getActiveSession();
						updateActiveUserInfo();
					}
					else {
						Log.d(TAG, "ActiveSession is not yet ready to be used");
					}
				}
			}
        });
	}
	
	/**
	 * Method to override default Activity Lifecycle
	 * onResume
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// As per UILifecycleHelper, it must get hooked to all Android Activity
		// Lifecycle events
		uiHelper.onResume();

		if (currentSession != null && currentSession.isOpened()) {
			Log.d(TAG, "onResume indicates that currentSession is open. Update the UI to SESSION_OPENED state");
			updateUI(UI_STATE.SESSION_OPENED);
		}
		
		else {
			Log.d(TAG, "onResume indicates that currentSession is not open. Nothing to do.");
		}
		
		// Try to Register for GCM Push Notes
		try {
			getRegistrationId();
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to override default Activity method
	 * onSaveInstanceState
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Hook the UILifecycleHelper state with current Android Activity
		// Lifecycle state
		uiHelper.onSaveInstanceState(outState);
	}

	/**
	 * Method to override default Activity method
	 * onActivityResult
	 * 
	 * This method hooks UILifecycleHelper and FacebookDialog.callback
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}

	/**
	 * Method to override default Activity Lifecycle
	 * onPause
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// As per UILifecycleHelper, it must get hooked to all Android Activity
		// Lifecycle events
		uiHelper.onPause();
	}

	/**
	 * Method to override default Activity Lifecycle
	 * onDestroy
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		// As per UILifecycleHelper, it must get hooked to all Android Activity
		// Lifecycle events
		uiHelper.onDestroy();
	}

	/**
	 * Method to update current session based on the passed in parameters
	 * 
	 * @param session
	 *            - Facebook Session as provided by Session StatusCallback
	 * @param state
	 *            - SessionState as provided by Session StatusCallback
	 * @param exception
	 *            - Exception (if any) as provided by Session StatusCallback
	 * 
	 */
	private void updateSession(Session session, SessionState state,
			Exception exception) {
		Log.d(TAG, "Session update method called");
		
		// If there is an exception, mark the current session as closed
		if (exception != null) {
			// Show exception details
			String errMsg = String.format("Error when updating Facebook session. Exception: %s", exception.getMessage());
			Log.e(TAG, errMsg);
			
			// Set current session to null
			currentSession = null;
		}

		// If SessionState is of OPEN category, set the current session to
		// active session
		// and update current user's info
		else if (state.isOpened()) {
			Log.d(TAG, "Facebook session is updated to OPEN category. Continue with UI updates");
			currentSession = session;
			updateActiveUserInfo();
		}

		// If SessionState is of CLOSED category, mark the current session as
		// closed
		else if (state.isClosed()) {
			Log.d(TAG, "Facebook session is updated to CLOSED category. Update the UI to reflect the CLOSED state");
			currentSession = null;
			updateUI(UI_STATE.SESSION_CLOSED);
		}
	}

	/**
	 * This method makes a Graph API request to get current user's name,
	 * picture, education details, and location. This corresponds to requirement
	 * number 3 of the assignment
	 * 
	 */
	private void updateActiveUserInfo() {
		Log.d(TAG, "updateActiveUserInfo - Method to update active user's information called");

		// Create a Bundle of fields that we want to get
		// for the user based on the requirements
		final Bundle params = new Bundle();
		params.putString("fields", "name,picture,education,location");

		// Create a new Graph API request to get required information
		new Request(currentSession, "/me", params, HttpMethod.GET,
				new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						Log.d(TAG, "onComplete callback for Logged-in user's data request invoked");
						// graphObject contains the response
						GraphObject graphObject = response.getGraphObject();

						// Error object
						FacebookRequestError error = response.getError();

						// If there is an error in our request\response
						if (error != null) {
							String errMsg = String.format("Request to get Logged-in user data returned an error. Error: %s", error.getErrorMessage());
							Log.e("TAG", errMsg);
							
							// Show Error
							showAlert("Error", errMsg);
						}

						// Parse GraphObject
						else {
							Log.d(TAG, "Request to get Logged-in user data was successful. Proceed with response parsing");
							if (graphObject != null) {
								// Parse GraphObject
								try {
									parseUserGraphObject(graphObject);
									
									Log.d(TAG, "Logged-in user data response parsed successfully. Update the UI to show user detials");
									// Update UI
									updateUI(UI_STATE.SESSION_OPENED);
								}
								
								catch (Exception e) {
									String errMsg = String.format("There was an exception when parsing Logged-in user's details. Exception: %s", e.getMessage());
									Log.e(TAG, errMsg);
									showAlert("Error", errMsg);
								}
							}

							else {
								String errMsg = "Even though request to get Logged-in user data returned no error, we were unable to get the graph object from response";
								Log.e(TAG, errMsg);
								showAlert("Error", errMsg);
							}
						}
					}
				}).executeAsync();
	}

	/**
	 * Method to parse current user's details as obtained via Graph API
	 * 
	 * @param graphObject
	 *            - Graph Data obtained as response
	 * 
	 */
	private void parseUserGraphObject(final GraphObject graphObject) throws Exception {
		Log.d(TAG, "parseUserGraphObject - Method to parse active user's information called");

		// Education Array
		ArrayList<Education> educationList = new ArrayList<Education>();

		// Check for null
		if (graphObject == null) {
			throw new Exception("Failed to parse Logged-in user's data because GraphObject is null");
		}

		// Convert the graph object to a Map
		Map<String, Object> graphMap = graphObject.asMap();
		if (graphMap == null) {
			throw new Exception("Failed to parse Logged-in user's data because GraphObject is invalid");
		}

		// Name
		if (graphMap.containsKey("name")) {
			String name = (String) graphMap.get("name");
			if (name == null) {
				Log.w(TAG, "Failed to get Logged-in user's name");
				name = "";
			}

			loggedInUserData.setName(name);
		}

		// User ID
		if (graphMap.containsKey("id")) {
			String id = (String) graphMap.get("id");
			if (id == null) {
				Log.w(TAG, "Failed to get Logged-in user's ID");
				id = "";
			}

			loggedInUserData.setUserId(id);
		}

		// Location
		if (graphMap.containsKey("location")) {
			JSONObject locationObject = (JSONObject) graphMap.get("location");

			if (locationObject == null) {
				Log.w(TAG, "Failed to get Logged-in user's location");
				loggedInUserData.setLocation("");
			}

			else {
				String location = locationObject.optString("name", "");
				loggedInUserData.setLocation(location);
			}
		}

		// Picture
		if (graphMap.containsKey("picture")) {
			JSONObject pictureObject = (JSONObject) graphMap.get("picture");

			if (pictureObject == null) {
				Log.w(TAG, "Failed to get Logged-in user's picture");
				loggedInUserData.setDisplayPictureUri("");
			}

			else {
				JSONObject dataObject = pictureObject.optJSONObject("data");

				if (dataObject == null) {
					Log.w(TAG, "Failed to get Logged-in user's picture");
					loggedInUserData.setDisplayPictureUri("");
				}

				String pictureUri = dataObject.optString("url", "");
				loggedInUserData.setDisplayPictureUri(pictureUri);
			}
		}

		// Education
		JSONArray educationArray = (JSONArray) graphMap.get("education");
		if (educationArray == null || educationArray.length() <= 0) {
			Log.w(TAG, "Failed to get Logged-in user's education details");
			loggedInUserData.setEducation(educationList);
		}

		else {
			for (int ndx = 0; ndx < educationArray.length(); ndx++) {
				try {
					JSONObject educationObject = educationArray
							.getJSONObject(ndx);
					ArrayList<String> concentrations = new ArrayList<String>();

					// Type
					String type = educationObject.optString("type", "");

					// Year
					JSONObject yearObject = educationObject
							.getJSONObject("year");
					String year = yearObject.optString("name", "");

					// School
					JSONObject schoolObject = educationObject
							.getJSONObject("school");
					String school = schoolObject.optString("name", "");

					// Concetration(s)
					JSONArray concentrationArray = educationObject
							.getJSONArray("concentration");

					for (int count = 0; count < concentrationArray.length(); count++) {
						JSONObject concentrationObject = concentrationArray
								.getJSONObject(count);
						String concentration = concentrationObject.optString(
								"name", "");
						concentrations.add(concentration);
					}

					Education education = new Education();
					education.setSchool(school);
					education.setType(type);
					education.setYear(year);
					education.setConcentration(concentrations);

					// Education List
					educationList.add(education);
				}
				
				catch (JSONException e) {
					Log.w(TAG, e.getMessage());
					continue;
				}
			}

			// Set education
			loggedInUserData.setEducation(educationList);
		}
	}

	/**
	 * Method to parse user's friends list details as obtained via Graph API
	 * 
	 * @param graphObject
	 *            - Graph Data obtained as response
	 * 
	 * @return List<FriendUserData> - List of FriendUserData objects
	 */
	private List<FriendUserData> parseFriendsListGraphObject(final GraphObject graphObject) {
		Log.d(TAG, "parseFriendsListGraphObject - Method to parse user's friend list called");
		
		ArrayList<FriendUserData> friendList = new ArrayList<FriendUserData>();

		// Check for null
		if (graphObject == null) {
			Log.e(TAG, "Failed to parse user's friends list because graph object is null");
			return null;
		}

		// Convert the graph object to a Map
		Map<String, Object> graphMap = graphObject.asMap();
		if (graphMap == null) {
			Log.e(TAG, "Failed to parse user's friends list because graph object is invalid");
			return null;
		}

		if (graphMap.containsKey("data")) {
			JSONArray dataArray = (JSONArray) graphMap.get("data");

			if (dataArray != null && dataArray.length() > 0) {
				for (int nCount = 0; nCount < dataArray.length(); nCount++) {
					try {
						JSONObject dataObject = dataArray.getJSONObject(nCount);
						FriendUserData friend = new FriendUserData();

						if (dataObject != null) {
							// Get user ID
							friend.setId(dataObject.optString("id", ""));

							// Get user name
							friend.setName(dataObject.optString("name", ""));

							// Get Picture URi
							JSONObject pictureObject = dataObject
									.optJSONObject("picture");
							if (pictureObject != null) {
								JSONObject pictureDataObject = pictureObject
										.optJSONObject("data");

								if (pictureDataObject != null) {
									friend.setDisplayPictureUrl(pictureDataObject
											.optString("url", ""));
									friend.setPictureHeight(pictureDataObject
											.optInt("height", 1));
									friend.setPictureWidth(pictureDataObject
											.optInt("width", 1));
									friend.setSilhouette(pictureDataObject
											.optBoolean("is_silhouette", true));

									// Add object to Array
									friendList.add(friend);
								}
							}
							
							else {
								Log.e(TAG, "Failed to parse friend's picture because data does not contain picture");
							}
						}
						
						else {
							Log.e(TAG, "Failed to parse user's friends list because graph map does not contain any data");
						}

					}

					catch (JSONException e) {
						e.printStackTrace();
						continue;
					}

				}
			}

			else {
				Log.e(TAG, "Failed to parse user's friends list because graph map data is empty");
				return null;
			}
		}
		
		else {
			Log.e(TAG, "Failed to parse user's friends list because graph map does not contain any data");
		}

		return friendList;
	}

	/**
	 * Method to update the UI
	 * 
	 * Since this app is fairly simple in terms of UI complexity
	 * and includes 3-4 fragments, I chose the easiest Fragment management
	 * strategy, which is that there's only one Fragment on the stack at
	 * any given time. When we want to show a fragment, we simply remove
	 * the previous Fragment, and add the new one.
	 * 
	 * I know other strategies / APIs for Fragment management such as
	 * replacing a Fragment, popping a Fragment off the stack (i.e. popBackStack)
	 * but in this case, I figured this was the easiest strategy given
	 * time constraints.
	 * 
	 * @param state - UI_STATE to which the UI is requested to be updated
	 * 
	 */
	private synchronized void updateUI(final UI_STATE state) {
		Log.d(TAG, "updateUI - Method to update the user interface called");
		
		// If UI state is Unknown, return
		if (state == UI_STATE.UNKNOWN) {
			Log.e(TAG, "UI cannot be updated because the state specified is unknown");
			return;
		}

		// Check that the activity is using container for fragments
		//
		if (fragmentContainer != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (mainBundle != null) {
				Log.e(TAG, "UI cannot be updated because mainBundle of the app is not null");
				return;
			}
		}

		// If state is Session Opened
		if (state == UI_STATE.SESSION_OPENED
				|| state == UI_STATE.SHOW_LOGGED_IN_USER_DATA) {
			Log.d(TAG, "Requested to updated the UI to show Logged-in user data");
			
			// Ensure Logout button is visible
			loginButton.setVisibility(View.VISIBLE);
			
			// Check if we already have the UserInfoDisplayFragment Fragment
			// Attached
			if (currentFragment != null
					&& currentFragment instanceof UserInfoDisplayFragment
					&& currentFragment.fragmentAttached.get()) {
				Log.i(TAG, "No need to update the user interface because current fragment is already showing Logged-in user data");
				return;
			}

			else if (currentSession != null && currentSession.isOpened()) {
				Log.d(TAG, "Attempting to update the UI to show Logged-in user data");
				
				// Create an instance of UserInfoDisplayFragment
				UserInfoDisplayFragment userInfoFragment = new UserInfoDisplayFragment();
				userInfoFragment.setUserInterfaceUpdater(this);

				// In case this activity was started with special instructions
				// from an Intent,
				// pass the Intent's extras to the fragment as arguments
				Bundle bundle = getIntent().getExtras();
				
				if(bundle == null) {
					bundle = new Bundle();
				}
				
				// Add the logged-in user data to bundle to be sent to the Fragment
				bundle.putParcelable(UserInfoDisplayFragment.LOGGED_IN_USER_DATA_KEY, loggedInUserData);
				userInfoFragment.setArguments(bundle);

				// If we have any other Fragment, remove it first
				removeFragment();

				// Add the fragment to the container
				getSupportFragmentManager().beginTransaction()
						.add(fragmentContainer.getId(), userInfoFragment)
						.commit();

				// Update the current Fragment
				currentFragment = userInfoFragment;
			}

			else {
				String errMsg = "Failed to show Logged-in user's data because current session is invalid or closed";
				Log.e(TAG, errMsg);
				showAlert("Error", errMsg);
			}
		}

		else if (state == UI_STATE.SESSION_CLOSED) {
			Log.i(TAG, "Update the UI to session closed state");
			
			// Check if we have the UserInfoDisplayFragment Fragment Attached
			try {
				if (currentFragment != null
						&& currentFragment.fragmentAttached.get()) {
					getSupportFragmentManager().beginTransaction()
							.remove(currentFragment).commit();

					currentFragment = null;
				}
			}

			catch (Exception e) {
				Log.e(TAG, String.format("Exception when updating the UI to session closed state. Details: %s", e.getMessage()));
			}
		}

		else if (state == UI_STATE.FIND_FRIENDS) {
			Log.d(TAG, "Requested to updated the UI to show user's friends");
			
			// If current fragment is already FindFriendsFragment, then return
			if (currentFragment != null
					&& currentFragment instanceof FindFriendsFragment
					&& currentFragment.fragmentAttached.get()) {
				Log.i(TAG, "No need to update the user interface because current fragment is already showing user's friends");
				return;
			}

			// Replace the current Fragment with FindFriendsFragment object
			else if (currentSession != null && currentSession.isOpened()) {
				Log.d(TAG, "Attempting to update the UI to show user's friends");
				
				// Hide Logout Button
				loginButton.setVisibility(View.GONE);
				
				// Create an instance of FindFriendsFragment - friendsListData
				FindFriendsFragment findFriendsFragment = new FindFriendsFragment();
				
				// Set the UI Updater
				findFriendsFragment.setUserInterfaceUpdater(this);

				// In case this activity was started with special instructions
				// from
				// an Intent,
				// pass the Intent's extras to the fragment as arguments
				Bundle bundle = getIntent().getExtras();
				
				if(bundle == null) {
					bundle = new Bundle();
				}
				
				// Add friendsListData to bundle
				bundle.putParcelableArrayList(FindFriendsFragment.FRIEND_USER_DATA_KEY, 
						(ArrayList<? extends Parcelable>) friendsListData);
				
				// Set the bundle
				findFriendsFragment.setArguments(bundle);

				// If we have any other Fragment, remove it first
				removeFragment();

				// Add FrindFriendsFragment
				getSupportFragmentManager().beginTransaction()
						.add(fragmentContainer.getId(), findFriendsFragment)
						.commit();

				currentFragment = findFriendsFragment;
			}

			else {
				String errMsg = "Failed to show user's friends because current session is invalid or closed";
				Log.e(TAG, errMsg);
				showAlert("Error", errMsg);
			}
		}

		else if (state == UI_STATE.SHOW_DISPLAY_IMAGE) {
			Log.d(TAG, "Requested to updated the UI to friend's display picture");
			
			// Hide the Logout button
			loginButton.setVisibility(View.GONE);
			
			// If current fragment is already UserDisplayPictureFragment, then
			// return
			if (currentFragment != null
					&& currentFragment instanceof UserDisplayPictureFragment
					&& currentFragment.fragmentAttached.get()) {
				Log.i(TAG, "No need to update the user interface because current fragment is already showing friend's display picture");
				return;
			}

			// Replace the current Fragment with UserDisplayPictureFragment
			// object
			else if (currentSession != null && currentSession.isOpened()) {
				Log.d(TAG, "Attempting to update the UI to show friend's display picture");
				
				// Create an instance of UserDisplayPictureFragment
				UserDisplayPictureFragment userDisplaySegment = new UserDisplayPictureFragment();
				userDisplaySegment.setUserInterfaceUpdater(this);

				// In case this activity was started with special instructions
				// from an Intent, pass the Intent's extras to the 
				// fragment as arguments
				Bundle bundle = getIntent().getExtras();
				
				// If there's no existing bundle, create a new one
				if(bundle == null) {
					bundle = new Bundle();
				}
				
				// Add selectedFriendDisplayUrl to the bundle
				bundle.putString(UserDisplayPictureFragment.SELECTED_FRIEND_DISPLAY_URL_KEY, selectedFriendDisplayUrl);
				userDisplaySegment.setArguments(bundle);

				// If we have any other Fragment, remove it first
				removeFragment();

				// Add UserDisplayPictureFragment
				getSupportFragmentManager().beginTransaction()
						.add(fragmentContainer.getId(), userDisplaySegment)
						.commit();

				currentFragment = userDisplaySegment;
			}

			else {
				String errMsg = "Failed to show friend's display picture because current session is invalid or closed";
				Log.e(TAG, errMsg);
				showAlert("Error", errMsg);
			}
		}
		
		else if(state == UI_STATE.SHOW_USER_LOCATION_MAP) {
			Log.d(TAG, "Attempting to update the UI to user's location on Map");
			
			Intent launchMapActivity = new Intent(LauncherFragmentActivity.this, MapActivity.class);
			launchMapActivity.putExtra(MapActivity.INTENT_LOCATION_LAT_KEY, userLocationLat);
			launchMapActivity.putExtra(MapActivity.INTENT_LOCATION_LNG_KEY, userLocationLng);
			launchMapActivity.putExtra(MapActivity.INTENT_LOCATION_NAME_KEY, loggedInUserData.getLocation());
			
			startActivity(launchMapActivity);
		}
	}

	/**
	 * Method to update the UI to show Logged-in
	 * user's Friends
	 * 
	 */
	@Override
	public void showFriendsList() {
		Log.d(TAG, "showFriendsList - Method to update the UI to show Logged-in user's Friends called");
		if (currentSession == null || currentSession.isClosed()) {
			String errMsg = "Unable to show Logged-in user's Friends because the current session is invalid or closed";
			Log.e(TAG, errMsg);
			showAlert("Error", errMsg);
			
			return;
		}

		// Create a Bundle of fields that we want to get
		// for the user based on the requirements
		final Bundle params = new Bundle();
		params.putString("fields", "name,id,picture.width(500).height(500)");

		// Since this is a new app, v1.0 API does not work. I was thinking of
		// getting all friends using v1.0 API but that is not going to work.
		//
		// I'll stick to v2.0 APIs. A few people at Facebook who are friends
		// with each other may need to install this app, and then they 
		// can see some friends
		//
		new Request(currentSession, "/me/friends", params, HttpMethod.GET,
				new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						Log.d(TAG, "onComplete callback for user's friends request invoked");
						// graphObject contains the response
						GraphObject graphObject = response.getGraphObject();

						// Error object
						FacebookRequestError error = response.getError();

						// If there is an error in our request\response
						if (error != null) {
							String errMsg = String.format("Request to get user's friends returned an error. Error: %s", error.getErrorMessage());
							Log.e("TAG", errMsg);
							
							// Show Error
							showAlert("Error", errMsg);
						}

						// Parse GraphObject
						else {
							if (graphObject != null) {
								// Parse GraphObject
								friendsListData = parseFriendsListGraphObject(graphObject);
								
								if(friendsListData == null) {
									String errMsg = "Unable to show user's friends because we encountered an error when parsing friends for Logged-in user";
									Log.e(TAG, errMsg);
									showAlert("Information", errMsg);
									return;
								}
								
								else if(friendsListData.size() <= 0) {
									String errMsg = "Unable to show user's friends because we retrieved 0 (Zero) friends for Logged-in user";
									Log.e(TAG, errMsg);
									showAlert("Information", errMsg);
									return;
								}

								Log.d(TAG, "User's friend response parsed successfully. Update the UI to show user's friends");
								
								// Update UI
								updateUI(UI_STATE.FIND_FRIENDS);
							}

							else {
								String errMsg = "Even though request to get user's Friends returned no error, we were unable to get the graph object from response";
								Log.e(TAG, errMsg);
								showAlert("Error", errMsg);
							}
						}
					}
				}).executeAsync();
	}

	/**
	 * This method is used to update the user interface
	 * to show selected Friend's display picture
	 * 
	 */
	@Override
	public void showFriendDisplayImage(final String displayImageUrl) {
		Log.d(TAG, "showFriendDisplayImage - Received request to update UI to show selected friend's display picture");
		selectedFriendDisplayUrl = displayImageUrl;
		updateUI(UI_STATE.SHOW_DISPLAY_IMAGE);
	}
	
	/**
	 * Overriding default onBackPressed to make sure
	 * we show appropriate Fragment
	 * 
	 * This is related to Fragment management strategy
	 * explained earlier in comments in this source file
	 * 
	 */
	@Override
	public void onBackPressed(){
		Log.d(TAG, "onBackPressed - User pressed the Back key");
		
		if(currentFragment instanceof UserDisplayPictureFragment) {
			Log.d(TAG, "Current Fragment: UserDisplayPictureFragment, Back To Fragment: FindFriendsFragment");
			updateUI(UI_STATE.FIND_FRIENDS);
		}
		
		else if(currentFragment instanceof FindFriendsFragment) {
			Log.d(TAG, "Current Fragment: FindFriendsFragment, Back To Fragment: UserInfoDisplayFragment");
			updateUI(UI_STATE.SHOW_LOGGED_IN_USER_DATA);
		}
		
		else {
			super.onBackPressed();
		}
	}
	
	/**
	 * Utility method to remove current fragment
	 * 
	 * This is related to Fragment management strategy
	 * explained earlier in comments in this source file
	 */
	private void removeFragment() {
		Log.d(TAG, "removeFragment - Method to remove current fragment invoked");
		
		// If we have any other Fragment, remove it first
		if (currentFragment != null
				&& currentFragment.fragmentAttached.get()) {
			Log.d(TAG, "Attempting to remove current fragment");
			getSupportFragmentManager().beginTransaction()
					.remove(currentFragment).commit();
		}
		
		else {
			Log.d(TAG, "Cannot remove current fragment because current fragment is null or is not attached");
		}
	}
	
	/**
	 * This is a utility method that is used to show an alert
	 * dialog
	 * 
	 * @param title - Title of the AlertDialog
	 * @param message - Message of the AlertDialog
	 * 
	 */
	public void showAlert(final String title, final String message) {
		runOnUiThread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				AlertDialog alertDialog1 = new AlertDialog.Builder(LauncherFragmentActivity.this).create();

				// Setting Dialog Title
		        alertDialog1.setTitle(title);

		        // Setting Dialog Message
		        alertDialog1.setMessage(message);

		        // Setting Icon to Dialog
		        alertDialog1.setIcon(R.drawable.ic_launcher);

		        // Setting OK Button
		        alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

		            public void onClick(DialogInterface dialog, int which) {
		            	// Nothing to do
		            }
		        });

		        // Showing Alert Message
		        alertDialog1.show();
			}
		});
	}
		
	/***************************** GCM Related Implementation *******************************/
	/**
	 * Method to setup GCM Push Notes
	 */
	public synchronized void getRegistrationId() {
		if(registrationId == null || 
				registrationId.length() <= 0) {
			Log.d(TAG, "getRegistrationId - Starting GcmRegistrationTask");
			GcmRegistrationTask gcmRegistration = new GcmRegistrationTask(gcm, this);
			Context[] params = {this};
			gcmRegistration.execute(params);
		}
    }

	/**
	 * This is a callback method that is invoked when
	 * GCM registration result becomes available.
	 * 
	 */
	@Override
	public void onPushNotesRegistration(GoogleCloudMessaging gcm, String registrationId) {
		this.gcm = gcm;
		this.registrationId = registrationId;
	}
	
	/**
	 * This method is used to show a custom AlertDialog
	 * that requests user input for a Push Note
	 * 
	 */
	@Override
	public void showPushNoteDialog() {
		Log.d(TAG, "Launch Send Push Note dialog");
		
		// Get Layout for Send Push Note Dialog
		LayoutInflater inflater = LayoutInflater.from(this);
		View sendPushNoteView = inflater.inflate(R.layout.send_push_note_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// Set send_push_note_dialog.xml to AlertDialog builder
		alertDialogBuilder.setView(sendPushNoteView);
		
		// Set AlertDialog Message
		alertDialogBuilder.setCancelable(false);
		
		// Create Alert Dialog
		final AlertDialog alertDialog = alertDialogBuilder.create();

		// EditText for Push Note Message
		final EditText pnMessage = (EditText) sendPushNoteView
				.findViewById(R.id.editTextMessage);
		
		// Send Push Note Button
		final Button sendPushNoteButton = (Button) sendPushNoteView
				.findViewById(R.id.buttonSendPushNote);
		
		final StringBuilder pushNoteMessage = new StringBuilder();
		sendPushNoteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check for Network
				if(! NetworkUtils.isOnline(LauncherFragmentActivity.this)) {
					String msg = "Network not available. Cannot process your request to send Push Note.";
					Log.w(TAG, msg);
					alertDialog.dismiss();
					showAlert("Network Error", msg);
					return;
				}
				
				pushNoteMessage.append(pnMessage.getText().toString());
				alertDialog.dismiss();
				
				sendPushNoteRequest(pushNoteMessage.toString());
			}
		});

		// Cancel Push Note Button
		final Button cancelPushNoteButton = (Button) sendPushNoteView
				.findViewById(R.id.buttonCancelPushNote);
		cancelPushNoteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		// Show AlertDialog
		alertDialog.show();
	}
	
	/**
	 * This method is used to execute an AsynTask that
	 * sends a Push Note request to our server
	 * 
	 * @param String - Message that we want to send via 
	 * 					Push Note
	 */
	protected void sendPushNoteRequest(String message) {
		// Check registration id
		if(registrationId == null || 
				registrationId.length() <= 0) {
			String errMsg = "Cannot send Push Note because this device does not have a Push Note registration ID";
			Log.e(TAG, errMsg);
			
			showAlert("Error", errMsg);
			return;
		}
		
		// Check for message
		if(message == null ||
				message.length() <= 0) {
			String errMsg = "Cannot send Push Note because user did not enter any text for message";
			Log.e(TAG, errMsg);
			
			showAlert("Error", errMsg);
			return;
		}
		
		Log.d(TAG, "Starting AsyncTask to send Push Note");
		
		// Send PN
		PushNoteSenderTask pnSender = new PushNoteSenderTask(this);
		String[] params = {registrationId, message};
		pnSender.execute(params);
	}

	/**
	 * This callback is invoked when we receive a response from
	 * server for send push note request
	 * 
	 * @param boolean - true if Push Note was sent successfully,
	 * 					false otherwise
	 */
	@SuppressLint("ShowToast")
	@Override
	public void onPushNoteSent(boolean bSent) {
		String message;
		
		if(bSent) {
			message = "Push Note was sent successfully.";
			Log.d(TAG, message);
			Toast.makeText(this, message, Toast.LENGTH_SHORT);
		}
		
		else {
			message = "Failed to send the Push Note.";
			Log.e(TAG, message);
			Toast.makeText(this, message, Toast.LENGTH_SHORT);
		}
	}
	
	/************************************* Geocoding Methods *****************************/
	
	/**
	 * This method is used to get lat and long corresponding
	 * to city and state
	 * 
	 * @param city
	 * @param state
	 */
	@Override
	public void showLocation(String location) {
		
		if(location == null ||
				location.length() <= 0) {
			String errMsg = "User location is invalid. Cannot launch MapView";
			Log.e(TAG, errMsg);
			showAlert("Error", errMsg);
			
			return;
		}
		
		String[] cityState = location.split(",");
		String city = "";
		String state = "";
		String[] params = new String[2];
		
		try {
			city = cityState[0].trim();
			
			if(cityState.length >= 2) {
				state = cityState[1].trim();
			}
		
			if(city != null &&
					city.length() > 0) {
				params[0] = city;
			}
			else {
				params[0] = "";
			}
		
			if(state != null && 
					state.length() > 0) {
				params[1] = state;
			}
			else {
				params[1] = "";
			}
		}
		
		catch(Exception e) {
			String errMessage = String.format("Failed to create Map Location request. Exception: %s", e.getMessage());
			Log.e(TAG, errMessage);
			showAlert("Error", errMessage);
			return;
		}
		
		GeocodingTask task = new GeocodingTask(this, this);
		task.execute(params);
	}

	/**
	 * Callback method that is invoked when AsyncTask processes
	 * Geocoding request
	 * 
	 */
	@Override
	public void onGeocodingResponseAvailable(String lat, String lng) {
		if(lat != null &&
				!lat.equalsIgnoreCase("#") &&
				lng != null &&
				!lng.equalsIgnoreCase("#")) {
			userLocationLat = lat;
			userLocationLng = lng;
			
			String msg = String.format("Successfully retrieved Lat = %s, Lng = %s", userLocationLat, userLocationLng);
			Log.d(TAG, msg);
			
			// Update the UI
			updateUI(UI_STATE.SHOW_USER_LOCATION_MAP);
		}
		
		else {
			String errMsg = "Unable to Geocode your Location. Cannot show Map View";
			Log.e(TAG, errMsg);
			showAlert("Error", errMsg);
		}
	}
}
