package nz.co.salespartner.Objects;

import android.graphics.Bitmap;

public class TMListing implements Comparable<Object>{

	public String title; 
	public String displayprice; 
	public String picturehref;
	public String listingid;
	public String latitude; 
	public String longitude;
	public String area;
	public String bathrooms;
	public String bedrooms;
	public String logo; 
	public String rv;
	public String address;
	public String district;
	public String agentsName; 
	public Bitmap thumbnail; 

	public TMListing(){

	}


	public int Compare(Object lhs, Object rhs)
	{
		TMListing lhs_TML = (TMListing) lhs;
		TMListing rhs_TML = (TMListing) rhs;
		int lhs_TML_rv = 0;
		int rhs_TML_rv = 0;
		try{
			lhs_TML_rv = Integer.parseInt(lhs_TML.rv);
			rhs_TML_rv = Integer.parseInt(rhs_TML.rv);
		}
		catch (Exception e) {
			
		}
		
		
		if (lhs_TML_rv < rhs_TML_rv) {
			return -1;
		} else if (lhs_TML_rv  > rhs_TML_rv) {
			return 1;
		} else {
			return 0;
		}
	}

	public int compareTo(Object another) {
		return Compare(this, another);
	}


}

