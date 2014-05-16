package nz.co.salespartner.Activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.FileDownloader;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.Helpers.ProxyFactory;
import nz.co.salespartner.Helpers.SalesPartnerDbAdapter;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Helpers.pspLog;
import nz.co.salespartner.Objects.Document;
import nz.co.salespartner.Objects.ListingRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.embarcadero.javaandroid.DBXException;
import com.embarcadero.javaandroid.DSProxy.TServerMethods1;
import com.embarcadero.javaandroid.TJSONArray;
import com.embarcadero.javaandroid.TJSONObject;
import com.embarcadero.javaandroid.TStream;
 /**
  * Listing synchro uses a custom pspLog.* method to write pspLogs to a text file
  * @author CFP
  *
  */
public class ListingSynchro extends Activity {
	private Button SyncListingsButton;

	private TextView photoSummaryText;
	private ProgressBar photoProgress;
	private TextView listingSummaryText;
	private ProgressBar listingProgress;
	private Button backButton;
	private Button cancelButton;
	private static String TAG = "ListingSynchro";
	private int listingsDeleted;
	private int listingsSkipped;
	private int listingsUpdated;
	private int listingsAdded;
	private int photosDeleted;
	private int photosUpdated;
	private int photosAdded;
	private int photosSkipped;
	private int maximumPhotosToDownload;
	private String mediaToDownload;
	private List<FileToDownload> photosToDownload;
	private List<FileToDownload> documentsToDownload;
	private List<String> listingsToDownload;
	private static String serverName;
	private static String serverPort;
	private static Boolean FinishedDownloadingPhotos;
	private static Boolean FinishedDownloadingListings;
	private Boolean synchroCanceled;
	private ProxyFactory proxyfactory;
	private String userName;
	private String password;


	private ListingSynchro context;

	private Button homeButton;

	private String profile;

	private ArrayList<MediaListItem> MediaList;

	/** support class*/
	private class PhotoToDownload extends FileToDownload{
		public String ref;
		public int picNum;

		PhotoToDownload(String ref, int picNum) {
			this.ref = ref;
			this.picNum = picNum;
		}
	}
	/** support class*/
	private class DocumentToDownload extends FileToDownload{
		public String ref;
		private String file_name;

		DocumentToDownload(String ref, String file_name) {
			this.ref = ref;
			this.file_name = file_name;
		}
		
		public String toString(){
			return "ref = "+ref+" file_name = "+file_name;
		}
	}

	private class FileToDownload{

	}

	/**class for holding a listings documents*/
	private class ListingsFiles{
		public ArrayList<Document> documents;
		public String ref;

		public ListingsFiles(String _ref){
			documents = new ArrayList<Document>();
			ref = _ref;
		}

		public int docCount() {
			// TODO Auto-generated method stub
			return documents.size();
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



	private class DownloadListingsListTask extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			//wipe log file
			pspLog.init(context);
			listingSummaryText.setText("Connecting to server...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			pspLog.i(TAG, "ProcessListings");
			resetCounters();
			photosToDownload.clear();  //clear lists
			listingsToDownload.clear();
			Listings.mDbHelper.markAllForDeletion();
			if (synchroCanceled)
				return true;

			TServerMethods1 proxy = proxyfactory.getProxy();

			pspLog.i(TAG,"calling getListings(\"\")");
			//getListings() TJSON method
			TJSONArray root;
			TJSONArray JSONCurrentListings = null;
			try {
				root = proxy.GetListings("");
				JSONCurrentListings = root;
				pspLog.i(TAG, "GetListings JSON Result : "+root.toString());
			} catch (DBXException e1) {
				pspLog.i(TAG,"getListings() failed");
				pspLog.error(e1.getMessage());
			}

			
			//end of proxy calls
			proxyfactory.logOut(proxy);
							
			
			if (JSONCurrentListings != null) {
				TJSONObject JSONListing;
				try {
					for (int i = 0; i < JSONCurrentListings.size(); i++) {
						JSONListing = JSONCurrentListings.getJSONObject(i);
						ProcessListings_Loop(JSONListing);
						publishProgress(i, (int)JSONCurrentListings.size());
						if (synchroCanceled)
							break;

					}
				} catch (Exception e) {
					pspLog.error(e.getMessage());
				}

			} else
				return false;
			
			
			DownloadPicsAndDocs();
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				//something bad happened.
				whenFinishedDownloading();
				listingSummaryText.setText("Failed to connect to server");
				return;
			}
			if (!synchroCanceled) {
				DownloadListings();
				DownloadFiles();
			} else {
				whenFinishedDownloading();
			}

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			listingSummaryText.setText("Searching for changes...");
			listingProgress.setMax(values[1]);
			listingProgress.setProgress(values[0]);
		}

	}


