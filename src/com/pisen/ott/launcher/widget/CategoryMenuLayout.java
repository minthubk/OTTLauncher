package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;

/**
 * 支持按键焦点浮层切换
 * 
 * @author yangyp
 * @version 1.0, 2014年12月16日 下午4:38:55
 */
public class CategoryMenuLayout extends LinearLayout implements android.view.View.OnFocusChangeListener,IMasterTitle {

	private DefaultKeyFocus mKeyFocus;
	private View lasFocusedView;
	private IDetailContent gridView;
	private boolean hasFocus = true;
	
	public CategoryMenuLayout(Context context) {
		super(context);
		initViews(context);
	}

	public CategoryMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public CategoryMenuLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	protected void initViews(Context context) {
		setOnFocusChangeListener(this);
		mKeyFocus = new DefaultKeyFocus(this);
		mKeyFocus.setKeepFocus(true);
//		mKeyFocus.setFocusImageResource(R.drawable.three_level_choice_highlight, R.dimen.space_line);
		mKeyFocus.setFocusImageResource(R.drawable.three_level_choice_highlight, R.dimen.banner_focus_border_menu);
		
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

	/*public void setOnItemClickListener(OnItemClickListener l) {
		mKeyFocus.setOnItemClickListener(l);
	}*/

	public void setOnItemFocusChangeListener(OnItemFocusChangeListener l) {
		mKeyFocus.setOnItemFocusChangeListener(l);
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
		if (hasFocus()) {
			if (lasFocusedView instanceof Button) {
				((Button) lasFocusedView).setTextColor(getContext().getResources().getColor(R.color.item_category_text));
			}
			lasFocusedView = getFocusedChild();
		}
		if (lasFocusedView instanceof Button) {
			if (!hasFocus) {
				((Button) lasFocusedView).setTextColor(getContext().getResources().getColor(R.color.whitelight));
			}else{
				((Button) lasFocusedView).setTextColor(Color.WHITE);
			}
		}
	}

	@Override
	public void requestChildFocus() {
		if (lasFocusedView != null) {
			hasFocus = true;
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
				if (gridView.hasData()) {
					gridView.requestChildFocus();
					hasFocus = false;
					return true;
				}
			}
		}

		return result;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			if (v instanceof Button) {
				((Button) v).setTextColor(getContext().getResources().getColor(R.color.item_category_text));
			}
		}
	}
}
