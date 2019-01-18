package com.pisen.ott.launcher.localplayer.video;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.slide.MenuLayout;

/**
 * @author mahuan
 * 
 */
public class VideoBottomMenuLayout extends MenuLayout {

	private OnItemBottonClickListener mOnItemClickListener;
	protected static final int DelayMillis = 5 *1000;
	
	protected Handler  handler = new Handler();
	protected Runnable hideRun=	new Runnable() {
		public void run() {
			hideMenu();
		}
	};

	public VideoBottomMenuLayout(Context context) {
		this(context, null);
	}

	public VideoBottomMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs, R.anim.slide_footer_appear, R.anim.slide_footer_disappear);
	}

	/**
	 * 处理bottomLayout 触摸
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		handler.removeCallbacks(hideRun);
		handler.postDelayed(hideRun, DelayMillis);
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
			handler.removeCallbacks(hideRun);
			handler.postDelayed(hideRun, DelayMillis);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onClick(View v) {
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemBottonClick(v);
		}
	}

	public void setOnItemBottonClickListener(OnItemBottonClickListener l) {
		this.mOnItemClickListener = l;
	}

	public interface OnItemBottonClickListener {
		void onItemBottonClick(View v);
	}

}
