package nz.co.salespartner.Activities;

import java.io.File;
import java.util.List;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.PhotoManager;
import nz.co.salespartner.R.id;
import nz.co.salespartner.R.layout;
import nz.co.salespartner.R.styleable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

public class PhotoGallery extends Activity {
	List<File> photos;
	private static String TAG = "GalleryTest";
	private ImageView bigPhoto;
	private Gallery gallery;
	private String selectedFileName;
	private String ref;
	private int gal_pos = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		/*dither gradients*/
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        setContentView(R.layout.photo_gallery);
        ref = getIntent().getExtras().getString("ref");
        selectedFileName = "";
        photos = PhotoManager.getAllFiles(ref); 
        
        gallery = (Gallery)findViewById(R.id.gallery_Gallery);
        gallery.setAdapter(new AddImgAdp(this));
        gallery.setOnItemClickListener(new OnItemClickListener() {
        	 public void onItemClick(AdapterView parent, View v, int position, long id) {
        		selectPhotoInList(position);
        		gal_pos = position;
        	 }
		});       
        
        bigPhoto = (ImageView)findViewById(R.id.gallery_photo);
        bigPhoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPhoto();
			}
		});
        bigPhoto.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				startGallery();
				return true;
			}
        });
        if(photos.size() > 0)
        selectPhotoInList(0);
    }

       
    protected void showPhoto() {
		if (selectedFileName != "") {
			Intent mIntent = new Intent(this, ViewPhoto.class);
			mIntent.putExtra("fileName", selectedFileName);
			mIntent.putExtra("ref", ref);
			mIntent.putExtra("pos", gal_pos);
			startActivity(mIntent);
		}			
	}
    
    private void startGallery(){ 
    	if (selectedFileName != ""){
    	Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.parse("file://" + selectedFileName), "image/*");
    	startActivity(intent);
    	}
    }

	public class AddImgAdp extends BaseAdapter {
        int GalItemBg;
        private Context cont;

        public AddImgAdp(Context c) {
            cont = c;
            TypedArray typArray = obtainStyledAttributes(R.styleable.GalleryTheme);
            GalItemBg = typArray.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);
            typArray.recycle();
        }

        public int getCount() {
            return photos.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	ImageView imgView;
        	if (convertView == null)
        		imgView = new ImageView(cont);
        	else
        		imgView = (ImageView)convertView;
        	
            String filename = photos.get(position).getAbsolutePath();
            Log.i(TAG, "Loading File " + filename);
            Bitmap bMap = BitmapFactory.decodeFile(filename);
            imgView.setImageBitmap(bMap);
            
            imgView.setLayoutParams(new Gallery.LayoutParams(120, 90));
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            imgView.setBackgroundResource(GalItemBg);
            

            return imgView;
        }
    }

	private void selectPhotoInList(int position) {

		selectedFileName = photos.get(position).getAbsolutePath();
		Bitmap bMap = BitmapFactory.decodeFile(selectedFileName);
        bigPhoto.setImageBitmap(bMap);
		
	}
}