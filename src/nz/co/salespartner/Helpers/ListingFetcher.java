package nz.co.salespartner.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nz.co.salespartner.Objects.ListingRecord;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

public class ListingFetcher {
  
	private Context context;

	public ListingFetcher(Context c){
		context = c;
	}
	

	
	public static ArrayList<ListingRecord> fetchAllListings(Context co){
		ArrayList<ListingRecord> Listings = new ArrayList<ListingRecord>();
		String tableName = "listings"; //define table
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(co);
		mDbHelper.open();
		Cursor c = mDbHelper.fetchAllListings();
		c.moveToFirst();
		while(!c.isAfterLast())
		{
			ListingRecord listing = new ListingRecord();
			listing.FromCursor(c);
			Listings.add(listing);
			c.moveToNext();
		}
		c.close();
		mDbHelper.close();
		return Listings;
	}
	
	

}
