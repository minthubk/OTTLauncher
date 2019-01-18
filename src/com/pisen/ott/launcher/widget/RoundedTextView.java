package com.pisen.ott.launcher.widget;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.RoundedDrawable.Corner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

/**
 * 
 * 
 * @author hegang
 * @version 1.0,2015年5月21日 下午1:28:56
 */
public class RoundedTextView extends TextView {
	public static final float DEFAULT_RADIUS = 0f;
	private Drawable mBackgroundDrawable;
	private final float[] mCornerRadii = new float[] { DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS };

	public RoundedTextView(Context context) {
		super(context);
	}

	public RoundedTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);
		float cornerRadiusOverride = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius, -1);

		mCornerRadii[Corner.TOP_LEFT.ordinal()] = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_top_left, -1);
		mCornerRadii[Corner.TOP_RIGHT.ordinal()] = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_top_right, -1);
		mCornerRadii[Corner.BOTTOM_RIGHT.ordinal()] = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_bottom_right, -1);
		mCornerRadii[Corner.BOTTOM_LEFT.ordinal()] = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_bottom_left, -1);
		boolean any = false;
		for (int i = 0, len = mCornerRadii.length; i < len; i++) {
			if (mCornerRadii[i] < 0) {
				mCornerRadii[i] = 0f;
			} else {
				any = true;
			}
		}

		if (!any) {
			if (cornerRadiusOverride < 0) {
				cornerRadiusOverride = DEFAULT_RADIUS;
			}
			for (int i = 0, len = mCornerRadii.length; i < len; i++) {
				mCornerRadii[i] = cornerRadiusOverride;
			}
		}
		updateBackgroundDrawableAttrs(true);
	}

	@Override
	public void setBackground(Drawable background) {
		setBackgroundDrawable(background);
	}

	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		mBackgroundDrawable = background;
		updateBackgroundDrawableAttrs(true);
		super.setBackgroundDrawable(mBackgroundDrawable);
	}

	private void updateBackgroundDrawableAttrs(boolean convert) {
		// if (mMutateBackground) {
		if (convert) {
			mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable);
		}
		updateAttrs(mBackgroundDrawable);
		// }
	}

	private void updateAttrs(Drawable drawable) {
		if (drawable == null) {
			return;
		}
		if (drawable instanceof RoundedDrawable) {
			if (mCornerRadii != null) {
				((RoundedDrawable) drawable).setCornerRadius(mCornerRadii[0], mCornerRadii[1], mCornerRadii[2], mCornerRadii[3]);
			}

		} else if (drawable instanceof LayerDrawable) {
			// loop through layers to and set drawable attrs
			LayerDrawable ld = ((LayerDrawable) drawable);
			for (int i = 0, layers = ld.getNumberOfLayers(); i < layers; i++) {
				updateAttrs(ld.getDrawable(i));
			}
		}
	}
	
}
