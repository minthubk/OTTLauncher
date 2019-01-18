package com.pisen.ott.launcher.search;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridLayout;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.IDetailContent;
import com.pisen.ott.launcher.widget.IMasterTitle;

/**
 * 支持按键焦点浮层切换
 * 
 * @author yangyp
 * @version 1.0, 2014年12月16日 下午4:38:55
 */
public class GridFocusLayout extends GridLayout implements IMasterTitle {

	private DefaultKeyFocus mKeyFocus;
	private View lasFocusedView;
	private IDetailContent gridView;

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
		this.lasFocusedView = view;
	}

	protected void initViews(Context context) {
		mKeyFocus = new DefaultKeyFocus(this);
		mKeyFocus.setFocusImageResource(R.drawable.search_num_bg, R.dimen.banner_focus_border_menu);
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mKeyFocus.setOnItemClickListener(l);
	}

	public void setOnItemFocusChangeListener(OnItemFocusChangeListener l) {
		mKeyFocus.setOnItemFocusChangeListener(l);
	}

	@Override
	public void setDetailContent(IDetailContent gridScaleView) {
		this.gridView = gridScaleView;
	}

	public void setChildFocusedView(View view) {
		if (lasFocusedView != view) {
			view.requestFocus();
			lasFocusedView = view;
		}
	}

	public boolean hasNewChildFocus() {
		return lasFocusedView != getFocusedChild();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mKeyFocus.layout(lasFocusedView);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mKeyFocus.draw(canvas);
		super.dispatchDraw(canvas);
	}

	@Override
	public void requestChildFocus() {
		if (lasFocusedView != null) {
			lasFocusedView.requestFocus();
		}
	}

	public View getLasFocusedView() {
		return lasFocusedView;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean result = super.dispatchKeyEvent(event);
		lasFocusedView = getFocusedChild();

		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() != KeyEvent.ACTION_UP) {
			View currentFocused = findFocus();
			if (currentFocused == this) {
				currentFocused = null;
			}

			View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, View.FOCUS_RIGHT);
			if (nextFocused == null) {
				if (gridView != null && gridView.hasData()) {
					gridView.requestChildFocus();
					return true;
				}
			}
		}

		return result;
	}
}
