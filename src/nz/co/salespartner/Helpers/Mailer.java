package nz.co.salespartner.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nz.co.salespartner.Objects.ListingRecord;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Mailer {
	private ListingRecord listing;
	private Context context;

	public Mailer(ListingRecord l, Context c){
		listing = l;
		context = c;
	}

	public void sendEmail() {
		String msg = createMessage();
		/* Create the Intent */
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		/* Fill it with Data */
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Listing: "+listing.ref);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);

		/* add multiple attachments */
		List<File> photos = PhotoManager.getAllFiles(listing.ref); 
	    ArrayList<Uri> uris = new ArrayList<Uri>();
	    for (File fileIn : photos)
	    {
	        
	        Uri u = Uri.fromFile(fileIn);
	        uris.add(u);
	    }
	   // emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			
		
		/* Send it off to the Activity-Chooser */
		context.startActivity(emailIntent);
	}

	public void sendText() {
		String msg = createTextMessage();
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", msg); 
		sendIntent.setType("vnd.android-dir/mms-sms");
		context.startActivity(sendIntent);

	}

	public void toFaceBook() {
		// TODO Auto-generated method stub

	}

	public void toTwitter() {
		// TODO Auto-generated method stub

	}

	private String createMessage(){
		String msg = createTextMessage();
		return msg;

	}
	
	private String createTextMessage(){
		String msg = "Listing: "+listing.ref+"\nPrice: "+listing.DisplayPrice+
				"\nFeatures: "+listing.Bedrooms+" bedrooms, "+listing.Bathrooms+" bathrooms"
				+"\nAddress: "+listing.StNo+", "+listing.Street+", "+listing.Suburb;
		
		Date now = new Date();
		Long hometime = (long)listing.OpenHomeTime;
		hometime *= 1000;
		if(hometime > now.getTime() && listing.OpenHomeDuration > 0)
		msg+="\nNext OpenHome: On "+listing.getOpenHomeTime(context)+" for "+listing.OpenHomeDuration+" minutes";
		return msg;

	}
}
