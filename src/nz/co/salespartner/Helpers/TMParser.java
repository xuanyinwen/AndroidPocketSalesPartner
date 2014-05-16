package nz.co.salespartner.Helpers;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nz.co.salespartner.Objects.TMListing;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class TMParser {
	ArrayList<TMListing> tmlisting;
	
	public TMParser(){

	}

	public ArrayList<TMListing> parseForTM(final String url){
		tmlisting = new ArrayList<TMListing>();
		//		XMLHandler myXMLHandler = null;
		//		
		//		try{
		//			/** Handling XML */
		//			SAXParserFactory spf = SAXParserFactory.newInstance();
		//			SAXParser sp = spf.newSAXParser();
		//			XMLReader xr = sp.getXMLReader();
		//			
		//			/** Send URL to parse XML Tags */
		//			URL sourceUrl = new URL(url);
		//			
		//			/** Create handler to handle XML Tags ( extends DefaultHandler ) */
		//			myXMLHandler = new XMLHandler();
		//			xr.setContentHandler(myXMLHandler);
		//			xr.parse(new InputSource(sourceUrl.openStream()));
		//			myXMLHandler.update();
		//		} catch (Exception e) {
		//			//System.out.println("XML Pasing Excpetion at TMParser = " + e);
		//		}
		//		
		//		ArrayList<TMListing> tmlisting = myXMLHandler.tmlistingArray;
		//		
		//		

		try {

//			SAXParserFactory factory = SAXParserFactory.newInstance();
//			SAXParser saxParser = factory.newSAXParser();
			


			DefaultHandler handler = new DefaultHandler() {

				boolean listingid = false;
				boolean pricedisplay = false;
				boolean latitude = false;
				boolean longitude = false;
				private boolean title = false;
				private boolean picturehref = false;
				private boolean area = false;
				private boolean bedrooms = false;
				private boolean bathrooms = false;
				private boolean RateableValue = false;
				private boolean logo = false;
				private boolean District = false;
				private boolean Address = false;
				private boolean Agency = false;
				private boolean FullName = false;

				public void startElement(String uri, String localName,String qName, 
						Attributes attributes) throws SAXException {

					System.out.println("Start Element :" + qName);

					if (qName.equalsIgnoreCase("Property")) {
						tmlisting.add(new TMListing());
						System.out.println("url = "+url);
					}
					if (qName.equalsIgnoreCase("ListingId")) {
						listingid = true;
					}
					if (qName.equalsIgnoreCase("title")) {
						title = true;
					}
					if (qName.equalsIgnoreCase("pricedisplay")) {
						pricedisplay = true;
					}

					if (qName.equalsIgnoreCase("latitude")) {
						latitude = true;
					}
					if (qName.equalsIgnoreCase("longitude")) {
						longitude = true;
					}
					if (qName.equalsIgnoreCase("picturehref")) {
						picturehref = true;
					}
					if (qName.equalsIgnoreCase("area")) {
						area = true;
					}
					if (qName.equalsIgnoreCase("bedrooms")) {
						bedrooms = true;
					}
					if (qName.equalsIgnoreCase("bathrooms")) {
						bathrooms = true;
					}
					if (qName.equalsIgnoreCase("logo")) {
						logo = true;
					}
					if (qName.equalsIgnoreCase("RateableValue")) {
						RateableValue = true;
					}
					if (qName.equalsIgnoreCase("Address")) {
						if(Agency == false)
						{
							Address = true;
						}
					}
					if (qName.equalsIgnoreCase("Agency")) {
							Agency = true;
					}
					if (qName.equalsIgnoreCase("FullName")) {
						FullName = true;
					}
					if (qName.equalsIgnoreCase("District")) {
						District = true;
					}

				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					if(qName.equalsIgnoreCase("Agency"))
						Agency = false;
//					//System.out.println("End Element :" + qName);

				}

				public void characters(char ch[], int start, int length) throws SAXException {
					int last = tmlisting.size() - 1;
					if (listingid) {
						//System.out.println(" <ListingId> : " + new String(ch, start, length));
						tmlisting.get(last).listingid = new String(ch, start, length);
						listingid = false;
					}
					else if (Address) {
						//System.out.println(" <Address> : " + new String(ch, start, length));
						tmlisting.get(last).address = new String(ch, start, length);
						Address = false;
					}
					else if (title) {
						//System.out.println(" <Title>: " + new String(ch, start, length));
						tmlisting.get(last).title = new String(ch, start, length);
						title = false;
					}

					else if (pricedisplay) {
						//System.out.println(" <PriceDisplay> : " + new String(ch, start, length));
						tmlisting.get(last).displayprice = new String(ch, start, length);
						pricedisplay = false;
					}

					else if (latitude) {
						//System.out.println(" <latitude> : " + new String(ch, start, length));
						tmlisting.get(last).latitude = new String(ch, start, length);
						latitude = false;
					}

					else if (longitude) {
						//System.out.println(" <longitude> : " + new String(ch, start, length));
						tmlisting.get(last).longitude = new String(ch, start, length);
						longitude = false;
					}
					
					else if (picturehref) {
						//System.out.println(" <picturehref> : " + new String(ch, start, length));
						tmlisting.get(last).picturehref = new String(ch, start, length);
						picturehref = false;
					}
					
					else if (area) {
						//System.out.println(" <area> : " + new String(ch, start, length));
						tmlisting.get(last).area = new String(ch, start, length);
						area = false;
					}
					else if (bathrooms) {
						//System.out.println(" <bathrooms> : " + new String(ch, start, length));
						tmlisting.get(last).bathrooms = new String(ch, start, length);
						bathrooms = false;
					}
					else if (bedrooms) {
						//System.out.println(" <bedrooms> : " + new String(ch, start, length));
						tmlisting.get(last).bedrooms = new String(ch, start, length);
						bedrooms = false;
					}
					else if (logo) {
						//System.out.println(" <logo> : " + new String(ch, start, length));
						tmlisting.get(last).logo = new String(ch, start, length);
						logo = false;
					}
					else if (RateableValue) {
						//System.out.println(" <RateableValue> : " + new String(ch, start, length));
						tmlisting.get(last).rv = new String(ch, start, length);
						RateableValue = false;
					}
					else if (District) {
						//System.out.println(" <District> : " + new String(ch, start, length));
						tmlisting.get(last).district = new String(ch, start, length);
						District = false;
					}
					else if (FullName) {
						//System.out.println(" <FullName> : " + new String(ch, start, length));
						tmlisting.get(last).agentsName = new String(ch, start, length);
						FullName = false;
					}
					else if (Agency) {
					}

				}

			};
			System.out.println("\nTMPARSER url = "+url);
			URL sourceUrl = new URL(url);
			InputSource in = new InputSource(sourceUrl.openStream());
//			saxParser.parse(in, handler);
			
//			XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
//			xmlReader.setContentHandler(handler);
//			xmlReader.parse(in);
//			
			
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			xr.setContentHandler(handler);
			xr.parse(in);
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		return tmlisting;


	}


}
