package nz.co.salespartner.Activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import nz.co.salespartner.R;
import nz.co.salespartner.SalesPartnerApplication;
import nz.co.salespartner.Activities.AllDocs.DeleteFileProgress;
import nz.co.salespartner.Activities.AllDocs.ListingFileList;
import nz.co.salespartner.Helpers.ProxyFactory;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Objects.Document;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.embarcadero.javaandroid.DBXException;
import com.embarcadero.javaandroid.DSProxy.TServerMethods1;
import com.embarcadero.javaandroid.TJSONArray;
import com.embarcadero.javaandroid.TJSONObject;
import com.embarcadero.javaandroid.TStream;

public class ListingDocuments extends Activity{
	private String serverName;
	private String serverPort;
	private TJSONArray fitems;
	private DocumentArrayAdapter mAdapter;
	private ArrayList<Document> DocumentArray; 
	private ListView lv;
	private TServerMethods1 proxy;
	private String ref = "";
	private TextView vd_msg;
	private SalesPartnerApplication app;
	private File dir;
	private String tag = "Listing Docs";
	private Context context;
	private String absolutePath;
	private StyleFactory stylefactory;
	private boolean pdfsOnly;
	private String password;
	private String userName;
	private boolean serverReachable;
	private boolean serverStatusEstablished;
	private boolean sendToVendor;
	private boolean sendToSelf;
	private String selfEmail;
	private String vendorEmail;
	private boolean useNoteTitleAsSubject;
	private String address;
	private String profile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.view_documents);
		Log.i("ListingDocuments","onCreate()");
		context = this;
		app = new SalesPartnerApplication();
		stylefactory = new StyleFactory(this);
		//if directory doesn't exist, create one
		styleTVs();
		ref = getIntent().getStringExtra("ref");
		address = getIntent().getStringExtra("address");
		vendorEmail = getIntent().getStringExtra("vendorEmail");
		lv = (ListView)findViewById(R.id.vd_listview);
		loadPreferences();
		serverStatusEstablished = false;
		vd_msg = (TextView)findViewById(R.id.vd_msg);
		initButtons();
	}

	private void loadPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		pdfsOnly = prefs.getBoolean("showPDFsOnly", true);		
		sendToVendor = prefs.getBoolean("sendToVendor", true);
		sendToSelf = prefs.getBoolean("sendToSelf", true);
		selfEmail = prefs.getString("selfEmail", "");
		useNoteTitleAsSubject = prefs.getBoolean("useNoteTitleAsSubject", false);
	}

	private void styleTVs() {
		int[] tvs = {
				R.id.vd_bold1, R.id.vd_bold2, R.id.vd_bold3
		};
		View current = null;
		for(int id : tvs)
		{
			current = (View)findViewById(id);
			stylefactory.applyBold(current);
		}
	}  

	private void writeNote(String title, String body){
		try {

			String NOTENAME = trimTitle(title);
			File newNote = new File(Environment.getExternalStorageDirectory() + "/PSPDocs/"+ref+"/"+NOTENAME+".txt");
			if(newNote.exists() ==false)
				newNote.createNewFile();


			FileWriter outFile = new FileWriter(newNote);
			PrintWriter out = new PrintWriter(outFile);
			out.println(body);

			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private String trimTitle(String title) {
		return title.replaceAll("\\s+","");

	}

	private void initDirectory() {
		dir = new File (Environment.getExternalStorageDirectory() + "/PSPDocs/"+ref+"/"); //defines directory location
		absolutePath = Environment.getExternalStorageDirectory() + "/PSPDocs/"+ref;
		if(dir.exists() == false) //make directory if doesn't exist
			dir.mkdirs();
	}

	/** called after onCreate() and when activity is revisted */
	@Override
	protected void onResume() {
		super.onResume();
		//test for server connection on first onResume();
		DocumentArray = new ArrayList<Document>();

		initDirectory();
		new LoadLocalFiles().execute();


	}


	/** load in background */
	public class ContactingServerProgress extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;
		public ContactingServerProgress() {
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

			mAdapter = new DocumentArrayAdapter();

			initDirectory();
			if(!serverReachable)
			{
				showNoServerMsg();
				new LoadLocalFiles().execute();
			}
			else
				new SyncDialog().execute();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			progress.dismiss();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			serverReachable = false;
			defineServerConnection();
			if(isOnline() && serverStatusEstablished == false)
			{
				testForServerReachable();
				serverStatusEstablished = true;
			}

			return null;
		}
	}


	private void showNoServerMsg() {
		Toast.makeText(context, "The server could not be reached.\nOnly local documents show.", Toast.LENGTH_SHORT).show();
	}

	private void testForServerReachable() {
		try {
			proxy.AuthenticationRequired();
			serverReachable = true;
		} catch (DBXException e) {
			Log.i("ListingDocuments","Server could not be reached");
			e.printStackTrace(); //The proxy couldn't connect
			serverReachable = false;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/** refresh document list */
	private void sync(){
		DocumentArray.clear();
		searchDirectory();  //search for offline docs
		if(!serverStatusEstablished)
			defineServerConnection();
		if(isOnline() && serverStatusEstablished == false)
		{
			testForServerReachable();
			serverStatusEstablished = true;
		}
		if(isOnline() && serverReachable && serverStatusEstablished)  
		{
			try {

				retrieveFileList();
				hideNoDocsMsg();

			}
			catch (Exception e) { //no files found for listing
			}
		}


	}

	/** load in background */
	public class SyncDialog extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;
		public SyncDialog() {
			progress = new ProgressDialog(context);
			progress.setMessage("Loading document list...");
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {

			progress.dismiss();
			mAdapter = new DocumentArrayAdapter();
			fillData();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			sync();
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			new LoadLocalFiles().execute();
		}
	}

	/** refresh document list */
	private void initialrefresh(){
		DocumentArray.clear();
		searchDirectory(); 

	}

	/** search sd/pspdocs/ref for local files */
	private void searchDirectory() {
		File[] localFiles = dir.listFiles();
		for(File f : localFiles)
		{
			Document current = new Document();
			current.name = f.getName();
			current.size = (int)(f.length()); //convert bytes to kb
			current.ext = parseForExt(f.getName());
			current.isLocal = true;
			DocumentArray.add(current);
		}
	}

	private void showNoConnectionDialog() {
		Toast.makeText(context, "Connection to server required. Please try again.",Toast.LENGTH_SHORT).show();
	}

	private void displayNoDocsMsg() {
		vd_msg.setVisibility(View.VISIBLE);
	}

	private void hideNoDocsMsg() {
		vd_msg.setVisibility(View.GONE);
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

	public void defineServerConnection(){
		/* establish connection */
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		serverName = prefs.getString("ServerName", "192.168.1.91");
		serverPort = prefs.getString("ServerPort", "8080");
		userName = prefs.getString("UserName", "");
		password = prefs.getString("Password", "");
		profile = prefs.getString("Profile", "");
		
		/* try fetch media list*/
		ProxyFactory pf = new ProxyFactory(serverName, serverPort, password, userName, profile);
		proxy = pf.getProxy();
	}

	/** load in background */
	public class LoadLocalFiles extends AsyncTask<Void, Void, Void> {
		private Boolean proccessed;
		private ProgressDialog progress;
		public LoadLocalFiles() {
			progress = new ProgressDialog(context);
			progress.setMessage("Refreshing...");
		}  
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			mAdapter = new DocumentArrayAdapter();
			fillData();

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			progress.dismiss();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			initDirectory();
			initialrefresh();
			return null;
		}
	}

	/** load in background */
	public class DownloadFile extends AsyncTask<Void, Void, Void> {
		private Document document;
		private File f;
		private ProgressDialog progress;
		public DownloadFile(Document d) {
			progress = new ProgressDialog(context);
			progress.setMessage("Downloading file...");
			document = d;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			try {
				openFileViewer(f, document);
			} catch (Exception e) {
				Log.i("","failed button click");
				e.printStackTrace();
			}
			sync();
			progress.dismiss();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			TStream filestream;
			try {
				filestream = proxy.GetMediaFile(ref, document.name);
				f = downloadFile(document.name, filestream);
			} catch (DBXException e) {
				Log.i("","failed  click");
				e.printStackTrace();
			}
			return null;
		}
	}

	/** load in background */
	public class DeleteAllFiles extends AsyncTask<Void, Void, Void> {
		private ProgressDialog prog;
		public DeleteAllFiles() {
			prog = new ProgressDialog(context);
			prog.setMessage("Deleting all local files...");
		}
		@Override
		public void onPreExecute() {
			prog.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			prog.dismiss();
			sync();
			fillData();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			deleteAllFiles();
			return null;
		}
	}

	private void fillData() {
		Log.i("","fillData called");

		if(DocumentArray.size()==0)
		{
			displayNoDocsMsg();
		}
		else
		{
			hideNoDocsMsg();
		}

		lv.setAdapter(mAdapter);
		lv.invalidateViews();
		updateTabHost();
	}

	/**change text on "Docs" tab*/
	private void updateTabHost() {
		int docCount = DocumentArray.size();
		ListingTabHost parent = (ListingTabHost) getParent();
		TabHost mTabHost = parent.getTabHost();
		TabWidget vTabs = mTabHost.getTabWidget();
		LinearLayout rLayout = (LinearLayout) vTabs.getChildAt(3); // 3 = pos of doc tab
		TextView tv = ((TextView) rLayout.findViewById(R.id.tabsText));
		tv.setText("Docs "+docCount);
	}

	/** array adapter for documents listview*/
	private class DocumentArrayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public DocumentArrayAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return DocumentArray.size();
		}

		public long getItemId(int position) {
			return position;
		}  

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.document_row, null);
				final Document document = DocumentArray.get(position);

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
							openFile(document);
						else
							new DownloadFile(document).execute();
					}
				});  

				//show sharing dialog on longClick
				convertView.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View v) {
						if(document.isLocal)
							Log.i("Listing Docs","Absolute ="+absolutePath);
						showFileDialog(document);
						return true;
					}
				});  
			} 
			else {}

			if(position % 2 == 0) //alternating colors
				convertView.setBackgroundResource(R.drawable.row_states_dark);
			else
				convertView.setBackgroundResource(R.drawable.row_states_light);

			return convertView;
		}

		public Object getItem(int position) {
			return null;
		}
	}


	private void initButtons() {
		/* new note button */
		LinearLayout newnote = (LinearLayout)findViewById(R.id.vd_new_note);
		newnote.setClickable(true);
		newnote.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				//refresh();
				showNoteDialog();
				Log.i("","new note clicked");
			}  
		});

		/* refresh button */
		LinearLayout fetch = (LinearLayout)findViewById(R.id.vd_fetch_button);
		fetch.setClickable(true);
		fetch.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				//refresh();
				if(!serverStatusEstablished)
					new ContactingServerProgress().execute();
				else if(serverStatusEstablished && serverReachable)
					new SyncDialog().execute();
				else if(serverStatusEstablished && !serverReachable);
			}
		});

		/* download all files button */
		LinearLayout download_all = (LinearLayout)findViewById(R.id.vd_download_all_button);
		download_all.setClickable(true);
		download_all.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.i("","download all clicked");
				new DownloadAllFiles().execute();;


			}
		});

		/* delete all files button */
		LinearLayout delete_all = (LinearLayout)findViewById(R.id.vd_delete_all_button);
		delete_all.setClickable(true);
		delete_all.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				new DeleteAllFiles().execute();
			}
		});
	}



	protected void showNoteDialog() {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_bg); //set to custom bg
		dialog.setContentView(R.layout.note_popup);

		dialog.setCancelable(true);

		final EditText bodyIn = (EditText)dialog.findViewById(R.id.popup_body_text);
		final EditText titleIn = (EditText)dialog.findViewById(R.id.popup_title_text);

		Button write_button = (Button) dialog.findViewById(R.id.popup_write);
		write_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String body = bodyIn.getText().toString();
				String title = titleIn.getText().toString();
				if(!TextUtils.isEmpty(body) && !TextUtils.isEmpty(title))
				{
					writeNote(title,body); //write note
					dialog.dismiss(); //dismiss dialog
					if(isOnline() && serverReachable)
						new SyncDialog().execute();
					else
						new LoadLocalFiles().execute(); //start loadlocalfiles dialog
				}
			}
		});

		ImageView close = (ImageView)dialog.findViewById(R.id.pu_note_close_icon);
		close.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();


	}

	private void openFileViewer(File file, Document document) {
		Intent intent = new Intent();  
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String fileType = getFileType(document);
		if(fileType != null)
		{
			if(fileType == "image/*")
			{
				Intent fullphoto = new Intent(this,FullScreenPhoto.class);
				fullphoto.putExtra("imagename", document.name);
				fullphoto.putExtra("absolutepath", absolutePath);
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

	private void openFile(Document document){
		openFileViewer(getLocalFile(document.name), document);
	}

	private void deleteFile(Document document){
		new DeleteFileProgress(document).execute();

	}

	/** delete a single file in background */
	public class DeleteFileProgress extends AsyncTask<Void, Void, Void> {
		private Document document;
		private ProgressDialog progress;
		public  DeleteFileProgress(Document d) {
			progress = new ProgressDialog(context);
			progress.setMessage("Deleting file...");
			document = d;
		}
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {

			progress.dismiss();
			sync();
			fillData();
		}

		@Override
		protected Void doInBackground(Void... arg0) {

			File target = getLocalFile(document.name);
			target.delete();
			return null;
		}
	}
	
	private String readNote(Document document) {
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(getLocalFile(document.name)));
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

	private void emailFile(Document document){
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("image/jpeg");
		String path = "file://"+absolutePath+"/"+document.name;
		
		if(document.isNote())
		{	
			String noteBody = readNote(document);
		Log.i("","noteBody"+noteBody);
			emailIntent.putExtra(Intent.EXTRA_TEXT, noteBody);//add note content
			if(useNoteTitleAsSubject)
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, document.getTitle()+" - "+address);
			else
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, address);
		}
		else
		{	
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));//or attach document
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, address);
		}
		if(document.isNote())
		{
			if(sendToVendor && !TextUtils.isEmpty(vendorEmail))
			{
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {vendorEmail});
			}
			
			
			if(sendToSelf && !TextUtils.isEmpty(selfEmail))
			{
				emailIntent.putExtra(Intent.EXTRA_BCC, new String[] {selfEmail});
			}
			else{
				Toast.makeText(context, "Send to self has been set in settings" +
						" but no email address was specified, please disable setting or provide a valid address.", Toast.LENGTH_SHORT).show();
			}
		}
		
		startActivity(emailIntent);
	} 

	private void deleteAllFiles(){
		for(Document d : DocumentArray)
		{
			File target = getLocalFile(d.name);
			target.delete();
		}


	}

	/** download all files in background */
	public class DownloadAllFiles extends AsyncTask<Void, Void, Void> {
		private File f;
		private Boolean processed = false;
		private ProgressDialog progress;

		public DownloadAllFiles() {
			progress = new ProgressDialog(context);
			progress.setMessage("Downloading all files...");
		}    
		@Override
		public void onPreExecute() {
			progress.show();
		}
		@Override
		public void onPostExecute(Void unused) {
			progress.dismiss();
			if(!processed)
				showNoConnectionDialog();
			sync();
			fillData();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if(isOnline() && serverReachable)
			{
				for(Document d : DocumentArray)
				{
					TStream filestream;
					try {
						filestream = proxy.GetMediaFile(ref, d.name);
						f = downloadFile(d.name, filestream);
						processed = true;
					} catch (DBXException e) {
						e.printStackTrace();
					}
					Log.i("","inside doInBackground");
				}

			}
			else
				processed = false;

			return null;
		}
	}

	private File getLocalFile(String fileName){
		File file = new File(dir, fileName);
		return file;
	}

	private File downloadFile(String fileName, InputStream is){

		if(DirectoryIsNull())
			initDirectory();

		File file = new File(dir, fileName);  //make file object at dir and with fileNaame
		if(file.exists() == false)
		{
			try{
				file.createNewFile();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(50);  //TODO change capacity?
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray());
				fos.close();
				Log.i("","file downloaded");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Listing Doc", "Error: " + e);
			}
		}
		return file;
	}

	private boolean DirectoryIsNull() {
		File test_dir = new File (Environment.getExternalStorageDirectory() + "/PSPDocs/"+ref+"/"); //defines directory location

		if(test_dir.exists() == false) //make directory if doesn't exist
			return false;
		else
			return true;
	}

	/** get list of files for given listing by ref*/
	private void retrieveFileList(){

		try {
			TJSONObject medialist = proxy.GetMediaList(ref); 
			TJSONObject media_fields = medialist.getJSONObject("fields");
			fitems = media_fields.getJSONArray("FItems");
			for(int i = 0; i < fitems.size(); i ++)
			{
				try{
					TJSONObject fitem = fitems.getAsJsonObject(i);

					TJSONObject fitem_fields = fitem.getJSONObject("fields");
					Document current = TJSONtoDocument(fitem_fields);
					if(documentIsNew(current))

					{
						if(pdfsOnly) //if settings PDFsOnly is check only show .pdf files
						{	if(current.ext.equalsIgnoreCase("pdf"))
							DocumentArray.add(current);
						}
						else
							DocumentArray.add(current);
					}

				}
				catch(Exception e){

				}
			}

		} catch (DBXException e) {
			e.printStackTrace();
			Log.i(tag,"Could not retrieve file list for listing");//TODO create failed connection dialog
		}

	}

	private boolean documentIsNew(Document document) {
		File[] localFiles = dir.listFiles();
		for(File f : localFiles)
		{
			if(document.name.equalsIgnoreCase(f.getName()))
				return false;
		}		

		return true;
	}

	private void showFileDialog(final Document document){
		final String items[] = {"Email","Open","Delete"};

		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("File");
		ab.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int choice) {
				if(choice == 0) {
					emailFile(document);
				}
				else if(choice == 1) {
					openFile(document);
				}
				else if(choice == 2) {
					deleteFile(document);
				}
			}
		});
		ab.show();
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


}
