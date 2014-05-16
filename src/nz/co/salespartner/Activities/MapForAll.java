package nz.co.salespartner.Activities;

import java.io.File; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.Helpers.Realtor;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Objects.ListingRecord;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.SlidingDrawer.OnDrawerScrollListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.embarcadero.javaandroid.DBXException;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapForAll extends MapActivity{
	private static final String TAG = "MapForAll";
	private MapView mapView;
	private Cursor listingsCursor;
	private SalesPartnerDbAdapter mDbHelper;
	private ArrayList<ListingRecord> all_listings;
	private ArrayList<Realtor> realtors;
	private Context context;
	private StyleFactory stylefactory;
	private int id;
	protected SlidingDrawer drawer;
	private EditText minPriceInput;
	private EditText maxPriceInput;
	private Spinner listedby_spinner;
	private GeoPoint center;
	private MapFilter map_filter;
	private CheckBox by_beds;
	private CheckBox by_price;
	private EditText maxBedsInput;
	private EditText minBedsInput;
	private TextView printOut;
	private int overlayCount;
	private LinearLayout mapMode;
	private LinearLayout handle;
//	private MyDataSetObserver cursorObserver;
	private boolean serverStatusEstablished = false;
	private boolean serverReachable = false;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_for_all);
		context = this;
		map_filter = new MapFilter();
		stylefactory = new StyleFactory(this);
		initViews();
		realtors = new ArrayList<Realtor>();
		
		
		styleTVs();
		initButtons();
		new EstablishConnectionDialog().execute();
	}
	
	/**test google geocoding services with an Address, if no response then don't proceed with geocoding listings*/
	private void testForServerReachable() {
			Geocoder geocoder = new Geocoder(context);
			try{
				String location = "10 Inglis St, Wellington, New Zealand";
				List<Address>addressList=geocoder.getFromLocationName(location ,5);
				serverReachable = true;
			}catch(IOException e){
				serverReachable = false;
				e.printStackTrace();
			}
		
			
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	/** create options menu*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.basic_menu, menu);
		return true;
	}
	/** set onClick for options menu*/ 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Options:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case R.id.HelpDesk:
			startActivity(new Intent(this, HelpDesk.class));
			return true;
		case R.id.Home:
			startActivity(new Intent(this, Landing.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/** check for internet connection */
	public boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		}
		else{
			// return active networks state
			boolean isConnected = ni.isConnected();
			return isConnected;
		}
	}
	
	private void showNoConnectionDialog() {
		Toast.makeText(context, "Internet connection required. Please try again.",Toast.LENGTH_SHORT).show();
	}
	
	/** allow back pressed to close an open drawer*/
	public void onBackPressed(){
		if(drawer.isOpened() || drawer.isMoving())
		{
			drawer.close();
		}
		else
		{
			finish();
		}
	}
	
	/** initialize views */
	private void initViews() {
		printOut = (TextView)findViewById(R.id.mfa_print);
		mapView = (MapView)findViewById(R.id.mfa_mapview);
		mapView.setBuiltInZoomControls(true);	
		mapView.setSatellite(false);
		drawer = (SlidingDrawer)findViewById(R.id.mfa_drawer);
		minPriceInput = (EditText)findViewById(R.id.mfa_min_price);
		maxPriceInput = (EditText)findViewById(R.id.mfa_max_price);
		minBedsInput = (EditText)findViewById(R.id.mfa_min_beds);
		maxBedsInput = (EditText)findViewById(R.id.mfa_max_beds);
		listedby_spinner = (Spinner)findViewById(R.id.listedby_spinner);
		by_price = (CheckBox)findViewById(R.id.mfa_by_price);
		by_beds = (CheckBox)findViewById(R.id.mfa_by_beds);
		mapMode = (LinearLayout)findViewById(R.id.mfa_map_mode_button);
		EditText[] edittexts = {minPriceInput,maxPriceInput,minBedsInput,maxBedsInput};
		stylefactory.setOnFocusChanged(edittexts);
	}

	/** set text on drawer fields to match map filter */
	private void refreshViews() {
		minPriceInput.setText(""+map_filter.minPrice/1000);
		maxPriceInput.setText(""+map_filter.maxPrice/1000);
		minBedsInput.setText(""+map_filter.minBeds);
		maxBedsInput.setText(""+map_filter.maxBeds);
	}

	/** support class for filtering map listings*/
	private class MapFilter{
		public final int default_min_Price = 0;
		public final int default_max_Price = 9000000;
		public final int default_min_Beds = 0;
		public final int default_max_Beds = 9;
		
		public int minPrice = default_min_Price;
		public int maxPrice = default_max_Price;
		public int minBeds = default_min_Beds;
		public int maxBeds = default_max_Beds;
		public String listedBy = "all";
		
		public MapFilter(){
			
		}
	
		public String toString(){
			return "***" +
					"\nminPrice = "+minPrice+
					"\nmaxPrice = "+maxPrice+
					"\nminBeds = "+minBeds+
					"\nmaxBeds = "+maxBeds;
		}
	}

	
	/** initialise buttons */
	private void initButtons() {
		Button filter = (Button)findViewById(R.id.mfa_filterbutton);
		filter.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				drawer.close();
				/*hide keyboard*/
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(drawer.getWindowToken(), 0);
				MapFilter mf = readFields();
				if(isOnline())
				{
					filterArray(mf);
					setPrintOut();
				}
				else
					showNoConnectionDialog();
			}
		});
		
		/* turn satellite mode on or off */
		mapMode.setClickable(true);
		mapMode.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(mapView.isSatellite()) //if satelite on
					mapView.setSatellite(false);
				else
					mapView.setSatellite(true);
			}
		});
		
		/* hiding zoom controls on drawer slide */
		drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			
			public void onDrawerOpened() {
				mapView.getZoomButtonsController().setVisible(false);
			}
		});
		
		drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
			
			public void onDrawerClosed() {
				mapView.getZoomButtonsController().setVisible(true);
			}
		});
		
		drawer.setOnDrawerScrollListener(new OnDrawerScrollListener() {
			
			public void onScrollStarted() {
				mapView.getZoomButtonsController().setVisible(false);
			}
			
			public void onScrollEnded() {
				
			}
		});
	}

	/** read fields in drawer and set map_filter values */
	protected MapFilter readFields() {
		map_filter = new MapFilter();

		if(by_price.isChecked() // if price checked and both fields not empty
				&& TextUtils.isEmpty(minPriceInput.getText().toString()) == false
				&& TextUtils.isEmpty(maxPriceInput.getText().toString()) == false)
		{	//set map filter to field values
			map_filter.minPrice = Integer.parseInt(minPriceInput.getText().toString()) * 1000;
			map_filter.maxPrice = Integer.parseInt(maxPriceInput.getText().toString()) * 1000;
			
		}	//else to defaults
		else
		{
			map_filter.minPrice = map_filter.default_min_Price;
			map_filter.maxPrice = map_filter.default_max_Price;
		}
		if(by_beds.isChecked() // if beds checked and both fields not empty
				&& TextUtils.isEmpty(minBedsInput.getText().toString()) == false
				&& TextUtils.isEmpty(maxBedsInput.getText().toString()) == false)
		{	//set map filter to field values
			
			int minBedsin = Integer.parseInt(minBedsInput.getText().toString());
			int maxBedsin = Integer.parseInt(maxBedsInput.getText().toString());
			
			map_filter.minBeds = minBedsin;
			map_filter.maxBeds = maxBedsin;
		}	//else to defaults
		else
		{
			map_filter.minBeds = map_filter.default_min_Beds;
			map_filter.maxBeds = map_filter.default_max_Beds;
		}
		
		Log.i("","MF to string = "+map_filter.toString());
		
		Realtor selected = realtors.get(listedby_spinner.getSelectedItemPosition());
		map_filter.listedBy = selected.name;
		return map_filter;
	}

	/** clear overlays and redraw with given MapFilter*/
	protected void filterArray(MapFilter mf) {
		mapView.getOverlays().clear();
		drawOverlays(mf);
		centerOnListing();
		mapView.invalidate();
	}

	/** custom font on textviews */
	private void styleTVs() {
	
		stylefactory.initHeader(this);

	}
	  
	
	/** establish whether connection is available */
	public class EstablishConnectionDialog extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public EstablishConnectionDialog() {
			progress = new ProgressDialog(context);
			progress.setMessage("Establishing connection...");
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			if(isOnline() && serverReachable && serverStatusEstablished)
				new LoadMap().execute();
			else
				showNoConnectionDialog();
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if(isOnline() && serverStatusEstablished == false)
			{
				testForServerReachable();
				serverStatusEstablished = true;
			}
			return null;
		}
	}

	/** load in background */
	public class LoadMap extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public LoadMap() {
			progress = new ProgressDialog(context);
			progress.setMessage("Loading...");
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			mapView.invalidate();
			setSpinnerAgents();
			centerOnListing();
			refreshViews();
			setPrintOut();
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mDbHelper.close();
			//listingsCursor.close();
			stopManagingCursor(listingsCursor);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			mDbHelper = new SalesPartnerDbAdapter(context);
			mDbHelper.open();
		
			all_listings = new ArrayList<ListingRecord>();
			loadListings();
			createRealtorList();
			drawOverlays(new MapFilter());
			
			mDbHelper.close();
			return null;
		}
	}
	
