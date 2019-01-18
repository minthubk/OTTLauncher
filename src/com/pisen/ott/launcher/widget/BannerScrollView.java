package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.HorizontalScrollView;

import com.pisen.ott.common.view.focus.PopupKeyFocus;
import com.pisen.ott.launcher.R;

/**
 * 板块页焦点切换动画
 * 
 * @author yangyp
 * @version 1.0, 2015年1月5日 下午2:34:14
 */
public class BannerScrollView extends HorizontalScrollView {

	protected Rect currentRect; // 当前焦点位置
	static final float SCALE_XY = 1.10f;

	private PopupKeyFocus mKeyFocus;

	public BannerScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public BannerScrollView(Context context) {
		super(context);
		initViews(context);
	}

	protected void initViews(Context context) {
		setFocusable(false);
		setHorizontalScrollBarEnabled(false);
		mKeyFocus = new PopupKeyFocus(this);
		mKeyFocus.setRequestFocus(true);
		mKeyFocus.setScale(SCALE_XY);
		mKeyFocus.setFocusImageResource(R.drawable.home_focus, R.dimen.banner_focus_border);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mKeyFocus.layout(null);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		mKeyFocus.draw(canvas);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		/*View view = findFocus();
		if (view != null) {
			if (view instanceof IDownloadItem) {
				IDownloadItem downloadItem = (IDownloadItem) view;
				if (downloadItem.isShowControl()) {
					return downloadItem.dispatchKeyEvent(event);
				}
			}
		}*/

		return super.dispatchKeyEvent(event);
	}

}
