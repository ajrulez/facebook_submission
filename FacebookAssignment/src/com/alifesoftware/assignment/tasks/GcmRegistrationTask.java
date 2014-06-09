package com.alifesoftware.assignment.tasks;

import com.alifesoftware.assignment.interfaces.IPushNotesRegistrationReceiver;
import com.alifesoftware.assignment.pushnotes.GCMIntentService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class extends an AsyncTask that completes
 * GCM registration and gets a registration ID for
 * this devie-app combination.
 *
 */
public class GcmRegistrationTask extends AsyncTask<Context, Void, String> {
	// GoogleCloudMessaging Object
	private GoogleCloudMessaging gcm;
	
	// IPushNotesRegistrationReceiver Object
	private IPushNotesRegistrationReceiver receiver;
	
	// Constructor
	public GcmRegistrationTask(GoogleCloudMessaging gcm, IPushNotesRegistrationReceiver receiver) {
		this.gcm = gcm;
		this.receiver = receiver;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected String doInBackground(Context... params) {
		// Registration ID
		String registrationId = "";
		
		// Check Params
		if(params != null && params.length > 0) {
			Context context = params[0];

			if(context != null) {
				try {
					if(gcm == null) {
						Log.d("GcmRegistrationTask", "Creating a new GoogleCloudMessaging object");
						gcm = GoogleCloudMessaging.getInstance(context);
					}

					// Get registration ID
					registrationId = gcm.register(GCMIntentService.GCM_SENDER_ID);
				}

				catch (Exception e) {
					String errMsg = String.format("Exception when getting GCM Registration ID. Exception: %s", e.getMessage());
					Log.e("GcmRegistrationTask", errMsg);
					
					return registrationId;
				}
			}
			
			else {
				Log.e("GcmRegistrationTask", "No Context passed to the AsyncTask. Abort processing");
				registrationId = "";
			}
		}
		
		else {
			Log.e("GcmRegistrationTask", "No Params passed to the AsyncTask. Abort processing");
			registrationId = "";
		}
		
		return registrationId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	public void onPostExecute(String registrationId) {
		if(receiver != null) {
			// Invoke the callback
			receiver.onPushNotesRegistration(gcm, registrationId);
		}
	}
}
