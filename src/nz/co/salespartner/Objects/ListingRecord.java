package nz.co.salespartner.Objects;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.embarcadero.javaandroid.DBXException;
import com.embarcadero.javaandroid.TJSONArray;
import com.embarcadero.javaandroid.TJSONObject;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

public class ListingRecord {
	
	private static final String TAG = "ListingRecord";
	
	public ListingRecord() {
		
	};
	
	public String ref;
	public String DisplayPrice;
	public int SearchPrice;
	public int Bedrooms;
	public int Bathrooms;
	public int Garaging;
	public int LivingAreas;
	public String ListingStatus;
	public String ListedDate;
	public String ExpiryDate;
	public String Tenancy;
	public String LegalDescription;
	public String KeyDetails;
	public String PrivateFeatures;
	public String Heading;
	public String Advert;
	public String Features;
	public String VendorGreeting;
	public String VendorTitInit;
	public String VendorSurname;
	public String VendorPh1;
	public String VendorPh2;
	public String VendorPh3;
	public String Fax;
	public String VendorPh1Type;
	public String VendorPh2Type;
	public String VendorPh3Type;
	public String VendorStNo;
	public String VendorStreet;
	public String VendorSuburb;
	public String VendorEmail;
	public String StNo;
	public String Street;
	public String Suburb;
	public String District;
	public int GV;
	public int LV;
	public double FloorArea;
	public double LandArea;
	public String FloorAreaUnit;
	public String LandAreaUnit;
	public String Rates;
	public String ChangeDate;
	public int ServerPictureCount;
	public String documentlist;
	public int Favourite;
	public String ListedBy;
	public String WebLink;
	public int OpenHomeDuration;
	public int OpenHomeTime;
	
	
	public int lat;
	public int lng;
	public int id;
	public int documentcount;
	public String ListedBy2 = "";
	public String SellingPoints;
	public String Age;

	public void ResetAllFields() {
		ref = "";
		DisplayPrice = "";
		SearchPrice = 0;
		Bedrooms = 0;
		Bathrooms = 0;
		Garaging = 0;
		LivingAreas = 0;
		ListingStatus = "";
		ListedDate = "";
		ExpiryDate = "";
		Tenancy = "";
		LegalDescription = "";
		KeyDetails = "";
		PrivateFeatures = "";
		Heading = "";
		Advert = "";
		Features = "";
		VendorGreeting = "";
		VendorTitInit = "";
		VendorSurname = "";
		VendorPh1 = "";
		VendorPh2 = "";
		VendorPh3 = "";
		Fax = "";
		VendorPh1Type = "";
		VendorPh2Type = "";
		VendorPh3Type = "";
		VendorStNo = "";
		VendorStreet = "";
		VendorSuburb = "";
		VendorEmail = "";
		StNo = "";
		Street = "";
		Suburb = "";
		District = "";
		GV = 0;
		LV = 0;
		FloorArea = 0;
		LandArea = 0;
		FloorAreaUnit = "";
		LandAreaUnit = "";
		Rates = "";
		ChangeDate = "";
		ServerPictureCount = 0;
		documentlist = "";
		Favourite = 0;
		ListedBy = "";
		lat = 0;
		lng = 0;
		id = 0;
		documentcount = 0;
		WebLink = "";
		OpenHomeDuration = 0;
		OpenHomeTime = 0;
	};

	
		
