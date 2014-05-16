package nz.co.salespartner.Activities;

import java.io.File;
import java.util.List;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.PhotoManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class ViewPhoto extends Activity {
	private ImageView photo;
	private static String TAG = "ViewPhoto";
	private ViewFlipper vf;
	private String ref;
	private Context context;
	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;
	private int pos;
	private Boolean scaleupPhoto;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //No "Pocket-SalesPartner along the top
		ref = getIntent().getStringExtra("ref");
		pos = getIntent().getIntExtra("pos", 0);
		setContentView(R.layout.view_photo);
		context = this;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		scaleupPhoto = prefs.getBoolean("scaleupPhoto", true);
		
		String fileName = getIntent().getExtras().getString("fileName");
		vf = (ViewFlipper)findViewById(R.id.view_photo_ViewFlipper);
		initViewFlipper();
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		
		vf.setDisplayedChild(pos); // set vf to passed image position

	}

	private void loadImage(String fileName) {
		Log.i(TAG, "Viewing Photo: " + fileName);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Boolean isPortraitScreen = metrics.widthPixels < metrics.heightPixels;

		Bitmap bMap = BitmapFactory.decodeFile(fileName);
		Boolean isPortraitImage = bMap.getWidth() < bMap.getHeight();

		//we need to rotate the image if image shape is different from screen shape

		if (isPortraitImage ^ isPortraitScreen) { //xor - exclusive or - if both different
			//rotate
			Matrix mat = new Matrix();
			mat.postRotate(90);
			bMap = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), mat, true);
		}

		photo.setImageBitmap(bMap);				
	}

	private void initViewFlipper() {

		List<File> photos;
		photos = PhotoManager.getAllFiles(ref); 
		// get screen dimens
		Display display = getWindowManager().getDefaultDisplay(); 
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();
		

		for(int i = 0; i < photos.size(); i ++)
		{
			String filename = photos.get(i).getAbsolutePath();
			Log.i(TAG, "Loading File " + filename);
			ImageView imgView = new ImageView(this);
			Bitmap bMap = BitmapFactory.decodeFile(filename);
			/*scale image to fit screensize*/
			if(scaleupPhoto)
			{
				int imageH = bMap.getHeight();
				int imageW = bMap.getWidth();
				double scale = (double)imageW / (double)imageH;
				int newH;
				int newW;
	
				if(imageW >= imageH )
				{
					newH = screenHeight;
					newW = (int)(screenHeight * scale);
				}
				else
				{
					newW = screenWidth;
					newH = (int)(screenWidth * scale);
				}
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(bMap, newW, newH, false);
				imgView.setImageBitmap(scaledBitmap);
			}
			else
				imgView.setImageBitmap(bMap);
			
			LinearLayout wrapper = new LinearLayout(this);
			wrapper.addView(imgView);
			vf.addView(wrapper);

		}
	}
	private float lastX = 0;
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	/* ------------------ VIEW FLIPPER ------------------ */
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					vf.setInAnimation(context, R.anim.in_from_right);
					vf.setOutAnimation(context, R.anim.out_to_left);
					vf.showNext();
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					vf.setInAnimation(context, R.anim.in_from_left);
					vf.setOutAnimation(context, R.anim.out_to_right);
					vf.showPrevious();
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}
}

/*@Override
	public boolean onTouchEvent(MotionEvent touchevent) {
		switch (touchevent.getAction())
		{
		case MotionEvent.ACTION_DOWN:
		{
			Log.i("","Action Down");

			float currentX = touchevent.getX();
			if (lastX < currentX)
			{
////				if (vf.getDisplayedChild()==0) break;
				vf.setInAnimation(this, R.anim.in_from_left);
				vf.setOutAnimation(this, R.anim.out_to_right);
				vf.showNext();
			}
			if (lastX > currentX)
			{
////				if (vf.getDisplayedChild()==1) break;
				vf.setInAnimation(this, R.anim.in_from_right);
				vf.setOutAnimation(this, R.anim.out_to_left);
				vf.showPrevious();
			}
			lastX = currentX;
			break;
		}
		case MotionEvent.ACTION_UP:
		{
			Log.i("","Action Up");
			float currentX = touchevent.getX();
			if (lastX < currentX)
			{
//				if (vf.getDisplayedChild()==4) break;
				vf.setInAnimation(this, R.anim.in_from_left);
				vf.setOutAnimation(this, R.anim.out_to_right);
				vf.showNext();
			}
			if (lastX > currentX)
			{
//				if (vf.getDisplayedChild()==0) break;
				vf.setInAnimation(this, R.anim.in_from_right);
				vf.setOutAnimation(this, R.anim.out_to_left);
				vf.showPrevious();
			}
			break;
		}
		}
		return true;
	}*/



