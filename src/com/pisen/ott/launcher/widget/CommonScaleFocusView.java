package com.pisen.ott.launcher.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.izy.widget.FocusScroller;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import com.pisen.ott.launcher.R;

/**
 * 支持按键焦点浮层切换 和选中项缩放
 * 支持包含listview和普通控件
 * @author hegang
 * @version 1.0, 2015年04月29日 上午09:39:18
 * 
 */
public class CommonScaleFocusView extends LinearLayout implements OnFocusChangeListener{

	private FocusScroller mScroller;
	private Drawable mDrawable;
	private static int FocusBorder = 18;
	protected Rect currentRect; // 当前焦点位置
	private Rect root;
	private int preAction = -1;

	private float mScale = 1.04f;
	private View lastSelectedView;
	private View lastAwayView;
	private OnFocusChangeListener onFocusChangeListener;
	private boolean requestDefaultFocus = false;


	public CommonScaleFocusView(Context context) {
		super(context);
	}
	public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
		this.onFocusChangeListener = onFocusChangeListener;
	}

	public CommonScaleFocusView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public CommonScaleFocusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context,attrs,defStyle);
	}

	@SuppressLint("Recycle")
	protected void initViews(Context context,AttributeSet attrs,int defStyle) {
		setWillNotDraw(false);
		// setClipToOutline(true);
		setClipChildren(false);
		setClipToPadding(false);
		setChildrenDrawingOrderEnabled(true);
		mScroller = new FocusScroller(context);
		mDrawable = getResources().getDrawable(R.drawable.home_focus);
		FocusBorder = getResources().getDimensionPixelSize(R.dimen.movie_focus_border);
		currentRect = new Rect();
		root = new Rect();
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScaleFocusView, defStyle, 0);
			requestDefaultFocus = a.getBoolean(R.styleable.ScaleFocusView_requestDefaultFocus, false);
		}
	}

//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		boolean result = super.dispatchKeyEvent(event);
//		if (preAction == KeyEvent.ACTION_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {// 长按
//			invalidateScroll();
//			validateZoom();
//		} else if (event.getAction() == KeyEvent.ACTION_UP) {
//			invalidateScroll();
//			validateZoom();
//		}
//		preAction = event.getAction();
//		return result;
//	}

	public void invalidateScroll() {
		lastAwayView = findFocus();
		if(lastAwayView == null){
			invalidate();
			return;
		}
		if (lastAwayView instanceof AbsListView) {
			AbsListView absListView = (AbsListView) lastAwayView;
			lastAwayView = absListView.getSelectedView();
		}
		invalidateUi(lastAwayView);
	}
	
	private void getViewRectInRoot(View v, Rect outRect) {
		resizeRoot();
		int[] location = new int[2];
		v.getLocationInWindow(location);
		outRect.left = location[0] - root.left;
		outRect.top = location[1] - root.top;
		outRect.right = outRect.left + v.getWidth();
		outRect.bottom = outRect.top + v.getHeight();
	}

	private void invalidateUi(View v) {
		if (hasFocus()) {
			Rect outRect = new Rect();
			getViewRectInRoot(v, outRect);
			mScroller.abortAnimation();
			if (!currentRect.equals(outRect)) {
				mScroller.startScroll(currentRect, outRect);
			}
		}
		invalidate();
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int[] location = new int[2];
		getLocationInWindow(location);
		root.left = location[0];
		root.right = (int) (root.left + getWidth());
		root.top = location[1];
		root.bottom = (int) (root.top + getHeight());
		initChildFocus(this);
	}

	private void resizeRoot(){
		int[] location = new int[2];
		getLocationInWindow(location);
		root.left = location[0];
		root.right = (int) (root.left + getWidth());
		root.top = location[1];
		root.bottom = (int) (root.top + getHeight());
	}
	
	private void initChildFocus(ViewGroup group) {
		for (int i = 0, N = group.getChildCount(); i < N; i++) {
			View view = group.getChildAt(i);
			if (view instanceof ViewGroup) {
				ViewGroup layout = (ViewGroup) view;
				if (layout.isFocusable()) {
					if (requestDefaultFocus && currentRect.isEmpty()) {
						validateZoom();
						getViewRectInRoot(layout, currentRect);
						layout.requestFocus();
					}
					layout.setOnFocusChangeListener(this);
				} else {
					initChildFocus(layout);
				}
			} else {
				if (view.isFocusable()) {
					if (requestDefaultFocus && currentRect.isEmpty()) {
						validateZoom();
						getViewRectInRoot(view, currentRect);
						view.requestFocus();
					}
				}
				view.setOnFocusChangeListener(this);
			}
		}
	}
	
//	/**
//	 * 设置默认显示的焦点
//	 */
//	protected void setDefaultFocusedChild(View view) {
//		if (view instanceof ViewGroup) {
//			ViewGroup layout = (ViewGroup) view;
//			if (layout.isFocusable()) {
//				if (currentRect.isEmpty()) {
//					getViewRectInRoot(layout, currentRect);
//					validateZoom();
//					layout.requestFocus();
//					return;
//				}
//			}
//			for (int i = 0, N = layout.getChildCount(); i < N; i++) {
//				View child = layout.getChildAt(i);
//				setDefaultFocusedChild(child);
//			}
//		} else {
//			if (view.isFocusable()) {
//				if (currentRect.isEmpty()) {
//					getViewRectInRoot(view, currentRect);
//					validateZoom();
//					view.requestFocus();
//					return;
//				}
//			}
//		}
//	}

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

	@Override
	public void onFocusChange(View paramView, boolean hasFocus) {
		validateZoom();
		invalidateScroll();
		if (onFocusChangeListener != null) {
			onFocusChangeListener.onFocusChange(paramView, hasFocus);
		}
	}

}
