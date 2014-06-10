package com.alifesoftware.assignment;

/**
 * This is the super class for all Fragments that
 * we will use in this app. All fragments that we use
 * need to have certain common properties, and having
 * a super class helps with that.
 * 
 */
import java.util.concurrent.atomic.AtomicBoolean;

import com.alifesoftware.assignment.interfaces.IUserIntefaceUpdater;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class BaseHeadlessFragment extends Fragment
								  implements OnClickListener {
	// Current Fragment
	protected String tagFragmentName = "";
		
	// Flag to see Fragment is Attached to Activity
	protected AtomicBoolean fragmentAttached = new AtomicBoolean(false);
	
	// IUserInterfaceUpdater implementation
	protected IUserIntefaceUpdater uiUpdater;
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		fragmentAttached.compareAndSet(false, true);
		Log.d(tagFragmentName, "Attached to Activity");
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		fragmentAttached.compareAndSet(true, false);
		Log.d(tagFragmentName, "No longer attached to Activity");
	}
	
	/**
	 * Method to get the Fragment name
	 * 
	 * @return Name of the fragment
	 */
	public String getFragmentName() {
		return tagFragmentName;
	}
	
	/**
	 * Utility method to get color from a resource ID
	 * 
	 */
	protected int getColor(int colorResourceId) {
		return getResources().getColor(colorResourceId);
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// No base implementation. Classes that extend
		// this class may provide onClick if needed
	}
	
	/**
	 * Method to set IUserInterfaceUpdater for this
	 * Fragment
	 * 
	 * @param uiInterfaceUpdater - An object that implements
	 * 		IUserInterfaceUpdater interface
	 * 
	 */
	public void setUserInterfaceUpdater(IUserIntefaceUpdater uiUpdater) {
		this.uiUpdater = uiUpdater;
	}
}
