package com.alifesoftware.assignment.ui;

import com.alifesoftware.assignment.R;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * This class is used to implement a custom ProgressDialog
 * 
 * Note: This class is copied from A-Life Software, StockTrainer
 * application developed by Anuj Saluja
 * 
 */
public class ProgressBarEx extends ProgressDialog {
	public ProgressBarEx(Context context) {
		super(context);
	
		// Set Interminate = true
		setIndeterminate(true);
		
		// Set interminate drawable i.e. the custom spin wheel
		setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progressbar_indeterminate));
		
		// Set the Icon
		setIcon(context.getResources().getDrawable(R.drawable.ic_launcher));
		
		// Set spinner style
		setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		// Uncomment this if you want a Horizontal Progress Bar
		//
		//setProgressStyle(this.STYLE_HORIZONTAL);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.ProgressDialog#onStart()
	 */
	public void onStart() {
		super.onStart();
	}
}
