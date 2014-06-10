package com.alifesoftware.assignment.tasks;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.alifesoftware.assignment.R;
import com.alifesoftware.assignment.interfaces.IGeocodingResponseReceiver;
import com.alifesoftware.assignment.ui.ProgressBarEx;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * This class is used to get coordinates for user's location
 * I am using http://geoservices.tamu.edu/ to Geocode city/state
 * 
 * NOTE: It appears this service only works for the US, which is
 * okay for this demo.
 * 
 */
public class GeocodingTask extends AsyncTask<String, Void, String> {
	
	// API URIs
	private final String GEOCODING_URI_CITY_ONLY = 
			"http://geoservices.tamu.edu/Services/Geocode/WebService/GeocoderWebServiceHttpNonParsed_V04_01.aspx?apiKey=2d72d57d085943a791b33169a9a59f39&version=4.01&city=%s";
	
	private final String GEOCODING_URI = 
			"http://geoservices.tamu.edu/Services/Geocode/WebService/GeocoderWebServiceHttpNonParsed_V04_01.aspx?apiKey=2d72d57d085943a791b33169a9a59f39&version=4.01&city=%s&state=%s";
	

	// IGeocodingResponseReceiver Notification Receiver
	private IGeocodingResponseReceiver geoCodingReceiver = null;
	
	// Progress Dialog
	private ProgressBarEx progress = null; 
	
	// Context
	private Context context;

	// Constructor
	public GeocodingTask(Context context, IGeocodingResponseReceiver receiver) {
		this.context = context;
		this.geoCodingReceiver = receiver;

		// Set up Progress Dialog
		if (context != null) {
			this.progress = new ProgressBarEx(context);
			progress.setIndeterminate(true);
			progress.setTitle("Please Wait");
			progress.setIcon(context.getResources().getDrawable(R.drawable.ic_launcher));
			progress.setMessage("Fetching Location Coordinates...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
	}

	// Start a Progress Bar
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		try {
			if(context != null &&
					progress != null) {
				progress.show();
			}
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		String city = null;
		String state = null;
		String uri = "";
		
		try {
			city = params[0];
			state = params[1];
			
			city = URLEncoder.encode(city, "UTF-8");
			state = URLEncoder.encode(state, "UTF-8");
		}
		
		catch (Exception e){
			// Nothing to log
		}
		
		if(city != null && 
				city.length() > 0 &&
				state != null &&
				state.length() > 0) {
			uri = String.format(GEOCODING_URI, city, state);
		}
		
		else if(city != null && 
					city.length() > 0 &&
					(state == null || state.length() > 0)) {
			uri = String.format(GEOCODING_URI_CITY_ONLY, city);
		}
		
		if(uri.length() > 0) {
			try{ 
				// Make HTTP Request
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(uri.toString());

				// Execute the request
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();

				InputStream responseData = httpEntity.getContent();

				// This is a Scanner trick to get our InputStream into a single
				// String
				// object.
				// "\A" is the beginning of input boundary, so this will scan all
				// the
				// way to the end of the input.
				Scanner s = new Scanner(responseData).useDelimiter("\\A");
				String data = "";
				if (s.hasNext())
					data = s.next();

				// Close input stream
				responseData.close();
				
				// Close the stream
				s.close();
				
				// Parse the response
				// Sample Response (Valid)
				// e4679b6c-4b71-4c5a-8c07-c0ea5fb35252,4.01,200,40.107561214997,-89.1611562968684,98,Unknown,100,Exact,Success,1,State,149995358488.219,Meters,LOCATION_TYPE_CITY,0.0780025,
				// 
				// Sample Response (Invalid)
				// 79d9294d-5844-402d-a2f4-955cc69ed098,4.01,500,0,0,99,Unmatchable,0,,Unmatchable,0,Unmatchable,-1,Unknown,LOCATION_TYPE_UNKNOWN,0,
				if (data != null && data.length() > 0) {
					String[] responseArray = data.split(",");
					
					// From the samples we can see that index 3 and 4 are lat and long
					// It would have been had they provided a JSON response :-(
					String lat = responseArray[3];
					String lng = responseArray[4];
					
					return String.format("%s,%s", lat, lng);
				}
			}
			
			catch(Exception e) {
				e.printStackTrace();
			}

		}
		
		return "#,#";
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String latLong) {
		if(context != null) {
			// Cancel the Progress Bar
			try {
				progress.dismiss();
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String[] latLngArray = latLong.split(",");
		
		// Invoke the callback
		geoCodingReceiver.onGeocodingResponseAvailable(latLngArray[0], latLngArray[1]);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onCancelled()
	 */
	@Override
	protected void onCancelled() {
		if(context != null) {
			// Cancel the Progress Bar
			try {
				progress.dismiss();
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Invoke the callback
		// Invoke the callback
		geoCodingReceiver.onGeocodingResponseAvailable(null, null);
	}	
}