	private static Document TJSONtoDocument(TJSONObject tjson) {
		Document current = new Document();
		current.name = tjson.getString("Name");
		current.size = tjson.getInt("size");
		current.ext = tjson.getString("Extension");
		current.isLocal = false;
		return current;
	}
	private static Document MediaListItemToDocument(MediaListItem mli) {
		Document current = new Document();
		current.name = mli.name;
		current.size = mli.size;
		current.ext = mli.ext;
		current.isLocal = false;
		current.ref = mli.ref;
		return current;
	}

	private class DownloadFilesTask extends AsyncTask<List<FileToDownload>, Void, Void> {
		@Override
		protected Void doInBackground(List<FileToDownload>... DownloadThese) {
			//start rest connection
			pspLog.e("","DownloadFilesTask");
			TServerMethods1 proxy = proxyfactory.getProxy();


			Display display = getWindowManager().getDefaultDisplay(); 
			int screenW = display.getWidth();
			int screenH = display.getHeight();
			//download photos
			for (FileToDownload pic : DownloadThese[0]) {
				if (!synchroCanceled) {
					PhotoToDownload p = (PhotoToDownload)pic;

					if (PhotoManager.exists(p.ref, p.picNum))
						photosUpdated += 1;
					else
						photosAdded += 1;

					getImage(p.ref, p.picNum, proxy, screenW, screenH);
					if(p.picNum == 1)
						createThumbnail(p.ref);
					publishProgress();
				}
			}
			//download files

			for (FileToDownload file : DownloadThese[1]) {
				if (!synchroCanceled) {
					DocumentToDownload doc = (DocumentToDownload)file;
					photosAdded += 1;

					TStream filestream = null;
					try { 
						pspLog.e(TAG,"getting media file for "+doc.ref);
						//make sure file is not an image
						if(isAPhoto(doc)==false)
						{
						filestream = proxy.GetMediaFile(doc.ref, doc.file_name);
						FileDownloader.downloadFile(doc.file_name, filestream, Environment.getExternalStorageDirectory()+"/PSPDocs/"+doc.ref);
						}
						else
							Log.i(TAG,"Skipping doc to download as it is an image");
					}
					catch (Exception e) {
						pspLog.error(e.getMessage());
					}


					publishProgress();
				}
			}
			proxyfactory.logOut(proxy);
			return null;
		}
		/**
		 * Check whether extension is an image file type
		 * @param doc
		 * @return
		 */
		public boolean isAPhoto(DocumentToDownload doc){
			String[] types = {"jpeg","jpg","png","tiff","gif"}; //image file types
			String[] split = doc.file_name.split("."); // split filename by .
			String ext = "";
			if(split.length>0)
			ext = split[split.length-1]; //get last string in array
			for(String type : types)
			{
				if(type.equalsIgnoreCase(ext))
					return true;
			}
			return false;
		}

