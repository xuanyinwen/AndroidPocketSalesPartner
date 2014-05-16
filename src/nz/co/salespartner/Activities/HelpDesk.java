package nz.co.salespartner.Activities;

import java.io.File;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.StyleFactory;
import nz.co.salespartner.Helpers.pspLog;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HelpDesk extends Activity{

	private StyleFactory stylefactory;
	private TextView versionView;
	private EditText msgET;
	private EditText nameET;
	private String version;
	private final String SUPPORT_NUMBER = "+64 4 4711 849";
	
	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.help_desk);
		context = this;
		stylefactory = new StyleFactory(this);
		initViews();
		styleTVs();
	}
	
	private void styleTVs() {
		stylefactory.initHeader(this);

	}


	private void initViews() {
		/*show version number*/
		versionView = (TextView)findViewById(R.id.version_number);
		version = "unknown";
		try {
			version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			
		}
		versionView.setText("Version: "+version);
		
		/*check for updates button*/
		TextView updateText = (TextView)findViewById(R.id.update_link);
		final String updateurl = "http://salespartner.dnserver.net.nz/updatepsp.php?&device_version="+version;
		updateText.setClickable(true);
		updateText.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(updateurl));
				startActivity(i);
			}
		});
		
		final CheckBox sendLog = (CheckBox)findViewById(R.id.hp_sendlog);
		msgET = (EditText)findViewById(R.id.hp_msg);
		nameET = (EditText)findViewById(R.id.hp_name);
		/*send button*/
		Button send = (Button)findViewById(R.id.hp_send);
		send.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				String msg = msgET.getText().toString();
				String name = nameET.getText().toString();
				if(TextUtils.isEmpty(msg)==false&&TextUtils.isEmpty(name)==false)
					{
					sendFeedback(name,msg,sendLog.isChecked());
					}
				else
					showIncompleteFeedback();
			}
		});
		/*phone buttons*/
		Button phone1 = (Button)findViewById(R.id.hd_pb1);
		phone1.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				callSupport();
			}
		});
		
	}

	/**start call to support line*/
	protected void callSupport() {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:"+SUPPORT_NUMBER));
			startActivity(callIntent);
		} catch (ActivityNotFoundException e) {
			Log.e("HelpDesk", "Call failed", e);
		}
	}

	protected void sendFeedback(String name, String msg, boolean sendLog) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@salespartner.co.nz"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "PSP-Android Feedback");
		intent.putExtra(Intent.EXTRA_TEXT, formatFeedack(name,msg));
		if(sendLog)
		{
			Uri uri = Uri.fromFile(pspLog.getLogFile());
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			
		}
		startActivity(Intent.createChooser(intent, "Send email..."));
	}

	private String formatFeedack(String name, String msg) {
		String body = "";
		body+="Name: "+name;
		
		body+="\n\nDevice: "+ android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
		body+="\nSDK: "+ android.os.Build.VERSION.SDK_INT;
		body+="\nApp Version: "+version;
		  
		body+="\n\nMessage: "+msg;
		return body;   
	}

	protected void showIncompleteFeedback() {
		Toast.makeText(context, "Form incomplete.", Toast.LENGTH_SHORT).show();
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