	/** method called for new REST server*/
	public String FromTJSONObject(TJSONObject x) {
		try {
			ref = x.getString("ref");
			DisplayPrice = x.getString("DisplayPrice");
		
			SearchPrice = x.getInt("SearchPrice");
			Bedrooms = x.getInt("Bedrooms");
			Bathrooms = x.getInt("Bathrooms");
			Garaging = x.getInt("Garaging");
			LivingAreas = x.getInt("LivingAreas");
			ListingStatus = x.getString("ListingStatus");
			ListedDate = x.getString("ListedDate");
			ExpiryDate = x.getString("ExpiryDate");
			Tenancy = x.getString("Tenancy");
			LegalDescription = x.getString("LegalDescription");
			KeyDetails = x.getString("KeyDetails");
			PrivateFeatures = x.getString("PrivateFeatures");
			Heading = x.getString("Heading");		
			Advert = x.getString("Advert");
			Features = x.getString("Features");
			SellingPoints = x.getString("SellingPoints");
			VendorGreeting = x.getString("VendorGreeting");			
			ListedBy = x.getString("ListBy");			
			ListedBy2 = x.getString("ListBy2");			
			VendorTitInit = x.getString("VendorTitInit");
			VendorSurname =  x.getString("VendorSurname");
			VendorPh1 = x.getString("VendorPh1");
			VendorPh2 = x.getString("VendorPh2");
			VendorPh3 = x.getString("VendorPh3");
			Fax = x.getString("VendorFax");
			VendorPh1Type = x.getString("VendorPh1Type");
			VendorPh2Type = x.getString("VendorPh2Type");
			VendorPh3Type = x.getString("VendorPh3Type");
			VendorStNo = x.getString("VendorStNo");
			VendorStreet = x.getString("VendorStreet");
			VendorSuburb = x.getString("VendorSuburb");
			StNo = x.getString("StNo");
			Street = x.getString("Street");
			Suburb = x.getString("Suburb");
			District = x.getString("District");
			GV = x.getInt("GV");
			LV = x.getInt("LV");
			FloorArea = x.getDouble("FloorArea");
			LandArea = x.getDouble("LandArea");
			FloorAreaUnit = x.getString("FloorAreaUnit");
			LandAreaUnit = x.getString("LandAreaUnit");
			Rates = x.getString("Rates");
			ChangeDate = x.getString("ChangeDate");
			ServerPictureCount = x.getInt("PictureCount");
			OpenHomeDuration = x.getInt("OpenHomeDuration");
			OpenHomeTime = x.getInt("OpenHomeTime");
			WebLink = x.getString("WebLink");
			Age = x.getString("Age");
			ServerPictureCount = x.getInt("PictureCount");
			Favourite = 0;  //TODO make variable
			documentcount = 0;  //TODO make variable
			//email
			try{
			TJSONArray vea = x.getJSONArray("VendorEmail");
				for(int i = 0; i < vea.size(); i++)
				{
					if(i>0)
						VendorEmail +=", ";
					VendorEmail += vea.getString(i);
				}
				
			}catch (Exception e) {
				VendorEmail = "";
				e.printStackTrace();
			}
			
			TJSONArray Emails = x.getJSONArray("VendorEmail");
			if(Emails.size()>0)
				VendorEmail = Emails.getString(0);
			else   
				VendorEmail = "";
			
			
			
			
			return "";
		} catch (Exception je) {
			Log.i(TAG,"failed at buildFromJSON");
			return je.getMessage();			
		}
	}
	
	private String FieldFromCursor(Cursor c, String FieldName) {
		int i = c.getColumnIndex(FieldName);
		if (-1 == i) {
			Log.w(TAG, "No FieldName " + FieldName);
			return "";
		} else {
			return c.getString(i);
		}
		
	}

