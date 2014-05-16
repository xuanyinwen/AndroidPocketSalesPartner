package nz.co.salespartner.Objects;

import nz.co.salespartner.R;
import nz.co.salespartner.Helpers.StyleFactory;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class CustomSpinner extends Spinner{


    private Context _context;
	private StyleFactory stylefactory;
	private View v;

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        _context = context;
        stylefactory = new StyleFactory(_context);
    }

    public CustomSpinner(Context context) {
        super(context);
        _context = context;
    }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context);
        _context = context;
    }

    @Override
    public View getChildAt(int index) {
    	
        return new View(_context);
    }
    
    public View getV() {
    	return new View(_context);
	}
      

  

}
