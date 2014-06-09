package com.alifesoftware.assignment.pushnotes;
import com.alifesoftware.assignment.NotificationActivity;
import com.alifesoftware.assignment.R;
import com.google.android.gcm.GCMBaseIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This class extends GCMBaseIntentService, and 
 * provides events that are called by GCM service 
 * to indicate Push Note state
 */
public class GCMIntentService extends GCMBaseIntentService {
	
	// GCM Sender ID
	public static final String GCM_SENDER_ID = "597632117822";
	
	// Notification ID
	private static final int NOTIFICATION_ID = 1213;
	
	// Push Note Text Intent Key
	public static final String PUSH_NOTE_TEXT_KEY = "push_note_text_key";

	// Constructor
    public GCMIntentService() {
        super(GCM_SENDER_ID);
    }

    /**
     * Method called on Error
     */
	@Override
	protected void onError(Context context, String error) {
		Log.d("GCMIntentService", String.format("Error in GCM - %s", error));
	}

	
    /**
     * Method called on Receiving a new message
     */
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d("GCMIntentService", "GCM - Message Received");
		
        String message = intent.getExtras().getString("message");

        // Notifies user
        generateNotification(context, message);
	}

	
    /**
     * Method called on device registered
     */
	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i("GCMIntentService", "GCM - Device Registered");
	}

	
    /**
     * Method called on device un registred
     */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i("GCMIntentService", "GCM - Device Un-Registered");
	}

	
    /**
     * Method called on receiving a deleted message
     */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i("GCMIntentService", "GCM - Received Deleted Messages Notification");
    }
    
    
    /**
     * Method called on Recoverable Error
     */
    @Override
    protected boolean onRecoverableError(Context context, String error) {
    	Log.w("GCMIntentService", String.format("Recoverable Error in GCM - %s", error));
    	
        return super.onRecoverableError(context, error);
    }
    
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
	private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        
        // Get notification manager
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        notificationIntent.putExtra(PUSH_NOTE_TEXT_KEY, message);
        
        // Set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                						Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIintent =
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        // Try switching PendingIntent.FLAG_UPDATE_CURRENT to PendingIntent.FLAG_CANCEL_CURRENT if it doesn't work
        // I found an isue related to PendingIntents not firing on Kitkat
        //
        // https://code.google.com/p/android/issues/detail?id=61850
        //
        
        NotificationCompat.Builder mBuilder =
        	    new NotificationCompat.Builder(context)
        	    .setSmallIcon(icon)
        	    .setContentTitle("Facebook Assignment")
        	    .setContentText(message)
        	    .setContentIntent(pendingIintent)
        	    .setWhen(when)
        	    .setAutoCancel(true);
        
        // Create a notification
        Notification notification = mBuilder.build();
        
        // Turn Sound On, If Enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        
        // Turn Vibration, If Enabled
        notification.defaults |= Notification.DEFAULT_SOUND;
        
        notificationManager.notify(NOTIFICATION_ID, notification);   
    }
}
