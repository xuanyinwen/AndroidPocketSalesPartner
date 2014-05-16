package nz.co.salespartner.Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import nz.co.salespartner.Objects.ListingRecord;
import android.content.Context;
import android.util.Log;

public class TMIdParser {

	public ArrayList<String> suburb;
	public ArrayList<String> id;
	private Context context;
	private String fileName;
	private String TAG = "TMIdParser";
	
	public TMIdParser(Context c){
		context = c ;
		fileName = "tm_suburb_ids.csv";
		suburb = new ArrayList<String>();
		id = new ArrayList<String>();
		init();
	}

	public String parse(ListingRecord l){
		Log.i(TAG ,"Parsing Listing Record");
		String idstr = "in";
		for(int i = 0; i < suburb.size(); i ++)
		{

			Log.i(TAG,"Parsing the listings. Listing Suburb = "+l.Suburb);
			if(l.Suburb.toString().equalsIgnoreCase(suburb.get(i).toString()))
			idstr = id.get(i);
		}
		return idstr;
	}
	
	private void init(){
		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    try {
	    	Log.i("BUFFER READER","in");
	    	String line;
	        while ((line = reader.readLine()) != null) {
	             String[] RowData = line.split(",");
	             suburb.add(RowData[5]);
	             id.add(RowData[4]);
	        }
	    }
	    catch (IOException ex) {
	        // handle exception
	    }
	    finally {
	        try {
	            is.close();
	        }
	        catch (IOException e) {
	            // handle exception
	        }
	    }
	}
}
