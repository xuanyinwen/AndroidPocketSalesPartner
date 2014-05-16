package nz.co.salespartner.Activities;



import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Wheel.ArrayWheelAdapter;
import nz.co.salespartner.Wheel.OnWheelChangedListener;
import nz.co.salespartner.Wheel.WheelView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Calculator extends Activity {
	private TextView tvCommission;
	private String NUMBERSBYONE[] = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};  
	private String NUMBERSBYONETHOUSAND[] = new String[] {"000", "100", "200", "300", "400", "500", "600", "700", "800", "900"};
	private String TAG = "CommissionCalculator";
	
	int wheel_0_position;   	
	int wheel_1_position;
	int wheel_2_position;
	int wheel_3_position;
	int wheel_4_position;
	
	SharedPreferences prefs;
	private boolean GSTpreferred;
	private double GSTpercent;
	private TextView tvHeader;
	private StyleFactory stylefactory;
	private TextView salepriceview;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calculator);
		context = this;
		tvHeader = (TextView)findViewById(R.id.textView2);
		tvCommission = (TextView)findViewById(R.id.textViewCommission);
		salepriceview = (TextView)findViewById(R.id.sale_price);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		stylefactory = new StyleFactory(this);
		TextView[] tvs = {tvCommission, tvHeader};
		stylefactory.applyFont(tvs);
		stylefactory.initHeader(this);
		stylefactory.applyBold(salepriceview);
		initWheel(R.id.comcalc_0, NUMBERSBYONE);
		initWheel(R.id.comcalc_1, NUMBERSBYONE);
		initWheel(R.id.comcalc_2, NUMBERSBYONE);
		initWheel(R.id.comcalc_3, NUMBERSBYONE);
		initWheel(R.id.comcalc_4, NUMBERSBYONETHOUSAND);	
		loadWheelPosition();
		if (getscrOrientation() == 1) {
			LinearLayout ll = (LinearLayout)findViewById(R.id.linearlayoutCommission);
			ll.setOrientation(LinearLayout.VERTICAL);
		}
		initButtons();	
	}
	
	private void initButtons() {
		Button settings_button = (Button)findViewById(R.id.comcalc_settingsbutton);
		settings_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				createSettingsDialog();
			}
		});
	}

	protected void createSettingsDialog() {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_bg); //set to custom bg
		dialog.setContentView(R.layout.calc_settings);
		dialog.setCancelable(true);
		
		
		/*define edittexts*/
		EditText flatrate = (EditText)dialog.findViewById(R.id.cs_fr);
		EditText basecommission = (EditText)dialog.findViewById(R.id.cs_bc);
		EditText uptoprice = (EditText)dialog.findViewById(R.id.cs_ut);
		EditText commissionafter = (EditText)dialog.findViewById(R.id.cs_ct);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final EditText[] edittextArray  = {flatrate,basecommission,uptoprice,commissionafter};
		final String[] settingsKeys = {"FlatFee","CommissionBase","UpToPrice","CommissionThereAfter"}; 
		
		stylefactory.setOnFocusChanged(edittextArray);
		
		/*set text*/
		for(int i = 0 ; i < edittextArray.length ; i++)
		{
			String settingStr = prefs.getString(settingsKeys[i].toString(), "0");
			EditText e = edittextArray[i];
			e.setText(settingStr);
		}
		
		
		
		/*define update button*/
		Button update = (Button)dialog.findViewById(R.id.cs_update_button);
		update.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// if none of edittext are empty then change shared prefs
				if(areNotEmpty(edittextArray))
				{
					for(int i = 0 ; i < edittextArray.length ; i++)
					{
						changePref(settingsKeys[i],edittextArray[i].getText().toString());
					}
				}
				//then update
				dialog.dismiss();
				updateStatus();
			}
			
			
		});
		
		/*define update button*/
		ImageView close = (ImageView)dialog.findViewById(R.id.cs_close_button);
		close.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				dialog.dismiss();
			}
			
			
		});
		
		
		dialog.show();
	}
	
	private boolean areNotEmpty(EditText[] ets){
		boolean notEmpty = true;
		for(EditText e : ets)
		{
			if(TextUtils.isEmpty(e.getText()))
					notEmpty = false;
		}
		return notEmpty;
	}

	/**edit shared preferences*/
	private void changePref(String from, String to) {
		Editor editor = prefs.edit();
		editor.putString(from, to);
		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		GSTpreferred = prefs.getBoolean("includeGST", true);
		updateStatus();
	}
	
	@Override
	protected void onDestroy() {
		saveWheelPostion();
		super.onDestroy();
	}
	
    private void initWheel(int id, String[] items) {
        WheelView wheel = (WheelView)findViewById(id);
        ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(context, items);
        if (getscrOrientation() == 1) //portrait 
        	adapter.setTextSize(36);
        else
        	adapter.setTextSize(24);
        	
        wheel.setViewAdapter(adapter);       
        wheel.addChangingListener(changedListener);
//        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
        updateStatus();
    }
    
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {     
                updateStatus();      
        }
    };
	
    private void updateStatus() {
    	wheel_0_position = ((WheelView)findViewById(R.id.comcalc_0)).getCurrentItem();   	
    	wheel_1_position = ((WheelView)findViewById(R.id.comcalc_1)).getCurrentItem();
    	wheel_2_position = ((WheelView)findViewById(R.id.comcalc_2)).getCurrentItem();
    	wheel_3_position = ((WheelView)findViewById(R.id.comcalc_3)).getCurrentItem();
    	wheel_4_position = ((WheelView)findViewById(R.id.comcalc_4)).getCurrentItem();
    	int housePrice = wheel_0_position * 1000000 + 
    	                 wheel_1_position * 100000 + 
    	                 wheel_2_position * 10000 + 
    	                 wheel_3_position * 1000 + 
    	                 wheel_4_position * 100;     

    	double flatFee;
    	double comBase;
    	double upTo;
    	double comThereAfter;
    	
    	try {
      	 	flatFee = Double.parseDouble(prefs.getString("FlatFee", "500"));
      	} catch (NumberFormatException e) {
      		flatFee = 500;	
      	};
    	try {
    		comBase = Double.parseDouble(prefs.getString("CommissionBase", "4")) / 100;
      	} catch (NumberFormatException e) {
      		comBase = 0.04;	
      	};
    	try {
    		upTo = Double.parseDouble(prefs.getString("UpToPrice", "200000"));
      	} catch (NumberFormatException e) {
      		upTo = 200000;	
      	};
    	try {
    		comThereAfter = Double.parseDouble(prefs.getString("CommissionThereAfter", "2")) / 100;
      	} catch (NumberFormatException e) {
      		comThereAfter = 0.02;	
      	};
    	
//    	Log.i(TAG, "Flat Fee = " + Double.toString(flatFee));
//    	Log.i(TAG, "Commission Base = " + Double.toString(comBase));   	
//    	Log.i(TAG, "Up to Price = " + Double.toString(upTo));
//    	Log.i(TAG, "Commission There After = " + Double.toString(comThereAfter));
    	
    	double commission;
    	if (housePrice > upTo) {
    		commission = flatFee + (comBase * upTo) + (housePrice - upTo) * comThereAfter;
    	} else {
    		commission = flatFee + (housePrice * comBase);	
    	}
    	
    	/*add gst if preferred*/
    	try {
    		GSTpercent = Double.parseDouble(prefs.getString("GST", "15")) / 100;
      	} catch (NumberFormatException e) {
      		GSTpercent = 0.15;	
      	};
    	if(GSTpreferred)
    		{
    		commission = commission + commission * GSTpercent;
    		tvHeader.setText("Commission incl. GST at "+prefs.getString("GST", "15")+"%");
    		}
    	else
    		{
    		tvHeader.setText("Commission");
    		}
    	
    	tvCommission.setText(java.text.NumberFormat.getCurrencyInstance().format(commission));    	
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

	
	private void saveWheelPostion() {
		Log.i(TAG, "saveWheelPosition:" + wheelPosition());
		updateStatus();
		SharedPreferences prefs = getSharedPreferences("wheel_position", MODE_PRIVATE);
		Editor e = prefs.edit();
		e.putInt("wheel_0_position", wheel_0_position);
		e.putInt("wheel_1_position", wheel_1_position);
		e.putInt("wheel_2_position", wheel_2_position);
		e.putInt("wheel_3_position", wheel_3_position);
		e.putInt("wheel_4_position", wheel_4_position);
		e.commit();
	}
	
	private void loadWheelPosition() {
		SharedPreferences prefs = getSharedPreferences("wheel_position", MODE_PRIVATE);
		int w0 = prefs.getInt("wheel_0_position", 0);
		int w1 = prefs.getInt("wheel_1_position", 0);
		int w2 = prefs.getInt("wheel_2_position", 0);
		int w3 = prefs.getInt("wheel_3_position", 0);
		int w4 = prefs.getInt("wheel_4_position", 0);
		
		((WheelView)findViewById(R.id.comcalc_0)).setCurrentItem(w0);
		((WheelView)findViewById(R.id.comcalc_1)).setCurrentItem(w1);
		((WheelView)findViewById(R.id.comcalc_2)).setCurrentItem(w2);
		((WheelView)findViewById(R.id.comcalc_3)).setCurrentItem(w3);
		((WheelView)findViewById(R.id.comcalc_4)).setCurrentItem(w4);

		updateStatus();
		Log.i(TAG, "LoadWheelPosition: " + wheelPosition());				
	} 
	
	private String wheelPosition() {
		return Integer.toString(wheel_0_position) 
		     + Integer.toString(wheel_1_position) 
		     + Integer.toString(wheel_2_position) 
		     + Integer.toString(wheel_3_position) 
		     + Integer.toString(wheel_4_position);
	}
	 
	private void showAbout() {
		Toast.makeText(getApplicationContext(), "Commission Calculator 0.1 by Computers for People", Toast.LENGTH_LONG).show();
	}
	
	public int getscrOrientation() {
		Display defaultDisplay = getWindowManager().getDefaultDisplay();

		int orientation;

		// Sometimes you may get undefined orientation Value is 0
		// simple logic solves the problem compare the screen
		// X,Y Coordinates and determine the Orientation in such cases

		if (defaultDisplay.getWidth() == defaultDisplay.getHeight()) {
			orientation = Configuration.ORIENTATION_SQUARE;
		} else { // if width is less than height than it is portrait
			if (defaultDisplay.getWidth() < defaultDisplay.getHeight()) {
				orientation = Configuration.ORIENTATION_PORTRAIT;
			} else { // if it is not any of the above it will definitely
						// be landscape 
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}
		
		return orientation; // return value 1 is portrait and 2 is Landscape
							// Mode
	}
}
