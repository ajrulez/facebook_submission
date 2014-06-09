package com.alifesoftware.assignment.interfaces;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This interface is used to invoke a callback
 * when Push Note GCM registration request
 * is complete
 *
 */
public interface IPushNotesRegistrationReceiver {
	// Callback Method that will be invoked after
	// GCM registration is complete
	public void onPushNotesRegistration(GoogleCloudMessaging gcm, String registrationId);
}
