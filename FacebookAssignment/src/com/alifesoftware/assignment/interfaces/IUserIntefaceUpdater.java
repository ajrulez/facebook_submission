package com.alifesoftware.assignment.interfaces;

/**
 * This interface includes several methods that
 * are used to update the user interface
 *
 */
public interface IUserIntefaceUpdater {
	// Method to update the User Interface with Friends List
	public void showFriendsList();
	
	// Method to update the User Interface with Friend's display picture
	public void showFriendDisplayImage(String pictureUrl);
	
	// Method to show an alert dialog
	public void showAlert(String title, String message);
	
	// Method to show Send Push Note Dialog
	public void showPushNoteDialog();
	
	// Method to show user location on Map
	public void showLocation(String location);
}
