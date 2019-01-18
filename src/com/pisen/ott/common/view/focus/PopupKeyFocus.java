package com.pisen.ott.common.view.focus;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

/**
 * 弹出层焦点动画
 * 
 * @author yangyp
 * @version 1.0, 2015年1月9日 下午4:46:58
 */
public class PopupKeyFocus extends DefaultKeyFocus {

	private float mScale = 1.10f;

	public PopupKeyFocus(ViewGroup layout) {
		super(layout);
	}

	public void setScale(float scale) {
		this.mScale = scale;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		if (hasFocus) {
			Rect outRect = getDrawRect(v);

			if (mCurrentRect.isEmpty()) {
				v.getHitRect(mCurrentRect);
			}

			if (!mCurrentRect.equals(outRect)) {
				mScroller.startScroll(mCurrentRect, outRect);
			}

			v.bringToFront();
			ViewPropertyAnimator animator = v.animate();
			animator.scaleX(mScale);
			animator.scaleY(mScale);
			animator.setDuration(250);
			animator.start();
		} else {
			ViewPropertyAnimator animator = v.animate();
			animator.scaleX(1f);
			animator.scaleY(1f);
			animator.setDuration(50);
			animator.start();
		}

		mLayout.invalidate();
	}

	/**
	 * 获取绘画的区域
	 * 
	 * @param v
	 * @return
	 */
	private Rect getDrawRect(View v) {
		int width = v.getWidth();
		int height = v.getHeight();
		int wMargin = Math.round(width * (mScale - 1) / 2);
		int hMargin = Math.round(height * (mScale - 1) / 2);

		Rect outRect = new Rect();
		v.getHitRect(outRect);
		outRect.left -= wMargin;
		outRect.top -= hMargin;
		outRect.right += wMargin;
		outRect.bottom += hMargin - ((v.getBottom() - outRect.bottom) * hMargin * 2) / height;
		return outRect;
	}

}
