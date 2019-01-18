package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.izy.widget.FocusScroller;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;

/**
 * 主界面块焦点切换动画
 * 
 * @author yangyp
 * @version 1.0, 2015年1月5日 下午2:34:14
 */
public class HomeScrollView extends HomeHScrollView implements OnFocusChangeListener {

	private FocusScroller mScroller;
	private Drawable mDrawable;
	private static int FocusBorder = 18;
	protected Rect currentRect; // 当前焦点位置

	public HomeScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public HomeScrollView(Context context) {
		super(context);
		initViews(context);
	}

	protected void initViews(Context context) {
		setFocusable(false);
		setWillNotDraw(false);
		// setClipToOutline(true);
		setClipChildren(false);
		setClipToPadding(false);
		setChildrenDrawingOrderEnabled(true);
		setHorizontalScrollBarEnabled(false);
		mScroller = new FocusScroller(context, new LinearInterpolator());
		mDrawable = getResources().getDrawable(R.drawable.home_focus);
		FocusBorder = getResources().getDimensionPixelSize(R.dimen.home_focus_border);
		currentRect = new Rect();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setDefaultFocusedChild();
	}

	/**
	 * 设置默认显示的焦点
	 */
	protected void setDefaultFocusedChild() {
		if (getChildCount() > 0) {
			final View childLayout = getChildAt(0);
			if (childLayout instanceof ViewGroup) {
				ViewGroup layout = (ViewGroup) childLayout;
				for (int i = 0, N = layout.getChildCount(); i < N; i++) {
					View child = layout.getChildAt(i);
					child.setOnFocusChangeListener(this);

					if (currentRect.isEmpty()) {
						child.getHitRect(currentRect);
						child.requestFocus();
					}
				}
			}
		}
	}

	public Rect getCurrentRect() {
		return new Rect(currentRect);
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
			} else {
				View child = findFocus();
				if (child instanceof HomeIconReflectView) {
					HomeIconReflectView iconView = (HomeIconReflectView) child;
					iconView.startViewAnimate();
				}
			}

			if (!currentRect.isEmpty()) {
				mDrawable.setBounds(currentRect.left - FocusBorder, currentRect.top - FocusBorder, currentRect.right + FocusBorder, currentRect.bottom
						+ FocusBorder);
				mDrawable.draw(canvas);
			}
		}
	}

	private OnItemFocusChangeListener mFocusChangeListener;

	public void setOnItemFocusChangeListener(OnItemFocusChangeListener l) {
		this.mFocusChangeListener = l;
	}

	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		if (getChildCount() == 0)
			return 0;

		int width = getWidth();
		int screenLeft = getScrollX();

		int fadingEdge = getHorizontalFadingEdgeLength();

		// leave room for left fading edge as long as rect isn't at very left
		if (rect.left > 0) {
			screenLeft += fadingEdge;
		}

		int scrollXDelta = 0;

		int screenMiddle = screenLeft + width / 2;
		int nextFocusedMiddle = rect.left + rect.width() / 2;

		if (nextFocusedMiddle > screenMiddle) {
			scrollXDelta += (nextFocusedMiddle - screenMiddle);
		} else if (nextFocusedMiddle < screenMiddle) {
			scrollXDelta -= (screenMiddle - nextFocusedMiddle);
		}

		// doScrollAnim(rect);

		return scrollXDelta;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (mFocusChangeListener != null) {
			mFocusChangeListener.onItemFocusChanged(v, hasFocus);
		}

		if (hasFocus) {
			/*
			 * if (!mScroller.isFinished()) { mScroller.abortAnimation();
			 * //currentRect.set(mScroller.getCurrRect()); }
			 */
			Rect outRect = new Rect();
			v.getHitRect(outRect);
			if (currentRect.isEmpty()) {
				currentRect.set(outRect);
			}

			// if (!currentRect.equals(outRect)) {

			if (mScroller.isFinished()) {
				mScroller.startScroll(currentRect, outRect);
			} else {
				mScroller.appendScroll(outRect);
			}
			// invalidate();
			// }
		}
		invalidate();
	}
}
