package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.OverScroller;

import com.pisen.ott.launcher.R;

/**
 * 支持按键焦点浮层切换
 * 
 * @author yangyp
 * @version 1.0, 2014年12月16日 下午4:37:45
 */
public class FListView extends ListView implements OnItemSelectedListener {

	private OverScroller mScroller;
	private Rect mCurrRect;
	private Drawable mDrawable;
	private static final int FocusBorder = 18;

	public FListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		setClipToPadding(false);
		setVerticalFadingEdgeEnabled(false);
		setOnItemSelectedListener(this);

		mScroller = new OverScroller(context, new DecelerateInterpolator());
		mCurrRect = new Rect();
		mDrawable = getResources().getDrawable(R.drawable.msg_item_bg_focus);
	}

	/**
	 * 通过此方法设置焦点背景图片
	 * 
	 * @param resourceId
	 */
	public void setFocusBitmap(int resourceId) {
		mDrawable = getResources().getDrawable(resourceId);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (getChildCount() > 0) {
			View child = getChildAt(0);
			if (mCurrRect.isEmpty()) {
				child.getHitRect(mCurrRect);
				child.requestFocus();
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (hasFocus()) {
			if (mScroller.computeScrollOffset()) {
				int currY = mScroller.getCurrY();
				mCurrRect.offsetTo(mCurrRect.left, currY);
				invalidate();
			}
			canvas.save();
			mDrawable.setBounds(mCurrRect.left - FocusBorder, mCurrRect.top - FocusBorder, mCurrRect.right + FocusBorder, mCurrRect.bottom + FocusBorder);
			mDrawable.draw(canvas);
			canvas.restore();
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		setSelection(0);
	}

	// ListView的item数量实际上是动态改变的，会在一个数值x和x+1甚至x+2之间徘徊，所以利用item的数量来计算焦点的移动是不行的，所以增加的实现此功能的复杂度
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = super.onKeyDown(keyCode, event);
		if (hasFocus()) {
			View v = getSelectedView();
			if (v != null) {
				Rect outRect = new Rect();
				v.getHitRect(outRect);
				if (mCurrRect.isEmpty()) {
					v.getHitRect(mCurrRect);
				}

				int dy = outRect.top - mCurrRect.top;
				mScroller.startScroll(0, mCurrRect.top, 0, dy);
			}
			invalidate();
		}
		return result;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

}
