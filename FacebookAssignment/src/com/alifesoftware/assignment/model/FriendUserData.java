package com.alifesoftware.assignment.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is a model for data that we need for
 * a friend object
 *
 * This class implements Parceable interface because
 * an object of this class has to be added to a Bundle
 * and passed on as an argument.
 * 
 */
public class FriendUserData implements Parcelable {
	// Name
	private String name;
	
	// ID
	private String id;
	
	// Picture URL
	private String displayPictureUrl;
	
	// Image Height
	private int pictureHeight;
	
	// Image Width
	private int pictureWidth;
	
	// Is Silhouette
	private boolean isSilhouette;

	// Accessors and Mutators
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayPictureUrl() {
		return displayPictureUrl;
	}

	public void setDisplayPictureUrl(String displayPictureUrl) {
		this.displayPictureUrl = displayPictureUrl;
	}

	public int getPictureHeight() {
		return pictureHeight;
	}

	public void setPictureHeight(int pictureHeight) {
		this.pictureHeight = pictureHeight;
	}

	public int getPictureWidth() {
		return pictureWidth;
	}

	public void setPictureWidth(int pictureWidth) {
		this.pictureWidth = pictureWidth;
	}

	public boolean isSilhouette() {
		return isSilhouette;
	}

	public void setSilhouette(boolean isSilhouette) {
		this.isSilhouette = isSilhouette;
	}
	
	// Creator Method for Parcelable
	//
	public static final Parcelable.Creator<FriendUserData> CREATOR = new Parcelable.Creator<FriendUserData>() {
		public FriendUserData createFromParcel(Parcel in) {
			return new FriendUserData(in);
		}

		public FriendUserData[] newArray(int size) {
			return new FriendUserData[size];
		}
	};
		
	// Default Constructor
	public FriendUserData() {
		name = "";
		id = "";
		displayPictureUrl = "";
		pictureHeight = 500;
		pictureWidth = 500;
		isSilhouette = false;
	}
	
	// Constructor with Parcel
	public FriendUserData(Parcel in) {
		// Must be done in order
		//
		//
		name = in.readString();
		id = in.readString();
		displayPictureUrl = in.readString();
		pictureHeight = in.readInt();
		pictureWidth = in.readInt();
		isSilhouette = in.readByte() != 0;     // isSilhouette == true if byte != 0
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(id);
		dest.writeString(displayPictureUrl);
		dest.writeInt(pictureHeight);
		dest.writeInt(pictureWidth);
		dest.writeByte((byte) (isSilhouette ? 1 : 0));     //if isSilhouette == true, byte == 1
	}
}
