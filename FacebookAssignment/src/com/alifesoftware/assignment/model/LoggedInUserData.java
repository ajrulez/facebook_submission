package com.alifesoftware.assignment.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is a model for data that we need for
 * a logged in user.
 *
 * This class implements Parceable interface because
 * an object of this class has to be added to a Bundle
 * and passed on as an argument.
 * 
 */
public class LoggedInUserData implements Parcelable {
	
	// Class for Education details
	public static class Education implements Parcelable {
		// Education type
		private String type;
		
		// Education Year
		private String year;
		
		// Name of the School
		private String school;
		
		// List of concentrations - A user can have multiple
		// concentrations from a school for the same year
		// For example, a major in computer science and a minor
		// in economics
		private List<String> concentration = new ArrayList<String> ();
		
		// Accessors and Mutators
		//
		public String getType() {
			return type;
		}
	
		public void setType(String type) {
			this.type = type;
		}
	
		public String getYear() {
			return year;
		}
	
		public void setYear(String year) {
			this.year = year;
		}
	
		public String getSchool() {
			return school;
		}
	
		public void setSchool(String school) {
			this.school = school;
		}
	
		public List<String> getConcentration() {
			return concentration;
		}
	
		public void setConcentration(List<String> concentration) {
			this.concentration = concentration;
		}
		
		// Creator Method for Parcelable
		//
		public static final Parcelable.Creator<Education> CREATOR
		= new Parcelable.Creator<Education>() {
			public Education createFromParcel(Parcel in) {
				return new Education(in);
			}
			
			public Education[] newArray(int size) {
				return new Education[size];
			}
		};
		
		// Default Constructor
		public Education() {
			type = "";
			year = "";
			school = "";
		}
		
		// Constructor with Parcel
		private Education(Parcel in) {
			// Must be done in order
			//
			//
			type = in.readString();
			year = in.readString();
			school = in.readString();
			in.readStringList(concentration);
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(type);
			dest.writeString(year);
			dest.writeString(school);
			dest.writeStringList(concentration);
		}

		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	// User's name
	private String name;
	
	// User's location
	private String location;
	
	// User's display picture URL
	private String displayPictureUri;
	
	// User's ID
	private String userId;
	
	// User's list of education
	// Each education item contains type, year, school name, 
	// and a list of concentrations (see above)
	private List<Education> education = new ArrayList<Education> ();

	// Accessors and Mutators
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDisplayPictureUri() {
		return displayPictureUri;
	}

	public void setDisplayPictureUri(String displayPictureUri) {
		this.displayPictureUri = displayPictureUri;
	}

	public List<Education> getEducation() {
		return education;
	}

	public void setEducation(List<Education> education) {
		this.education = education;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	// Creator Method for Parcelable
	//
	public static final Parcelable.Creator<LoggedInUserData> CREATOR
	= new Parcelable.Creator<LoggedInUserData>() {
		public LoggedInUserData createFromParcel(Parcel in) {
			return new LoggedInUserData(in);
		}
		
		public LoggedInUserData[] newArray(int size) {
			return new LoggedInUserData[size];
		}
	};
	
	// Default Constructor
	public LoggedInUserData() {
		name = "";
		location = "";
		displayPictureUri = "";
		userId = "";
	}
	
	// Constructor with Parcel
	private LoggedInUserData(Parcel in) {
		// Must be done in order
		//
		//
		name = in.readString();
		location = in.readString();
		displayPictureUri = in.readString();
		userId = in.readString();
		in.readList(education, Education.class.getClassLoader());
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(location);
		dest.writeString(displayPictureUri);
		dest.writeString(userId);
		dest.writeList(education);
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}
}