		/** 
		 * creates a scaled thumbnail (160x120px) with the ref's first image
		 * and stores it at the filepath returned by PhotoManager.getThumbName()
		 **/
		private void createThumbnail(String ref) {
			String largepath = PhotoManager.getFileName(ref, 1);
			String thumbpath = PhotoManager.getThumbName(ref, 1);
			Bitmap large = BitmapFactory.decodeFile(largepath);
			if(large!=null){
				
				Bitmap thumb = Bitmap.createScaledBitmap(large, 160, 120, false);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				thumb.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
				File f = new File(thumbpath);
				try{
					f.createNewFile();
					FileOutputStream fo = new FileOutputStream(f);
					fo.write(bytes.toByteArray());
					fo.close();
				}
				catch (Exception e) {
					pspLog.error(e.getMessage());
				}
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			photoProgress.incrementProgressBy(1);
			photoSummary();
		}

		@Override
		protected void onPostExecute(Void result) {
			FinishedDownloadingPhotos = true;
			if (FinishedDownloadingListings)
				whenFinishedDownloading();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.listing_synchro);
		loadPreferences();
		context = this;
		MediaList = new ArrayList<ListingSynchro.MediaListItem>();
		StyleFactory styleFactory = new StyleFactory(this);
		styleFactory.initHeader(this);

		homeButton = (Button)findViewById(R.id.go_home_button);
		
		SyncListingsButton = (Button) findViewById(R.id.SynchroniseListingsButton);
		SyncListingsButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SyncListingsButton.setEnabled(false);
				backButton.setEnabled(false);
				homeButton.setEnabled(false);
				cancelButton.setEnabled(true);
				new DownloadListingsListTask().execute();							
			}
		});

		backButton = (Button) findViewById(R.id.synchro_BackButton);
		backButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		cancelButton = (Button) findViewById(R.id.synchro_CancelButton);
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				synchroCanceled = true;
				cancelButton.setEnabled(false);
			}
		});

		photosToDownload = new ArrayList<FileToDownload>();
		documentsToDownload = new ArrayList<FileToDownload>();
		listingsToDownload = new ArrayList<String>();

		photoSummaryText = (TextView) findViewById(R.id.synchro_PhotoSummary);
		photoProgress = (ProgressBar) findViewById(R.id.synchro_PhotoProgress);
		listingSummaryText = (TextView) findViewById(R.id.synchro_ListingsSummary);
		listingProgress = (ProgressBar) findViewById(R.id.synchro_ListingsProgress);


		FinishedDownloadingListings = false;
		FinishedDownloadingPhotos = false;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		synchroCanceled = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadPreferences();
		proxyfactory = new ProxyFactory(serverName, serverPort, password, userName, profile);
	}

	/**load shared preferences and set default values*/
	private void loadPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		serverName = prefs.getString("ServerName", "192.168.1.91");
		serverPort = prefs.getString("ServerPort", "8080");
		userName = prefs.getString("UserName", "");
		profile = prefs.getString("Profile", "");
		password = prefs.getString("Password", "");
		((TextView) findViewById(R.id.synchro_ServerName)).setText("Server Name: " + serverName+"\nPort: "+serverPort);
		String maxPhotosStr = prefs.getString("MaxPhotos", "19");;
		try {
			maximumPhotosToDownload = Integer.parseInt(maxPhotosStr); 	
		}
		catch (NumberFormatException nfe) {
			maximumPhotosToDownload = 19;
		}
		mediaToDownload = prefs.getString("MediaToDownload", "1");

	}

	/** download listings */
	private class DownloadListingsTask extends AsyncTask<List<String>, Void, Void> {	

		@Override
		protected void onPostExecute(Void result) {			
			deleteListings();
			FinishedDownloadingListings = true;
			if (FinishedDownloadingPhotos)
				whenFinishedDownloading();
		}
		

		@Override
		protected void onProgressUpdate(Void... values) {
			// update progress bar etc

			listingProgress.incrementProgressBy(1);
			listingSummary();
			// if (synchroCanceled)
			// cancel(true);
		}

		@Override /** create new listings records*/
		protected Void doInBackground(List<String>... params) {

			//start rest connection
			TServerMethods1 proxy = proxyfactory.getProxy();

			for (String ref : params[0]) {
				if (synchroCanceled)
					break;

				pspLog.i(TAG,"DownloadListingsTask for ref " + ref);

				TJSONObject root = null;
				try {
					root = proxy.GetListing(ref);
				} catch (DBXException e) {
					pspLog.error(e.getMessage());
				}


				if(root!=null)
				{
					TJSONObject fields = root.getJSONObject("fields");


					ListingRecord listingRecord = new ListingRecord();

					String ErrorMessage = listingRecord.FromTJSONObject(fields);
					if (ErrorMessage != "") {
						pspLog.w(TAG,"listingRecord.FromTJSONObject(fields) failed");
						//					pspLog.w(TAG, ErrorMessage);
					}

					if (listingRecord.ref == "") {
						pspLog.w(TAG, "Failed to obtain listing: " + ref);
					} else {
						pspLog.i(TAG, "Processing Listing " + listingRecord.ref);
					}

					publishProgress();
					if (Listings.mDbHelper.updateOrInsertListing(listingRecord))
						listingsAdded += 1;
					else
						listingsUpdated += 1;
				}
				else
				{
					pspLog.i(TAG,"root == null");
				}
				
			}
			proxyfactory.logOut(proxy);
			return null;
		}
	}

	private void resetCounters() {
		listingsDeleted = 0;
		listingsSkipped = 0;
		listingsUpdated = 0;
		listingsAdded = 0;
		photosDeleted = 0;
		photosUpdated = 0;
		photosAdded = 0;
		photosUpdated = 0;
		photosSkipped = 0;
	}


	protected JSONArray StringToJSON(String s) {
		if (s == "")
			return null;
		try {
			JSONObject root = new JSONObject(s);
			JSONArray result = root.getJSONArray("result");
			return result.getJSONArray(0);
		} catch (Exception je) {
			pspLog.error(je.getMessage());
			return null;
		}
	}

	protected void copyStream(InputStream i, OutputStream o) {
		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = i.read(buf)) > 0)
				o.write(buf, 0, len);
		} catch (IOException e) {
			pspLog.error(e.getMessage());
		}
	}

	protected static void getImage(String ref, int picNum, TServerMethods1 proxy, int screenW, int screenH) {
		pspLog.i(TAG, "Getting Image for " + ref);
		String fileName = PhotoManager.getFileName(ref, picNum);

		try {
			FileDownloader.DownloadImage(ref, picNum, fileName, proxy, screenW, screenH);
		} catch (IOException e) {
			pspLog.error(e.getMessage());
		}
	}

	protected static void createThumbnail(String ref) {
		pspLog.i(TAG, "Getting Image for " + ref);
		String fileName = PhotoManager.getThumbName(ref, 1);


	}

	protected void DownloadPicsAndDocs() {
		TServerMethods1 proxy = proxyfactory.getProxy();
		//get documents to download
		ArrayList<ListingRecord> listings = new ArrayList<ListingRecord>();
		String tableName = "listings"; //define table
		SalesPartnerDbAdapter mDbHelper = new SalesPartnerDbAdapter(context);
		
		
		//TODO remove DB search use JSON
		mDbHelper.open();
		Cursor c = mDbHelper.fetchAllListings();
		c.moveToFirst();
		while(!c.isAfterLast())
		{
			ListingRecord listing = new ListingRecord();
			listing.FromCursor(c);
			listings.add(listing);
			c.moveToNext();
		}
		c.close();
		mDbHelper.close();
		// end
		
		for(MediaListItem mli: MediaList)
		{
			Document doc = MediaListItemToDocument(mli);
			
			//build a list of documents to download
			DocumentToDownload dtd = new DocumentToDownload(doc.ref, doc.name);
			File testFile = new File(Environment.getExternalStorageDirectory()+"/PSPDocs/"+doc.ref+"/"+doc.name); //date modified
			if(!testFile.exists()) //test whether doc already exists
			{
				documentsToDownload.add(dtd);
				pspLog.i("",dtd.toString());
			}
			else
			{
				pspLog.i("","Doc already exists");
			}
		}
		pspLog.e("","in DownloadPicsAndDocs documentsToDownload.size() = "+documentsToDownload.size());
		proxyfactory.logOut(proxy); 
		
	}
	private void DownloadFiles(){	
		pspLog.e("","DownloadFiles()");
		pspLog.e("","documentsToDownload.size() = "+documentsToDownload.size());
		PhotoManager.CreateDirectories();
		photoProgress.setProgress(0);
		photoProgress.setMax(photosToDownload.size()+documentsToDownload.size());
		new DownloadFilesTask().execute(photosToDownload, documentsToDownload);
	}
	
	protected void DownloadListings() {
		listingProgress.setProgress(0);
		listingProgress.setMax(listingsToDownload.size());
		new DownloadListingsTask().execute(listingsToDownload);
	}

	protected static Date StringToDate(String s) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date result;
		try {
			result = df.parse(s);
		} catch (ParseException e) {
			result = new Date(1900, 1, 1);
			// return a really old date - any local file will have a newer date.
		}
		return result;
	}
	/**
	 * Support class
	 */
	class MediaListItem{
		public String ref;
		public String name;
		public int size;
		public String ext;
		
		public MediaListItem(String ref, String name, int size, String ext){
			this.ref = ref;
			this.name = name;
			this.size = size;
			this.ext = ext;
		}
		
		public boolean DownloadThisOne() {
	        boolean isJPG = ext.equals("jpg");		
			if (mediaToDownload.equals("1")) {
				return true;	
			} else if (mediaToDownload.equals("2")) {
				return false;
			} else if (mediaToDownload.equals("3")) {
				return !isJPG;
			} else			
			   return true;
		}
	}

	protected void ProcessListings_Loop(TJSONObject jSONListing) {
		try {
			String ref = jSONListing.getString("ref");
			
			//media list
			TJSONArray mediaArray = jSONListing.getJSONArray("media");
			if(mediaArray!=null)
			{
				//if media list exists iterate the files within
				for(int i = 0 ; i < mediaArray.size() ; i++)
				{
					TJSONObject current = (TJSONObject) mediaArray.get(i);
					String ext = current.getString("extension");
					MediaListItem mediaListItem = new MediaListItem(ref, current.getString("name"),Integer.parseInt(current.getString("size")),ext);
					if (mediaListItem.DownloadThisOne()) {
						MediaList.add(mediaListItem);
					};
				}
			}
			
			
			String serverChangeDate = jSONListing.getString("ChangeDate");
			String localChangeDate = Listings.mDbHelper.changeDateForListing(ref);
			if (jSONListing.has("PictureChangeDates")) {
				TJSONArray Pictures = jSONListing.getJSONArray("PictureChangeDates");
				for (int i = 0; i < Math.min(Pictures.size(), maximumPhotosToDownload); i++) {					
					int picNum = i + 1;
					File f = PhotoManager.getFile(ref, picNum);
					if (f.exists()) {
						String PictureChangeDate = Pictures.getString(i);
						Date ServerModifiedDate = StringToDate(PictureChangeDate);
						Date LocalModifiedDate = new Date(f.lastModified());
						if (LocalModifiedDate.before(ServerModifiedDate)) {
							// server date is newer so download the photo
							photosToDownload.add(new PhotoToDownload(ref, picNum));
						} else {
							photosSkipped += 1;
						}
					} else {
						photosToDownload.add(new PhotoToDownload(ref, picNum));
					}
				}
			}
			if (!serverChangeDate.equals(localChangeDate)) {
				// ProcessListing(ref);
				listingsToDownload.add(ref);
			} else {
				Listings.mDbHelper.unmarkForDeletion(ref);
				listingsSkipped += 1;
			}
		} catch (Exception e) {
			pspLog.error(e.getMessage());
		}
	}

	protected void ProcessListings() {

	}

	protected void deleteListings() {
		Cursor c = Listings.mDbHelper.fetchListingsMarkedForDeletion();
		// for each listing delete any photos, then delete listings
		listingsDeleted = c.getCount();
		String ref;
		while (c.isAfterLast() == false) {
			ref = c.getString(c.getColumnIndex("ref"));
			photosDeleted += PhotoManager.deleteAllPhotos(ref);
			c.moveToNext();
		}
		c.close();
		Listings.mDbHelper.deleteListingsMarkedForDeletion();
	}

	void photoSummary() {
		photoSummaryText.setText(String.format("Added:%d\nUpdated:%d\nSkipped:%d\nDeleted:%d", photosAdded,
				photosUpdated, photosSkipped, photosDeleted));
	}

	void listingSummary() {
		listingSummaryText.setText(String.format("Added:%d\nUpdated:%d\nSkipped:%d\nDeleted:%d", listingsAdded,
				listingsUpdated, listingsSkipped, listingsDeleted));
	}

	protected void whenFinishedDownloading() {
		pspLog.i("","whenFinishedDownloading()");
		listingSummary();
		photoSummary();
		backButton.setEnabled(true);
		homeButton.setEnabled(true);
		cancelButton.setEnabled(false);
		String toastText;
		if (!synchroCanceled)
			toastText = "Finished";
		else
			toastText = "Canceled";
		Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && ((!FinishedDownloadingPhotos) || (!FinishedDownloadingListings))) {
			Toast.makeText(getApplicationContext(), "Cannot return until finished downloading", Toast.LENGTH_SHORT)
			.show();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
