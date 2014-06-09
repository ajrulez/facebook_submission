package com.alifesoftware.assignment.pushnotes;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * This class extends GCMBroadcastReceiver, and sets the
 * class that handles GCM Intents
 *
 */
public class PushNoteBroadcastReceiver extends GCMBroadcastReceiver {
	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		// Return GCM Intent Service
		return "com.alifesoftware.assignment.pushnotes.GCMIntentService";
	}
}