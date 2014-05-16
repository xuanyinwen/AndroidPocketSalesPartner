package nz.co.salespartner.Activities;

import nz.co.salespartner.R;
import nz.co.salespartner.R.id;
import nz.co.salespartner.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class FilterActivity extends Activity {
	static final int DIALOG_MAXBEDS_ID = 1;
	static final int DIALOG_MINBEDS_ID = 2;
	static final int DIALOG_MAXPRICE_ID = 3;
	static final int DIALOG_MINPRICE_ID = 4;

	protected CheckBox byPrice;
	protected EditText priceMinEdit;
	protected EditText priceMaxEdit;
	protected Button priceMinButton;
	protected Button priceMaxButton;

	protected CheckBox byBedrooms;
	protected EditText bedsMinEdit;
	protected EditText bedsMaxEdit;
	protected Button bedsMinButton;
	protected Button bedsMaxButton;

	protected Button applyButton;
	protected Button cancelButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listing_filter);
		bindControls();
	}

	protected void bindControls() {
		byPrice = (CheckBox) findViewById(R.id.listing_filter_ByPrice);
		byPrice.setChecked(Listings.ListingsFilter.FilterByPrice);

		priceMinEdit = (EditText) findViewById(R.id.listing_filter_PriceMin);
		priceMinEdit.setText(Integer.toString(Listings.ListingsFilter.PriceMin / 1000));

		priceMaxEdit = (EditText) findViewById(R.id.listing_filter_PriceMax);
		priceMaxEdit.setText(Integer.toString(Listings.ListingsFilter.PriceMax / 1000));

		priceMinButton = (Button) findViewById(R.id.listing_filter_PriceMinButton);
		priceMinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_MINPRICE_ID);
			}
		});
		priceMaxButton = (Button) findViewById(R.id.listing_filter_PriceMaxButton);
		priceMaxButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_MAXPRICE_ID);
			}
		});

		byBedrooms = (CheckBox) findViewById(R.id.listing_filter_ByBeds);
		byBedrooms.setChecked(Listings.ListingsFilter.FilterByBedrooms);

		bedsMinEdit = (EditText) findViewById(R.id.listing_filter_BedsMin);
		bedsMinEdit.setText(Integer.toString(Listings.ListingsFilter.BedroomsMin));

		bedsMaxEdit = (EditText) findViewById(R.id.listing_filter_BedsMax);
		bedsMaxEdit.setText(Integer.toString(Listings.ListingsFilter.BedroomsMax));

		bedsMinButton = (Button) findViewById(R.id.listing_filter_BedsMinButton);
		bedsMinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_MINBEDS_ID);
			}
		});
		bedsMaxButton = (Button) findViewById(R.id.listing_filter_BedsMaxButton);
		bedsMaxButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_MAXBEDS_ID);
			}
		});

		applyButton = (Button) findViewById(R.id.listing_filter_ApplyButton);
		applyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				applyFilter();
			}
		});

		cancelButton = (Button) findViewById(R.id.listing_filter_CancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	protected void applyFilter() {
		Listings.ListingsFilter.FilterByPrice = true;
		Listings.ListingsFilter.PriceMin = Integer.parseInt(priceMinEdit.getText().toString());
		Listings.ListingsFilter.PriceMin = Listings.ListingsFilter.PriceMin *1000;
		Listings.ListingsFilter.PriceMax = Integer.parseInt(priceMaxEdit.getText().toString());
		Listings.ListingsFilter.PriceMax = Listings.ListingsFilter.PriceMax *1000;
		Listings.ListingsFilter.FilterByBedrooms = byBedrooms.isChecked();
		Listings.ListingsFilter.BedroomsMin = Integer.parseInt(bedsMinEdit.getText().toString());
		Listings.ListingsFilter.BedroomsMax = Integer.parseInt(bedsMaxEdit.getText().toString());

		setResult(RESULT_OK);
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_MINBEDS_ID:
			return showBedroomsDialog(id);
		case DIALOG_MAXBEDS_ID:
			return showBedroomsDialog(id);
		case DIALOG_MINPRICE_ID:
			return showPriceDialog(id);
		case DIALOG_MAXPRICE_ID:
			return showPriceDialog(id);
		default:
			return null;
		}
	}

	int lastBedroomsDialog;
	final CharSequence[] bedroomsOptions = { "0", "1", "2", "3", "4", "5", "99" };
	protected Dialog showBedroomsDialog(int id) {
		lastBedroomsDialog = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Number of Bedrooms");
		builder.setItems(bedroomsOptions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				byBedrooms.setChecked(true);
				if (lastBedroomsDialog == DIALOG_MINBEDS_ID)
					bedsMinEdit.setText(bedroomsOptions[item]);
				else
					bedsMaxEdit.setText(bedroomsOptions[item]);
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}
	
	int lastPriceDialog;
	final CharSequence[] priceOptionsDisplay = { "0",  "50k",   "100k",  "150k",   "200k", "250k", "300k", "400k", "500k", "600k", "800k", "1M", "1.5M", "2M" };
	final int[] priceOptionsValues =           {  0,    50,      100,     150,      200,    250,    300,    400,    500,    600,    800,    1000, 1500,  2000 };
	
	protected Dialog showPriceDialog(int id) {
		lastPriceDialog = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Price");
		builder.setItems(priceOptionsDisplay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				byPrice.setChecked(true);
				if (lastPriceDialog == DIALOG_MINPRICE_ID)
					priceMinEdit.setText(Integer.toString(priceOptionsValues[item]));
				else
					priceMaxEdit.setText(Integer.toString(priceOptionsValues[item]));
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}
}
