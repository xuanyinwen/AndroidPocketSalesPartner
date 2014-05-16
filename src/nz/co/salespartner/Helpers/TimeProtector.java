package nz.co.salespartner.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Enables one to unlock private details for a certain amount of time
 * @author CFP
 *
 */
public class TimeProtector {
	private static final int MAX_TIME = 1000*60*15; //15 minutes
	private final Context context;
	private SharedPreferences prefs;
	/**
	 * Constructor
	 */ 
	public TimeProtector(Context context){
		this.context = context;
		prefs =  PreferenceManager.getDefaultSharedPreferences(context);
	}
	/**
	 * is allowed to open private
	 * @return 
	 */
	public boolean isAllowedToOpen(){
		//if time passed is less than MAX_TIME or is not passwordEnabled then return true
		if(isPasswordEnabled()==false)
		{
			Log.i("","isPasswordEnabled()==false");
			return true;
		}
		else if(getTimeElapsed()<(getLastTime()+MAX_TIME))
		{
			Log.i("","getTimeElapsed()<MAX_TIME");
			return true;
		}
		else
			return false;
	}
	/**
	 * show dialog for password - on success make the children visible
	 */ 
	public void askForPassword(final Activity activity, final int[] children){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final EditText input = new EditText(context);
		builder.setView(input);
		builder.setMessage("Enter password")
		       .setCancelable(false)
		       .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                if(evaluatePassword(input.getText().toString()))
		                {
		                	for(int child : children)//make all the views visible
		                	{
								View child_view = (View)activity.findViewById(child);
								child_view.setVisibility(View.VISIBLE);
							}
		                }
		                else
		                {
		                	Toast.makeText(activity, "Incorrect password", Toast.LENGTH_SHORT).show();
		                }
		                dialog.dismiss();
		           }
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	/**
	 * Evaluate password attempt if successful return true and reset timer
	 * @param attempt
	 * @return
	 */
	protected boolean evaluatePassword(String attempt) {
		String password = prefs.getString("timerPassword","");
		if(attempt.equalsIgnoreCase(password))//compare attempt and password
		{
			resetTimer();
			Log.i("","attempt is correct");
			return true;
		}
		else
		{
			Log.i("","attempt is wrong");
			return false;
		}
	}
	/**
	 * return the long millis of lasttime password was entered correctly
	 * @return
	 */
	private long getTimeElapsed() {
		long lasttime = getLastTime();
		if(lasttime == 0) //it doesn't exist in prefs yet so create it
		{
			lasttime = System.currentTimeMillis();
			resetTimer();
		}	
		return lasttime;
	}
	/**
	 * Get the last time
	 * @return
	 */
	private long getLastTime(){
		return prefs.getLong("lastTimePasswordEntered",0);
	}
	/**
	 * When password entered correctly reset timer to current time
	 */
	private void resetTimer(){
		Editor edit = prefs.edit();
		edit.putLong("lastTimePasswordEntered", System.currentTimeMillis());
		edit.commit();
	}
	/**
	 * determine whether is is necessary to check passwords
	 * @return
	 */
	private boolean isPasswordEnabled(){
		return prefs.getBoolean("timerPasswordCheck",false);
	}
	
	
}
