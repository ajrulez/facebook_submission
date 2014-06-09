package com.alifesoftware.assignment;

import com.alifesoftware.assignment.pushnotes.GCMIntentService;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends Activity {
	// Text View for Push Note Text
	TextView tvPushNoteText;
	
	// Button for Dismissing Alert Dialog
	Button btnDismissDialog;
	
	// Push Note text String
	String pushNoteMessage;
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the Layout
		setContentView(R.layout.pushnote_alert_dialog_layout);
		
		// Get the extras Bundle from Intent
		if(getIntent() != null &&
				getIntent().getExtras() != null) {
			Bundle extras = getIntent().getExtras();
			
			// Get Push Note Text from Intent Extras
			String pushNoteText = extras.getString(GCMIntentService.PUSH_NOTE_TEXT_KEY);
			pushNoteMessage = pushNoteText;
		}
		
		// Set up the Widgets
		tvPushNoteText = (TextView) findViewById(R.id.pushNoteText);
		btnDismissDialog = (Button) findViewById(R.id.dismissPushNoteButton);
		btnDismissDialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Finish the activity
				finish();
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();
		
		// Show the Dialog
		String formattedPushNoteMessage = this.getString(R.string.pushNoteText);
		
		if(formattedPushNoteMessage != null && 
				formattedPushNoteMessage.length() > 0) {
			formattedPushNoteMessage = String.format(formattedPushNoteMessage, pushNoteMessage);
		}
		
		else {
			formattedPushNoteMessage = pushNoteMessage;
		}
		
		// Set the Text
		tvPushNoteText.setText(formattedPushNoteMessage);
	}

}
