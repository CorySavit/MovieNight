package edu.pitt.cs1635.movienight;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
 * Custom ImageView that calculates the exact size of our posters
 */
public class PosterImageView extends ImageView {

	public PosterImageView(Context context) {
		super(context);
	}

	public PosterImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PosterImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		
		// the posters are 500px x 750px or 2:3 ratio
        setMeasuredDimension(width, (int) (width * 3.0 / 2));
	}

}
