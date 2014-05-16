package nz.co.salespartner.Activities;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.Authenicator;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class Prefs extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	private static final String defValue = "";
	private Context context;
	private Activity activity;
	private EditTextPreference GST;
	private Preference CommissionThereAfter;
	private Preference UpToPrice;
	private Preference CommissionBase;
	private Preference FlatFee;
	private Preference Password;
	private Preference UserName;
	private Preference serverPort;
	private Preference serverName;
	private SharedPreferences prefs;
	private Preference MaxPhotos;
	private ListPreference mediaToDownload;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		setContentView(R.layout.view_prefs);
		context = this;
		activity = this;
		StyleFactory stylefactory = new StyleFactory(this);
		stylefactory.initHeader(this);
		defineButtons();
		prefs =  PreferenceManager.getDefaultSharedPreferences(this);
		// init preferences
		serverName = (Preference)findPreference("ServerName");
		serverPort = (Preference)findPreference("ServerPort");
		UserName = (Preference)findPreference("UserName");
		Password = (Preference)findPreference("Password");
		MaxPhotos = (Preference)findPreference("MaxPhotos");
		
		FlatFee = (Preference)findPreference("FlatFee");
		CommissionBase= (Preference)findPreference("CommissionBase");
		UpToPrice= (Preference)findPreference("UpToPrice");
		CommissionThereAfter= (Preference)findPreference("CommissionThereAfter");
		GST= (EditTextPreference)findPreference("GST");
		mediaToDownload = (ListPreference)findPreference("MediaToDownload");
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		refresh();

		// Set up a listener whenever a key changes            
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Refresh all the summaries
	 */
	private void refresh() {
		// server details
		serverName.setSummary(prefs.getString("ServerName", defValue));		
		serverPort.setSummary(prefs.getString("ServerPort", defValue));		
		UserName.setSummary(prefs.getString("UserName", defValue));		
		Password.setSummary(starOut(prefs.getString("Password", defValue)));		
		MaxPhotos.setSummary(prefs.getString("MaxPhotos", defValue));		
		// commision
		FlatFee.setSummary("$"+prefs.getString("FlatFee", defValue));
		CommissionBase.setSummary(prefs.getString("CommissionBase", defValue)+"%");
		UpToPrice.setSummary("$"+prefs.getString("UpToPrice", defValue)+"  -  the price up to which the base commision applies");
		CommissionThereAfter.setSummary(prefs.getString("CommissionThereAfter", defValue)+"%  -  the commision rate beyond the up to price");
		GST.setSummary(GST.getText()+"%");
		
		mediaToDownload.setSummary(mediaToDownload.getEntry());
			//all the other above preferences could be simplified to something like this (probably)
	}

	/**
	 * mask the password 
	 */
	private CharSequence starOut(String string) {
		String starred = "";
		for(int i = 0 ; i < string.length(); i ++)
			starred+="*";
		return starred;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		refresh();
		
	}
	
	private void defineButtons() {
		/*define preference buttons*/
		Preference clearAll = (Preference)findPreference("clearAllData");
		clearAll.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				showClearDataDialog();
				return true;
			}

		});
		
		Preference syncServer = (Preference)findPreference("syncServer");
		syncServer.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Prefs.this, ListingSynchro.class);
				startActivity(intent);
				return true;
			}

		});
		
		Preference clearDB = (Preference)findPreference("clearDB");
		clearDB.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				showClearDBDialog();
				return true;
			}

		});
		
		Preference clearFiles = (Preference)findPreference("clearFiles");
		clearFiles.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				showDeleteFilesDialog();
				return true;
			}

			
		});
		Preference forceAuthentication = (Preference)findPreference("forceAuthentication");
		forceAuthentication.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				
				Toast.makeText(context, "Authenticating...", Toast.LENGTH_SHORT).show();
				Authenicator auth = new Authenicator(context, activity);
				auth.forceAuthentication();
				return true;
			}
			
		});
		Preference DeviceID = (Preference)findPreference("DeviceID");
		DeviceID.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences prefs = activity.getSharedPreferences("authenicationsettings",Context.MODE_PRIVATE);
				
				Toast.makeText(context, "DeviceID = "+prefs.getString("uniqueID", "unset"), Toast.LENGTH_SHORT).show();

				return true;
			}
			
		});
		Preference getStatus = (Preference)findPreference("getStatus");
		getStatus.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences prefs = activity.getSharedPreferences("authenicationsettings",Context.MODE_PRIVATE);
				Authenicator au = new Authenicator(context, activity);
				
				String msg = au.getRegisteredStatus()+"\nGrace Period = "+prefs.getBoolean("gracePeriodOn", true);
				Date LastDate = new Date(prefs.getLong("lastAuthentication", 0));
				SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String last = ""+df.format(LastDate);
				msg+= "\nLast authentication on "+last;
				Long lockout = au.getCurrentTime() - prefs.getLong("lastAuthentication", 0);
				lockout = au.GRACE_PERIOD_MILLIS - lockout;
				Long lockoutdays = lockout / 1000 / 60 / 60 / 24; //convert millis to days 
				msg+="\nWill lock in "+lockoutdays+" days if unable to reach server";
				Toast.makeText(context, msg,Toast.LENGTH_LONG).show();
				
				return true;
			}
			
		});
		
	}

	/**show delete everything dialog*/
	private void showClearDataDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to delete all local data?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		        	   deleteEverything(); // delete DB then all files
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	

	/**show delete all files dialog*/
	private void showDeleteFilesDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to delete all files including images and documents?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				deleteAllFiles(); //delete all files
			}
		})
		
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/**show clear database dialog*/
	private void showClearDBDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to clear the listings database?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				clearDatabase(); //clear the database
			}  
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void deleteEverything() {
		clearDatabase();
		deleteAllFiles();
	}

	private void clearDatabase() {
		new clearDatabase_Loader().execute();
	}
	
	/** shows progress dialog while deleting the database */
	public class clearDatabase_Loader extends AsyncTask<Void, Void, Void> {
		ProgressDialog progress;
		public clearDatabase_Loader() {
			progress = new ProgressDialog(context);
			progress.setMessage("Deleting the listings database...");
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			context.deleteDatabase(SalesPartnerDbAdapter.DATABASE_NAME);
			return null;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

	}

	/** delete PSPDocs and pics1 folders */
	private void deleteAllFiles() {
		new deleteAllFiles_Loader().execute(); //show progress dialog
	}
	
	/** download all files in background */
	public class deleteAllFiles_Loader extends AsyncTask<Void, Void, Void> {
		ProgressDialog progress;
		public deleteAllFiles_Loader() {
			progress = new ProgressDialog(context);
			progress.setMessage("Deleting all local files...");
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			File pics_folder = new File(PhotoManager.LOCATION);
				deleteDirectory(pics_folder);
			File docs_folder = new File(Environment.getExternalStorageDirectory() + "/PSPDocs/");
				deleteDirectory(docs_folder);
			return null;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

	}

	/**recursive support method for deleting non-empty directories*/
	public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
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

}
