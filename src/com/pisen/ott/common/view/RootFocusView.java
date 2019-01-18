package com.pisen.ott.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.izy.widget.FocusScroller;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.pisen.ott.launcher.R;

/**
 * 支持按键焦点浮层切换
 * 
 * @author yangyp
 * @version 1.0, 2014年12月16日 下午4:39:18
 */
public class RootFocusView extends LinearLayout {

	private FocusScroller mScroller;
	private Drawable mDrawable;
	private static int FocusBorder = 18;
	protected Rect currentRect; // 当前焦点位置

	public RootFocusView(Context context) {
		super(context);
		initViews(context);
	}

	public RootFocusView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public RootFocusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	protected void initViews(Context context) {
		setWillNotDraw(false);
		// setClipToOutline(true);
		setClipChildren(false);
		setClipToPadding(false);
		setChildrenDrawingOrderEnabled(true);
		mScroller = new FocusScroller(context);
		mDrawable = getResources().getDrawable(R.drawable.home_focus);
		currentRect = new Rect();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean result = super.dispatchKeyEvent(event);
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			lastAwayView = findFocus();
			if (lastAwayView instanceof AbsListView) {
				AbsListView absListView = (AbsListView) lastAwayView;
				lastAwayView = absListView.getSelectedView();
			}

			invalidateUi(lastAwayView);
		}

		return result;
	}

	private void invalidateUi(View v) {
		if (hasFocus()) {
			Rect outRect = new Rect();
			v.getGlobalVisibleRect(outRect);
			if (currentRect.isEmpty()) {
				currentRect.set(outRect);
			}

			mScroller.abortAnimation();
			if (!currentRect.equals(outRect)) {
				mScroller.startScroll(currentRect, outRect);
			}
		}
		invalidate();
	}

	private View lastAwayView;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setDefaultFocusedChild(this);
	}

	/**
	 * 设置默认显示的焦点
	 */
	protected void setDefaultFocusedChild(View view) {
		if (view instanceof ViewGroup) {
			ViewGroup layout = (ViewGroup) view;
			for (int i = 0, N = layout.getChildCount(); i < N; i++) {
				View child = layout.getChildAt(i);
				setDefaultFocusedChild(child);
			}
		} else {
			if (view.isFocusable()) {
				if (currentRect.isEmpty()) {
					view.getGlobalVisibleRect(currentRect);
					view.requestFocus();
				}
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (hasFocus()) {
			// 判断开始矩形是否已经到达目标位置
			if (mScroller.computeScrollOffset()) {
				Rect currRect = mScroller.getCurrRect();
				currentRect.set(currRect);
				invalidate();
			}

			canvas.save();
			mDrawable.setBounds(currentRect.left - FocusBorder, currentRect.top - FocusBorder, currentRect.right + FocusBorder, currentRect.bottom
					+ FocusBorder);
			mDrawable.draw(canvas);
			canvas.restore();
		}
	}

}
