package nz.co.salespartner.Activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;

import nz.co.salespartner.R;
import nz.co.salespartner.Activities.AllDocs.ListingFileList;
import nz.co.salespartner.Helpers.ProxyFactory;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Objects.Document;
import nz.co.salespartner.Objects.ListingRecord;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.embarcadero.javaandroid.DBXException;
import com.embarcadero.javaandroid.DSProxy.TServerMethods1;
import com.embarcadero.javaandroid.TJSONArray;
import com.embarcadero.javaandroid.TJSONObject;
import com.embarcadero.javaandroid.TStream;

public class AllDocs extends Activity{
	private Context context;
	private StyleFactory stylefactory;
	private ArrayList<ListingFileList> listingsfiles;
	private ArrayList<ListingFileList> listingsfiles_notesonly;
	private ArrayList<Document> notesArray;
	private ArrayList<ListingRecord> listings;
	public SalesPartnerDbAdapter mDbHelper;
	private String serverName;
	private String serverPort;
	private String userName;
	private String password;
	private TJSONArray fitems;
	private boolean pdfsOnly;
	private ListView listings_docs_listview;
	private ListView office_docs_listview;
	private ListView notes_listview;
	private TServerMethods1 proxy;
	private boolean firstTime = true;
	private File globaldirectory;
	private boolean serverReachable;
	private boolean serverStatusEstablished;
	private TabHost mTabHost;
	private ArrayList<ListingFileList> listingsfiles_docsonly;
	private boolean expandNotes;
	private String fakeListingRef;
	private boolean fakeListingsEnabled = false;
	private ArrayList<ListingFileList> listingsfiles_officeonly;
	private TextView fakeEnabledMsg;
	private Cursor listingsCursor;
	private boolean sendToVendor;
	private boolean sendToSelf;
	private String selfEmail;
	private String profile;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.all_documents);
		context = this;
		serverStatusEstablished = false;

		fakeEnabledMsg = (TextView)findViewById(R.id.fakenotenabledMsg);

		globaldirectory = new File(Environment.getExternalStorageDirectory() + "/PSPDocs/");
		listings_docs_listview = (ListView)findViewById(R.id.ad_listview);
		notes_listview = (ListView)findViewById(R.id.notes_listview);
		office_docs_listview = (ListView)findViewById(R.id.office_docs_listview);
		listingsfiles = new ArrayList<ListingFileList>();
		listingsfiles_notesonly = new ArrayList<ListingFileList>();
		listingsfiles_docsonly = new ArrayList<ListingFileList>();
		listingsfiles_officeonly = new ArrayList<ListingFileList>();
		notesArray = new ArrayList<Document>();
		mDbHelper = new SalesPartnerDbAdapter(context);

		initButtons();
		stylefactory = new StyleFactory(this);
		stylefactory.initHeader(this);
		initTabs();
		firstTime = true;
		Log.i("","onCreate()"); 
	} 
 


	/**TAB STUFF*/

	private void initTabs() {

		/*set up tabs*/
		mTabHost = (TabHost) findViewById(R.id.tabhost2);
		mTabHost.setup();
		setupTab(new TextView(this), "Office", R.id.office_docs_view);
		setupTab(new TextView(this), "Listings", R.id.listing_docs_view);
		setupTab(new TextView(this), "Notes", R.id.notes_docs_view);
	}

	private void changeTab(int index, String newText){
		TabWidget vTabs = mTabHost.getTabWidget();
		LinearLayout rLayout = (LinearLayout) vTabs.getChildAt(index);
		TextView tv = ((TextView) rLayout.findViewById(R.id.tabsText));
		tv.setText(newText);
	}

	private void setupTab(final View view, final String tag,int view_id) {
		View tabview = createTabView(mTabHost.getContext(), tag);
		TabSpec tab = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
			public View createTabContent(String tag) {return view;}
		});
		tab.setContent(view_id);
		mTabHost.addTab(tab);
	}

	private View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		stylefactory.applyBold(tv);
		return view;
	}

	//---------------------//



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

	private void initButtons() {


		/* refresh button */
		LinearLayout fetch1 = (LinearLayout)findViewById(R.id.ad_fetch_button);
		LinearLayout fetch2 = (LinearLayout)findViewById(R.id.od_fetch_button);
		LinearLayout fetch3 = (LinearLayout)findViewById(R.id.nd_fetch_button);
		LinearLayout[] fetchbuttons = {fetch1, fetch2, fetch3};
		for(LinearLayout fetch : fetchbuttons){
			fetch.setClickable(true);
			fetch.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					sync();
				}
			});
		}

		/* delete all files button */
		LinearLayout delete_all = (LinearLayout)findViewById(R.id.ad_delete_all_button);
		delete_all.setClickable(true);
		delete_all.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDeleteAllAlert(listingsfiles_docsonly);
			}
		});

		/* delete all files button */
		delete_all = (LinearLayout)findViewById(R.id.nd_delete_all_button);
		delete_all.setClickable(true);
		delete_all.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDeleteAllAlert(listingsfiles_notesonly);
			}
		});

		/* delete all files button */
		delete_all = (LinearLayout)findViewById(R.id.od_delete_all_button);
		delete_all.setClickable(true);
		delete_all.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDeleteAllAlert(listingsfiles_officeonly);
			}
		});
	}

	protected void showDeleteAllAlert(final ArrayList<ListingFileList> passedlist) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete all files from device?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if(passedlist == listingsfiles_notesonly)
					deleteAllNotes(passedlist);
				else
					deleteAllFiles(passedlist);
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadPreferences();
		Log.i("","onResume()");
