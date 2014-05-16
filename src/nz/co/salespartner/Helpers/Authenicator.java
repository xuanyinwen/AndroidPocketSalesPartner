package nz.co.salespartner.Helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.Date;
import java.util.UUID;

import nz.co.salespartner.Activities.Prefs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
/**
 * This class runs a background thread to check whether phone is authenticated.
 * On first run ever it checks to see if phone exists on the server, if not it registers the phone with the server.
 * Next it checks whether device is activated if not it initiates a lock out of home screen.
 * If the thread fails to contact server for 7 days, it shows it starts a grace period and shows a warning for another 7days 
 *
 */
public class Authenicator extends AsyncTask<Boolean,Void,Integer> {

	private Context context;
	private Activity activity;
	private String TAG = "PSP-Android-Authenicator";
	private final int RESPONSE_GOOD = 0;
	private final int RESPONSE_BAD = 1;
	private final int NO_RESPONSE = 3;
	private final int NO_NETWORK = 4;
	public static final long GRACE_PERIOD_MILLIS = 		 604800000 * 2; //14 DAYS
	private final long ONE_WEEK_MILLIS = 			  604800000 ; //7 DAY
	private final long TIME_BETWEEN_ATTEMPTS_MILLIS = 10800000; //3 HOURS

	private SharedPreferences prefs;
	private int response;
	private boolean isRegTestFailed;
	private boolean serverContactFailed;

	public Authenicator(Context c, Activity a){
		context = c ;
		activity = a;
		
	}

	/**authenicate device based on device ID if a time period has passed*/
	public void authenicateDeviceIfRequired() {
		this.execute(false);
		
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

	}
	
	public String getRegisteredStatus(){
		prefs = activity.getSharedPreferences("authenicationsettings",Context.MODE_PRIVATE);
		boolean firstTime = prefs.getBoolean("firsttime", true);	
		if(firstTime)
		{
			Log.i(TAG,"It's the first time");
			initialiseHiddenPreferences(); //if first time init prefs
		}
		SharedPreferences mainprefs = PreferenceManager.getDefaultSharedPreferences(activity);
		String custumerNumber = mainprefs.getString("customerNumber", "");
		
//Alister: Should probably do this instead		String deviceNickName = URLEncoder.encode(mainprefs.getString("deviceName", ""));
		String deviceNickName = mainprefs.getString("deviceName", "");
		deviceNickName = deviceNickName.replaceAll(" ", "%20");

		Log.i(TAG,"customerNumber ="+custumerNumber);
		String deviceId = prefs.getString("uniqueID", getDeviceID());
		Log.i(TAG,"deviceid "+deviceId);
		String deviceDetails = android.os.Build.MANUFACTURER+"%20"+android.os.Build.MODEL;
  
		//server details
		String serverAddress = "http://salespartner.dyndns.org:8090/datasnap/rest/TServerMethods1";

		//urls
		String registerDeviceURL = serverAddress+"/RegisterDevice/"+
				custumerNumber+"/"+deviceId+"/"+deviceNickName+"%20"+deviceDetails+"/";
		String isRegisteredURL = serverAddress+"/IsDeviceRegistered/"+deviceId+"/";
		String deviceExistsURL = serverAddress+"/DeviceExists/"+deviceId+"/";
		Log.i(TAG,"registerDeviceURL  "+registerDeviceURL);
		boolean deviceExists = deviceExists(deviceExistsURL);
		boolean isRegistered = false;
		if(deviceExists)
		{
			Log.i(TAG,"Device exists. Now test for registered.");
			isRegistered = IsDeviceRegistered(isRegisteredURL);
		}
		
		return "Device exists on server = "+deviceExists+"\nDevice is registered = "+isRegistered;
	}


	private boolean IsDeviceRegistered(String url) {
		JSONObject jArray = null;
		String result = getJSONfromUrl(url);
		boolean isRegistered = false;
		isRegTestFailed = true;
		
		try{
			jArray = new JSONObject(result);
			JSONArray ja = jArray.getJSONArray("result");
					Log.i("","JSON response = "+ja.getBoolean(0));
			isRegistered = ja.getBoolean(0);
					isRegTestFailed = false; //if boolean aquired then regoFailed = false
		}catch(JSONException e){
					isRegTestFailed = true;
			Log.i(TAG,"Error testing for registration - device does not exist or server cannot be reached");
			Log.e("log_tag", "Error parsing data "+e.toString());
		}

		return isRegistered;
	}


