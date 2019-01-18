package com.pisen.ott.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;

/**
 * 支持按键焦点浮层切换
 * 
 * @author yangyp
 * @version 1.0, 2014年12月16日 下午4:38:55
 */
public class GridFocusLayout extends GridLayout implements OnItemFocusChangeListener{

	private DefaultKeyFocus mKeyFocus;
	private View mCurrentView;

	public GridFocusLayout(Context context) {
		super(context);
		initViews(context);
	}

	public GridFocusLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public GridFocusLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	public void setCurrentView(View view, boolean isDetailPage) {
		this.mCurrentView = view;
	}

	protected void initViews(Context context) {
		mKeyFocus = new DefaultKeyFocus(this);
		mKeyFocus.setFocusImageResource(R.drawable.home_focus, R.dimen.banner_category_focus_border);
		mKeyFocus.setOnItemFocusChangeListener(this);
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mKeyFocus.setOnItemClickListener(l);
	}

	public void setOnFocusChangedListener(DefaultKeyFocus.OnItemFocusChangeListener f) {
		mKeyFocus.setOnItemFocusChangeListener(f);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mKeyFocus.layout(mCurrentView);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mKeyFocus.draw(canvas);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			if (mCurrentView != null) {
				mCurrentView.requestFocus();
			}
		}
	}
	
	@Override
	public void onItemFocusChanged(View v, boolean hasFocus) {
		Drawable[] maps = ((TextView) v).getCompoundDrawables();
		for (int i = 0; i < maps.length; i++) {
			if (maps[i] != null) {
				Drawable drawable = maps[i];
				if (hasFocus) {
					drawable.setAlpha(255);
				}else{
					drawable.setAlpha(100);
				}
				((TextView) v).setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			}
		}
	}

}
