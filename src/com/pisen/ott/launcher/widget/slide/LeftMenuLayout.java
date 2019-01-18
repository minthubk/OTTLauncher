package com.pisen.ott.launcher.widget.slide;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.appmanage.AppManageActivity;
import com.pisen.ott.launcher.base.OttBaseActivity;
import com.pisen.ott.launcher.localplayer.LocalPlayerActivity;
import com.pisen.ott.launcher.message.MessageCenterActivity;
import com.pisen.ott.launcher.search.SearchActivity;
import com.pisen.ott.launcher.widget.ContentLayout;

/**
 * 左菜单
 * 
 * @author yangyp
 * @version 1.0, 2015年1月22日 下午4:51:13
 */
public class LeftMenuLayout extends MenuLayout {

	private OttBaseActivity baseActivity;
	protected DefaultKeyFocus mKeyFocus;
	
	public void setActivity(OttBaseActivity baseActivity) {
		this.baseActivity = baseActivity;
	}

	public LeftMenuLayout(Context context) {
		this(context, null);
		initViews(context);
	}

	public LeftMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs, R.anim.slide_left_appear, R.anim.slide_left_disappear);
		if (!isInEditMode()) {
			initViews(context);
		}
		
	}

	protected void initViews(Context context) {
		mKeyFocus = new DefaultKeyFocus(this);
//		mKeyFocus.setFocusImageResource(R.drawable.three_level_choice_highlight, R.dimen.space_line);
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
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!ContentLayout.hasNextFocused(this, View.FOCUS_RIGHT)) {
					hideMenu();
					baseActivity.toggleLeftMenuGuide(true);
					return true;
				}
				break;
			}
		}
		return super.executeKeyEvent(event);
	}

	@Override
	public void onItemClick(View v) {
//		hideMenu();
		switch (v.getId()) {
		case R.id.leftSetting:
			OttBaseActivity activity = (OttBaseActivity) getContext();
			activity.startActivity("com.pisen.ott.settings", "com.pisen.ott.settings.SettingsActivity");
			break;
		case R.id.leftMrg:
			OttBaseActivity.startActivity(getContext(), AppManageActivity.class);
			break;
		case R.id.leftMessage:
			OttBaseActivity.startActivity(getContext(), MessageCenterActivity.class);
			break;
		case R.id.leftPlay:
			OttBaseActivity.startActivity(getContext(), LocalPlayerActivity.class);
			break;
		case R.id.leftSearch:
			OttBaseActivity.startActivity(getContext(), SearchActivity.class);
			break;
		}
	}

}