//		startManagingCursor(listingsCursor);
		new OfflineList().execute();
	}

	/** load in background */
	public class onResumeProgress extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;
		public onResumeProgress() {
			progress = new ProgressDialog(context);
			progress.setMessage("Contacting server...");
		}  
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {

			progress.dismiss();
			//	
			//			if(!serverReachable)
			//			{
			//				showNoServerMsg();
			//				new LoadLocalFiles().execute();
			//			}
			//			else
			//				new SyncDialog().execute();
			refresh();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			progress.dismiss();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}
	}

	private void loadPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		serverName = prefs.getString("ServerName", "192.168.1.91");
		serverPort = prefs.getString("ServerPort", "8080");
		userName = prefs.getString("UserName", "");
		password = prefs.getString("Password", "");
		profile = prefs.getString("Profile", ""); 
		pdfsOnly = prefs.getBoolean("showPDFsOnly", true);	
		expandNotes = prefs.getBoolean("expandNotes", true);	
		fakeListingsEnabled = prefs.getBoolean("fakeListingEnabled", false);	
		fakeListingRef = prefs.getString("fakelistingRef", "1SALES");
		sendToVendor = prefs.getBoolean("sendToVendor", true);
		sendToSelf = prefs.getBoolean("sendToSelf", true);
		selfEmail = prefs.getString("selfEmail", "");
		
		/*show or hide fake listings msg*/
		if(fakeListingsEnabled)
			fakeEnabledMsg.setVisibility(View.GONE);
		else
			fakeEnabledMsg.setVisibility(View.VISIBLE);
	}	


	public void testForServerReachable() {

		try {
			proxy.AuthenticationRequired();
			serverReachable = true;
		} catch (DBXException e) {
			Log.i("ListingDocuments","Server could not be reached");
			e.printStackTrace();
			serverReachable = false;
		}		
	}
	
	public boolean ServerIsReachable() {

		try {
			proxy.AuthenticationRequired();
			return  true;
		} catch (DBXException e) {
			Log.i("ListingDocuments","Server could not be reached");
			e.printStackTrace();
			return  false;
		}		
	}

	


	/**check to signal no local */

	public void defineServerConnection() {
		ProxyFactory pf = new ProxyFactory(serverName, serverPort, password, userName, profile);
		proxy = pf.getProxy();
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
		Toast.makeText(context, "Connection to server required. Please try again.",Toast.LENGTH_SHORT).show();
	}

	/** download all files in background */
	public class DownloadAllFilesFromList extends AsyncTask<Void, Void, Void> {
		private File f;
		private Boolean processed;
		private ProgressDialog progress;
		private boolean running;
		private ArrayList<ListingFileList> passedFileList;
		public DownloadAllFilesFromList(ArrayList<ListingFileList> _passedFileList, ProgressDialog p) {
			progress = p;
			passedFileList = _passedFileList;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			if(processed)
			{
				retrieveFileLists();

			} 
			else
				showNoConnectionDialog();

			refresh();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			running = false;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if(isOnline() && serverStatusEstablished && serverReachable)
			{
				running = true;
				while(running)
					for(ListingFileList list : passedFileList)
					{
						ArrayList<Document> DocumentArray = list.documents;
						for(Document d : DocumentArray)
						{
							TStream filestream = null;
							try {
								filestream = proxy.GetMediaFile(list.ref, d.name);
								f = downloadFile(d.name, filestream, list.dir);
								processed = true;

							} catch (DBXException e) {
								e.printStackTrace();
							}
							try {
								filestream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							Log.i("","inside doInBackground");

						}
						running = false;
					}

			}
			else
				processed = false;

			return null;
		}
	}

	/** download all files in background */
	public class LoadList extends AsyncTask<Void, Void, Void> {
		private File f;
		private Boolean processed;
		private ProgressDialog progress;
		public LoadList() {
			progress = new ProgressDialog(context);
			progress.setMessage("Loading file lists from server...");
		}
		@Override
		protected Void doInBackground(Void... arg0) {

			mDbHelper.open();
			if(listings == null)
				createListingArray();
			if(ServerIsReachable())
				retrieveFileLists();
			else
				serverReachable = false;

			mDbHelper.close();
			return null;
		}

		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			if(serverReachable == false)
				Toast.makeText(context, "Server connection failed", Toast.LENGTH_SHORT).show();
			else
				setAdapter();

		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			new OfflineList().execute();
		}

	}

	/** download all files in background */
	public class OfflineList extends AsyncTask<Void, Void, Void> {
		private File f;
		private Boolean processed;
		private ProgressDialog progress;

		public OfflineList() {
			progress = new ProgressDialog(context);
			progress.setMessage("Searching local directories...");
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			mDbHelper.open();
			if(firstTime)
			{
				Log.i("FirstTime","FirstTime");
				if(listings == null)
					createListingArray();

				firstTime = false;
			}
			offlineSearch();

			mDbHelper.close();
			return null;
		}

		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			setAdapter();

		}

	}

	private boolean documentIsNew(Document document, Directory directory) {
		File[] localFiles = directory.dir.listFiles();
		for(File f : localFiles)
		{
			if(document.name.equalsIgnoreCase(f.getName()))
				return false;
		}		

		return true;
	}



	/** search sd/pspdocs/ref for local files */
	private ListingFileList searchDirectory(Directory dir, ListingFileList current_list) {
		File[] localFiles = dir.dir.listFiles();

		for(File f : localFiles)
		{
			Document current = new Document();
			current.name = f.getName();
			current.size = (int)(f.length()); //convert bytes to kb
			current.ext = parseForExt(f.getName());
			current.isLocal = true;
			current.lastMod = new Date(f.lastModified());

			addDocument(current_list, current);
		}
		return current_list;
	}

	private String parseForExt(String name){
		String[] substrings = name.split("\\.");
		int last = substrings.length - 1;
		String ext = substrings[last];
		return ext;

	}

	private String getFileType(Document document) {
		if(document.ext.equalsIgnoreCase("jpg"))
			return "image/*";
		else if(document.ext.equalsIgnoreCase("png"))
			return "image/*";
		else if(document.ext.equalsIgnoreCase("jpeg"))
			return "image/*";
		else if(document.ext.equalsIgnoreCase("pdf"))
			return "application/pdf";
		else if(document.ext.equalsIgnoreCase("txt"))
			return "text/plain";

		return null;
	}

	private int getExtIcon(Document document) {
		if(document.ext.equalsIgnoreCase("jpg"))
			return R.drawable.ic_jpg;
		else if(document.ext.equalsIgnoreCase("png"))
			return R.drawable.ic_pdf;
		else if(document.ext.equalsIgnoreCase("jpeg"))
			return R.drawable.ic_jpg;
		else if(document.ext.equalsIgnoreCase("pdf"))
			return R.drawable.ic_pdf;
		else if(document.ext.equalsIgnoreCase("txt"))
			return R.drawable.stickynote;

		return 0;
	}

	private void openFile(Document document, ListingFileList current_file_list ){
		openFileViewer(getLocalFile(document.name, current_file_list.dir), document, current_file_list.dir);
	}

	private File getLocalFile(String fileName, Directory dir){
		File file = new File(dir.absolutePath, fileName);
		Log.i("","got local file at ="+dir.absolutePath+" / "+fileName);
		return file;
	}

	private Directory initDirectory(String ref) {
		Directory directory = new Directory();
		directory.dir = new File (Environment.getExternalStorageDirectory() + "/PSPDocs/"+ref+"/"); //defines directory location
		directory.absolutePath = Environment.getExternalStorageDirectory() + "/PSPDocs/"+ref;
		if(directory.dir.exists() == false) //make directory if doesn't exist
			directory.dir.mkdirs();

		return directory;
	}

	private class Directory{
		public String absolutePath;
		public File dir;

	}

	private void openFileViewer(File file, Document document, Directory dir) {
		Intent intent = new Intent();  
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String fileType = getFileType(document);
		if(fileType != null)
		{
			if(fileType == "image/*")
			{
				Intent fullphoto = new Intent(this,FullScreenPhoto.class);
				fullphoto.putExtra("imagename", document.name);
				fullphoto.putExtra("absolutepath", dir.absolutePath);
				startActivity(fullphoto);
			}
			else
			{
				intent.setDataAndType(Uri.fromFile(file), fileType);  
				startActivity(intent);
			}

		} 
		else{
			//TODO unknown file type dialog
		}

	}

	/** load in background */
	public class DownloadFile extends AsyncTask<Void, Void, Void> {
		private Document document;
		private File f;
		private ProgressDialog progress;
		private ListingFileList parent;
		public DownloadFile(Document d, ListingFileList _parent) {
			progress = new ProgressDialog(context);
			progress.setMessage("Downloading file...");
			document = d;
			parent= _parent;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			try {
				//				openFileViewer(f, document);
			} catch (Exception e) {
				Log.i("","failed button click");
				e.printStackTrace();
			}
			progress.dismiss();
			refresh();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			TStream filestream;
			try {
				filestream = proxy.GetMediaFile(parent.ref, document.name);
				f = downloadFile(document.name, filestream, parent.dir);
				document.isLocal = true;
			} catch (DBXException e) {
				Log.i("","failed button click");
				e.printStackTrace();
			}
			return null;
		}
	}

	private File downloadFile(String fileName, InputStream is, Directory dir){

		String directory = dir.absolutePath;
		File dirF = new File(directory);
		if(dirF.exists() == false) //make directory if doesn't exist
			dirF.mkdirs();

		File file = new File(directory, fileName);  //make file object at dir and with fileNaame
		if(file.exists() == false)
		{
			try{
				/*
						InputStream is = ucon.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
		FileOutputStream fos = new FileOutputStream(file);
		byte[] baf = new byte[BUFFER_SIZE];
		int actual = 0;
		while (actual != -1) {
		    fos.write(baf, 0, actual)
		    actual = bis.read(baf, 0, BUFFER_SIZE);


				 */
				file.createNewFile();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(bis.available());  //TODO change capacity?
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray()); //TODO fix errors here
				fos.close();
				Log.i("","file downloaded");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Listing Doc", "Error: " + e);
			}
		}
		return file;
	}

	/** array adapter for displaying all listing documents in ad_listview*/
	private class AllDocumentArrayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public AllDocumentArrayAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return listingsfiles_docsonly.size();
		}

		public long getItemId(int position) {
			return position;
		}  

		public View getView(int position, View wrapper, ViewGroup parent) {

			final ListingFileList current_file_list = listingsfiles_docsonly.get(position);
			ArrayList<Document> documents = current_file_list.documents;

		
				wrapper = mInflater.inflate(R.layout.document_list_wrapper, null);
				/*set banner onClick*/
				LinearLayout banner = (LinearLayout)wrapper.findViewById(R.id.dlw_banner);				
				banner.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Intent mIntent = new Intent(context, ListingTabHost.class);
						mIntent.putExtra("id", current_file_list.id);
						startActivity(mIntent);
					}
				});
				/*set header title*/
				TextView title = (TextView)wrapper.findViewById(R.id.dlw_title);
				title.setText(current_file_list.ref);
				TextView subtitle = (TextView)wrapper.findViewById(R.id.dlw_subtitle);
				subtitle.setText(current_file_list.address);
				int convCount = 0;
				if(documents.size()>0)  
				{
					for(final Document document : documents)
					{
						View convertView = mInflater.inflate(R.layout.document_row, null);

						/*set text fields*/
						TextView name = (TextView)convertView.findViewById(R.id.docrow_name);
						name.setText(document.name);
						TextView size = (TextView)convertView.findViewById(R.id.docrow_size);
						int file_size  = document.size;
						size.setText(file_size/1000+"kb");
						TextView[] tvs = {size,name};
						stylefactory.applyFont(tvs);

						/*set image icon*/
						ImageView thumb = (ImageView)convertView.findViewById(R.id.docrow_thumb);
						int icon = getExtIcon(document);
						if(icon != 0)
							thumb.setImageResource(icon);

						/*set local light*/
						ImageView local_icon = (ImageView)convertView.findViewById(R.id.docrow_local_icon);

						if(document.isLocal)
							local_icon.setImageResource(R.drawable.local_on);
						else
							local_icon.setImageResource(R.drawable.local_off);

						//add click listener to row
						convertView.setClickable(true);
						convertView.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if(document.isLocal)
									openFile(document, current_file_list);
								else
									new DownloadFile(document, current_file_list).execute();
							}
						});  

						//show sharing dialog on longClick
						convertView.setOnLongClickListener(new OnLongClickListener() {
							public boolean onLongClick(View v) {
								showFileDialog(document, current_file_list);
								return true;
							}
						});  


						if(convCount % 2 == 0) //alternating colors
							convertView.setBackgroundResource(R.drawable.row_states_dark);
						else
							convertView.setBackgroundResource(R.drawable.row_states_light);
						((ViewGroup) wrapper).addView(convertView);

