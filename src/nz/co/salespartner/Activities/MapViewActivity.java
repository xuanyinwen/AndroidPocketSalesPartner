package nz.co.salespartner.Activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nz.co.salespartner.R;
import nz.co.salespartner.Activities.MapForAll.EstablishConnectionDialog;
import nz.co.salespartner.Activities.MapForAll.LoadMap;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Helpers.TMIdParser;
import nz.co.salespartner.Helpers.TMParser;
import nz.co.salespartner.Objects.ListingRecord;
import nz.co.salespartner.Objects.TMListing;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
public class MapViewActivity extends MapActivity{
	private int id;
	private String locationName;
	public static String COUNTRY = "New Zealand";
	private MapView mapView;
	private ListingRecord l;

	private Context context;
	private int listing_lat, listing_lng;
	private int LAT_BOUND = 20000;
	private int LNG_BOUND = 20000;
	private int LISTING_BOUND = 100;
	private int MAP_ZOOM = 17;
	private GeoPoint pt;
	private boolean displayTradeMe;
	private boolean showAgencyLogos;
	private StyleFactory stylefactory;
	private boolean serverReachable = false;
	private boolean serverStatusEstablished = false;
	private boolean showAgentNames;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.map_view);
		context = this;
		stylefactory = new StyleFactory(this);
		loadPreferences();
		id = getIntent().getExtras().getInt("id");

		initListing();
		printLocation();
		mapView = (MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		initButtons();
		styleTVs();
	
		new EstablishConnectionDialog().execute();


	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadPreferences();
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
	
	
	private void showNoConnectionDialog() {
		Toast.makeText(context, "Internet connection required. Please try again.",Toast.LENGTH_SHORT).show();
	}
	

	private void styleTVs() {
		int[] tvs = {R.id.location_print};
			stylefactory.applyFont(tvs,this);
		int[] bold_tvs = {R.id.map_mode_text};
			stylefactory.applyBold(bold_tvs,this);
		
	}

	private void loadPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		displayTradeMe = prefs.getBoolean("DisplayTradeMe", true);				
		showAgencyLogos = prefs.getBoolean("showAgencyLogos", false);			
		showAgentNames = prefs.getBoolean("MVagentNames", false);			
	}

	/** initialise map controls */
	private void initButtons() {
		LinearLayout map_mode = (LinearLayout)findViewById(R.id.map_mode_button);
		map_mode.setClickable(true);
		map_mode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mapView.isSatellite()) //if satelite on
					mapView.setSatellite(false);
				else
					mapView.setSatellite(true);
			}
		});

		LinearLayout map_location = (LinearLayout)findViewById(R.id.map_location_button);
		map_location.setClickable(true);
		map_location.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(pt !=  null)
					mapView.getController().animateTo(pt);
			}
		});
	}

	/** parse surburb and nearby suburbs XML and add overlays using lat and long*/
	private void addTradeMe() {
		TMParser tmp = new TMParser();
		TMIdParser tmip = new TMIdParser(context);

		ArrayList<TMListing> tmlistings = tmp.parseForTM("http://api.trademe.co.nz/v1/Search/Property/Residential.xml?suburb="+tmip.parse(l));
		Log.i("","tmparse result id "+tmip.parse(l));
		for(int i = 0 ; i < tmlistings.size(); i ++)
		{
			if(!TextUtils.isEmpty(tmlistings.get(i).latitude) && !TextUtils.isEmpty(tmlistings.get(i).longitude)){
				double lat = Double.parseDouble(tmlistings.get(i).latitude);
				double lng = Double.parseDouble(tmlistings.get(i).longitude);
				int latE6 = (int) (lat * 1000000);
				int lngE6 = (int) (lng * 1000000);
				if(withinBounds(latE6, lngE6) && notTooClose(latE6, lngE6))
					addOverLay(latE6, lngE6, tmlistings.get(i));}
		}
	}
	/** add other listings */
	public void addOtherListings() {
		ArrayList<ListingRecord> OtherListings = new ArrayList<ListingRecord>();
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		Cursor c = mDbHelper.fetchSameSuburb(l.Suburb);
		c.moveToFirst();
		while(!c.isAfterLast()) {
			String currentid = c.getString(c.getColumnIndex("_id"));
			Log.i("", "currentid: "+currentid);
//			Cursor specific = mDbHelper.fetchListing(currentid);
//
//			l = new ListingRecord();
//			l.FromCursor(specific);
//			OtherListings.add(l);
//			specific.close();
		c.moveToNext();
		}




		c.close();
		mDbHelper.close();
	}

	private boolean notTooClose(int lat, int lng) {
		if(lat >= listing_lat + LISTING_BOUND
				|| lat <= listing_lat - LISTING_BOUND)
		{
			if(lng >= listing_lng + LISTING_BOUND
					|| lng <= listing_lng - LISTING_BOUND)
				return true;
			else
				return false;
		}
		else
			return false;
	}

	private boolean withinBounds(int lat, int lng) {
		if(lat <= listing_lat + LAT_BOUND
				&& lat >= listing_lat - LAT_BOUND
				&& lng <= listing_lng + LNG_BOUND
				&& lng >= listing_lng - LNG_BOUND)
			return true;
		else
			return false;
	}

	private void addOverLay(int lat, int lng, TMListing tmListing){

		Drawable marker2=getResources().getDrawable(
				R.drawable.map_pin_alt);
		marker2.setBounds(0, 0, marker2.getIntrinsicWidth(), 
				marker2.getIntrinsicHeight());
		mapView.getOverlays().add(new OverlayPin(marker2, lat, lng, tmListing));  	

	}

	/** print location to top address bar */
	private void printLocation() {
		TextView locationprint = (TextView)findViewById(R.id.location_print);
		locationprint.setText(locationName);
	}

	/** geocode address string and center map on first result */
	private void setMapLocation() {
		Geocoder geocoder = new Geocoder(this);
		try{
			List<Address>addressList=geocoder.getFromLocationName(locationName,5);
			if(addressList!=null && addressList.size()>0)
			{
				listing_lat=(int)(addressList.get(0).getLatitude()*1000000);
				listing_lng=(int)(addressList.get(0).getLongitude()*1000000);

				pt = new GeoPoint(listing_lat,listing_lng);
				mapView.getController().setZoom(MAP_ZOOM);
				mapView.getController().setCenter(pt);

				Drawable marker=getResources().getDrawable(
						R.drawable.map_pin);
				marker.setBounds(0, 0, marker.getIntrinsicWidth(), 
						marker.getIntrinsicHeight()); //TODO change
				mapView.getOverlays().add(new OverlayPin(marker, 
						listing_lat, listing_lng, null));
			}
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	/** fetch listing from DB using id*/
	private void initListing() {
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		Cursor c = mDbHelper.fetchListing(id);
		l = new ListingRecord();
		l.FromCursor(c);
		c.close();
		mDbHelper.close();

		locationName = l.StNo+" "+l.Street+" "+l.Suburb+" "+l.District+" "+COUNTRY;
	}

	protected boolean isRouteDisplayed() {
		return false;
	}


	/**class for managing map overlays*/
	class OverlayPin extends ItemizedOverlay<OverlayItem>{

		private List<OverlayItem> locations = 
				new ArrayList<OverlayItem>();
		private Drawable marker;
		private TMListing tmlisting;
		public OverlayPin(Drawable defaultMarker, 
				int LatitudeE6, int LongitudeE6, TMListing tmListing) {
			super(defaultMarker);
			this.marker=defaultMarker;
			GeoPoint myPlace = new GeoPoint(LatitudeE6,LongitudeE6);
			locations.add(new OverlayItem(myPlace , 
					"My Place", "My Place"));
			populate();
			tmlisting = tmListing;
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

			if(tmlisting!=null){
				Dialog d =  createListingDialog(tmlisting);
				d.show();
			}
			return true;

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
			if(displayTradeMe)
				addTradeMe();
			//addOtherListings(); Having issues
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			setMapLocation();

			return null;
		}
	}


	/** show popup.xml dialog on overlay tap*/
	public Dialog createListingDialog(final TMListing listing) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_bg); //set to custom bg
		dialog.setContentView(R.layout.popup);
		dialog.setCancelable(true);

		/*init textview*/
		TextView rv = (TextView) dialog.findViewById(R.id.popup_rv);
			rv.setText(listing.rv);
		TextView title = (TextView) dialog.findViewById(R.id.popup_title);
			title.setText(listing.title);
		TextView price = (TextView) dialog.findViewById(R.id.popup_price);
			price.setText(listing.displayprice);
		TextView area = (TextView) dialog.findViewById(R.id.popup_area);
			if(!TextUtils.isEmpty(listing.area))
				area.setText(listing.area + " m2");
		TextView bedrooms = (TextView) dialog.findViewById(R.id.popup_bedrooms);
			bedrooms.setText(listing.bedrooms);
		TextView bathrooms = (TextView) dialog.findViewById(R.id.popup_bathrooms);
			bathrooms.setText(listing.bathrooms);
		TextView agent = (TextView) dialog.findViewById(R.id.popup_agentName);
			
		
		/* add custom fonts */
		TextView[] tvs = {price,area,title,bedrooms,bathrooms};
			stylefactory.applyFont(tvs);
		int[] bold_tvs = {R.id.pu_bold1, R.id.pu_bold2
				, R.id.pu_bold3, R.id.pu_bold4, R.id.pu_bold5};
			stylefactory.applyBold(bold_tvs, this);

		/*load image from picturehref*/
		ImageView img = (ImageView) dialog.findViewById(R.id.popup_img);
		try{
			URL url = new URL(listing.picturehref);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);

			img.setImageBitmap(myBitmap);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/*load agency logo*/
		TableRow logo_wrapper = (TableRow)dialog.findViewById(R.id.logo_holder);
		TableRow agent_wrapper = (TableRow)dialog.findViewById(R.id.popup_agent_wrapper);
		
		if(showAgencyLogos)
		{	
			ImageView logoview = (ImageView) dialog.findViewById(R.id.popup_logo);
			try{
				URL url = new URL(listing.logo);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(input);
	
				logoview.setImageBitmap(myBitmap);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			logo_wrapper.setVisibility(View.VISIBLE);
		}
		else
			
			logo_wrapper.setVisibility(View.GONE);
		
		/*agent name*/
		String agentName = "";
		if(listing.agentsName!=null)
			agentName = listing.agentsName;
		if(showAgentNames && TextUtils.isEmpty(agentName)==false){
			agent.setText(agentName);
			agent_wrapper.setVisibility(View.VISIBLE);
		}
		else
			agent_wrapper.setVisibility(View.GONE);

		/*init buttons*/
		Button visit = (Button) dialog.findViewById(R.id.popup_visit);
		visit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String url = "http://www.trademe.co.nz/Browse/Listing.aspx?id="+listing.listingid;
				Uri uriUrl = Uri.parse(url);
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);  //Launch tradeMe intent
				startActivity(launchBrowser);  
			}    
		});
		
		ImageView close_icon = (ImageView)dialog.findViewById(R.id.pu_close_icon);
		close_icon.setClickable(true);
		close_icon.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		
		return dialog;
	}


}
