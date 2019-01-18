package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.izy.widget.FocusScroller;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.launcher.R;

/**
 * 详情页面自定义GridView
 * 
 * @author Liuhc
 * @version 1.0 2015年2月3日 上午10:50:05
 */
public class GridScaleView extends GridView implements IDetailContent {

	private float mScale = 1.1f;
	private View lastSelectedView;
	private FocusScroller mScroller;
	private Rect mCurrRect;
	private Drawable mDrawable;
	private int FocusBorder;
	public DefaultKeyFocus mKeyFocus;

	private int lastSelectedItemPos = -1;
	private IMasterTitle menuLayout;
	private boolean itemLock = false;
	private View upFocusView;

	public GridScaleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		setClipToPadding(false);
		setVerticalFadingEdgeEnabled(false);
		setChildrenDrawingOrderEnabled(true);

		mScroller = new FocusScroller(context, new DecelerateInterpolator());
		mCurrRect = new Rect();
		mDrawable = getResources().getDrawable(R.drawable.home_focus);
		FocusBorder = getResources().getDimensionPixelSize(R.dimen.banner_focus_border);
	}

	@Override
	public void setMasterTitle(IMasterTitle menuLayout) {
		menuLayout.setDetailContent(this);
		this.menuLayout = menuLayout;
	}

	@Override
	public boolean hasData() {
		return getCount() > 0;
	}

	@Override
	public void requestChildFocus() {
		if (hasData()) {
			setFocusable(true);
			requestFocus();
			setSelection(lastSelectedItemPos);
			((BaseAdapter) getAdapter()).notifyDataSetChanged();

			View selectedView = getSelectedView();
			if (selectedView != null) {
				Rect outRect = getDrawRect(selectedView);
				mCurrRect.set(outRect);
			}
		}
	}
	
	public void setUpFocusView(View upFocusView) {
		this.upFocusView = upFocusView;
	}


	/**
	 * 设置选中项获得光标
	 * @param pos
	 */
	public void setSelectionFocused(View v){
		if (v != null) {
			Rect outRect = getDrawRect(v);
			if (mCurrRect.isEmpty()) {
				v.getHitRect(mCurrRect);
			}
			if (!mCurrRect.equals(outRect)) {
				mScroller.startScroll(mCurrRect, outRect);
			}
		}
//		if (pos < (getCount()-1)) {
//			if (lastSelectedItemPos != pos) {
//				View v = getChildAt(pos);
//				if (v != null) {
//					Rect outRect = getDrawRect(v);
//					if (mCurrRect.isEmpty()) {
//						v.getHitRect(mCurrRect);
//					}
//					if (!mCurrRect.equals(outRect)) {
//						mScroller.startScroll(mCurrRect, outRect);
//					}
//				}
//			}
//		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (getChildCount() > 0) {
			View selectedView = getSelectedView();
			if (selectedView != null) {
				if (isFocused()) {
					itemZoomIn(selectedView);
					lastSelectedView = selectedView;
				}
				Rect outRect = getDrawRect(selectedView);
				if (mCurrRect.isEmpty()) {
					mCurrRect.set(outRect);
				}
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (hasFocus()) {
			if (mScroller.computeScrollOffset()) {
				Rect currRect = mScroller.getCurrRect();
				mCurrRect.set(currRect);
				invalidate();
			}
			mDrawable.setBounds(mCurrRect.left - FocusBorder, mCurrRect.top - FocusBorder, mCurrRect.right + FocusBorder, mCurrRect.bottom + FocusBorder);
			mDrawable.draw(canvas);
		}
	}

	@Override
	public void setSelection(int position) {
		lastSelectedItemPos = position;
		super.setSelection(position);
	}

	public void lockItem() {
		itemLock = true;
	}

	public void unlockItem() {
		itemLock = false;
	}

	public boolean isLockItem() {
		return itemLock;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (itemLock) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				return super.dispatchKeyEvent(event);
			default:
				return true;
			}
		}

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
				int numCol = getNumColumns();
				int selectedItemPos = getSelectedItemPosition() + 1;
				int rowNum = selectedItemPos / numCol + (selectedItemPos % numCol > 0 ? 1 : 0); // 当前所在行数
				if (rowNum <= 1) {
					if(upFocusView!=null){
						lastSelectedItemPos = getSelectedItemPosition();
						upFocusView.requestFocus();
						View selectedView = getSelectedView();
						if (selectedView != null) {
							itemZoomOut(selectedView);
						}
					}
					return true;
				}
			}
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
				int itemCount = getCount();
				int numCol = getNumColumns();
				int selectedItemPos = getSelectedItemPosition() + 1;
				int rowNum = selectedItemPos / numCol + (selectedItemPos % numCol > 0 ? 1 : 0); // 当前所在行数
				int rowCount = itemCount / numCol + (itemCount % numCol > 0 ? 1 : 0); // 总行数
				if (rowNum >= rowCount) {
					return true;
				}
			}
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
				int numCol = getNumColumns();
				int selectedItemPos = getSelectedItemPosition() + 1;
				// 判断当前是否是左边第一列
				if (selectedItemPos % numCol == 1) {
					lastSelectedItemPos = getSelectedItemPosition();
					if (menuLayout != null) {
						menuLayout.requestChildFocus();
					}
					setFocusable(false);
					View selectedView = getSelectedView();
					if (selectedView != null) {
						itemZoomOut(selectedView);
					}
					return true;
				}
			}
		}

		return super.dispatchKeyEvent(event);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = super.onKeyDown(keyCode, event);
		View selectedView = getSelectedView();
		if (lastSelectedView == selectedView) {
			return result;
		}

		if (lastSelectedView != null) {
			itemZoomOut(lastSelectedView);
		}

		if (selectedView != null) {
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			itemZoomIn(selectedView);
			lastSelectedView = selectedView;

			Rect outRect = getDrawRect(selectedView);
			if (mCurrRect.isEmpty()) {
				selectedView.getHitRect(mCurrRect);
			}
			if (!mCurrRect.equals(outRect)) {
				mScroller.startScroll(mCurrRect, outRect);
			}

			invalidate();

		}
		return result;
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