	private boolean registerDevice(String url){
		Log.i(TAG,"Registering device on server");
		JSONObject jArray = null;
		String result = getJSONfromUrl(url);
		boolean registrationSuccess = false;
		//try parse the string to a JSON object
		try{
			jArray = new JSONObject(result);
			JSONArray ja = jArray.getJSONArray("result");

			registrationSuccess = ja.getBoolean(0);
		}catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
		}
		Log.i(TAG,"registrationSuccess = "+registrationSuccess);
		return registrationSuccess;
		//		return jArray;
	}
 

	private String getJSONfromUrl(String url) {
		//initialize
		InputStream is = null;
		String result = "";
		//http post
		try{
			//set time out
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 3*1000*60);
			HttpConnectionParams.setSoTimeout(httpParameters, 3*1000*60);
			//connect
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}catch(Exception e){
			
			Log.e("log_tag", "Error in http connection "+e.toString());
		}
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
			Log.i("","JSON result = "+result);

		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}
		return result;
	}
	
	public void forceAuthentication(){
		Log.i("", "FORCE AUTHENTICATING");
		execute(true);
	}

	//--- main thread --//
	@Override
	protected Integer doInBackground(Boolean... force) {
		Log.i(TAG,"Authenicating in background thread...");
		int result = 0;
		if(sufficientTimeSinceLastAttempt() || force[0])
		{
			Log.i(TAG,"new authentication required");
			//make an attempt to server
			String id = prefs.getString("uniqueID", "");
			sendToServerAndInterpret(id);

		}
		else{
			Log.i(TAG,"insufficient time since last authentication");
		}
		return result;
	}

	private Connection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	private void sendToServerAndInterpret(String id) {
		response = NO_RESPONSE;

		Log.i(TAG,"Inside sendToServer");
		//phone details
		SharedPreferences mainprefs = PreferenceManager.getDefaultSharedPreferences(activity);
		String custumerNumber = mainprefs.getString("customerNumber", "");
		String deviceNickName = mainprefs.getString("deviceName", "");
		deviceNickName = deviceNickName.replaceAll(" ", "%20");
		Log.i(TAG,"customerNumber ="+custumerNumber);
		String deviceId = prefs.getString("uniqueID", getDeviceID());
		Log.i(TAG,"deviceid "+deviceId);
		String deviceDetails = android.os.Build.MANUFACTURER+"%20"+android.os.Build.MODEL;
  
		//server details
		String serverAddress = "http://salespartner.dyndns.org:8090/datasnap/rest/TServerMethods1";
		try {
			deviceDetails = URLEncoder.encode( deviceDetails, "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			Log.i(TAG,"Failed to encode deviceDetails");
			e.printStackTrace();
		}
		try {
			deviceNickName = URLEncoder.encode( deviceNickName, "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			Log.i(TAG,"Failed to encode deviceNickName");
			e.printStackTrace();
		}
		
		//urls
		String registerDeviceURL = serverAddress+"/RegisterDevice/"+
				custumerNumber+"/"+deviceId+"/"+deviceNickName+"%20"+deviceDetails+"/";
		String isRegisteredURL = serverAddress+"/IsDeviceRegistered/"+deviceId+"/";
		String deviceExistsURL = serverAddress+"/DeviceExists/"+deviceId+"/";
		
		Log.i(TAG,"registerDeviceURL  "+registerDeviceURL);
		boolean deviceExists = deviceExists(deviceExistsURL);
		if(deviceExists)
		{
			Log.i(TAG,"Device exists. Now test for registered.");
			boolean isRegistered = IsDeviceRegistered(isRegisteredURL);
				Log.i(TAG,"Device isRegistered = "+isRegistered);
			if(isRegistered)
				response = RESPONSE_GOOD;
			else if(!isRegistered)
				response = RESPONSE_BAD;

			
		}
		else if(!serverContactFailed && !deviceExists)
		{
			Log.i(TAG,"contacted server BUT device does not exist");
			registerDevice(registerDeviceURL);//contacted server but device
		}
		else if(serverContactFailed)
			{
			Log.i(TAG,"failed to contact server");
			response = NO_RESPONSE;
			}
		interpretResponse(response);
	}

	/*---------------Checking for device on server------------*/
	private boolean deviceExists(String url) {
		Log.i(TAG,"Checking for device on server");
		JSONObject jArray = null;
		String result = getJSONfromUrl(url);
		boolean deviceExists = false;
		//try parse the string to a JSON object
		try{
			jArray = new JSONObject(result);
			JSONArray ja = jArray.getJSONArray("result");
			deviceExists = ja.getBoolean(0);
			serverContactFailed = false;
		}catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
			serverContactFailed = true;
		}
		return deviceExists;
	
	}

	private void interpretResponse(int response) {
		Log.i(TAG,"response from server: "+response);
		if(response==RESPONSE_BAD)
			instigateLockOut();
		else if(response==RESPONSE_GOOD)
			{
			setNewLastAuthenicatedTime();
//			evaluateGracePeriod(true);
			}
		else if(response==NO_RESPONSE)
			evaluateGracePeriod();
		else
			evaluateGracePeriod();

	}

	private void evaluateGracePeriod() {
		Log.i(TAG,"Evalutating GracePeriod: ");
		long TimeElapsed =  getCurrentTime() - prefs.getLong("lastAuthentication", 0) ;
		if(TimeElapsed > ONE_WEEK_MILLIS) //allow time for bad connections
		{
			//look at grace time elapsed
			Log.i(TAG,"Time elapsed greater than one WEEK so start grace and show sign: ");
			GracePeriod grace = BuildGraceFromPrefs(); 	
			Log.i(TAG,"Currently in grace of duration: "+grace.getElapsedTime());

				setShowGraceToastPref();
			if(grace.getElapsedTime() > GRACE_PERIOD_MILLIS)
				instigateLockOut();

		}
	}

	private void setShowGraceToastPref() {
		Editor prefEditor = prefs.edit();
			prefEditor.putBoolean("gracePeriodOn", true); //will show a toast in landing that grace period on
		prefEditor.commit();
	}
	

	private void removeGracePrefToast() {
		Editor prefEditor = prefs.edit();
			prefEditor.putBoolean("gracePeriodOn", false); //will show a toast in landing that grace period on
		prefEditor.commit();		
	}

	private GracePeriod BuildGraceFromPrefs() {
		Long lastAuth = prefs.getLong("lastAuthentication", 0);
		return new GracePeriod(lastAuth);
	}



	private void instigateLockOut() {
		Log.i(TAG,"instigating lockOut");
		Editor prefEditor = prefs.edit();
		prefEditor.putBoolean("isLocked", true); //set isLocked to locked so that Landing locks up on resume
		prefEditor.commit();
	}



	private void unLockifLocked() {
		Log.i(TAG,"reversing lockOut");
		Editor prefEditor = prefs.edit();
		prefEditor.putBoolean("isLocked", false); //set isLocked to locked so that Landing locks up on resume
		prefEditor.commit();		
	}


	private void setNewLastAuthenicatedTime() {
		Log.i(TAG,"new LastAuthenicatedTime set");
		Editor prefEditor = prefs.edit();
		prefEditor.putLong("lastAuthentication", getCurrentTime()); //as is the first time
		prefEditor.commit();

		unLockifLocked();
		removeGracePrefToast();
	}


	private boolean sufficientTimeSinceLastAttempt() {
		prefs = activity.getSharedPreferences("authenicationsettings",Context.MODE_PRIVATE);
		
		/*test for first time*/
		boolean firstTime = prefs.getBoolean("firsttime", true);	
		if(firstTime)
		{
			Log.i(TAG,"It's the first time");
			initialiseHiddenPreferences(); //if first time init prefs
			promptUserToRegister();
		}
		/*then check current time against lastAuth*/
		long TimeElapsed =  getCurrentTime() - prefs.getLong("lastAttempt", 0) ;
		if(TimeElapsed > TIME_BETWEEN_ATTEMPTS_MILLIS)
		{
			Log.i(TAG,"TimeElapsed > 3hrs");
			Log.i(TAG,"sufficient time has passed");
			return true; // if dif is more than period tell parent method to contact server again
		}
		else
			return false;
	}

	private void promptUserToRegister() {
		activity.getApplication().startActivity(new Intent(context, Prefs.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	private void startNewGracePeriod() {

	}

	/*support object to store grace period when begun*/
	private class GracePeriod{
		long startTime;

		public GracePeriod(long timeIn){
			startTime = timeIn;
		}

		public long getElapsedTime(){
			return getCurrentTime() - startTime;
		}
	}

	private void initialiseHiddenPreferences() {
		Editor prefEditor = prefs.edit();
			prefEditor.putBoolean("firsttime", false);
			prefEditor.putBoolean("isLocked", false);
			prefEditor.putBoolean("gracePeriodOn", false);
			prefEditor.putString("uniqueID", getDeviceID());
			prefEditor.putLong("lastAuthentication", getCurrentTime()); //as is the first time
			prefEditor.putLong("lastAttempt", getCurrentTime()); //as is the first time
		prefEditor.commit();

	}

	public long getCurrentTime() {
		Date d = new Date();
		return d.getTime();
	}

	private String getDeviceID() {
		final TelephonyManager tm = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, tmPhone, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();

		return deviceId;
	}


}
