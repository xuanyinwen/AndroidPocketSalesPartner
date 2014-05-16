package nz.co.salespartner.Activities;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import nz.co.salespartner.R;
import nz.co.salespartner.SalesPartnerApplication;
import nz.co.salespartner.Helpers.Mailer;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Helpers.TimeProtector;
import nz.co.salespartner.Objects.Item;
import nz.co.salespartner.Objects.ListingRecord;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ViewListing extends Activity {
	private static String TAG = "ViewListing";
	private String ref;
	private SalesPartnerApplication app;
	private ViewFlipper vf;
	private final String COUNTRY = "New Zealand";
	private final int DIALOG_SHARE_ID = 1;
	private ListAdapter share_menu_adapter;
	private Mailer mailer;
	private ListingRecord l;
	private float lastX = 0;
	View.OnTouchListener gestureListener;
	private Context context;
	private int id;
	private CheckBox checkbox;
	private Cursor c;
	private SalesPartnerDbAdapter mDbHelper;
	private StyleFactory stylefactory;
	ArrayList<TextView> textviews;
	private TimeProtector timeProtector;
	private Activity activity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*dither gradients*/
			getWindow().setFormat(PixelFormat.RGBA_8888); 
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.view_listing);
		stylefactory = new StyleFactory(this);
		app = new SalesPartnerApplication();
		context = this;
		activity = this;
		id = getIntent().getExtras().getInt("id");
		fillData(id);
		mailer = new Mailer(l,this);
		initPMChecksBoxes();
		initStarButton();
		initShareButtons();
		initShareAdaptor();
		//styleAllTVs(R.id.vl_wrapper);
		styleAll();
		timeProtector = new TimeProtector(context);
	} 

	/**make all bold textviews Roboto-Bold.ttf font*/
	private void styleAll() {
		int[] tvs = {
				R.id.share_bold_1,R.id.share_bold_2,
				R.id.vl_bold1, R.id.vl_bold2,
				R.id.vl_bold3, R.id.vl_bold4,
				R.id.vl_bold5, R.id.vl_bold6,
				R.id.vl_bold7, R.id.vl_bold8,
				R.id.vl_bold9, R.id.vl_bold10,
				R.id.vl_bold11, R.id.vl_bold12
				, R.id.vl_bold13, R.id.vl_bold14
				, R.id.vl_bold15, R.id.vl_bold16
				, R.id.vl_bold17, R.id.vl_bold18
				, R.id.vl_bold19, R.id.vl_bold20
				, R.id.vl_bold21, R.id.vl_bold22
				, R.id.vl_bold23, R.id.vl_bold24
				, R.id.vl_bold25, R.id.vl_bold26
				, R.id.vl_bold27, R.id.vl_bold28
		};
		TextView current = null;
		for(int id : tvs)
		{
			current = (TextView)findViewById(id);
			stylefactory.applyBold(current);
		}
	}

	private void styleAllTVs(int wrapper_id) {
		TableLayout v = (TableLayout)findViewById(wrapper_id);
		textviews = new ArrayList<TextView>();
		iterateChildren(v);
		Typeface tf = null;
		for(TextView current : textviews)
		{
			tf = current.getTypeface();
			
			if(tf.getStyle()==Typeface.BOLD)
				stylefactory.applyBold(current);
			else
				stylefactory.applyFont(current);
		}
		
	}

	private void iterateChildren(View v) {
		int numChildren = 0;
		if(v instanceof LinearLayout || v instanceof TableRow)
			numChildren = ((ViewGroup) v).getChildCount();
		View child = null;
		
		
		
		for(int i = 0 ; i < numChildren; i++)
		{
			child = ((ViewGroup) v).getChildAt(i);
			if(child instanceof TextView)
			{
				Log.i("","inside instanceof");
				textviews.add((TextView) child);
			}
			
			else if (child instanceof LinearLayout || child instanceof TableRow)
				{
				if(((ViewGroup) child).getChildCount() > 0)
				iterateChildren(child);
				}
		}		
	}

	/** plus minus checkboxes for hiding private info*/
	private void initPMChecksBoxes() {
		RelativeLayout banner2 = (RelativeLayout)findViewById(R.id.banner2);
		CheckBox priv = (CheckBox)findViewById(R.id.private_pm);
			int[] priv_children = {R.id.private_1, R.id.private_2, R.id.private_3};
			setPMonClick(banner2,priv, priv_children);
		
		RelativeLayout banner1 = (RelativeLayout)findViewById(R.id.banner1);
		CheckBox legal = (CheckBox)findViewById(R.id.legal_pm);
			int[] legal_children = {R.id.legal_1, R.id.legal_2, R.id.legal_3, R.id.legal_4, R.id.legal_5};
			setPMonClick(banner1, legal, legal_children);
		
		RelativeLayout banner3 = (RelativeLayout)findViewById(R.id.banner3);
		CheckBox vend = (CheckBox)findViewById(R.id.vendor_pm);
			int[] vend_children = {R.id.vendor_1, R.id.vendor_2, R.id.vendor_3, R.id.vendor_4
					, R.id.vendor_5, R.id.vendor_6, R.id.vendor_7, R.id.vendor_8, R.id.vendor_9};
			setPMonClick(banner3,vend, vend_children);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Boolean autoHide = prefs.getBoolean("hidePrivate", true);
			if(autoHide)
			{
				legal.setChecked(true);
				vend.setChecked(true);
				priv.setChecked(true);
			}
	}

	/**onClick listener for checkboxes
	 * @param banner */
	private void setPMonClick(final RelativeLayout banner, final CheckBox checkbox, final int[] children) {
		banner.setOnClickListener(new OnClickListener() {
			 
			public void onClick(View arg0) {
				checkbox.setChecked(!checkbox.isChecked());
			}
		});
		
		
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				View child_view;
				if(isChecked)
				{
					for(int child : children)//if checked hide
					{
						child_view = (View)findViewById(child);
						child_view.setVisibility(View.GONE);
					}
				}
				else{   //show children if allowed
						Log.i(TAG,"is allowed to open = "+timeProtector.isAllowedToOpen());
						if(timeProtector.isAllowedToOpen())
						{
							for(int child : children)
							{
								child_view = (View)findViewById(child);
								child_view.setVisibility(View.VISIBLE);
							}
						}
						else{
							timeProtector.askForPassword(activity,children);
						}
					}
			}
		});
	}
	

	/** initialise checkbox*/
	private void initStarButton() {
		checkbox = (CheckBox)findViewById(R.id.vl_star_box);
		if(isFavourite()) //check if fav
			checkbox.setChecked(true);
		else
			checkbox.setChecked(false);
		
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() { //set on check listener
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					l.Favourite = SalesPartnerDbAdapter.FAVOURITE;
				else
					l.Favourite = SalesPartnerDbAdapter.NOT_FAVOURITE;
				mDbHelper.open();
				c = mDbHelper.fetchListing(id);
				mDbHelper.updateListing(c.getInt(c.getColumnIndex("_id")), l);
			}
			
		});
	}

	/** check if listing is favourite*/
	private boolean isFavourite() {
		if(l.Favourite == SalesPartnerDbAdapter.FAVOURITE)
			return true;
		else
			return false;
	}

	/**popup menu for sharing listing*/
	private void initShareAdaptor() {
		share_menu_adapter = new ArrayAdapter<Item>(
				this,
				android.R.layout.select_dialog_item,
				android.R.id.text1,
				items){
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView)v.findViewById(android.R.id.text1);
				tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);
				return v;
			}
		};

	}

	/**initialise sub-menu buttons*/
	private void initShareButtons() {
		//Share button
		LinearLayout share_button = (LinearLayout)findViewById(R.id.share_button);
		share_button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				showDialog(DIALOG_SHARE_ID);
			}
		});
		//TradeMe button
		LinearLayout trademe_button = (LinearLayout)findViewById(R.id.trademe_button);
		trademe_button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent trademeIntent =  new Intent(context,TradeMe.class);
				trademeIntent.putExtra("id", id);
				trademeIntent.putExtra("rv", l.GV);
				context.startActivity(trademeIntent);
			}
		});

	}

	private void setViewInvisibleIfZero(View view, int x) {
		if (x == 0) {
			view.setVisibility(View.INVISIBLE);
		} else {
			view.setVisibility(View.VISIBLE);
		}

	}

	private void setTextOnView(int id, String value) {
		TextView x = (TextView) findViewById(id);
		if (value != "null")
			{
			x.setText(value);
			stylefactory.applyFont(x);
			}
		
	}
	
	private void setEmailText(int id, final String value) {
		TextView x = (TextView) findViewById(id);
		
		if (value != "null")
			{
			x.setText(value);
			x.setClickable(true);
			x.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					String aEmailList[] = { value };
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
					emailIntent.setType("plain/text");
					startActivity(emailIntent);
				}
			});
			
			}
	}

	/**open DB, find listing by id and set Text on ViewListing TextViews*/
	private void fillData(int id) {
		mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		c = mDbHelper.fetchListing(id);
		l = new ListingRecord();
		l.FromCursor(c);
		c.close();
		mDbHelper.close();

		ref = l.ref;
		setTextOnView(R.id.view_listing_ref, ref);

		String stno_street = (l.StNo + " " + l.Street).trim();
		String map_query = "geo:0,0?q="+stno_street +", " + l.Suburb+", "+l.District+", "+ COUNTRY+"&z=8"; //set Google Map query

		// Bedrooms Bathrooms and Garaging
		TextView bedroomsView = (TextView) findViewById(R.id.view_listing_bedrooms);
		TextView bathroomsView = (TextView) findViewById(R.id.view_listing_bathrooms);
		TextView garagingView = (TextView) findViewById(R.id.view_listing_garaging);
		ImageView bedroomsIcon = (ImageView) findViewById(R.id.view_listing_bathrooms_icon);
		ImageView bathroomsIcon = (ImageView) findViewById(R.id.view_listing_bathrooms_icon);
		ImageView garagingIcon = (ImageView) findViewById(R.id.view_listing_garaging_icon);
		bedroomsView.setText(Integer.toString(l.Bedrooms));
		bathroomsView.setText(Integer.toString(l.Bathrooms));
		garagingView.setText(Integer.toString(l.Garaging));
		setViewInvisibleIfZero(bedroomsView, l.Bedrooms);
		setViewInvisibleIfZero(bedroomsIcon, l.Bedrooms);
		setViewInvisibleIfZero(bathroomsView, l.Bathrooms);
		setViewInvisibleIfZero(bathroomsIcon, l.Bathrooms);
		setViewInvisibleIfZero(garagingView, l.Garaging);
		setViewInvisibleIfZero(garagingIcon, l.Garaging);

		// Property Address
		setTextOnView(R.id.view_listing_street, stno_street);
		setTextOnView(R.id.view_listing_suburb, l.Suburb);
		setTextOnView(R.id.view_listing_district, l.District);
		setTextOnView(R.id.view_listing_listedby, l.getListedBy());
		setMapButton(R.id.view_listing_map_button, map_query, stno_street);

		// Property Details
		setTextOnView(R.id.view_listing_rates, l.Rates);
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		nf.setMaximumFractionDigits(0);
		setTextOnView(R.id.view_listing_RV, nf.format(l.GV));
		setTextOnView(R.id.view_listing_LV, nf.format(l.LV));

		setTextOnView(R.id.view_listing_FloorArea, l.FloorArea + l.FloorAreaUnit);
		setTextOnView(R.id.view_listing_LandArea, l.LandArea + l.LandAreaUnit);
		setTextOnView(R.id.view_listing_legal_description, l.LegalDescription);
		setTextOnView(R.id.view_listing_Tenancy, l.Tenancy);

		// Listing Details
		setTextOnView(R.id.view_listing_status, l.ListingStatus);
		setTextOnView(R.id.view_listing_ListedDate, l.ListedDate);
		setTextOnView(R.id.view_listing_ExpiryDate, l.ExpiryDate);
		setTextOnView(R.id.view_listing_price, l.DisplayPrice);
		
		// Weblink and openhome
		setTextOnView(R.id.view_listing_weblink, l.WebLink);
		TextView link = (TextView)findViewById(R.id.view_listing_weblink);
		link.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String url = l.WebLink;
				if (!url.startsWith("http://") && !url.startsWith("https://"))
					   url = "http://" + url;
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(browserIntent);
			}
		});
		
		String openhome = "";
		if(l.OpenHomeTime > 0 && l.OpenHomeDuration > 0)
		{ 
		openhome = "On "+l.getOpenHomeTime(context)+" for "+l.OpenHomeDuration+" minutes";
		setTextOnView(R.id.view_listing_openhome, openhome);
		}
		// Advertising
		setTextOnView(R.id.view_listing_AdvertHeading, l.Heading);
		setTextOnView(R.id.view_listing_AdvertBody, l.Advert);
		setTextOnView(R.id.view_listing_SellingPoints, l.SellingPoints);
		setTextOnView(R.id.view_listing_Features, l.Features);

		// Private Stuff
		setTextOnView(R.id.view_listing_PrivateFeatures, l.PrivateFeatures);
		setTextOnView(R.id.view_listing_KeyDetails, l.KeyDetails);
		setTextOnView(R.id.view_listing_SearchPrice, Integer.toString(l.SearchPrice));

		// Vendor Details
		setTextOnView(R.id.view_listing_VendorSurname, l.VendorSurname);
		setTextOnView(R.id.view_listing_VendorGreeting, l.VendorGreeting);
		setTextOnView(R.id.view_listing_VendorTitle, l.VendorTitInit);
		setTextOnView(R.id.view_listing_Ph1, l.VendorPh1);
		setCallButton(R.id.view_listing_Ph1_call_button, l.VendorPh1);
		setCallButton(R.id.view_listing_Ph2_call_button, l.VendorPh2);
		setCallButton(R.id.view_listing_Ph3_call_button, l.VendorPh3);
		setTextOnView(R.id.view_listing_Ph2, l.VendorPh2);
		setTextOnView(R.id.view_listing_Ph3, l.VendorPh3);
		setTextOnView(R.id.view_listing_Ph1Type, l.VendorPh1Type+" ");
		setTextOnView(R.id.view_listing_Ph2Type, l.VendorPh2Type+" ");
		setTextOnView(R.id.view_listing_Ph3Type, l.VendorPh3Type+" ");
		setTextOnView(R.id.view_listing_Fax, l.Fax);
		setEmailText(R.id.view_listing_VendorEmail, l.VendorEmail);

		String VendorAddress = (l.VendorStNo + " " + l.VendorStreet).trim();
		if (l.VendorSuburb != "") {
			VendorAddress += "\n" + l.VendorSuburb;
		}

		setTextOnView(R.id.view_listing_VendorAddress, VendorAddress);
		
	}

	private void setCallButton(int id, final String number) {
		Button b = (Button)findViewById(id);
		if(TextUtils.isEmpty(number) == false)
		{
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					call(number);
				}
			});
		}
		else
		{
			b.setVisibility(View.GONE);
		}
	}

	private void setMapButton(int id, final String query, String streetno) {
		Button b = (Button)findViewById(id);
		if(TextUtils.isEmpty(streetno) == false)
		{
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					launchMap(query);
				}
			});
		}
		else
		{
			b.setVisibility(View.GONE);
		}
	}

	/**opens external map application*/
	protected void launchMap(String address) {
		Uri geoUri = Uri.parse(address);  
		Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);  
		startActivity(mapCall); 
	}

	protected void showPhoto() {
		if (PhotoManager.photoCount(ref) == 1) {
			Intent mIntent = new Intent(this, ViewPhoto.class);
			mIntent.putExtra("fileName", PhotoManager.getFileName(ref, 1));
			startActivity(mIntent);
		} else {
			Intent mIntent = new Intent(this, PhotoGallery.class);
			mIntent.putExtra("ref", ref);
			startActivity(mIntent);			
		}
	}
	/** Start phone call to passed number*/
	private void call(String number) {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:"+number));
			startActivity(callIntent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Call failed", e);
		}
	}
	
	/** Start phone call to passed number*/
	private void email(String address) {
		try {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setData(Uri.parse("email:"+address));  //??
			startActivity(emailIntent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Call failed", e);
		}
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SHARE_ID:
			return createShareDialog();
		default:
			return null;
		}
	}
	/** share listing dialog*/
	protected Dialog createShareDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Share Listing");
		builder.setAdapter(share_menu_adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0: mailer.sendEmail(); break;  //Email
				case 1: mailer.sendText(); break;	//Text
//				case 2: mailer.toFaceBook(); break;	//FaceBook
//				case 3: mailer.toTwitter(); break;	//Twitter
				default: break;
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
			
		AlertDialog alert = builder.create();
		return alert;
	}
	/** dialog items */
	final Item[] items = {
			new Item("Email", R.drawable.ic_email),
			new Item("Txt", R.drawable.ic_text),
//			new Item("Facebook", R.drawable.ic_facebook),
//			new Item("Twitter", R.drawable.ic_twitter),
	};

	
}
