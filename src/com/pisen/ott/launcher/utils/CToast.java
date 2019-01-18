package com.pisen.ott.launcher.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 自定义时长的Toast
 * 
 * @author DexYang
 *
 */
public class CToast {

	public static final int LENGTH_SHORT = 1500;
	public static final int LENGTH_LONG = 2500;

	private final Handler mHandler = new Handler();
	private int mDuration = LENGTH_SHORT;
	private int mGravity = Gravity.CENTER;
	private int mX, mY;
	private float mHorizontalMargin;
	private float mVerticalMargin;
	private View mView;
	private View mNextView;

	private WindowManager mWM;
	private WindowManager.LayoutParams mParams;

	public static CToast makeText(Context context, int resId) {
		return makeText(context, context.getResources().getText(resId));
	}

	public static CToast makeText(Context context, CharSequence text) {
		return makeText(context, text, LENGTH_SHORT);
	}

	public static CToast makeText(Context context, int resId, int duration) {
		return makeText(context, context.getResources().getText(resId), duration);
	}

	public static CToast makeText(Context context, CharSequence text, int duration) {
		CToast result = new CToast(context);
		LinearLayout mLayout = new LinearLayout(context);
		TextView tv = new TextView(context);
		tv.setText(text);
		tv.setTextColor(Color.BLACK);//Color.White
		tv.setGravity(Gravity.CENTER);
		mLayout.setBackgroundResource(android.R.drawable.alert_light_frame);

		int w = context.getResources().getDisplayMetrics().widthPixels / 2;
		int h = context.getResources().getDisplayMetrics().widthPixels / 10;
		mLayout.addView(tv, w, h);
		result.mNextView = mLayout;
		result.mDuration = duration;

		return result;
	}

	public CToast(Context context) {
		mWM = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		mParams = new WindowManager.LayoutParams();
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE//
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE//
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		mParams.format = PixelFormat.TRANSLUCENT;
		mParams.windowAnimations = android.R.style.Animation_Toast;
		mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		mParams.setTitle("Toast");
	}

	/**
	 * Set the view to show.
	 * 
	 * @see #getView
	 */
	public void setView(View view) {
		mNextView = view;
	}

	/**
	 * Return the view.
	 * 
	 * @see #setView
	 */
	public View getView() {
		return mNextView;
	}

	/**
	 * Set how long to show the view for.
	 * 
	 * @see #LENGTH_SHORT
	 * @see #LENGTH_LONG
	 */
	public void setDuration(int duration) {
		mDuration = duration;
	}

	/**
	 * Return the duration.
	 * 
	 * @see #setDuration
	 */
	public int getDuration() {
		return mDuration;
	}

	/**
	 * Set the margins of the view.
	 *
	 * @param horizontalMargin
	 *            The horizontal margin, in percentage of the container width,
	 *            between the container's edges and the notification
	 * @param verticalMargin
	 *            The vertical margin, in percentage of the container height,
	 *            between the container's edges and the notification
	 */
	public void setMargin(float horizontalMargin, float verticalMargin) {
		mHorizontalMargin = horizontalMargin;
		mVerticalMargin = verticalMargin;
	}

	/**
	 * Set the location at which the notification should appear on the screen.
	 * 
	 * @see android.view.Gravity
	 * @see #getGravity
	 */
	public void setGravity(int gravity, int xOffset, int yOffset) {
		mGravity = gravity;
		mX = xOffset;
		mY = yOffset;
	}

	/**
	 * schedule handleShow into the right thread
	 */
	public void show() {
		mHandler.post(mShow);
		if (mDuration > 0) {
			mHandler.postDelayed(mHide, mDuration);
		}
	}

	/**
	 * schedule handleHide into the right thread
	 */
	public void hide() {
		mHandler.post(mHide);
	}

	private final Runnable mShow = new Runnable() {
		public void run() {
			handleShow();
		}
	};

	private final Runnable mHide = new Runnable() {
		public void run() {
			handleHide();
		}
	};

	private void handleShow() {
		if (mView != mNextView) {
			// remove the old view if necessary
			handleHide();
			mView = mNextView;
			// mWM = WindowManagerImpl.getDefault();
			final int gravity = mGravity;
			mParams.gravity = gravity;
			if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
				mParams.horizontalWeight = 1.0f;
			}
			if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
				mParams.verticalWeight = 1.0f;
			}
			mParams.x = mX;
			mParams.y = mY;
			mParams.verticalMargin = mVerticalMargin;
			mParams.horizontalMargin = mHorizontalMargin;
			if (mView.getParent() != null) {
				mWM.removeView(mView);
			}
			mWM.addView(mView, mParams);
		}
	}

	private void handleHide() {
		if (mView != null) {
			if (mView.getParent() != null) {
				mWM.removeView(mView);
			}
			mView = null;
		}
	}
}
