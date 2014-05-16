package nz.co.salespartner.Activities;

import java.io.File;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Objects.ListingRecord;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class ListingTabHost extends TabActivity{

	private TabHost mTabHost;
	private StyleFactory stylefactory;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.listing_tab_host);
		stylefactory = new StyleFactory(this);
		stylefactory.initHeader(this);
		int id = getIntent().getExtras().getInt("id");
		/** TabHost will have Tabs */
		TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

		/*open database, find current listing*/
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		Cursor c = mDbHelper.fetchListing(id);
		ListingRecord l = new ListingRecord();
		l.FromCursor(c);
		c.close();
		mDbHelper.close();
		String ref = l.ref;
		
		/*get doc count*/
		File dir = new File(Environment.getExternalStorageDirectory()+"/PSPDocs/"+ref);
		Log.i("FILE DIR = ",Environment.getExternalStorageDirectory()+"/PSPDocs/"+ref);
		if(!dir.exists())
		{
			File pspdocsFolder =new File(Environment.getExternalStorageDirectory()+"/PSPDocs");
			pspdocsFolder.mkdir();
			dir.mkdir();
		}
		int docCount = dir.listFiles().length;
		
		/*get pic count*/
		int picCount = PhotoManager.photoCount(ref);
		
		/*view listings intent*/
		Intent ViewListingIntent = new Intent(this,ViewListing.class);
		ViewListingIntent.putExtra("id", id);

		/*set up photogallery intent*/
		Intent PhotoGalleryIntent = new Intent(this,PhotoGallery.class);
		PhotoGalleryIntent.putExtra("ref", ref);
		if(l.ServerPictureCount > 0) // check for no photos
			PhotoGalleryIntent.putExtra("hasPhotos", true);
		else
			PhotoGalleryIntent.putExtra("hasPhotos", false);

		/*view documents intent*/
		Intent ListingDocs = new Intent(this,ListingDocuments.class);
		ListingDocs.putExtra("id", id);
		ListingDocs.putExtra("ref", ref);
		ListingDocs.putExtra("vendorEmail", l.VendorEmail);
		ListingDocs.putExtra("address", l.StNo+" "+l.Street);

		/*map view intent*/
		Intent MapIntent = new Intent(this,MapViewActivity.class);
		MapIntent.putExtra("id", id);

		/*set up tabs*/
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		setupTab(new TextView(this), "Info", ViewListingIntent);
		setupTab(new TextView(this), "Pics "+picCount, PhotoGalleryIntent);
		setupTab(new TextView(this), "Maps", MapIntent);
		setupTab(new TextView(this), "Docs "+docCount, ListingDocs);
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

	private void setupTab(final View view, final String tag,Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag);
		TabSpec tab = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
			public View createTabContent(String tag) {return view;}
		});
		tab.setContent(intent);
		mTabHost.addTab(tab);
	}

	private View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		stylefactory.applyBold(tv);
		return view;
	}

}
