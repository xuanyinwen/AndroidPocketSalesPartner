package nz.co.salespartner.Activities;

import nz.co.salespartner.R;
import nz.co.salespartner.R.id;
import nz.co.salespartner.R.layout;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity{
	private Context context;
	private String[] grid_titles = {"Listings","Vendors","Documents","Help"};
	private int[] grid_icons = 
			{android.R.drawable.ic_menu_agenda,
			android.R.drawable.ic_menu_view,
			android.R.drawable.ic_menu_edit,
			android.R.drawable.ic_menu_help};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;
		initGrid();
	}

	private void initGrid() {
		final GridView grid_main = (GridView)findViewById(R.id.homeGrid);
		grid_main.setAdapter(new ImageAdapter(this));
		final ImageAdapter grid = new ImageAdapter(context);

		grid_main.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				grid .getCount();
				grid .getItem(position);
				Toast.makeText(context,""+grid_main.getCount() , 4000).show();

			}
		});


	}
	
	 public class ImageAdapter extends BaseAdapter{
	        Context mContext;
	        public static final int ACTIVITY_CREATE = 10;
	        public ImageAdapter(Context c){
	            mContext = c;
	        }
	        public int getCount() {
	            // TODO Auto-generated method stub
	            return 4;
	        }

	        public View getView(int position, View convertView, ViewGroup parent) {
	            // TODO Auto-generated method stub
	            View v;
	            TextView tv;
	            if(convertView==null){
	                LayoutInflater li = getLayoutInflater();
	                v = li.inflate(R.layout.grid_item, null);
	                tv = (TextView)v.findViewById(R.id.grid_item_text);
	                tv.setText(grid_titles[position]);
	                ImageView iv = (ImageView)v.findViewById(R.id.grid_item_image);
	                iv.setImageResource(grid_icons[position]);
	                v.setOnClickListener(new OnClickListener() {
						
						public void onClick(View arg0) {
							startActivity(new Intent(context, Listings.class));
						}
					});

	            }
	            else
	            {
	                v = convertView;
	            }
	            return v;
	        }
	        public Object getItem(int position) {
	            return position;
	        }

	        public long getItemId(int position) {
	            return position;
	        }
	    }
}