//						View divider = mInflater.inflate(R.layout.divider, null);
//						((ViewGroup) wrapper).addView(divider, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 2));

						convCount++;
					} 


				


			}

			return wrapper;
		}

		public Object getItem(int position) {
			return null;
		}
	}

	/** array adapter for displaying all listing documents in ad_listview*/
	private class OfficeDocumentArrayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public OfficeDocumentArrayAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return listingsfiles_officeonly.size();
		}

		public long getItemId(int position) {
			return position;
		}  

		public View getView(int position, View wrapper, ViewGroup parent) {

			final ListingFileList current_file_list = listingsfiles_officeonly.get(position);
			ArrayList<Document> documents = current_file_list.documents;

			
				wrapper = mInflater.inflate(R.layout.document_list_wrapper, null);
				/*set banner onClick*/
				LinearLayout banner = (LinearLayout)wrapper.findViewById(R.id.dlw_banner);				
				banner.setVisibility(View.GONE); //MAKE BANNER INVISIBLE


				int convCount = 0;
				if(documents.size()>0)  
				{
					for(final Document document : documents)
					{
						View convertView = mInflater.inflate(R.layout.document_row, null);

						/*set text fields*/
						TextView name = (TextView)convertView.findViewById(R.id.docrow_name);
						name.setText(document.name);
						TextView size = (TextView)convertView.findViewById(R.id.docrow_size);
						int file_size  = document.size;
						size.setText(file_size/1000+"kb");
						TextView[] tvs = {size,name};
						stylefactory.applyFont(tvs);

						/*set image icon*/
						ImageView thumb = (ImageView)convertView.findViewById(R.id.docrow_thumb);
						int icon = getExtIcon(document);
						if(icon != 0)
							thumb.setImageResource(icon);

						/*set local light*/
						ImageView local_icon = (ImageView)convertView.findViewById(R.id.docrow_local_icon);

						if(document.isLocal)
							local_icon.setImageResource(R.drawable.local_on);
						else
							local_icon.setImageResource(R.drawable.local_off);

						//add click listener to row
						convertView.setClickable(true);
						convertView.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if(document.isLocal)
									openFile(document, current_file_list);
								else
									new DownloadFile(document, current_file_list).execute();
							}
						});  

						//show sharing dialog on longClick
						convertView.setOnLongClickListener(new OnLongClickListener() {
							public boolean onLongClick(View v) {
								showFileDialog(document, current_file_list);
								return true;
							}
						});  


						if(convCount % 2 == 0) //alternating colors
							convertView.setBackgroundResource(R.drawable.row_states_dark);
						else
							convertView.setBackgroundResource(R.drawable.row_states_light);
						((ViewGroup) wrapper).addView(convertView);

