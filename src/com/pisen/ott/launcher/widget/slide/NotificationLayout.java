package com.pisen.ott.launcher.widget.slide;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.pisen.ott.common.view.NotifyDefKeyFocusWrap;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.OttBaseActivity;
import com.pisen.ott.launcher.localplayer.LocalPlayerActivity;
import com.pisen.ott.launcher.message.MessageCenterActivity;
import com.pisen.ott.launcher.widget.ContentLayout;

/**
 * 通知栏
 * 
 * @author yangyp
 * @version 1.0, 2015年1月22日 下午4:51:25
 */
public class NotificationLayout extends MenuLayout {

	private NotifyDefKeyFocusWrap mKeyFocus;
	
	public NotificationLayout(Context context) {
		this(context, null);
		initViews(context);
	}

	public NotificationLayout(Context context, AttributeSet attrs) {
		super(context, attrs, R.anim.slide_header_appear, R.anim.slide_header_disappear);
		initViews(context);
	}

	protected void initViews(Context context) {
		mKeyFocus = new NotifyDefKeyFocusWrap(this);
		mKeyFocus.setKeepFocus(true);
//		mKeyFocus.setFocusImageResource(R.drawable.three_level_choice_highlight, R.dimen.action_bar_item_space);
		mKeyFocus.setFocusImageResource(R.drawable.three_level_choice_highlight, R.dimen.banner_focus_border_menu);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mKeyFocus.layout(null);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mKeyFocus.draw(canvas);
	}
	
	@Override
	public boolean executeKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (!ContentLayout.hasNextFocused(this, View.FOCUS_DOWN)) {
					hideMenu();
					return true;
				}
				break;
			}
		}
		return super.executeKeyEvent(event);
	}

	
	@Override
	public void onItemClick(View v) {
		switch (v.getId()) {
		case R.id.txtMenuMessage:
			OttBaseActivity.startActivity(getContext(), MessageCenterActivity.class);
			break;
		case R.id.txtMenuUsb:
			OttBaseActivity.startActivity(getContext(), LocalPlayerActivity.class);
			break;
		case R.id.txtMenuWifi:
			OttBaseActivity activity = (OttBaseActivity) getContext();
			activity.startActivity("com.pisen.ott.settings", "com.pisen.ott.settings.network.WifiActivity");
			break;
		default:
			break;
		}
	}

}
