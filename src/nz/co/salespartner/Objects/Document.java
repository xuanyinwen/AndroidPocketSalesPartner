package nz.co.salespartner.Objects;

import java.util.Date;

import nz.co.salespartner.Activities.AllDocs.ListingFileList;

/** private class for storing document attributes 
 * @param <T>*/
public class Document{
	public String name;
	public int size;
	public String ext;
	public boolean isLocal = false;
	public String content;
	public Date lastMod;
	public String listing_id;
	public String listing_address;
	public ListingFileList parentFileList;
	public String ref;

	public Document(){

	}
	
	/**check file extention and return true if .txt file */
	public boolean isNote(){
		if(ext.equalsIgnoreCase("txt"))
			return true;
		else
			return false;
	}

	public String getTitle() {
		String title = name;
		title = title.replaceAll("."+ext, "");
				
		return title;
	}

}