//						View divider = mInflater.inflate(R.layout.divider, null);
//						((ViewGroup) wrapper).addView(divider, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 2));

						convCount++;
					} 


				


			}

			return wrapper;
		}

		public Object getItem(int position) {
			return null;
		}
	}

	/** array adapter for notes listview*/
	private class NotesArrayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Document document;
		private ListingFileList current_file_list;

		public NotesArrayAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return notesArray.size();
		}

		public long getItemId(int position) {
			return position;
		}  

		public View getView(int position, View wrapper, ViewGroup parent) {
		
			document = notesArray.get(position);
			Log.i("","position = "+position);
			Log.i("","document name = "+document.name);
			
				wrapper = mInflater.inflate(R.layout.document_list_wrapper, null);
				current_file_list = document.parentFileList;
				final Document thisDoc = document;
				final ListingFileList thisList = current_file_list;
				/*set banner onClick*/
				LinearLayout banner = (LinearLayout)wrapper.findViewById(R.id.dlw_banner);				
				banner.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Intent mIntent = new Intent(context, ListingTabHost.class);
						mIntent.putExtra("id", thisList.id);
						startActivity(mIntent);
					}
				});


				/*set header title*/
				TextView title = (TextView)wrapper.findViewById(R.id.dlw_title);
				title.setText(document.listing_id);
				TextView subtitle = (TextView)wrapper.findViewById(R.id.dlw_subtitle);
				subtitle.setText(current_file_list.address);
				int convCount = 0;
				if(notesArray.size()>0)
				{  

					View convertView = mInflater.inflate(R.layout.document_row, null);

					/*set text fields*/
					TextView name = (TextView)convertView.findViewById(R.id.docrow_name);
					name.setText(document.name);
					TextView note_content = (TextView)convertView.findViewById(R.id.docrow_size);
					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - hh:mm");
					if(document.lastMod!=null)
					{
						String lastMod = formatter.format(document.lastMod);
						note_content.setText(lastMod+"\n"+document.content); 
					}
					if(!expandNotes) //hide note content if preferred
						note_content.setVisibility(View.GONE);
					TextView[] tvs = {note_content,name};
					stylefactory.applyFont(tvs);
  
					/*set image icon*/
					ImageView thumb = (ImageView)convertView.findViewById(R.id.docrow_thumb);
					int icon = getExtIcon(document);
					if(icon != 0)
						thumb.setImageResource(icon);

					/*set local light to GONE*/
					ImageView local_icon = (ImageView)convertView.findViewById(R.id.docrow_local_icon);
					local_icon.setVisibility(View.GONE);

					//add click listener to row
					convertView.setClickable(true);
					
					convertView.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							if(document.isLocal)
							{
								openFile(thisDoc, thisList);
							}
							else
								new DownloadFile(document, current_file_list).execute();
						}
					});  

					//show sharing dialog on longClick
					convertView.setOnLongClickListener(new OnLongClickListener() {
						public boolean onLongClick(View v) {
							showFileDialog(thisDoc, thisList);
							return true;
						}
					}); 


					if(convCount % 2 == 0) //alternating colors
						convertView.setBackgroundResource(R.drawable.row_states_dark);
					else
						convertView.setBackgroundResource(R.drawable.row_states_light);
					((ViewGroup) wrapper).addView(convertView);

