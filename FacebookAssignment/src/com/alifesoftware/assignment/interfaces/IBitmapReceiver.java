package com.alifesoftware.assignment.interfaces;

import android.graphics.Bitmap;

/**
 * This interface is used to as a callback to
 * notify the caller when friend's profile picture
 * URL is converted to a Bitmap object
 * 
 */
public interface IBitmapReceiver {
	
	// Callback method to be invoked when friend's
	// profile picture HTTP URL is converted to Bitmap
	public void onConversionComplete(Bitmap bitmap);
}
