package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;

public class AppIconReflectView extends IconReflectView {

	private ImageView imgOriginal;
	private TextView txtName;

	public AppIconReflectView(Context context) {
		super(context);
	}

	public AppIconReflectView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public View onInflateView(Context context) {
		return View.inflate(context, R.layout.icon_reflect_app, null);
	}

	@Override
	public void onViewCreated(Context context) {
		imgOriginal = (ImageView) findViewById(R.id.imgOriginal);
		txtName = (TextView) findViewById(R.id.txtName);
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
		if (imgOriginal != null) {
			imgOriginal.setEnabled(gainFocus);
		}
	}

}