//					View divider = mInflater.inflate(R.layout.divider, null);
//					((ViewGroup) wrapper).addView(divider, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 2));

					convCount++;



				}


			

			return wrapper;
		}

		public Object getItem(int position) {
			return null;
		}
	}


	private void deleteFile(Document document, final ListingFileList listingfilelist){
		
		new DeleteFileProgress(document, listingfilelist).execute();
	}
	
	/** delete a single file in background */
	public class DeleteFileProgress extends AsyncTask<Void, Void, Void> {
		private Document document;
		private File f;
		private ProgressDialog progress;
		private ListingFileList parent;
		public  DeleteFileProgress(Document d, ListingFileList _parent) {
			progress = new ProgressDialog(context);
			progress.setMessage("Deleting file...");
			document = d;
			parent= _parent;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			
			progress.dismiss();
			sync();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			File target = getLocalFile(document.name, parent.dir);
			Log.i("AllDocs","File "+target.getName()+" at "+target.getAbsolutePath()+" deleted");
			target.delete();
			return null;
		}
	}

	private void emailFile(Document document, ListingFileList listingfilelist){
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("image/jpeg");
		String path = "file://"+listingfilelist.dir.absolutePath+"/"+document.name;
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));

		startActivity(emailIntent);
	} 


	private void showFileDialog(final Document document, final ListingFileList listingfilelist){
		final String items[] = {"Email","Open","Delete"};

		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("File");
		ab.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int choice) {
				if(choice == 0) {
					emailFile(document,listingfilelist);
				}
				else if(choice == 1) {
					openFile(document, listingfilelist);
				}
				else if(choice == 2) {
					deleteFile(document, listingfilelist);
				}
			}
		});
		ab.show();
	}


	/**class for holding a listings documents*/
	public class ListingFileList{
		protected int id;
		public String address;
		public Directory dir;
		public ArrayList<Document> documents;
		public ArrayList<Document> notes;
		public String ref;
		public int docCount;

		public ListingFileList(String _ref, String _address, int _id){
			documents = new ArrayList<Document>();
			notes = new ArrayList<Document>();
			ref = _ref;
			id = _id;
			address = _address;
			docCount = 0;
			dir = initDirectory(ref);
		}

		public void increaseCount() {
			docCount++;
		}

		public boolean hasFiles(){
			if(documents.size() > 0)
				return true;
			else
				return false;
		}

		public String toString(){
			return "\n-----" +"\nref = "+ref+
					"\ndocuments.size() = "+documents.size() +
					"\nnotes.size() = "+notes.size();
		}

		public int docCount() {
			return Math.max(0,documents.size());
		}

		public int notesCount() {
			return Math.max(0,notes.size());
		}

		public void Logi() {
			Log.i("",this.toString());
		}
	}


	public void createListingArray() {
		listings = new ArrayList<ListingRecord>();
		listings.clear();
		listingsCursor = mDbHelper.fetchAllListings(); 
		Log.i("","createListingsArray");
		

		int listingcount = listingsCursor.getCount();
		listingsCursor.moveToFirst();
		listingsCursor.moveToFirst();
		while (listingsCursor.isAfterLast() == false) {

			/* build current listing*/
			ListingRecord current = new ListingRecord();
			current.ref = listingsCursor.getString(listingsCursor.getColumnIndex("ref"));
			current.id = listingsCursor.getInt(listingsCursor.getColumnIndex("_id"));
			current.StNo = listingsCursor.getString(listingsCursor.getColumnIndex("StNo"));
			current.Street = listingsCursor.getString(listingsCursor.getColumnIndex("Street"));
			listings.add(current);

			listingsCursor.moveToNext();
		}

//		listingsCursor.close();

	}

	public void refresh(){
		
			new OfflineList().execute();


	}

	private void sync(){

		new OfflineList().execute();
//		new syncProgress().execute();

	}
	
	/** download all files in background */
	public class syncProgress extends AsyncTask<Void, Void, Void> {
		private File f;
		private Boolean processed;
		private ProgressDialog progress;

		public syncProgress() {
			progress = new ProgressDialog(context);
			progress.setMessage("Contacting server");
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			if(!serverStatusEstablished)
			{
				serverReachable = false;
				defineServerConnection();
				if(isOnline() && serverStatusEstablished == false)
				{
					testForServerReachable();
					serverStatusEstablished = true;
				}
							
			}
			return null;
		}

		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			if(firstTime)
			{
				if(!serverReachable)
					Toast.makeText(context, "Connection to server required", Toast.LENGTH_SHORT).show();
				firstTime = false;
			}
			refresh();

		}

	}

	/**set the adapters on all three listviews*/
	public void setAdapter() {
		listings_docs_listview.setAdapter(new AllDocumentArrayAdapter());
		listings_docs_listview.invalidate();
		notes_listview.setAdapter(new NotesArrayAdapter());
		notes_listview.invalidate();
		office_docs_listview.setAdapter(new OfficeDocumentArrayAdapter());
		office_docs_listview.invalidate();
		updateTabs();
	}


	/** method to convert TJSON object to a Document object */
	public Document TJSONtoDocument(TJSONObject tjson) {
		Document current = new Document();
		current.name = tjson.getString("Name");
		current.size = tjson.getInt("size");
		current.ext = tjson.getString("Extension");
		current.isLocal = false;
		return current;
	}

	private void deleteAllFiles(ArrayList<ListingFileList> targetlistingsfiles){
		for(ListingFileList list : targetlistingsfiles)
		{
			ArrayList<Document> DocumentArray = list.documents;
			for(Document d : DocumentArray)
			{
				File target = getLocalFile(d.name,list.dir);
				target.delete(); //delete file
				d.isLocal = false;
				File dir = new File(list.dir.absolutePath);
				dir.delete(); //delete directory
			}
		}
		refresh();  
	}
	private void deleteAllNotes(ArrayList<ListingFileList> targetlistingsfiles){
		for(ListingFileList list : targetlistingsfiles)
		{
			ArrayList<Document> DocumentArray = list.notes;
			for(Document d : DocumentArray)
			{
				File target = getLocalFile(d.name,list.dir);
				target.delete(); //delete file
				d.isLocal = false;
				File dir = new File(list.dir.absolutePath);
				dir.delete(); //delete directory
			}
		}
		refresh();  
	}

	public void offlineSearch(){
		listingsfiles.clear();
		for(ListingRecord listing : listings)
		{
			listing.documentcount = 0; //clear this listings doc count
			String ref = listing.ref;
			String address = listing.StNo+" "+listing.Street; //build listingfilelist address
			if(TextUtils.isEmpty(ref)==false)
			{
				ListingFileList currentlist = new ListingFileList(ref, address, listing.id);

				currentlist = searchDirectory(currentlist.dir, currentlist); //first find local docs

				if(currentlist.docCount()>0 || currentlist.notesCount()>0)
					if(doesNotExist(currentlist))
					{
						listingsfiles.add(currentlist);
						Log.i("","retrieve files just added = "+currentlist.toString());

					}
				listing.documentcount = currentlist.docCount(); //set listing records doc count
				// mDbHelper.updateDocumentCount(listing); //update the listing in the database
			}
		}

		cleanArrays();
	}



	private boolean doesNotExist(ListingFileList currentlist) {
		Boolean result = true;
		for(ListingFileList list : listingsfiles)
		{
			if(currentlist.ref.equalsIgnoreCase(list.ref))
				result = false;
		}
		Log.i("","currentlist ref "+currentlist.ref+" exists = "+result);
		return result;

	}



	public void retrieveFileLists() {
		listingsfiles.clear();

		for(ListingRecord listing : listings)
		{
			String ref = listing.ref;
			String address = listing.StNo+" "+listing.Street; //build listingfilelist address
			if(TextUtils.isEmpty(ref)==false && serverStatusEstablished)
			{
				ListingFileList currentlist = new ListingFileList(ref, address, listing.id);
				Boolean success;
				TJSONObject medialist = null;
				try{
					medialist = proxy.GetMediaList(ref);
					success = true;
				}
				catch (Exception e) {
					Log.i("", "No media list for "+ref);	
					success = false;
				}   

				currentlist = searchDirectory(currentlist.dir, currentlist); //first find local docs

				if(success && medialist!=null && serverReachable && ServerIsReachable())
				{ //then try online
					TJSONObject media_fields = medialist.getJSONObject("fields");
					fitems = media_fields.getJSONArray("FItems");
					for(int i = 0; i < fitems.size(); i ++)
					{
						try{
							TJSONObject fitem = fitems.getAsJsonObject(i);

							TJSONObject fitem_fields = fitem.getJSONObject("fields");
							Document current = TJSONtoDocument(fitem_fields);

							if(documentIsNew(current, currentlist.dir))
							{
								addDocument(currentlist,current);
							}

						}
						catch(Exception e){

						}
					}


				}
				if(currentlist.docCount() > 0 || currentlist.notesCount()>0)
					if(doesNotExist(currentlist))
					{
						listingsfiles.add(currentlist);
						Log.i("","retrieve files just added = "+currentlist.toString());
					}
			}
		}
		cleanArrays();


	}


	@SuppressWarnings("unchecked") /*NOT TYPE SAFE*/
	private void cleanArrays() {
		listingsfiles_notesonly.clear();
		listingsfiles_officeonly.clear();
		listingsfiles_docsonly.clear();
		notesArray.clear();

		/*add filelists to their appropriate arraylists*/
		for(ListingFileList filelist : listingsfiles)
		{
			/*if listing is the fake listing then add to that array*/
			if(fakeListingsEnabled && filelist.ref.equalsIgnoreCase(fakeListingRef))
			{
				listingsfiles_officeonly.add(filelist);
			}
			else
			{
				if(filelist.notes.size()>0) //if filelist has notes, make a copy into notes only array
					listingsfiles_notesonly.add(filelist);
				if(filelist.documents.size()>0)	
					listingsfiles_docsonly.add(filelist);
			}
		}

		/*add all the notes to the notesArray*/
		for(ListingFileList notes_filelist : listingsfiles_notesonly)
		{
			for(int i = 0 ; i < notes_filelist.notes.size() ; i ++)
			{
				Document current = notes_filelist.notes.get(i);
				current.parentFileList = notes_filelist;
				current.listing_id = notes_filelist.ref; //load doc with parents id (ref)
				notesArray.add(current);
				Log.i("", "added to notes array = "+current.name);
			}
		}
		
		
		
		
		Log.i("","notesArraysize = "+notesArray.size());
		/*sort the notes_only array by date*/
		Collections.sort(notesArray, new Comparator(){

			public int compare(Object o1, Object o2) {
				Document d1 = (Document) o1;
				Document d2 = (Document) o2;
				long date1 = d1.lastMod.getTime();
				long date2 = d2.lastMod.getTime();
				int result = 0;
				if(date1 == date2)
					result = 0;
				else if(date1 < date2)
					result = 1;
				else if(date1 > date2)
					result = -1;
				
				Log.i("","Compare to = "+result);
				return result;
			}

		});

	}

	/**update tabs to display number of documents for each category*/
	private void updateTabs() {
//		changeTab(0, "Office ("+listingsfiles_officeonly.size()+")"); dont change office
		changeTab(1, "Listings "+listingsfiles_docsonly.size());
		changeTab(2, "Notes "+notesArray.size());
	}



	/**add a document to either the document array or notes array of the ListingFileList*/
	private void addDocument(ListingFileList currentlist, Document document) {

		if(document.isNote())
		{
			document.content = readNote(document,currentlist);
			currentlist.notes.add(document);
		}
		else if(pdfsOnly) //if settings PDFsOnly is check only show .pdf files
		{	
			if(document.ext.equalsIgnoreCase("pdf"))
				currentlist.documents.add(document);
		}
		else
			currentlist.documents.add(document);

		currentlist.increaseCount();
		currentlist.Logi();
	}



	private String readNote(Document document, ListingFileList currentlist) {
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(getLocalFile(document.name, currentlist.dir)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			while (scanner.hasNextLine()){
				text.append(scanner.nextLine() + NL);
			}
		}
		finally{
			scanner.close();
		}
		return text.toString();
	}
}