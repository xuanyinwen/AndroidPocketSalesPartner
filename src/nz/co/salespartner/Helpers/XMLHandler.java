package nz.co.salespartner.Helpers;

import java.util.ArrayList;

import nz.co.salespartner.Objects.TMListing;
import nz.co.salespartner.Objects.TMListingPin;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;
import android.util.Log;

public class XMLHandler extends DefaultHandler {

	Boolean currentElement = false;
	String currentValue = null;
	public static TMListingPin tmlisting = null;
	public static ArrayList<TMListing> tmlistingArray = new ArrayList<TMListing>();
	
	private ArrayList<String> name = new ArrayList<String>();
	private ArrayList<String> website = new ArrayList<String>();
	private ArrayList<String> listingid = new ArrayList<String>();
	private ArrayList<String> title = new ArrayList<String>();
	private ArrayList<String> latitude = new ArrayList<String>();
	private ArrayList<String> longitude = new ArrayList<String>();
	private ArrayList<String> displayprice = new ArrayList<String>();
	private ArrayList<String> picturehref = new ArrayList<String>();
	private ArrayList<String> area = new ArrayList<String>();
	private ArrayList<String> bedrooms = new ArrayList<String>();
	private ArrayList<String> bathrooms = new ArrayList<String>();
	private ArrayList<String> logos = new ArrayList<String>();
	private ArrayList<String> rvs = new ArrayList<String>();
	private ArrayList<String> addresses = new ArrayList<String>();;
	private ArrayList<String> districts = new ArrayList<String>();;
	
	public XMLHandler(){
		tmlistingArray.clear();
	}

	public static TMListingPin getSitesList() {
		return tmlisting;
	}

	public static void setSitesList(TMListingPin sitesList) {
		XMLHandler.tmlisting = sitesList;
	}

	/** Called when tag starts ( ex:- <name>AndroidPeople</name>
	 * -- <name> )*/
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		currentElement = true;
		
		if (localName.equalsIgnoreCase("list"))
		{
			/** Start */
//			tmlistingArray.add(new TMListing());
		} else if (localName.equals("website")) {
			/** Get attribute value */
			String attr = attributes.getValue("category");
			tmlisting.setCategory(attr);
		}
		
	}

	/** Called when tag closing ( ex:- <name>AndroidPeople</name>
	 * -- </name> )*/
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		currentElement = false;
		
		/** set value */
		if (localName.equalsIgnoreCase("name"))
		{	
			if(TextUtils.isEmpty(currentValue)==false)
			name.add(currentValue);
		}
		else if (localName.equalsIgnoreCase("website"))
			website.add(currentValue);
		else if (localName.equalsIgnoreCase("listingid"))
			listingid.add(currentValue);
		else if (localName.equalsIgnoreCase("title"))
			title.add(currentValue);
		else if (localName.equalsIgnoreCase("pricedisplay"))
			displayprice.add(currentValue);
		else if (localName.equalsIgnoreCase("latitude"))
			latitude.add(currentValue);
		else if (localName.equalsIgnoreCase("longitude"))
			longitude.add(currentValue);
		else if (localName.equalsIgnoreCase("picturehref"))
			picturehref.add(currentValue);
		else if (localName.equalsIgnoreCase("area"))
			area.add(currentValue);
		else if (localName.equalsIgnoreCase("bedrooms"))
			bedrooms.add(currentValue);
		else if (localName.equalsIgnoreCase("bathrooms"))
			bathrooms.add(currentValue);
		else if (localName.equalsIgnoreCase("logo")) //agency logos
			logos .add(currentValue);
		else if (localName.equalsIgnoreCase("RateableValue"))
			rvs.add(currentValue);
		else if (localName.equalsIgnoreCase("Address"))
			addresses.add(currentValue);
		else if (localName.equalsIgnoreCase("District"))
			districts.add(currentValue);
		
		currentValue=""; //reset for empties
		
	}

	public void update() {
		tmlistingArray.clear(); //clear before adding
		
		for(int i = 0 ; i < name.size(); i ++)
		{
			if(TextUtils.isEmpty(name.get(i).toString()) == false)
			tmlistingArray.add(new TMListing());
		}
		for(int i = 0 ; i < tmlistingArray.size(); i ++)
		{
			TMListing listing = tmlistingArray.get(i);
			listing.displayprice = displayprice.get(i);
			listing.latitude = latitude.get(i);
			listing.listingid = listingid.get(i);
			listing.longitude = longitude.get(i);
			listing.picturehref = picturehref.get(i);
			listing.title = title.get(i);
			listing.area = area.get(i);
			listing.bathrooms = bedrooms.get(i);
			listing.bedrooms = bathrooms.get(i);
			listing.logo = logos.get(i);  
			listing.rv = rvs.get(i);
			listing.address = addresses.get(i);
			listing.district = districts.get(i);
		}

	}

	/** Called to get tag characters ( ex:- <name>AndroidPeople</name>
	 * -- to get AndroidPeople Character ) */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (currentElement) {
			currentValue = new String(ch, start, length);
			currentElement = false;
		}

	}

}