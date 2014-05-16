package nz.co.salespartner.Helpers;

import java.util.ArrayList;
import java.util.List;

import nz.co.salespartner.Objects.ListingFilterOptions;
import nz.co.salespartner.Objects.ListingRecord;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class SalesPartnerDbAdapter {

	public static final String KEY_REFERENCE = "ref";
	public static final String KEY_ROWID = "_id";

	public static final int ORDER_REF = 0;
	public static final int ORDER_PRICE = 1;
	public static final int ORDER_STREET = 2;
	public static final int ORDER_SUBURB = 3;
	public static final int ORDER_LISTEDBY = 4;
	public static final int ORDER_LISTEDDATE = 5;
	public static final int NOT_FAVOURITE = 0;
	public static final int FAVOURITE = 1;

	private static final String TAG = "SalesPartnerDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
 
	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table listings (_id integer primary key autoincrement, "
			+ "ref text not null, DisplayPrice text, "
			+ "SearchPrice integer, Bedrooms integer, Bathrooms integer, Garaging integer, LivingAreas integer, "
			+ "ListingStatus text, ListedDate text, ExpiryDate text, Tenancy text, LegalDescription text, "
			+ "KeyDetails text, PrivateFeatures text, Heading text, Advert text, Features text, SellingPoints text, "
			+ "VendorGreeting text, VendorTitInit text, VendorSurname text, VendorPh1 text, VendorPh2 text, VendorPh3 text, Fax text, VendorPh1Type text, VendorPh2Type text, VendorPh3Type text, "
			+ "VendorStNo text, VendorStreet text, VendorSuburb text, VendorEmail text, "
			+ "StNo text, Street text, Suburb text, District text, GV integer, LV integer, FloorArea Real, LandArea Real, FloorAreaUnit text, LandAreaUnit text, Rates text, "
			+ "ChangeDate text, DeleteFlag integer, Age text, Favourite integer, ListedBy text, ListedBy2 text, Latitude integer, Longitude integer, DocumentCount integer, WebLink text, OpenHomeTime integer, OpenHomeDuration integer);";

	public static final String DATABASE_NAME = "salespartner.sqlite3";
	private static final String DATABASE_TABLE = "listings";
	private static final int DATABASE_VERSION = 6;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating Database");
			db.execSQL(DATABASE_CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			onCreate(db);
		}
		
		private void delete() {
//			this.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
//			onCreate(db);
		}
	}

	public static void deleteDB(){
//		mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
//		onCreate(db);
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public SalesPartnerDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         Initialisation call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public SalesPartnerDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteNote(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
 
	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllListings() {
		return mDb.rawQuery("Select * from listings", null);
	}

	private String buildQuery(int Order, List<String> where) {
		String q = "Select * from listings";

		if (where.size() > 0) {
			String andstr = "";
			q += " where ";
			for (String str : where) {
				q += andstr + (str);
				andstr = " and ";
			}
		}
		switch (Order) {
		case ORDER_REF:
			q += " order by ref";
			break;
		case ORDER_PRICE:
			q += " order by SearchPrice";
			break;
		case ORDER_STREET:
			q += " order by Street";
			break;
		case ORDER_SUBURB:
			q += " order by Suburb";
			break;
		case ORDER_LISTEDBY:
			q += " order by ListedBy";
			break;
		case ORDER_LISTEDDATE: //make order descending
			q += " order by ListedDate DESC";
			break;
		}
		return q;
	}

	public Cursor fetchListings(int Order, ListingFilterOptions Filter, String LISTEDBY) {
		List<String> where = new ArrayList<String>();
		Log.i("","Filter priceMin "+Filter.PriceMin+"\npriceMax "+Filter.PriceMax);
		if (Filter.FilterByPrice) {
			if (Filter.PriceMin >= 0) {
				where.add("SearchPrice >= " + Filter.PriceMin);
			}
			if (Filter.PriceMax > 0) {
				where.add("SearchPrice <= " + Filter.PriceMax);
			}
		}
		if (Filter.FilterByBedrooms) {
			if (Filter.BedroomsMin > 0) {
				where.add("Bedrooms >= " + Filter.BedroomsMin);
			}
			if (Filter.BedroomsMax > 0) {
				where.add("Bedrooms <= " + Filter.BedroomsMax);
			}
		}
		if(LISTEDBY.equalsIgnoreCase("All"))
		{
			//do nothing
		}
		else
		{
			where.add("ListedBy = '"+LISTEDBY+"'");
		}
		String q = buildQuery(Order, where);
		Log.i(TAG, "build query raw query = "+q);
		return mDb.rawQuery(q, null);
	}

	public Cursor fetchListingsMarkedForDeletion() {
		Cursor c = mDb.rawQuery(
				"select ref from listings where DeleteFlag = 1", null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchListing(long rowId) throws SQLException {
		Log.i(TAG, "fetching listing " + rowId);
		Cursor mCursor = mDb.rawQuery("select * from listings where _id = "
				+ rowId, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchListing(String ref) throws SQLException {
		Log.i(TAG, "fetching listing " + ref);
		Cursor mCursor = mDb.rawQuery("select * from listings where ref = \""
				+ ref + "\"", null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public String changeDateForListing(String ref) {
		Log.i(TAG, "Getting ChangeDate for listing: " + ref);
		Cursor c = mDb.rawQuery(
				"select ChangeDate from listings where ref = \"" + ref + "\"",
				null);
		String result = "";
		c.moveToFirst();

		if (c.getCount() > 0) 
			result = c.getString(c.getColumnIndex("ChangeDate"));

		c.close();
		return result;
	}

	public Boolean updateOrInsertListing(ListingRecord l) {
		// returns true if inserted a listing
		Boolean result;
		Cursor c = fetchListing(l.ref);
		if (c.getCount() == 0) {
			// insert listing
			insertListing(l);
			result = true;
		} else {
			// update listing
			updateListing(c.getInt(c.getColumnIndex("_id")), l);
			result = false;
		}
		c.close();
		return result;
	}

	public void updateListing(int _id, ListingRecord l) {
		Log.i(TAG, "Updating listing in Database: " + l.ref);
		ContentValues values = listingToContentValues(l);
		mDb.update("listings", values, "_id=?", new String[] { Integer
				.toString(_id) });
	}

	public void updateLatLong(ListingRecord l){
		Cursor c = fetchListing(l.ref);
		int id = c.getInt(c.getColumnIndex("_id"));
		c.close();
		ContentValues values = latlongToContentValues(l);
		mDb.update("listings", values, "_id=?", new String[] { Integer
				.toString(id) });
	}

	public void updateDocumentCount(ListingRecord l){
		Cursor c = fetchListing(l.ref);
		int id = c.getInt(c.getColumnIndex("_id"));
		c.close();
		ContentValues values = documentCountToContentValues(l);
		mDb.update("listings", values, "_id=?", new String[] { Integer
				.toString(id) });
	}
	public void updateFavourite(ListingRecord l){
		Cursor c = fetchListing(l.ref);
		int id = c.getInt(c.getColumnIndex("_id"));
		c.close();
		ContentValues values = favToContentValues(l);
		mDb.update("listings", values, "_id=?", new String[] { Integer
				.toString(id) });
	}

	private ContentValues latlongToContentValues(ListingRecord l) {
		ContentValues values = new ContentValues();
		values.put("Latitude", l.lat);
		values.put("Longitude", l.lng);
		return values;
	}

	private ContentValues documentCountToContentValues(ListingRecord l) {
		ContentValues values = new ContentValues();
		values.put("DocumentCount", l.documentcount);
		return values;
	}
	private ContentValues favToContentValues(ListingRecord l) {
		ContentValues values = new ContentValues();
		values.put("Favourite", Integer.toString(l.Favourite));
		return values;
	}

	/**makes content values to be inserted into database*/
	private ContentValues listingToContentValues(ListingRecord l) {
		ContentValues values = new ContentValues();
		values.put("ref", l.ref);
		values.put("DisplayPrice", l.DisplayPrice);
		values.put("SearchPrice", l.SearchPrice);
		values.put("Bedrooms", l.Bedrooms);
		values.put("Bathrooms", l.Bathrooms);
		values.put("Garaging", l.Garaging);
		values.put("LivingAreas", l.LivingAreas);
		values.put("ListingStatus", l.ListingStatus);
		values.put("ListedDate", l.ListedDate);
		values.put("ExpiryDate", l.ExpiryDate);
		values.put("Tenancy", l.Tenancy);
		values.put("LegalDescription", l.LegalDescription);
		values.put("KeyDetails", l.KeyDetails);
		values.put("PrivateFeatures", l.PrivateFeatures);
		values.put("Heading", l.Heading);
		values.put("Advert", l.Advert);
		values.put("Features", l.Features);
		values.put("SellingPoints", l.SellingPoints);
		values.put("VendorGreeting", l.VendorGreeting);
		values.put("VendorTitInit", l.VendorTitInit);
		values.put("VendorSurname", l.VendorSurname);
		values.put("VendorPh1", l.VendorPh1);
		values.put("VendorPh2", l.VendorPh2);
		values.put("VendorPh3", l.VendorPh3);
		values.put("Fax", l.Fax);
		values.put("VendorPh1Type", l.VendorPh1Type);
		values.put("VendorPh2Type", l.VendorPh2Type);
		values.put("VendorPh3Type", l.VendorPh3Type);
		values.put("VendorStNo", l.VendorStNo);
		values.put("VendorStreet", l.VendorStreet);
		values.put("VendorSuburb", l.VendorSuburb);
		values.put("VendorEmail", l.VendorEmail);
		values.put("StNo", l.StNo);
		values.put("Street", l.Street);
		values.put("Suburb", l.Suburb);
		values.put("District", l.District);
		values.put("GV", l.GV);
		values.put("LV", l.LV);
		values.put("FloorArea", l.FloorArea);
		values.put("LandArea", l.LandArea);
		values.put("FloorAreaUnit", l.FloorAreaUnit);
		values.put("LandAreaUnit", l.LandAreaUnit);
		values.put("Rates", l.Rates);
		values.put("ChangeDate", l.ChangeDate);
		values.put("DeleteFlag", 0);
		values.put("Favourite", Integer.toString(l.Favourite));
		values.put("ListedBy", l.ListedBy);
		values.put("ListedBy2", l.ListedBy2);
		values.put("Latitude", l.lat);
		values.put("Longitude", l.lng);
		values.put("DocumentCount", l.documentcount);
		values.put("WebLink", l.WebLink);
		values.put("OpenHomeDuration", l.OpenHomeDuration);
		values.put("OpenHomeTime", l.OpenHomeTime);
		return values;
	}

	private void insertListing(ListingRecord l) {
		Log.i(TAG, "Inserting listing into Database: " + l.ref);
		ContentValues values = listingToContentValues(l);
		try {
			mDb.insertOrThrow("listings", null, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void markAllForDeletion() {
		Log.i(TAG, "Marking all listings for deletion");
		mDb.execSQL("update listings set DeleteFlag = 1");
	}

	public void unmarkForDeletion(String ref) {
		Log.i(TAG, "Unmarking listing for (not) deletion " + ref);
		mDb.execSQL("update listings set DeleteFlag = 0 where ref = \"" + ref + "\"");				
	}

	public void deleteListingsMarkedForDeletion() {
		Log.i(TAG, "Deleting listings marked for deletion");
		mDb.execSQL("delete from listings where DeleteFlag = 1");

	}

	/** search db for LIKE passed string, return cursor */
	public Cursor fetchFavourites(int Order) {
											//defines criteria for a search
		Log.i("","order = "+Order);
		String q = "SELECT * FROM listings WHERE favourite = "+ FAVOURITE+switchOnOrder(Order);
		Log.i("", "favs raw query = "+q);
		   Cursor cur = mDb.rawQuery(q,  null);
		   return cur;

	}
	
	private String switchOnOrder(int Order){
		String result = " ORDER BY ListedBy";
		switch (Order) {
		case ORDER_REF:
			result = " ORDER BY ref";
			break;
		case ORDER_PRICE:
			result = " ORDER BY SearchPrice";
			break;
		case ORDER_STREET:
			result = " ORDER BY Street";
			break;
		case ORDER_SUBURB:
			result = " ORDER BY Suburb";
			break;
		case ORDER_LISTEDBY :
			result = " ORDER BY ListedBy";
			break;
		}
		return result;
	}

	public Cursor fetchSameSuburb(String suburb) {
			//defines criteria for a search
	Cursor cur = mDb.rawQuery("SELECT * FROM listings WHERE Suburb = "+ suburb,  null);
	return cur;

	}

	public Cursor fetchAllUnordered() {
		//defines criteria for a search
	Cursor cur = mDb.rawQuery("SELECT * FROM listings ",  null);
	return cur;

	}
  
	/** search db for LIKE passed string, return cursor */
	public Cursor search(String search) {
											//defines criteria for a search
		   Cursor cur = mDb.rawQuery("SELECT * FROM listings WHERE ref LIKE '%" + search
				   						+"%' OR suburb LIKE '%" + search +"%'"
				   						+" OR VendorSurname LIKE '%" + search +"%'"
				   						+" OR stno LIKE '%" + search +"%'"
				   						+" OR street LIKE '%" + search +"%'"
				   						+" OR district LIKE '%" + search +"%'",


				   null);
		   return cur;


	}

}




