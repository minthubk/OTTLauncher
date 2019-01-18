package com.pisen.ott.launcher.localplayer.video;

import android.content.Context;
import android.util.AttributeSet;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.slide.LeftMenuLayout;

/**
 * @author mahuan 视频左侧控制菜单
 */
public class VideoLeftMenuLayout extends LeftMenuLayout {

	public VideoLeftMenuLayout(Context context) {
		super(context);
	}

	public VideoLeftMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void initViews(Context context) {
		super.initViews(context);
		mKeyFocus.setFocusImageResource(R.drawable.side_left_selected, R.dimen.videoplayer_focus_border);
	}
}
