package com.alifesoftware.assignment.tasks;

import java.net.URL;

import com.alifesoftware.assignment.R;
import com.alifesoftware.assignment.interfaces.IBitmapReceiver;
import com.alifesoftware.assignment.ui.ProgressBarEx;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

/**
 * This class extends an AsyncTask that is used to 
 * get a Bitmap object from HTTP URL of a user's
 * display picture
 *
 */
public class UrlToBitmapTask extends AsyncTask<String, Void, Bitmap> {
	
	// IBitmapConverter Notification Receiver
	private IBitmapReceiver bitmapReceiver = null;
	
	// Progress Dialog
	private ProgressBarEx progress = null; 
	
	// Context
	private Context context;

	// Constructor
	public UrlToBitmapTask(Context context, IBitmapReceiver receiver) {
		this.context = context;
		this.bitmapReceiver = receiver;

		// Set up Progress Dialog
		if (context != null) {
			this.progress = new ProgressBarEx(context);
			progress.setIndeterminate(true);
			progress.setTitle("Please Wait");
			progress.setIcon(context.getResources().getDrawable(R.drawable.ic_launcher));
			progress.setMessage("Fetching Bitmap...");
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

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bmp = null;
		
		try {
			if(params != null &&
					params.length > 0) {
				// Only one param - URL to user's display picture
				String urlString = params[0];
				URL url = new URL(urlString);
				
				// Use the bitmap factory to convert a HTTP response to HTTP
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			}
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return bmp;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Bitmap bmp) {
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
		bitmapReceiver.onConversionComplete(bmp);
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
		bitmapReceiver.onConversionComplete(null);
	}	
}
