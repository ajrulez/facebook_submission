package com.alifesoftware.assignment.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.alifesoftware.assignment.R;
import com.alifesoftware.assignment.interfaces.IImageDownloadReceiver;
import com.alifesoftware.assignment.model.FriendUserData;
import com.alifesoftware.assignment.ui.ProgressBarEx;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class ProfilePictureDownloadTask extends AsyncTask<String, Bitmap, Void> {
	// Context for Progress
	private Context context;
	
	// Number of Images downloaded
	private int downloadedImageCount = 0;
	
	// Number of total Images to download
	private int totalImageCount = 0;
	
	// Progress Dialog
	private ProgressBarEx progress = null; 

	// Image Download Receiver
	private IImageDownloadReceiver receiver;
	
	// URL Format
	private static final String PROFILE_PICTURE_URL = "https://graph.facebook.com/%s/picture?type=normal";
	
	// Current User Id
	private String currentUserId;
	
	
	// Constructor
	public ProfilePictureDownloadTask(Context ctx, IImageDownloadReceiver rcvr) {
		// Set the context
		context = ctx;
		
		// Set the IImageDownloadReceiver object
		receiver = rcvr;
		
		// Set up Progress Dialog
		if (context != null) {
			this.progress = new ProgressBarEx(context);
			progress.setIndeterminate(true);
			progress.setTitle("Please Wait");
			progress.setIcon(context.getResources().getDrawable(
					R.drawable.ic_launcher));
			progress.setMessage("Fetching Profile Images...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
	}
	
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
	protected Void doInBackground(String... userIds) {
		totalImageCount = userIds.length;
		
		if(progress != null) {
			//progress.setTotalSize = totalImageCount;
		}
		
		for (String userId : userIds) {
			if (!isCancelled()) {
				currentUserId = userId;
				String url = String.format(PROFILE_PICTURE_URL, currentUserId);
				Bitmap bitmap = downloadFile(url);
				publishProgress(bitmap);
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
	 */
	@Override
	protected void onProgressUpdate(Bitmap... bitmaps) {
		super.onProgressUpdate(bitmaps);
		if (context != null && 
				progress != null) {
			//progress.setProgress(++downloadedImageCount);
			FriendUserData.putIntoCache(currentUserId, bitmaps[0]);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void ignore) {
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
		receiver.onImageDownloadComplete();
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
		receiver.onImageDownloadComplete();
	}
	
	/**
	 * Method to get image from a URL and convert
	 * it to a bitmap
	 * 
	 * @param url
	 * @return Bitmap object
	 * 
	 */
	private Bitmap downloadFile(String url) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
		}
		
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}
