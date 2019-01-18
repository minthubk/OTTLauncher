package com.pisen.ott.launcher.localplayer.music;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.IDetailContent;
import com.pisen.ott.launcher.widget.IMasterTitle;

/**
 * 支持按键焦点浮层切换(透明)
 * @author Liuhc
 * @version 1.0 2015年3月6日 下午3:58:18
 */
public class MusicContorlLayout extends LinearLayout implements IMasterTitle {

	private DefaultKeyFocus mKeyFocus;
	private View lasFocusedView;
	private IDetailContent listView;

	public MusicContorlLayout(Context context) {
		super(context);
		initViews(context);
	}

	public MusicContorlLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public MusicContorlLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	protected void initViews(Context context) {
		mKeyFocus = new DefaultKeyFocus(this);
		if (isInEditMode()) { 
			return; 
		}
		mKeyFocus.setFocusImageResource(R.color.full_transparent, R.dimen.banner_category_focus_border);
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mKeyFocus.setOnItemClickListener(l);
	}

	public void setOnItemFocusChangeListener(OnItemFocusChangeListener l) {
		mKeyFocus.setOnItemFocusChangeListener(l);
	}

	@Override
	public void setDetailContent(IDetailContent gridScaleView) {
		this.listView = gridScaleView;
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
		if (isInEditMode()) {
			mKeyFocus.draw(canvas);
		}
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

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT ) {
				View currentFocused = findFocus();
				if (currentFocused == this) {
					currentFocused = null;
				}
				View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, View.FOCUS_LEFT);
				if (nextFocused == null && listView != null) {
					if (listView.hasData() && ((View)listView).isShown()) {
						listView.requestChildFocus();
						return true;
					}
				}
			}
		}
		return result;
	}

}
