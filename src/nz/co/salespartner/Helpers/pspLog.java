package nz.co.salespartner.Helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class pspLog {
	private static final String TAG = "pspLog";
	static String dirPath = Environment.getExternalStorageDirectory()+"/PSPDocs";
	static String folderPath = dirPath+"/Logs";
	static String logPath = folderPath+"/log.txt";
	
	public static void init(Context context) {
		//get prefs
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String serverName = prefs.getString("ServerName", "192.168.1.91");
		String serverPort = prefs.getString("ServerPort", "8080");
		String userName = prefs.getString("UserName", "");
		String password = prefs.getString("Password", "");
		String profile = prefs.getString("Profile", "");
		
		//make folders
		makeIfNew(dirPath);
		makeIfNew(folderPath);
		File log = new File(logPath);
		if(log.exists()){
			log.delete(); //delete old log if it exists
			log = new File(logPath);
		}
		try {
			log.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Error in creating log file");
			e.printStackTrace();
		} 
		
		write("*** SETUP DETAILS ***");
		String details = "\nServer name = "+serverName;
		details += "\nServer port = "+serverPort;
		details += "\nUsername = "+userName;
		details += "\nPassword = "+password;
		details += "\nProfile = "+profile;
		write(details);
		write("*** NEW SYNCHRO - date = "+getCurrentTime()+" ***");
	}
	
	private static String getCurrentTime() {
		Date now = new Date();
		long time = now.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
		return df.format(time);
	}

	private static void makeIfNew(String path){
		File f = new File(path);
		if(!f.exists())
		{
			boolean exists = f.exists();
			f.mkdir();
		}
	}

	public static void i(String t, String s) {
		Log.i(t,s);
		write(t,s);
	}

	public static void e(String t, String s) {
		Log.e(t,s);
		write(t,s);
	}

	public static void w(String t, String s) {
		Log.w(t,s);
		write(t,s);
	}

	public static void write(String tag, String text){
		write(tag+" - "+text);
	}
	public static void write(String text){
		 File logFile = new File(logPath);
		   if (!logFile.exists())
		   {
		      try
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      }
		   }
		   try
		   {
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		      buf.append(text);
		      buf.newLine();
		      buf.close();
		   }
		   catch (IOException e)
		   {
			  
		      e.printStackTrace();
		   }
	}

	public static File getLogFile() {
		return new File(logPath);
	}

	public static void error(String message) {
		Log.e("ERROR", message);
		write("***ERROR*** - "+message);
	}
}