//	private class MyDataSetObserver extends DataSetObserver {
//	    public void onChanged(){
//	        Log.e("", "CHANGED CURSOR!");
//	    }
//	    public void onInvalidated(){
//	        Log.e("", "INVALIDATED CURSOR!");
//	        //listingsCursor.requery();
//	    }
//	}

	private void loadListings() {
		listingsCursor = mDbHelper.fetchAllListings(); 
//		startManagingCursor(listingsCursor);
//		cursorObserver = new MyDataSetObserver();
//		listingsCursor.registerDataSetObserver(cursorObserver);
	    
		
		
		int listingcount = listingsCursor.getCount();
		listingsCursor.moveToFirst();
		while (listingsCursor.isAfterLast() == false) {

			/* build current listing*/
			ListingRecord current = new ListingRecord();
			current.ref = listingsCursor.getString(listingsCursor.getColumnIndex("ref"));
			current.Street = listingsCursor.getString(listingsCursor.getColumnIndex("Street"));
			current.StNo= listingsCursor.getString(listingsCursor.getColumnIndex("StNo"));
			current.Suburb = listingsCursor.getString(listingsCursor.getColumnIndex("Suburb"));
			current.Bedrooms = listingsCursor.getInt(listingsCursor.getColumnIndex("Bedrooms"));
			current.Bathrooms = listingsCursor.getInt(listingsCursor.getColumnIndex("Bathrooms"));
			current.Garaging = listingsCursor.getInt(listingsCursor.getColumnIndex("Garaging"));
			current.ListedBy = listingsCursor.getString(listingsCursor.getColumnIndex("ListedBy"));
			current.DisplayPrice = listingsCursor.getString(listingsCursor.getColumnIndex("DisplayPrice"));
			current.SearchPrice = listingsCursor.getInt(listingsCursor.getColumnIndex("SearchPrice"));
			/* build location string */
			String location = listingsCursor.getString(listingsCursor.getColumnIndex("StNo")) +", "+ 
					listingsCursor.getString(listingsCursor.getColumnIndex("Street")) +", "+
					listingsCursor.getString(listingsCursor.getColumnIndex("Suburb")) +", "+
					listingsCursor.getString(listingsCursor.getColumnIndex("District"))+", "
					+MapViewActivity.COUNTRY;

			/* get listings latitude and longitude from DB*/
			int listing_lat = listingsCursor.getInt(listingsCursor.getColumnIndex("Latitude"));
			int listing_lng = listingsCursor.getInt(listingsCursor.getColumnIndex("Longitude"));
			
			/* if lat long = 0 ask google for geocoding else using DB ints*/
			if(listing_lat != 0 && listing_lng != 0)
			{
				current.lat = listing_lat;
				current.lng = listing_lng;
			}
			else
			{
				getLocation getlocation = new getLocation(location);
				current.lat = getlocation.lat;
				current.lng = getlocation.lng;
				mDbHelper.updateLatLong(current);
			}
			current.id = listingsCursor.getInt(listingsCursor.getColumnIndex("_id"));
			all_listings.add(current);

			listingsCursor.moveToNext();
		}
		
		////listingsCursor.close();
	}
	
	public void setPrintOut() {
		printOut.setText("Results: "+overlayCount); //
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//if(listingsCursor!=null)
			////listingsCursor.close();
	}

	/**populate the ListedBy spinner with realtors*/
	public void setSpinnerAgents() {
		ArrayList<String> agent_names = new ArrayList<String>(); 
		
		for(Realtor r : realtors)
		{
			agent_names.add(r.name);
		}
	
		  
		    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		            this, android.R.layout.simple_spinner_item, agent_names);
		    spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

		    listedby_spinner.setAdapter(spinnerArrayAdapter);
		    listedby_spinner.setOnItemSelectedListener(new listedby_spinner_listener());
	}
	
	public class listedby_spinner_listener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
  
	/** read through listings and create a list of agents*/
	public void createRealtorList() {
		
		realtors.add(new Realtor("All"));
		for(ListingRecord current : all_listings)
		{
			if(realtors.size()>0)
			{
				Boolean unassigned = true;
				for(Realtor r : realtors)
				{
					if(r.name.equalsIgnoreCase(current.ListedBy))
						unassigned = false;
				}
				if(unassigned)
					realtors.add(new Realtor(current.ListedBy));
			}
			else
				realtors.add(new Realtor(current.ListedBy));
		}
		
		Log.i("","realtors: "+realtors.toString());
	}

	public void drawOverlays(MapFilter mf) {
		overlayCount = 0;
		for(int i = 0; i < all_listings.size(); i ++)
		{
			//Log.i("","mf to String: "+mf.toString());
			
			ListingRecord l = all_listings.get(i);
			/* filter listings based on passed MapFilter */
			if(mf.listedBy.equalsIgnoreCase(l.ListedBy) || mf.listedBy.equalsIgnoreCase("all"))
				if(mf.minPrice <= l.SearchPrice && mf.maxPrice >= l.SearchPrice
				   && mf.minBeds <= l.Bedrooms && mf.maxBeds >= l.Bedrooms)
				{
				addOverLay(l);
				overlayCount ++;
				if(i == all_listings.size() / 2) //center on middle listing
					center = new GeoPoint(l.lat, l.lng);
				}
		}
		
		
	}
	
	/** animate map to middle listing */
	private void centerOnListing() {
		mapView.getController().animateTo(center);
	}

	/** add a pin to map from passed listings lat and long */
	private void addOverLay(ListingRecord l){

		Drawable marker2=getResources().getDrawable(
				R.drawable.map_pin);
		marker2.setBounds(0, 0, marker2.getIntrinsicWidth(), 
				marker2.getIntrinsicHeight());
		mapView.getOverlays().add(new OverlayPin(marker2, l.lat, l.lng, l));  	

	}
	
	/**class for managing map overlays*/
	class OverlayPin extends ItemizedOverlay<OverlayItem>{

		private List<OverlayItem> locations = 
				new ArrayList<OverlayItem>();
		private Drawable marker;
		private ListingRecord listing;
		public OverlayPin(Drawable defaultMarker, 
				int LatitudeE6, int LongitudeE6, ListingRecord Listing) {
			super(defaultMarker);
			this.marker=defaultMarker;
			GeoPoint myPlace = new GeoPoint(LatitudeE6,LongitudeE6);
			locations.add(new OverlayItem(myPlace , 
					"My Place", "My Place"));
			populate();
			listing = Listing;
		}

		@Override
		protected OverlayItem createItem(int i) {
			return locations.get(i);
		}

		@Override
		public int size() {
			return locations.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, 
				boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}

		@Override
		protected boolean onTap(int index) {
			super.onTap(index);

			if(listing!=null){
				Dialog d =  createListingDialog(listing);
				d.show();
			}
			return true;

		}
	}
	
	/** show popup.xml dialog on overlay tap*/
	public Dialog createListingDialog(final ListingRecord listing) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_bg); //set to custom bg
		dialog.setContentView(R.layout.listing_row_popup);
		dialog.setCancelable(true);
		
		LinearLayout wrapper = (LinearLayout)dialog.findViewById(R.id.lrpu_wrapper);
		wrapper.setClickable(true);
		wrapper.setOnClickListener(new OnClickListener() {
			
			

			public void onClick(View v) {
				Intent mIntent = new Intent(context, ListingTabHost.class);
				mIntent.putExtra("id", listing.id);
				startActivity(mIntent);				
			}
		});
		
		/* close icon*/
		ImageView closeicon = (ImageView)dialog.findViewById(R.id.lr_close_icon);
		closeicon.setClickable(true);
		closeicon.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		/* define views*/
		TextView refView = (TextView) dialog.findViewById(R.id.lrpu_text1);
		TextView streetView = (TextView) dialog.findViewById(R.id.lrpu_street);
		TextView bedroomsView = (TextView) dialog.findViewById(R.id.lrpu_bedrooms);
		TextView bathroomsView = (TextView)dialog.findViewById(R.id.lrpu_bathrooms);
		TextView garagingView = (TextView) dialog.findViewById(R.id.lrpu_garaging);
		TextView listedView = (TextView) dialog.findViewById(R.id.lrpu_listedby);
		ImageView bedroomsIcon = (ImageView)dialog.findViewById(R.id.lrpu_bedrooms_icon);
		ImageView bathroomsIcon = (ImageView)dialog.findViewById(R.id.lrpu_bathrooms_icon);
		ImageView garagingIcon = (ImageView)dialog.findViewById(R.id.lrpu_garaging_icon);
		TextView priceView = (TextView)dialog.findViewById(R.id.lrpu_listing_row_DisplayPrice);
		ImageView thumbnail = (ImageView)dialog.findViewById(R.id.lrpu_row_thumbnail);

		String ref = listing.ref;
		String street = listing.Street;
		String stno = listing.StNo;
		String suburb = listing.Suburb;
		String listedby = listing.ListedBy;
		int bedrooms = listing.Bedrooms;
		int bathrooms = listing.Bathrooms;
		int garaging = listing.Garaging;
		String displayPrice = listing.DisplayPrice;
		
		
		Log.i("","ref to string = "+listing.ref);
		
		refView.setText(ref);
		streetView.setText(stno + " " + street+"\n"+suburb);
		bedroomsView.setText(Integer.toString(bedrooms));
		bathroomsView.setText(Integer.toString(bathrooms));
		garagingView.setText(Integer.toString(garaging));
		listedView.setText("Listed by: "+listedby);
		
		TextView cameraView = (TextView)dialog.findViewById(R.id.lrpu_listingrow_camera);			
		cameraView.setText(Integer.toString(PhotoManager.photoCount(ref)));			

		//set thumbnail
		String picture1FileName = PhotoManager.getThumbName(ref, 1);
		File file = new File(picture1FileName);
		if (file.exists()) { //Set thumbnail and make visible
			Log.d("MapForAll", "Picture Found: " + picture1FileName);
			Bitmap bMap = BitmapFactory.decodeFile(picture1FileName);
			thumbnail.setImageBitmap(bMap);
			View parent = (View)thumbnail.getParent();
			parent.setVisibility(View.VISIBLE);
		} else { // Set thumbnail null and make invisible
			View parent = (View)thumbnail.getParent();
			parent.setVisibility(View.INVISIBLE);
			thumbnail.setImageBitmap(null); //TODO clear image
			Log.d("MapForAll", "Picture not Found: " + picture1FileName);
		}

		setViewInvisibleIfZero(bedroomsView, bedrooms);
		setViewInvisibleIfZero(bedroomsIcon, bedrooms);
		setViewInvisibleIfZero(bathroomsView, bathrooms);
		setViewInvisibleIfZero(bathroomsIcon, bathrooms);
		setViewInvisibleIfZero(garagingView, garaging);
		setViewInvisibleIfZero(garagingIcon, garaging);
		priceView.setText(displayPrice);
		
		TextView[] tvs = {priceView,refView, streetView, bedroomsView, bedroomsView
				,bathroomsView, garagingView};
		stylefactory.applyFont(tvs);
		

		return dialog;
	}
	
	private void setViewInvisibleIfZero(View view, int x) {
		if (x == 0) {
			view.setVisibility(View.INVISIBLE);
		} else {
			view.setVisibility(View.VISIBLE);
		}

	}

	/**geocode a String location into lat and long E6*/
	public class getLocation{
		public int lat;
		public int lng;
		public getLocation(String location) {
			Geocoder geocoder = new Geocoder(context);
			try{
				List<Address>addressList=geocoder.getFromLocationName(location,5);
				if(addressList!=null && addressList.size()>0)
				{
					lat=(int)(addressList.get(0).getLatitude()*1000000);
					lng=(int)(addressList.get(0).getLongitude()*1000000);

				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
