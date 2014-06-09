package com.alifesoftware.assignment.interfaces;

/**
 * This interface is used to support callback
 * mechanism for geocoding response
 *
 */
public interface IGeocodingResponseReceiver {
	// Callback method that gets invoked when Geocoding
	// Response becomes available
	public void onGeocodingResponseAvailable(String lat, String lng);
}
