package nz.co.salespartner.Objects;

import java.util.ArrayList;

public class TMListingPin {

	/** Variables */
	private ArrayList<String> name = new ArrayList<String>();
	private ArrayList<String> website = new ArrayList<String>();
	private ArrayList<String> category = new ArrayList<String>();
	private ArrayList<String> listingid = new ArrayList<String>();
	private ArrayList<String> title = new ArrayList<String>();
	private ArrayList<String> latitude = new ArrayList<String>();
	private ArrayList<String> longitude = new ArrayList<String>();
	private ArrayList<String> displayprice = new ArrayList<String>();
	private ArrayList<String> picturehref = new ArrayList<String>();

	public TMListingPin(){
	}
	/** In Setter method default it will return arraylist
	* change that to add */

	public ArrayList<String> getName() {
	return name;
	}

	public void setName(String name) {
	this.name.add(name);
	}

	public ArrayList<String> getWebsite() {
	return website;
	}

	public void setWebsite(String website) {
	this.website.add(website);
	}

	public ArrayList<String> getCategory() {
	return category;
	}

	public void setCategory(String category) {
	this.category.add(category);
	}
	
	public ArrayList<String> getListingId() {
		return listingid;
	}
	
	public void setListingId(String category) {
		this.listingid.add(category);
	}
	
	public ArrayList<String> getTitle() {
		return title;
	}
	
	public void setTitle(String t) {
		this.title.add(t);
	}
	
	public ArrayList<String> getLatitude() {
		return latitude;
	}
	
	public void setLatitude(String t) {
		this.latitude.add(t);
	}
	
	public ArrayList<String> getLongitude() {
		return longitude;
	}
	
	public void setLongitude(String t) {
		this.longitude.add(t);
	}
	
	public ArrayList<String> getDisplayPrice() {
		return displayprice;
	}
	
	public void setDisplayPrice(String t) {
		this.displayprice.add(t);
	}
	
	public ArrayList<String> getPictureHref() {
		return picturehref;
	}
	
	public void setPictureHref(String currentValue) {
		this.picturehref.add(currentValue);		
	}

	
	
}
