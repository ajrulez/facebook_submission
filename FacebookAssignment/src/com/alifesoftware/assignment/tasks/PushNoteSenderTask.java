package com.alifesoftware.assignment.tasks;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.alifesoftware.assignment.interfaces.IPushNotesSendResultReceiver;

import android.os.AsyncTask;
import android.util.Log;

/**
 * This class extends an AsyncTask that
 * is used to send a Push Note request to
 * our server
 * 
 *
 */
public class PushNoteSenderTask extends AsyncTask<String, Void, Boolean> {
	// IPushNotesSendResultReceiver Object
	private IPushNotesSendResultReceiver receiver;
	
	// Constructor
	public PushNoteSenderTask(IPushNotesSendResultReceiver receiver) {
		this.receiver = receiver;
	}
	
	// Push Note Send URL
	private final String PUSH_NOTES_SEND_URL = "http://alifesoftware.co.in/services/pushnotes/pnsend.php?";

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Boolean doInBackground(String... params) {
		Boolean ret = Boolean.valueOf(false);
		
		// Check params
		if(params == null ||
				params.length < 2) {
			Log.e("PushNoteSenderTask", "Unable to send Push Note because the params are empty");
			return ret;
		}
		
		// I know it's kind of hard-coded, but for now this will do
		//
		// index 0 = registration ID
		// index 1 = message
		String registrationId = params[0];
		String message = params[1];
		
		// Check registration ID and message
		if(registrationId == null ||
				registrationId.length() <= 0 ||
				message == null ||
				message.length() <= 0) {
			Log.e("PushNoteSenderTask", "Unable to send Push Note because registration and\\or message is not provided");
			return ret; 
		}
		
		try {
			// Construct a HTTP Get URI with params
			message = URLEncoder.encode(message, "UTF-8");
			StringBuilder request = new StringBuilder();
			request.append(PUSH_NOTES_SEND_URL);
			request.append("regId=");
			request.append(registrationId);
			request.append("&message=");
			request.append(message);
			
			// Make HTTP Request
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(request.toString());

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

			// Close the stream
			s.close();

			// Parse the server response to check whether
			// or not Push Note was sent successfully
			if (data != null && data.length() > 0) {
				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(data);

					if (jsonObj != null) {
						int success = jsonObj.optInt("success", 0);
						int failure = jsonObj.optInt("failure", 1);

						if (success > 0 || failure <= 0) {
							ret = Boolean.valueOf(true);
						}
					}
				}

				catch (Exception e) {
					Log.e("PushNoteSenderTask",
							"Unable to parse response from Push Note Send request");
				}
			}

			else {
				Log.e("PushNoteSenderTask",
						"Empty response from Push Note Send request");
			}
		}
		
		catch(Exception e) {
			Log.e("PushNoteSenderTask", String.format("Exception when sending Push Note Send request. Exception: %s", e.getMessage()));
		}
		
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	public void onPostExecute(Boolean result) {
		if(receiver != null) {
			// Invoke the callback
			receiver.onPushNoteSent(result.booleanValue());
		}
	}
}