	public void FromCursor(Cursor c) {
		ref = FieldFromCursor(c, "ref");		
		DisplayPrice = FieldFromCursor(c, "DisplayPrice");
		ListedBy = FieldFromCursor(c, "ListedBy");
		ListedBy2 = FieldFromCursor(c, "ListedBy2");
		SearchPrice = c.getInt(c.getColumnIndex("SearchPrice"));		
		Bedrooms = c.getInt(c.getColumnIndex("Bedrooms"));
		Bathrooms = c.getInt(c.getColumnIndex("Bathrooms"));
		Garaging = c.getInt(c.getColumnIndex("Garaging"));
		LivingAreas = c.getInt(c.getColumnIndex("LivingAreas"));
		ListingStatus = FieldFromCursor(c, "ListingStatus");
		ListedDate = FieldFromCursor(c, "ListedDate");
		ExpiryDate = FieldFromCursor(c, "ExpiryDate");
		Tenancy = FieldFromCursor(c, "Tenancy");	
		LegalDescription = FieldFromCursor(c, "LegalDescription");
		KeyDetails = FieldFromCursor(c, "KeyDetails");
		PrivateFeatures = FieldFromCursor(c, "PrivateFeatures");
		Heading = FieldFromCursor(c, "Heading");
		Advert = FieldFromCursor(c, "Advert");
		Features = FieldFromCursor(c, "Features");
		SellingPoints = FieldFromCursor(c, "SellingPoints");
		Age = FieldFromCursor(c, "Age");
		VendorGreeting = FieldFromCursor(c, "VendorGreeting");
		VendorTitInit = FieldFromCursor(c, "VendorTitInit");
		VendorSurname = FieldFromCursor(c, "VendorSurname");
		VendorPh1 = FieldFromCursor(c, "VendorPh1");
		VendorPh2 = FieldFromCursor(c, "VendorPh2");
		VendorPh3 = FieldFromCursor(c, "VendorPh3");
		Fax = FieldFromCursor(c, "Fax");
		VendorPh1Type = FieldFromCursor(c, "VendorPh1Type");
		VendorPh2Type = FieldFromCursor(c, "VendorPh2Type");
		VendorPh3Type = FieldFromCursor(c, "VendorPh3Type");
		VendorStNo = FieldFromCursor(c, "VendorStNo");
		VendorStreet = FieldFromCursor(c, "VendorStreet");
		VendorSuburb = FieldFromCursor(c, "VendorSuburb");		
		VendorEmail = FieldFromCursor(c, "VendorEmail");
		StNo = FieldFromCursor(c, "StNo");
		Street = FieldFromCursor(c, "Street");
		Suburb = FieldFromCursor(c, "Suburb");
		District = FieldFromCursor(c, "District");		
		GV = c.getInt(c.getColumnIndex("GV"));
		LV = c.getInt(c.getColumnIndex("LV"));
		Favourite = c.getInt(c.getColumnIndex("Favourite")); //TODO

		FloorArea = c.getDouble(c.getColumnIndex("FloorArea"));
		LandArea = c.getDouble(c.getColumnIndex("LandArea"));
		FloorAreaUnit = FieldFromCursor(c, "FloorAreaUnit");
		LandAreaUnit = FieldFromCursor(c, "LandAreaUnit");
		Rates = FieldFromCursor(c, "Rates");
		ChangeDate = c.getString(c.getColumnIndex("ChangeDate"));
		ServerPictureCount = 0;
		lat = c.getInt(c.getColumnIndex("Latitude"));
		lng = c.getInt(c.getColumnIndex("Longitude"));
		documentcount = c.getInt(c.getColumnIndex("DocumentCount"));
		
		WebLink = c.getString(c.getColumnIndex("WebLink"));
		OpenHomeDuration = c.getInt(c.getColumnIndex("OpenHomeDuration"));
		OpenHomeTime = c.getInt(c.getColumnIndex("OpenHomeTime"));
		
	}


	/**
	 * converts UNIX epoch to string 
	 */
	public String getOpenHomeTime(Context context) {
		
		long longtime = (long)OpenHomeTime;
		longtime *= 1000;
		longtime -= 13*1000*60*60; // for GMT
		Date d = new Date(longtime);  
        SimpleDateFormat day = new SimpleDateFormat("dd MMM ' at ' HH:mm");
		return day.format(d);

	}



	public String getListedBy() {
		String list = ListedBy;
		if(!TextUtils.isEmpty(ListedBy2) && ListedBy2!=null)
			list += ", "+ListedBy2;
		return list;
	}

	

}
