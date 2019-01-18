package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;

/**
 * 主面板块倒影
 * 
 * @author yangyp
 */
public class HomeIconReflectView extends IconReflectView {

	static final float ScaleXY = 1.04f;
	private ImageView imgOriginal;
	private TextView txtName;

	public HomeIconReflectView(Context context) {
		super(context);
	}

	public HomeIconReflectView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public View onInflateView(Context context) {
		return View.inflate(context, R.layout.icon_reflect_home, null);
	}

	@Override
	public void onViewCreated(Context context) {
		//setClipChildren(false);
		//setClipToPadding(false);
		imgOriginal = (ImageView) findViewById(R.id.imgOriginal);
		txtName = (TextView) findViewById(R.id.txtName);
	}

	@Override
	protected void initStyledAttributes(TypedArray a, int attr) {
		super.initStyledAttributes(a, attr);
		switch (attr) {
		case R.styleable.IconReflectView_iconTextBackground:
			Drawable iconTextBackground = a.getDrawable(R.styleable.IconReflectView_iconTextBackground);
			if (iconTextBackground != null) {
				txtName.setBackground(iconTextBackground);
			}
			break;
		case R.styleable.IconReflectView_iconTextDrawableLeft:
			Drawable iconTextDrawableLeft = a.getDrawable(R.styleable.IconReflectView_iconTextDrawableLeft);
			if (iconTextDrawableLeft != null) {
				txtName.setCompoundDrawablesWithIntrinsicBounds(iconTextDrawableLeft, null, null, null);
			}
			break;
		}
	}

	@Override
	public void setIconText(String name) {
		super.setIconText(name);
		txtName.setText(name);
	}

	@Override
	public void setIconImageBitmap(Bitmap bm) {
		super.setIconImageBitmap(bm);
		imgOriginal.setImageBitmap(bm);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			// startAnimator();
		} else {
			ViewPropertyAnimator animator = imgOriginal.animate();
			animator.scaleX(1.0f);
			animator.scaleY(1.0f);
			animator.translationY(0);
			animator.setDuration(50);
			animator.start();
		}
	}

	/**
	 * 启动动画
	 */
	public void startViewAnimate() {
		if (hasFocus()) {
			float translationY = imgOriginal.getHeight() * ((ScaleXY - 1) / 2);
			ViewPropertyAnimator animator = imgOriginal.animate();
			animator.scaleX(ScaleXY);
			animator.scaleY(ScaleXY);
			animator.translationY(-translationY);
			animator.setDuration(250);
			animator.start();
		}
	}
}
