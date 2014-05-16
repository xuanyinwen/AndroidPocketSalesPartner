package nz.co.salespartner.Activities;

import nz.co.salespartner.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


public class FullScreenPhoto extends Activity implements OnTouchListener{

	private ImageView imageview;
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();


	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //No "Pocket-SalesPartner along the top
		setContentView(R.layout.full_screen_photo);
		imageview = (ImageView)findViewById(R.id.fsp_img);
		getImage();
	}

	private void getImage() {
		String name = getIntent().getStringExtra("imagename");
		String absolutepath = getIntent().getStringExtra("absolutepath");
		String path = absolutepath + "/" + name;

		Bitmap photo = BitmapFactory.decodeFile(path);
		if(photo!=null)
		{
			imageview.setImageBitmap(photo);
			imageview.setOnTouchListener(this);
		}
		else
		{}
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/** handle pinch to zoom and drag*/
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d("tag", "oldDist=" + oldDist);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d("tag", "mode=ZOOM");
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			Log.d("tag", "mode=NONE");
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				// ...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x,
						event.getY() - start.y);
			}
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d("tag", "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}
}
