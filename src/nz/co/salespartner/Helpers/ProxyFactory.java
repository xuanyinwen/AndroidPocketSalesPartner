package nz.co.salespartner.Helpers;

import android.util.Log;

import com.embarcadero.javaandroid.DBXException;
import com.embarcadero.javaandroid.DSProxy.TServerMethods1;
import com.embarcadero.javaandroid.DSRESTConnection;

/** establishes connection, return proxy */
public class ProxyFactory{
	private DSRESTConnection conn;
	private String TAG = "ProxyFactory";
	private String serverPort;
	private String serverName;
	private String userName;
	private String password;
	private String profile;

	public ProxyFactory(String sN, String sP, String pW, String uN, String pr){ 
		serverName = sN.trim();
		serverPort = sP.trim();
		userName = uN.trim();
		password = pW.trim();
		profile = pr.trim();
	}
	
	public TServerMethods1 getProxy(){
		conn = new DSRESTConnection();
		conn.setHost(serverName);  
		conn.setPort(Integer.parseInt(serverPort));
		
		TServerMethods1 proxy = new TServerMethods1(conn);
		
		try { 
			if(proxy.AuthenticationRequired())
			{
				Log.i(TAG,"Login required");
				proxy.LogIn(userName, password, profile);
				Log.i(TAG,"Login successful");
			}
			else
			{
				Log.i(TAG,"Login not required");					
			}
				
			} catch (DBXException e) {
				Log.i(TAG,"Failed to log in");
				e.printStackTrace();
			}
		
		return proxy;
	}
	
	public void logOut(TServerMethods1 proxy){
		try {
			proxy.LogOut();
		} catch (DBXException e) {
			Log.i(TAG,"Failed to log out");
			e.printStackTrace();
		}
	}
}