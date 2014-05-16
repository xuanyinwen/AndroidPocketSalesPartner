package nz.co.salespartner.Activities;

import java.net.Authenticator;
import java.util.UUID;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.Authenicator;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Landing extends Activity{

	private Context context;
	private StyleFactory stylefactory;
	Activity activity;
	private Button[] buttonArray; 
	private String TAG = "PSP-Android Main";
	private int TOAST_TIME = 8000;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		context = this;
		setContentView(R.layout.landing);
		stylefactory = new StyleFactory(this);
		styleTitles();
		initButtons();
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(this);
		mDbHelper.open();
		Listings.mDbHelper = mDbHelper;
		activity = this;
		
		}
	
	@Override
	protected void onResume() {
		super.onResume();
		//make authenicator
		Authenicator auth = new Authenicator(this.context, this.activity);
		auth.authenicateDeviceIfRequired();
		//check prefs and lock landing buttons if isLocked returns true
		SharedPreferences hiddenPrefs = activity.getSharedPreferences("authenicationsettings",Context.MODE_PRIVATE);
		if(hiddenPrefs.getBoolean("isLocked", false))
		{
			Log.i(TAG,"locking landing buttons");
			lockLanding();
		}
		else
		{
			Log.i(TAG,"unlocking landing");
			initButtons();
		}
		if(hiddenPrefs.getBoolean("gracePeriodOn", true) && !hiddenPrefs.getBoolean("isLocked", false))
			showEarlyGraceToast();
	}

	private void showGraceToast() {
		Toast.makeText(context, "The grace period for not registering PSP Android has expired." +
				"Make sure you have set a valid customer number in settings or contact support.", TOAST_TIME).show();	
	}
	private void showEarlyGraceToast() {
		Toast.makeText(context, "This app is currently in a 14day grace period. Please contact support.", TOAST_TIME ).show();	
	}

	// disable landing button key presses and show toast instead;
	private void lockLanding() {
		for(Button b : buttonArray)
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
				showGraceToast();
				}
			});
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

	private void styleTitles() {

	}

	private void initButtons() {
		
		/*listings button*/
		Button listings = (Button)findViewById(R.id.landing_listings_button);
		listings.setOnClickListener(new OnClickListener() {
			  
			public void onClick(View arg0) {
			Intent listings_intent = new Intent(context, Listings.class);	
			startActivity(listings_intent);
			}
		});

		/*docs button*/
		Button docs = (Button)findViewById(R.id.landing_documents_button);
		docs.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent docs_intent = new Intent(context, AllDocs.class);	
				startActivity(docs_intent);
			}
		});

		/*maps button*/
		Button maps = (Button)findViewById(R.id.landing_map_button);
		maps.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent maps_intent = new Intent(context, MapForAll.class);	
				startActivity(maps_intent);
			}
		});
		
		/*calc button*/
		Button tools = (Button)findViewById(R.id.landing_tools_button);
		tools.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent tools_intent = new Intent(context, Calculator.class);	
				startActivity(tools_intent);
			}
		});
		
		/*options button*/
		Button options = (Button)findViewById(R.id.landing_option_button);
		options.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent options_intent = new Intent(context, ListingSynchro.class);	
				startActivity(options_intent);
			}
		});
		
		/*help button*/
		Button help = (Button)findViewById(R.id.landing_help_button);
		help.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent help_intent = new Intent(context, HelpDesk.class);	
				startActivity(help_intent);
			}
		});
		
		Button[] buttons = {help,options,tools,maps,docs,listings};
		buttonArray = buttons;
		stylefactory.applyFont(buttons);
	}
	
	
}