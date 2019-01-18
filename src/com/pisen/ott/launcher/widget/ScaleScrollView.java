package com.pisen.ott.launcher.widget;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.izy.widget.FocusScroller;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.View.OnFocusChangeListener;
import android.view.animation.LinearInterpolator;

public class ScaleScrollView extends HomeHScrollView implements OnFocusChangeListener{
	private float mScale = 1.04f;
	
	private View lastSelectedView;
	
	private FocusScroller mScroller;
	private Drawable mDrawable;
	private static int FocusBorder = 18;
	protected Rect currentRect; // 当前焦点位置
	private boolean requestDefaultFocus = false;
	public ScaleScrollView(Context context) {
		super(context);
		initViews(context,null);
	}
	
	public ScaleScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context, attrs);
	}

	@SuppressLint("Recycle")
	protected void initViews(Context context,AttributeSet attrs) {
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
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScaleFocusView);
			requestDefaultFocus = a.getBoolean(R.styleable.ScaleFocusView_requestDefaultFocus, false);
		}
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
					if (currentRect.isEmpty() && child.isFocusable()&&requestDefaultFocus) {
						Rect rect = getDrawRect(child);
						currentRect.set(rect);
//						child.getHitRect(currentRect);
						child.requestFocus();
						validateZoom();
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
//			Rect outRect = new Rect();
			Rect outRect = getDrawRect(v);
//			v.getHitRect(outRect);
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
		validateZoom();
		invalidate();
	}
	
	private void validateZoom(){
		View view = findFocus();
		if (view != null) {
			if (lastSelectedView != null) {
				if(lastSelectedView == view)
					return;
				itemZoomOut(lastSelectedView);
			}
			itemZoomIn(view);
			lastSelectedView = view;
		}else{
			if (lastSelectedView != null){
				itemZoomOut(lastSelectedView);
				lastSelectedView = null;
			}
		}
	}
	
	/**
	 * 放大
	 * 
	 * @param selectedView
	 */
	private void itemZoomIn(View selectedView) {
		ViewPropertyAnimator animator = selectedView.animate();
		animator.scaleX(mScale);
		animator.scaleY(mScale);
		animator.setDuration(250);
		animator.start();
	}

	/**
	 * 选中项缩小
	 * 
	 * @param selectedView
	 */
	private void itemZoomOut(View selectedView) {
		ViewPropertyAnimator animator = selectedView.animate();
		animator.scaleX(1f);
		animator.scaleY(1f);
		animator.setDuration(50);
		animator.start();
	}
	
	/**
	 * 获取绘画的区域
	 * 
	 * @param v
	 * @return
	 */
	private Rect getDrawRect(View v) {
		Rect outRect = new Rect();
		v.getDrawingRect(outRect);
		int wMargin = Math.round(outRect.width() * (mScale - 1) / 2);
		int hMargin = Math.round(outRect.height() * (mScale - 1) / 2);

		v.getHitRect(outRect);
		outRect.left -= wMargin;
		outRect.top -= hMargin;
		outRect.right += wMargin;
		outRect.bottom += hMargin;
		return outRect;
	}
}
