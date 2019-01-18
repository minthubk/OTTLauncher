package com.pisen.ott.common.view.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.izy.widget.FocusScroller;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;

public class DefaultKeyFocus implements IKeyFocus, OnFocusChangeListener,
		OnClickListener {

	protected Context mContext;
	protected ViewGroup mLayout;

	protected FocusScroller mScroller;
	protected Drawable mDrawable; // 焦点图
	protected int mMargin; // 边框宽度
	protected View mCurrentView;//获取焦点的View
	protected Rect mCurrentRect; // 当前焦点位置

	protected OnItemClickListener mItemClickListener;
	protected OnItemFocusChangeListener mFocusChangeListener;
	protected boolean mRequestFocus; // 获取焦点
	protected boolean mKeepFocus = false; // 保留离开后的焦点
	protected OnFocus onFocus;
	protected View focus;

	public void setmCurrentRect(Rect mCurrentRect) {
		this.mCurrentRect = mCurrentRect;
	}

	public void setOnFocus(OnFocus onFocus) {
		this.onFocus = onFocus;
	}

	public DefaultKeyFocus(ViewGroup layout) {
		mContext = layout.getContext();
		mLayout = layout;
		layout.setWillNotDraw(false);
		// setClipToOutline(true);
		// layout.setChildrenDrawingOrderEnabled(true);
		layout.setClipChildren(false);
		layout.setClipToPadding(false);

		mScroller = new FocusScroller(mContext);
		mCurrentRect = new Rect();
	}

	public void setRequestFocus(boolean requestFocus) {
		this.mRequestFocus = requestFocus;
	}

	/**
	 * 焦点离开，焦点框是否显示
	 * 
	 * @param keepFocus
	 */
	public void setKeepFocus(boolean keepFocus) {
		setRequestFocus(true);
		this.mKeepFocus = keepFocus;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		this.mItemClickListener = l;
	}

	public void setOnItemFocusChangeListener(OnItemFocusChangeListener l) {
		this.mFocusChangeListener = l;
	}

	public void setFocusImageResource(int resId) {
		mDrawable = mContext.getResources().getDrawable(resId);
		mMargin = 0;
	}

	public void setFocusImageResource(int resId, int marginResId) {
		mMargin = mContext.getResources().getDimensionPixelSize(marginResId);
		mDrawable = mContext.getResources().getDrawable(resId);
	}

	/**
	 * 获取当前获取焦点的View
	 * 
	 * @return
	 */
	public View getCurrentView() {
		return mCurrentView;
	}

	@Override
	public void layout(View focus) {
		this.focus = focus;
		setDefaultFocusedChild(mLayout);
	}

	public void requestChildFocus(View focus) {
		this.focus = focus;
	}

	/**
	 * 设置默认显示的焦点
	 */
	protected void setDefaultFocusedChild(View view) {
		if (view.isShown()) {
			if (view instanceof ViewGroup && !view.isFocusable()) {
				ViewGroup layout = (ViewGroup) view;
				for (int i = 0, N = layout.getChildCount(); i < N; i++) {
					View child = layout.getChildAt(i);
					setDefaultFocusedChild(child);
				}
			} else {
				if (view.isFocusable()) {
					view.setOnFocusChangeListener(this);
					if (mItemClickListener != null) {
						view.setOnClickListener(this);
					}
					if (view.isFocused()
							|| (view.isFocusable() && mRequestFocus)) {
						if (focus != null) {
							if (focus == view) {
								setChildRequestFocus(view);
							}
						} else {
							setChildRequestFocus(view);
						}
					}
				}
			}
		}
	}

	protected void setChildRequestFocus(View view) {
		if (mCurrentView == null) {
			mCurrentView = view;
		}
		if (mCurrentRect.isEmpty()) {
			if (view != null) {
				view.getHitRect(mCurrentRect);
				view.requestFocus();
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (mLayout.getChildCount() == 0) {
			return;
		}

		if (mLayout.hasFocus() || mKeepFocus) {
			// 判断开始矩形是否已经到达目标位置
			if (mScroller.computeScrollOffset()) {
				Rect currRect = mScroller.getCurrRect();
				mCurrentRect.set(currRect);//在滚动过程中,重新赋值焦点矩形位置
				mLayout.invalidate();
			}
			
			canvas.save();
//			以下代码决定焦点框绘制位置
			mDrawable.setBounds(mCurrentRect.left - mMargin, mCurrentRect.top
					- mMargin, mCurrentRect.right + mMargin,
					mCurrentRect.bottom + mMargin);
			mDrawable.draw(canvas);
			canvas.restore();
		} else {
			mCurrentRect.setEmpty();
		}
	}

	@Override
	public void onClick(View v) {
		if (mItemClickListener != null) {
			mItemClickListener.onItemClick(v);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (mFocusChangeListener != null) {
			mFocusChangeListener.onItemFocusChanged(v, hasFocus);
		}

		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		if (hasFocus) {
			if (this.onFocus != null) {
				this.onFocus.doFocus();
			}
			Rect outRect = new Rect();
			v.getHitRect(outRect);

			if (mCurrentRect.isEmpty()) {
				v.getHitRect(mCurrentRect);
			}

			if (!mCurrentRect.equals(outRect)) {
				mScroller.startScroll(mCurrentRect, outRect);
			}
		}
		mCurrentView = v;
		mLayout.invalidate();
	}

	public interface OnItemClickListener {
		void onItemClick(View v);
	}

	public interface OnFocus {
		void doFocus();
	}

	/**
	 * view 焦点改变监听
	 * @author Liuhc
	 * @version 1.0 2015年2月5日 下午3:53:42
	 */
	public interface OnItemFocusChangeListener {
		void onItemFocusChanged(View v, boolean hasFocus);
	}
}
