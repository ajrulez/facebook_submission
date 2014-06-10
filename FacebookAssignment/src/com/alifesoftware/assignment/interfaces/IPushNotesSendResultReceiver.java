package com.alifesoftware.assignment.interfaces;

/**
 * This interface is used to provide a callback
 * that is invoked when a Push Note send request
 * is completed
 *
 */
public interface IPushNotesSendResultReceiver {
	// Callback method that is called after a 
	// Push Note send request is completed
	public void onPushNoteSent(boolean bSent);
}
