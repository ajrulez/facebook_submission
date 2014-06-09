package com.alifesoftware.assignment;

import java.text.DecimalFormat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MapActivity extends FragmentActivity {
	// Google Map
	private GoogleMap map;
	
	// Location of user
	private LatLng userLocation;
	
	// Location Name
	private String locationName;
	
	// Intent Key for Location Lat
	public static String INTENT_LOCATION_LAT_KEY = "location_lat";
	
	// Intent Key for Location Lng
	public static String INTENT_LOCATION_LNG_KEY = "location_lng";
	
	// Intent Key for Location Name
	public static String INTENT_LOCATION_NAME_KEY = "location_name";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map_fragment);

        // Get a handle to the Map Fragment
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        
        // Check if the intent is valid and it has extras
        if(getIntent() != null &&
        		getIntent().getExtras() != null) {
        	// Get Lat and Lng from Intent Extras Bundle
        	String lat = getIntent().getExtras().getString(INTENT_LOCATION_LAT_KEY);
        	String lng = getIntent().getExtras().getString(INTENT_LOCATION_LNG_KEY);
        	
        	// Create LatLng userLocation
        	if(lat != null &&
        			lat.length() >= 2 &&
        			lng != null &&
        			lng.length() >= 2) {
        		// Google Maps has problems showing negative LatLng
        		// Kinda sucks...
        		userLocation = new LatLng(doubleToThreeDecimals(Double.parseDouble(lat)), doubleToThreeDecimals(Double.parseDouble(lng)));
        	}
        	
        	locationName = getIntent().getExtras().getString(INTENT_LOCATION_NAME_KEY);
        	
        	if(locationName == null) {
        		locationName = "";
        	}
        }
        
        if(userLocation != null) {
        	map.addMarker(new MarkerOptions()
                	.title(locationName)
                	.snippet("Your Location as per Facebook")
                	.position(userLocation));
        	
        	// Move the camera instantly to User Location with a zoom of 15.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
    }
    
    /**
     * Method to Format a double to exactly three decimal places
     * 
     */
    private double doubleToThreeDecimals(double value) {
    	DecimalFormat df = new DecimalFormat("#.000"); 
    	String newVal = df.format(value);
    	
    	return Double.parseDouble(newVal);
    }
}