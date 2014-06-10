package com.alifesoftware.assignment.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

	private static boolean isNetAvailable(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    
			if (netInfo != null && netInfo.isConnected()) {
				return true;
			}
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	    
	    return false;
	}
	
	
	public static boolean isOnline(Context context) {
		try {
			ConnectivityManager connectivityManager = 
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
			NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            
            if(wifiInfo != null && 
            		wifiInfo.isConnected()) {
                return true;
            }
            
            if(mobileInfo != null && 
            		mobileInfo.isConnected()) {
            	return true;
            }
            
            if(netInfo != null &&
            		netInfo.isConnected()) {
            	return true;
            }
            
        }
		
        catch(Exception e) {
           e.printStackTrace();
        }
		
        return isNetAvailable(context);
    }
}
