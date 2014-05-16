package nz.co.salespartner.Helpers;

import nz.co.salespartner.R;
import nz.co.salespartner.Activities.Landing;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
/**
 * 
 * Class for styling views with custom fonts
 *
 */
public class StyleFactory {

	private Typeface font;
	private Typeface fontbold;
	private TextView tv;
	private RadioButton rb;
	private Button b;
	private Typeface title;
	private Context context;

	public StyleFactory(Context _context){
		context = _context;
		font = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
		fontbold = Typeface.createFromAsset( context.getAssets(), "Roboto-Bold.ttf");
		title = Typeface.createFromAsset( context.getAssets(), "ClementeBold.ttf");
	}

	public void initHeader(final Activity activity){
		/* home button */
		Button home = (Button)activity.findViewById(R.id.go_home_button);
		home.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent homeintent = new Intent(context, Landing.class);
				activity.startActivity(homeintent);
			}
		});

		/* style titles */
		TextView title = (TextView)activity.findViewById(R.id.hi_title_1);
		this.applyTitle(title);
		title = (TextView)activity.findViewById(R.id.hi_title_2);
		this.applyTitle(title);

	}


	public void applyFont(View v){
		apply(font, v);
	}

	public void applyBold(View v){

		apply(fontbold, v);
	}

	private void apply(Typeface font, View view){
		if(view instanceof TextView)
		{
			tv = (TextView)view;
			tv.setTypeface(font);
		}
		if(view instanceof RadioButton)
		{
			rb = (RadioButton)view;
			rb.setTypeface(font);
		}
		if(view instanceof Button)
		{
			b = (Button)view;
			b.setTypeface(font);
		}

	}

	public void applyFont(TextView[] tvs) {
		for(TextView tv : tvs)
			applyFont(tv);
	}

	public void applyFont(Button[] tvs) {
		for(TextView tv : tvs)
			applyFont(tv);
	}

	public void applyBold(TextView[] tvs) {
		for(TextView tv : tvs)
			applyBold(tv);

	}

	public void applyTitle(TextView current) {
		current.setTypeface(title);
	}

	public void applyFont(int[] tvs, Activity c) {
		View v = new View(c);
		for(int i : tvs)
		{
			v= (TextView)c.findViewById(i);
			applyFont(v);
		}

	}

	public void applyBold(int[] tvs, Activity c) {
		View v = new View(c);
		for(int i : tvs)
		{
			v= (TextView)c.findViewById(i);
			applyBold(v);
		}

	}

	public void setOnFocusChanged(EditText[] edittexts){
		for(final EditText e : edittexts){
			e.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus) 
				{
					if (hasFocus==true)
					{
						e.setText("");
					}
				}
			});
		}
	}


}
