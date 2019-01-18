package com.pisen.ott.launcher.widget.slide;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.OttBaseActivity;
import com.pisen.ott.launcher.widget.ContentLayout;

/**
 * 下菜单
 * 
 * @author yangyp
 * @version 1.0, 2015年1月22日 下午4:51:13
 */
public class BottomMenuLayout extends MenuLayout {

	public BottomMenuLayout(Context context) {
		this(context, null);
	}

	public BottomMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs, R.anim.slide_footer_appear, R.anim.slide_footer_disappear);
	}

	@Override
	public boolean executeKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:
				if (!ContentLayout.hasNextFocused(this, View.FOCUS_UP)) {
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
		case R.id.navMovie:
		case R.id.navGame:
		case R.id.navApp:
		case R.id.navEdu:
		case R.id.navSocial:
		case R.id.navShopping: {
			OttBaseActivity activity = (OttBaseActivity) getContext();
			activity.startContainer(String.valueOf(v.getTag()));
			break;
		}
		case R.id.navSettings: {
			OttBaseActivity activity = (OttBaseActivity) getContext();
			activity.startActivity("com.pisen.ott.settings", "com.pisen.ott.settings.SettingsActivity");
			break;
		}
		}
		
	}

}
