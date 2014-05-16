package nz.co.salespartner.Activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import nz.co.salespartner.R;
import nz.co.salespartner.SalesPartnerApplication;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Helpers.TMIdParser;
import nz.co.salespartner.Helpers.TMParser;
import nz.co.salespartner.Objects.ListingRecord;
import nz.co.salespartner.Objects.TMListing;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class TradeMe extends Activity{
	private int INITIAL_RANGE = 10;
	private Context context;
	private ListingRecord l;
	private int id; 
	private ArrayList<Bitmap> bitmaps;  
	private ProgressDialog progress;
	private ListView lv;
	private ArrayList<TMListing> tm_listings;
	private ArrayList<TMListing> original_tm_listings;
	private SalesPartnerApplication app;
	private TextView percentView;
	private SeekBar seekBar;
	private int listing_RV;
	private TextView vtm_print;
	private boolean filterPreferred;
	private TextView vtm_header;
	private StyleFactory stylefactory;
	private final double MAX_RV = 50;
	private ArrayList<Bitmap> original_bitmaps;
	protected boolean showAgentNames = false;
	private boolean currentFilterEnabled = true;
	private SlidingDrawer drawer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_trademe);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		
		stylefactory = new StyleFactory(this);
		stylefactory.initHeader(this);
		app = new SalesPartnerApplication();
		loadPreferences();
		original_tm_listings = new  ArrayList<TMListing>();
		original_bitmaps = new ArrayList<Bitmap>();

		listing_RV = getIntent().getIntExtra("rv",0);
		
		context = this;
		id = getIntent().getIntExtra("id", 0);
		bitmaps = new ArrayList<Bitmap>();
		lv = (ListView)findViewById(R.id.trade_me_lv);
		initViews();
		if(isOnline())  
		{
			progress = new ProgressDialog(this);
			progress.setMessage("Loading...");
			new LoadTradeMe(progress).execute();
		}
		else
		{
			showNoConnectionDialog();
		}

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
	
	private void loadPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		INITIAL_RANGE = Integer.parseInt(prefs.getString("initialRange", "10"));	
		filterPreferred = prefs.getBoolean("filterTradeMe", false);
		
		showAgentNames = prefs.getBoolean("TMagentNames", false);
		System.out.print("INITIAL_RANGE"+INITIAL_RANGE);
	}

	private void initViews() {
		drawer = (SlidingDrawer)findViewById(R.id.vtm_drawer);
		
		percentView = (TextView)findViewById(R.id.vtm_percent);
		seekBar = (SeekBar)findViewById(R.id.vtm_seekbar);
		
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				percentView.setText("+/-"+String.valueOf(progress)+"%");
				if(progress == 100)
					percentView.setText("All");
					 
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
		seekBar.setMax((int)MAX_RV);
		seekBar.setProgress(INITIAL_RANGE);

		Button filter_button = (Button)findViewById(R.id.vtm_filterbutton);
		filter_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				filter_listings();
			}
		});
		
		
		CheckBox rvEnabled = (CheckBox)findViewById(R.id.vtm_enabled);
		rvEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				currentFilterEnabled = checked;
			}
		});
		rvEnabled.setChecked(filterPreferred);
		
		vtm_print = (TextView)findViewById(R.id.vtm_print);
		vtm_header = (TextView)findViewById(R.id.vtm_header);
		TextView[] bold = {vtm_print,vtm_header};
		stylefactory.applyBold(bold);
	}

	protected void filter_listings() {
		double progress = ((double)seekBar.getProgress());
		double percent = progress / 100;
		System.out.println("seekBar.getProgress() = "+seekBar.getProgress());
		System.out.println("percent = "+percent);
		if(currentFilterEnabled){ //seekbar is at end
			tm_listings.clear();
			bitmaps.clear();
			double lowerBound = (double)listing_RV - (double)listing_RV*percent;
			double upperBound = (double)listing_RV + (double)listing_RV*percent;
		
			for(int i = 0; i < original_tm_listings.size(); i++)
			{
				TMListing l = original_tm_listings.get(i);
				try{
					double lRV = Double.parseDouble(l.rv);
					System.out.println("lRV = "+lRV);
					if(lRV >= lowerBound && lRV <= upperBound)
					{
						tm_listings.add(l);
						bitmaps.add(original_bitmaps.get(i));
					}
				}
				catch(Exception e)
				{
					Log.i("","error inside filter_listings()");
					e.printStackTrace();
				}
			}
		}
		else
		{
			tm_listings = (ArrayList<TMListing>) original_tm_listings.clone();
			bitmaps = (ArrayList<Bitmap>) original_bitmaps.clone();
		}
		
		setAdapter();
		
	}

	private void setPrintOut() {
		vtm_print.setText("("+tm_listings.size()+")");
	}

	public boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		boolean isConnected = false;
		if (ni == null) {
			// There are no active networks.
			
		}
		else{
			// return active networks state
			isConnected = ni.isConnected();
			
		}

		final ConnectivityManager connMgr = (ConnectivityManager)
				this.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi =
				connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile =
				connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if( wifi.isAvailable() || mobile.isAvailable())
		{
			isConnected = true;
		}
		else
		{
			isConnected = false;
		}

		return isConnected;

	}

	private void showNoConnectionDialog() {
		Log.i("","no connection");		
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void setAdapter() {
		sortTMArray();
		setPrintOut();
		lv.setAdapter(new ArrayAdapter<TMListing>(this, R.layout.trademe_row,tm_listings) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row;
				if (null == convertView) {
					row = View.inflate(context,R.layout.trademe_row, null);

				} else {
					row = convertView;
				}  

				/* define views */
				TextView title = (TextView)row.findViewById(R.id.tmr_title);
				TextView price = (TextView)row.findViewById(R.id.tmr_price);
				TextView rv = (TextView)row.findViewById(R.id.tmr_rv);
				TextView agentName = (TextView)row.findViewById(R.id.tmr_agentName);
				
				TextView streetView = (TextView) row.findViewById(R.id.tmr_address);
				TextView bedroomsView = (TextView) row.findViewById(R.id.bedrooms);
				TextView bathroomsView = (TextView) row.findViewById(R.id.bathrooms);
				/* reset views */
				title.setText("");
				price.setText("");
				rv.setText("");
				streetView.setText("");
				bedroomsView.setText("");
				bathroomsView.setText("");
				agentName.setText("");
				if(showAgentNames && tm_listings.get(position).agentsName!=null)
				{
					agentName.setVisibility(View.VISIBLE);
					agentName.setText("Agent: "+tm_listings.get(position).agentsName);
				}
				else
					agentName.setVisibility(View.GONE);
				
				/* apply fonts*/
				TextView[] tvs = {title,price,rv};
				stylefactory.applyFont(tvs);
				
				/* try set Bitmap */
				ImageView img = (ImageView)row.findViewById(R.id.tmr_thumbnail);
				LinearLayout imgholder = (LinearLayout)row.findViewById(R.id.tmr_thumbnailwrapper);
				try 
				{
					img.setImageBitmap(tm_listings.get(position).thumbnail);
					img.setVisibility(View.VISIBLE);
					imgholder.setVisibility(View.VISIBLE);
				}
				catch(Exception e)
				{
					img.setVisibility(View.INVISIBLE);
					imgholder.setVisibility(View.INVISIBLE);
				}

				/* set textviews */
				title.setText(tm_listings.get(position).title);
				price.setText(tm_listings.get(position).displayprice);
				if(tm_listings.get(position).rv!=null) //catch trademe passing back "null" String
					rv.setText("RV: $"+tm_listings.get(position).rv);
				String street = "";
				if(tm_listings.get(position).address!=null)
					street = tm_listings.get(position).address;
				if(TextUtils.isEmpty(street)==false)
					street += ", ";
				streetView.setText(street + tm_listings.get(position).district);
				bedroomsView.setText(tm_listings.get(position).bedrooms);
				bathroomsView.setText(tm_listings.get(position).bathrooms);

				/* give row alternating colors */
				if(position % 2 == 0) 
					row.setBackgroundResource(R.drawable.row_states_dark);
				else
					row.setBackgroundResource(R.drawable.row_states_light);

				/* row onClick listeners*/
				row.setClickable(true);
				final int thisPosition = position;
				row.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) { //launch TradeMe url based on tm_listing listingid
						String url = "http://www.trademe.co.nz/Browse/Listing.aspx?id="+tm_listings.get(thisPosition).listingid;
						Uri uriUrl = Uri.parse(url);
						Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl); 
						startActivity(launchBrowser);  
					}
				});  

				return row;
			}
		});		
	}

	private void sortTMArray() {
		//Collections.sort(tm_listings);
	}

	private void slimTMArray() {
		ArrayList<TMListing> non_empty = new ArrayList<TMListing>();
		for(TMListing l : tm_listings)
		{
			if(TextUtils.isEmpty(l.title)==false)
				non_empty.add(l);
		}
		tm_listings = non_empty;
	}

	private void loadBitmaps() {
		bitmaps.clear(); //clear bitmap array
		for(int i = 0; i < original_tm_listings.size(); i++)
		{
			try {
				URL url = new URL(original_tm_listings.get(i).picturehref);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(input);
				bitmaps.add(myBitmap);
			} catch (IOException e) {
				e.printStackTrace();
				//bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.icon));

			}

		}
		original_bitmaps = (ArrayList<Bitmap>) bitmaps.clone();
	}

	private void initListing() {
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		Cursor c = mDbHelper.fetchListing(id);
		l = new ListingRecord();
		l.FromCursor(c);
		c.close();
		mDbHelper.close();		
	}

	public class LoadTradeMe extends AsyncTask<Void, Void, Void> {
		public LoadTradeMe(ProgressDialog p) {
			progress = p;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {

			progress.dismiss();
			//loadBitmaps();
			setAdapter();
			if(!filterPreferred) //if filter not preferred filter at 100%
				INITIAL_RANGE = 100;	
			filter_listings();
			

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			initListing();
			TMParser tmp = new TMParser();
			TMIdParser tmip = new TMIdParser(context);
			tm_listings = tmp.parseForTM("http://api.trademe.co.nz/v1/Search/Property/Residential.xml?suburb="+tmip.parse(l));
			for(TMListing l : tm_listings){
				l.thumbnail = getBitmap(l.picturehref);
			}
//			slimTMArray();
			sortTMArray();
			
			original_tm_listings = (ArrayList<TMListing>) tm_listings.clone(); //save a back up;
			System.out.println("tm_listings size = "+tm_listings.size());
			System.out.println("original_tm_listings size = "+original_tm_listings.size());
			return null;
		}
	}
	
	private Bitmap getBitmap(String href){
		Bitmap myBitmap = null;
		try {
			URL url = new URL(href);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			myBitmap = BitmapFactory.decodeStream(input);
			
		} catch (IOException e) {
			e.printStackTrace();
			//bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.icon));

		}
		return myBitmap;
	}


}
