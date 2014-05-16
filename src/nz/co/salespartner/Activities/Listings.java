package nz.co.salespartner.Activities;

import java.io.File;
import java.util.ArrayList;

import nz.co.salespartner.R;
import nz.co.salespartner.SalesPartnerApplication;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.Helpers.Realtor;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Helpers.TMIdParser;
import nz.co.salespartner.Helpers.TMParser;
import nz.co.salespartner.Objects.ListingFilterOptions;
import nz.co.salespartner.Objects.ListingRecord;
import nz.co.salespartner.Objects.TMListing;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Listings extends ListActivity {
	private static String TAG = "Listings";
	public static SalesPartnerDbAdapter mDbHelper;
	private Cursor listingsCursor;
	private Context context;
	private SlidingDrawer drawer;
	static final int DIALOG_QUIT_ID = 1;
	static final int DIALOG_ORDER_ID = 2;
	static final int DIALOG_SEARCH_ID = 3;
	static ListingFilterOptions ListingsFilter = new ListingFilterOptions();
	private int order = SalesPartnerDbAdapter.ORDER_STREET;
	final CharSequence[] order_items = { "Reference", "Price", "Street", "Suburb" };
	private SalesPartnerApplication app;
	private EditText min_price;
	private EditText max_price;
	private EditText min_beds;
	private EditText max_beds;
	private CheckBox byPrice;
	private CheckBox byBedrooms;
	private LinearLayout searchResultsBar;
	private int lastChecked;
	private RadioGroup tab_radio;
	private RadioButton radio_all;
	/** Called when the activity is first created. */
	private ListView lv;
	private RadioButton radio_search;
	private StyleFactory stylefactory;
	private RadioButton radio_fav;
	private LinearLayout fake_drawer;
	private boolean showSearchPrice;
	private boolean showListedBy;
	private nz.co.salespartner.Objects.CustomSpinner spinner;
	private String search;
	private Spinner listedby_spinner;
	private ArrayList<Realtor> realtors;
	private ArrayList<ListingRecord> listings_array;
	private String LISTEDBY;
	private ArrayList<String> agent_names;
	private LinearLayout submenu;
	private LinearLayout favAllBar;
	private LinearLayout clearFavsBar;
	private boolean showListedDate;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//dither gradients
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.listings);
		app = new SalesPartnerApplication();
		context = this;
		loadPrefences();
		initVariables();
		styleAll();
		initDrawer();
		initButtons();
		createRealtorList();
		fillData();
		initialiseFavCount(); 
	}

	private void initVariables() {
		listings_array = new ArrayList<ListingRecord>();
		lv = (ListView)findViewById(android.R.id.list);
		mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		drawer = (SlidingDrawer) findViewById(R.id.drawer);
		submenu= (LinearLayout)findViewById(R.id.sub_menu);
		fake_drawer = (LinearLayout)findViewById(R.id.fake_handle);
		stylefactory = new StyleFactory(this);
		stylefactory.initHeader(this);
		search = "";
		LISTEDBY = "All";
		realtors = new ArrayList<Realtor>();
 
	}

	private void loadPrefences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		showSearchPrice = prefs.getBoolean("showSearchPrice", false);		
		showListedDate = prefs.getBoolean("showListedDate", false);		
		showListedBy = prefs.getBoolean("showListedBy", false);		
	}

	@Override
	protected void onResume() {
		super.onResume();
		initialiseFavCount();
	}


	private void styleAll() {
		int[] tvs = {
				R.id.display_all_button,R.id.display_fav_button,
				R.id.drawer_bold1,R.id.drawer_bold2,R.id.drawer_bold4,R.id.foot_text
		};
		View current = null;
		for(int id : tvs)
		{
			current = (View)findViewById(id);
			stylefactory.applyBold(current);
		}
	}

	/** read through listings and create a list of agents*/
	public void createRealtorList() {
		realtors.clear();

		createListingArray();

		realtors.add(new Realtor("All"));

		for(ListingRecord current : listings_array)
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
		setSpinnerAgents();

	}

	/**populate the ListedBy spinner with realtors*/
	public void setSpinnerAgents() {
		agent_names = new ArrayList<String>(); 

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

	/**spinner listener*/
	public class listedby_spinner_listener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			LISTEDBY = agent_names.get(pos);
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}

	public void createListingArray() {

		listings_array.clear();
		listingsCursor = mDbHelper.fetchAllListings();


		Log.i("","createListingsArray");

		startManagingCursor(listingsCursor);

		int listingcount = listingsCursor.getCount();
		listingsCursor.moveToFirst();
		listingsCursor.moveToFirst();
		while (listingsCursor.isAfterLast() == false) {

			/* build current listing*/
			ListingRecord current = new ListingRecord();
			current.ListedBy = listingsCursor.getString(listingsCursor.getColumnIndex("ListedBy"));
			listings_array.add(current);

			listingsCursor.moveToNext();
		}

		//		listingsCursor.close();

	}


	/** load data */
	public class LoadData extends AsyncTask<Void, Void, Void> {
		ProgressDialog progress;
		public LoadData(ProgressDialog p) {
			progress = p;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			//			setListAdapter(listings);
			initDrawer();
			initButtons();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			fillDataFirstTime();
			return null;
		}
	}

	/**intercept press of search hard button*/
	@Override
	public boolean onSearchRequested() {
		if(radio_search!=null)
			radio_search.setChecked(true);
		return false;
	}


	/** allow back pressed to close an open drawer*/
	public void onBackPressed(){
		if(drawer.isOpened() || drawer.isMoving())
		{
			drawer.close();
		}
		else
		{
			int p = android.os.Process.myPid();
			android.os.Process.killProcess(p);
		}
	}

	private void initButtons() {

		//search bar show in search results 
		searchResultsBar = (LinearLayout)findViewById(R.id.search_reset_button);
		searchResultsBar.setClickable(true);
		searchResultsBar.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDialog(DIALOG_SEARCH_ID);
			}
		});

		//fav all
		favAllBar = (LinearLayout)findViewById(R.id.fav_all);
		favAllBar.setClickable(true);
		favAllBar.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				favouriteCurrentList();
			}
		});

		//delete all favs
		clearFavsBar = (LinearLayout)findViewById(R.id.clear_all_favs);
		clearFavsBar.setClickable(true);
		clearFavsBar.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				clearFavourites();
			}
		});

		//define radio buttons 
		radio_all = (RadioButton)findViewById(R.id.display_all_button);
		radio_fav = (RadioButton)findViewById(R.id.display_fav_button);
		radio_search = (RadioButton)findViewById(R.id.display_search_button);

		//init radio group listener 
		tab_radio = (RadioGroup)findViewById(R.id.listings_radio_group);
		tab_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if(checkedId == radio_all.getId())
				{
					showDrawer();
					ShowHide(favAllBar, clearFavsBar);
					hideSearchBar();
					fillData();
					lastChecked = radio_all.getId();
				}
				else if(checkedId == radio_fav.getId())
				{
					ShowHide(clearFavsBar,favAllBar);
					hideDrawer();
					hideSearchBar();
					fillFavourites();
					lastChecked = radio_fav.getId();
				}
				else if(checkedId == radio_search.getId())
				{
					showDialog(DIALOG_SEARCH_ID);
				}
				Log.i("","last checked in CheckListener: "+lastChecked + ", checked id: "+checkedId);
			}
		});

		//check "all" radio button
		radio_all.setChecked(true);
		lastChecked = radio_all.getId();
	}



	protected void ShowHide(LinearLayout a, LinearLayout b) {
		a.setVisibility(View.VISIBLE);
		b.setVisibility(View.GONE);
	}




	protected void favouriteCurrentList() {
		new favSelectionProgress().execute();

	}
	protected void clearFavourites() {
		new clearFavouritesProgress().execute();

	}

	/**progress dialog shown when CLEARING favourites*/
	public class clearFavouritesProgress extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public clearFavouritesProgress() {
			progress = new ProgressDialog(context);
			progress.setMessage("Clearing favourites...");
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {

			progress.dismiss();

			initialiseFavCount();
			fillFavourites();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			for(ListingRecord l : listings_array)
			{
				l.Favourite = SalesPartnerDbAdapter.NOT_FAVOURITE;
				Log.i("","ref = "+l.ref);
				mDbHelper.updateFavourite(l);
			}
			return null;
		}
	}
	/**progress dialog shown when adding a selection to favourites*/
	public class favSelectionProgress extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public favSelectionProgress() {
			progress = new ProgressDialog(context);
			progress.setMessage("Adding selection to favourites...");
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {

			progress.dismiss();

			initialiseFavCount();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			for(ListingRecord l : listings_array)
			{
				l.Favourite = SalesPartnerDbAdapter.FAVOURITE;
				Log.i("","ref = "+l.ref);
				mDbHelper.updateFavourite(l);
			}
			return null;
		}
	}




	protected void hideDrawer() {
		drawer.setVisibility(View.GONE);
		fake_drawer.setVisibility(View.VISIBLE);
	}

	protected void showDrawer() {
		drawer.setVisibility(View.VISIBLE);
		fake_drawer.setVisibility(View.GONE);
	}

	protected void hideSearchBar() {
		searchResultsBar.setVisibility(View.GONE);
		submenu.setVisibility(View.VISIBLE);
	}

	protected void clearSearchResults() {
		fillData();
	}

	private void showSearchBar() {
		hideDrawer();
		submenu.setVisibility(View.GONE);
		searchResultsBar.setVisibility(View.VISIBLE);
	}

	/** calls search() method on DB using LIKE commands to return cursor*/
	private void doSearch(String _search) {    
		listingsCursor = mDbHelper.search(_search);
		startManagingCursor(listingsCursor);

		ListingsCursorAdapter listings = new ListingsCursorAdapter(this,
				listingsCursor);
		setListAdapter(listings);
		listings.changeCursor(listingsCursor);
		showSearchBar();
	}

	public class MyCustomAdapter extends ArrayAdapter<String>{

		public MyCustomAdapter(Context context, int textViewResourceId,
				String[] objects) {
			super(context, textViewResourceId, objects);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return new View(context);
		}

		@Override
		public void setDropDownViewResource(int resource) {
			// TODO Auto-generated method stub
			super.setDropDownViewResource(resource);
		}
		
		

	}    


	/** initialize slide up options drawer*/
	private void initDrawer() {
		//Handle 
		LinearLayout drawer_handle = (LinearLayout)findViewById(R.id.drawer_handle);
		drawer_handle.setClickable(true);
		drawer_handle.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				updateDrawerFields();
			}
		}); 

		//Order spinner
		spinner = (nz.co.salespartner.Objects.CustomSpinner) findViewById(R.id.order_spinner);
		String[] orderarray = getResources().getStringArray(R.array.order_array);
		MyCustomAdapter adapter = new MyCustomAdapter((Context) context, android.R.layout.simple_spinner_item, orderarray);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener((OnItemSelectedListener) new OrderItemSelectedListener());



		/*ListedBy spinner*/
		listedby_spinner = (Spinner)findViewById(R.id.agent_spinner);

		//define edittexts
		min_price = (EditText)findViewById(R.id.dr_min_price);
		max_price = (EditText)findViewById(R.id.dr_max_price);
		min_beds = (EditText)findViewById(R.id.dr_min_beds);
		max_beds = (EditText)findViewById(R.id.dr_max_beds);
		byPrice = (CheckBox)findViewById(R.id.dr_by_price);
		byBedrooms = (CheckBox)findViewById(R.id.dr_by_beds);

		//set text values
		updateDrawerFields();

		//set checkbox listeners 
		byPrice.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ListingsFilter.FilterByPrice = byPrice.isChecked();
			}
		});

		byBedrooms.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ListingsFilter.FilterByBedrooms = byBedrooms.isChecked();
			}
		});

		//clear edittexts onFocus
		min_price.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				if (hasFocus==true)
				{
					min_price.setText("");
				}
			}
		});
		max_price.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				if (hasFocus==true)
				{
					max_price.setText("");
				}
			}
		});
		min_beds.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				if (hasFocus==true)
				{
					min_beds.setText("");
				}
			}
		});
		max_beds.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) 
			{
				if (hasFocus==true)
				{
					max_beds.setText("");
				}
			}
		});

		//set on click filter button
		Button filter_button = (Button)findViewById(R.id.dr_filterbutton);
		filter_button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				if(byPrice.isChecked() && areNotEmpty(min_price, max_price))
				{
					ListingsFilter.PriceMin = Integer.parseInt(min_price.getText().toString()) *1000;
					ListingsFilter.PriceMax = Integer.parseInt(max_price.getText().toString()) *1000;
				}
				else
				{
					ListingsFilter.FilterByPrice = true;
					ListingsFilter.PriceMin = 0;
					ListingsFilter.PriceMax = 9000000;
				}

				if(byBedrooms.isChecked() && areNotEmpty(min_beds, max_beds))
				{
					ListingsFilter.BedroomsMin = Integer.parseInt(min_beds.getText().toString());
					ListingsFilter.BedroomsMax = Integer.parseInt(max_beds.getText().toString());
				}
				else
				{
					ListingsFilter.BedroomsMin = 0;
					ListingsFilter.BedroomsMax = 99;
				}
				fillData();
				//hide keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(drawer.getWindowToken(), 0);
				drawer.close();


			}
		});

	}


	/**determine if edittexts are empty or not*/
	protected boolean areNotEmpty(EditText et1, EditText et2) {
		if(TextUtils.isEmpty(et1.getText().toString())==false && TextUtils.isEmpty(et2.getText().toString())==false)
			return true;
		else
			return false;
	}


	/** update textviews and checkboxes to match ListingsFilter fields*/
	public void updateDrawerFields(){
		//set text values
		ListingsFilter.FilterByBedrooms = byBedrooms.isChecked();
		ListingsFilter.FilterByPrice = byPrice.isChecked();
		min_price.setText(Integer.toString(ListingsFilter.PriceMin));
		max_price.setText(Integer.toString(ListingsFilter.PriceMax));
		min_beds.setText(Integer.toString(ListingsFilter.BedroomsMin));
		max_beds.setText(Integer.toString(ListingsFilter.BedroomsMax));
	}

	/** listener for drawer order spinner */
	public class OrderItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			order = pos;
			refresh();
		}


		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}

	private void refresh() {
		if(radio_all.isChecked())
			fillData();
		else if(radio_fav.isChecked())
			fillFavourites();
		else if(radio_search.isChecked())
			doSearch(search);
	}

	/** support method for order spinner */
	public String orderToString(int i){
		String result = "";
		switch(i){
		case SalesPartnerDbAdapter.ORDER_PRICE: result = "Price"; break;
		case SalesPartnerDbAdapter.ORDER_REF: result = "Reference"; break;
		case SalesPartnerDbAdapter.ORDER_STREET: result = "Street"; break;
		case SalesPartnerDbAdapter.ORDER_SUBURB: result = "Suburb"; break;
		case SalesPartnerDbAdapter.ORDER_LISTEDBY: result = "ListedBy"; break;
		default: break;
		}
		return result;
	}


	final String[] orderOptions = { "Price","Reference","Street","Suburb","ListedBy" };
	protected void showOrderDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Order Listings");
		builder.setItems(orderOptions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				order = item;
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

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

	@Override
	protected void onPause() {
		super.onPause();
		//		listingsCursor.close();
	}

	private void showListingsSyncActivity() {
		Intent mIntent = new Intent(this, ListingSynchro.class);
		startActivityForResult(mIntent, 2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				fillData();
			}
			break;

		case 2:
			Toast.makeText(getApplicationContext(),
					"Returned from Sync Listings", Toast.LENGTH_LONG).show();
			break;
		}
	};

	protected void showFilterListingsActivity() {
		Intent mIntent = new Intent(this, FilterActivity.class);
		startActivityForResult(mIntent, 1);
	}

	protected Dialog createQuitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit?").setCancelable(
				false).setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}



	protected Dialog createChangeOrderDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Sort Order");
		builder.setItems(order_items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				order = item;
				fillData();
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}

	/** returns search dialog*/
	protected Dialog createSearchDialog(){

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.search_dialog);
		dialog.setTitle("Search Listings");
		dialog.setCancelable(true);

		final EditText edittext = (EditText) dialog.findViewById(R.id.sd_edittext);

		//set up button
		Button searchb = (Button) dialog.findViewById(R.id.sd_search_button);
		searchb.setOnClickListener(new OnClickListener() {


			public void onClick(View v) {
				dialog.dismiss();
				search = edittext.getText().toString();
				doSearch(search);

			}
		});

		Button cancel = (Button) dialog.findViewById(R.id.sd_cancel_button);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Log.i("","last checked: "+lastChecked);
				radio_all.setChecked(true); //TODO fix

			}
		});


		return dialog;
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_QUIT_ID:
			return createQuitDialog();
		case DIALOG_ORDER_ID:
			return createChangeOrderDialog();
		case DIALOG_SEARCH_ID:
			return createSearchDialog();
		default:
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		//		mDbHelper.close();
		super.onDestroy();
	}

	private void quit() {
		showDialog(DIALOG_QUIT_ID);
	}

	private void changeSortOrder() {
		showDialog(DIALOG_ORDER_ID);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent mIntent = new Intent(this, ListingTabHost.class);
		mIntent.putExtra("id", listingsCursor.getInt(listingsCursor
				.getColumnIndex("_id")));
		startActivity(mIntent);
		//		/Log.i("", "OnListItemClick... id = "l.id)
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		listingsCursor = mDbHelper.fetchListings(order, ListingsFilter, LISTEDBY);
		updateListingsArray();
		startManagingCursor(listingsCursor);
		ListingsCursorAdapter listings = new ListingsCursorAdapter(this,
				listingsCursor);

		setListAdapter(listings);
		lv.invalidate();
		setAllCount(listings.getCount());
		Log.i("Listings", "Completed fillData()");
	}  

	private void setAllCount(int count) {
		radio_all.setText("All  ("+count+")");
	}

	private void setFavouriteCount(int count) {
		radio_fav.setText("Favourites  ("+count+")");
	}

	private ListingsCursorAdapter fillDataFirstTime() {
		// Get all of the rows from the database and create the item list
		listingsCursor = mDbHelper.fetchListings(order, ListingsFilter, LISTEDBY);
		startManagingCursor(listingsCursor);
		ListingsCursorAdapter listings = new ListingsCursorAdapter(this,
				listingsCursor);
		updateListingsArray();
		return listings;

	}

	private void fillFavourites() {
		// Get all of the rows from the database and create the item list
		listingsCursor = mDbHelper.fetchFavourites(order);
		startManagingCursor(listingsCursor);
		ListingsCursorAdapter listings = new ListingsCursorAdapter(this,
				listingsCursor);
		setListAdapter(listings);
		updateListingsArray();
		setFavouriteCount(listings.getCount());
		Log.i("Listings", "Completed fillFavourites()");
	}

	private void updateListingsArray() {
		listings_array.clear();
		listingsCursor.moveToFirst();
		while (listingsCursor.isAfterLast() == false) {

			/* build current listing*/
			ListingRecord current = new ListingRecord();
			current.ref = listingsCursor.getString(listingsCursor.getColumnIndex("ref"));
			current.Favourite = listingsCursor.getInt(listingsCursor.getColumnIndex("Favourite"));
			listings_array.add(current);

			listingsCursor.moveToNext();
		}

	}


	private void initialiseFavCount(){
		Cursor favs = mDbHelper.fetchFavourites(order);
		startManagingCursor(favs);
		ListingsCursorAdapter flistings = new ListingsCursorAdapter(this,
				favs);
		setFavouriteCount(flistings.getCount());
	}

	/** fills listview in listings activity*/
	class ListingsCursorAdapter extends CursorAdapter {

		public ListingsCursorAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		private void setViewInvisibleIfZero(View view, int x) {
			if (x == 0) {
				view.setVisibility(View.INVISIBLE);
			} else {
				view.setVisibility(View.VISIBLE);
			}

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView refView = (TextView) view.findViewById(R.id.text1);
			TextView streetView = (TextView) view.findViewById(R.id.street);
			TextView bedroomsView = (TextView) view.findViewById(R.id.bedrooms);
			TextView bathroomsView = (TextView) view
					.findViewById(R.id.bathrooms);
			TextView garagingView = (TextView) view.findViewById(R.id.garaging);
			TextView documents_text = (TextView) view.findViewById(R.id.documents_text);
			ImageView bedroomsIcon = (ImageView) view
					.findViewById(R.id.bedrooms_icon);
			ImageView bathroomsIcon = (ImageView) view
					.findViewById(R.id.bathrooms_icon);
			ImageView garagingIcon = (ImageView) view
					.findViewById(R.id.garaging_icon);
			ImageView docsIcon = (ImageView) view
					.findViewById(R.id.documents_icon);
			TextView priceView = (TextView) view.findViewById(R.id.listing_row_DisplayPrice);
			TextView searchpriceView = (TextView) view.findViewById(R.id.listing_row_SearchPrice);
			TextView ListedView = (TextView) view.findViewById(R.id.listing_row_listedBy);
			TextView listeddateView = (TextView) view.findViewById(R.id.listing_row_listedDate);
			ImageView thumbnail = (ImageView)view.findViewById(R.id.row_thumbnail);

			String ref = cursor.getString(cursor.getColumnIndex("ref"));
			String street = cursor.getString(cursor.getColumnIndex("Street"));
			String stno = cursor.getString(cursor.getColumnIndex("StNo"));
			String suburb = cursor.getString(cursor.getColumnIndex("Suburb"));
			int bedrooms = cursor.getInt(cursor.getColumnIndex("Bedrooms"));
			int bathrooms = cursor.getInt(cursor.getColumnIndex("Bathrooms"));
			int garaging = cursor.getInt(cursor.getColumnIndex("Garaging"));
			int doccount = cursor.getInt(cursor.getColumnIndex("DocumentCount"));
			String displayPrice = cursor.getString(cursor.getColumnIndex("DisplayPrice"));
			String searchPrice = cursor.getString(cursor.getColumnIndex("SearchPrice"));
			String ListedDate = cursor.getString(cursor.getColumnIndex("ListedDate"));
			String listedBy = cursor.getString(cursor.getColumnIndex("ListedBy"));

			refView.setText(ref);
			streetView.setText(stno + " " + street+"\n"+suburb);
			bedroomsView.setText(Integer.toString(bedrooms));
			bathroomsView.setText(Integer.toString(bathrooms));
			garagingView.setText(Integer.toString(garaging));
			documents_text.setText(Integer.toString(doccount));

			TextView cameraView = (TextView)view.findViewById(R.id.listingrow_camera);			
			cameraView.setText(Integer.toString(PhotoManager.photoCount(ref)));			

			//set thumbnail
			String picture1FileName = PhotoManager.getThumbName(ref, 1);
			File file = new File(picture1FileName);
			if (file.exists()) { //Set thumbnail and make visible
				Log.d(TAG, "Picture Found: " + picture1FileName);
				Bitmap bMap = BitmapFactory.decodeFile(picture1FileName);
				thumbnail.setImageBitmap(bMap);
				View parent = (View)thumbnail.getParent();
				parent.setVisibility(View.VISIBLE);
			} else { // Set thumbnail null and make invisible
				View parent = (View)thumbnail.getParent();
				parent.setVisibility(View.INVISIBLE);
				thumbnail.setImageBitmap(null); //TODO clear image
				Log.d(TAG, "Picture not Found: " + picture1FileName);
			}

			setViewInvisibleIfZero(bedroomsView, bedrooms);
			setViewInvisibleIfZero(bedroomsIcon, bedrooms);
			setViewInvisibleIfZero(bathroomsView, bathrooms);
			setViewInvisibleIfZero(bathroomsIcon, bathrooms);
			setViewInvisibleIfZero(garagingView, garaging);
			setViewInvisibleIfZero(garagingIcon, garaging);
			setViewInvisibleIfZero(docsIcon, doccount);
			setViewInvisibleIfZero(documents_text, doccount);
			priceView.setText(displayPrice);
			ListedView.setText("LB: "+listedBy);
			searchpriceView.setText("SP: $"+searchPrice);
			listeddateView.setText("Listed: "+ListedDate);

			//hide or show search price depending on preferences
			if(showSearchPrice)
				searchpriceView.setVisibility(View.VISIBLE);
			else
				searchpriceView.setVisibility(View.GONE);
			if(showListedDate)
				listeddateView.setVisibility(View.VISIBLE);
			else
				listeddateView.setVisibility(View.GONE);

			//hide or show listedby depending on preferences
			if(showListedBy)
				ListedView.setVisibility(View.VISIBLE);
			else
				ListedView.setVisibility(View.GONE);
  

			TextView[] tvs = {priceView,refView, streetView, bedroomsView, bedroomsView
					,bathroomsView, garagingView};  
			stylefactory.applyFont(tvs);
		}

		@Override  /** give listview children alternating colors*/
		public View getView(int position, View convertView, ViewGroup parent) {  
			View v = super.getView(position, convertView, parent);  

			if(position % 2 == 0) //alternating colors
				v.setBackgroundResource(R.drawable.row_states_dark);
			else
				v.setBackgroundResource(R.drawable.row_states_light);

			return v;  
		}  


		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.listing_row, parent, false);


			return v;

		}



	}
}