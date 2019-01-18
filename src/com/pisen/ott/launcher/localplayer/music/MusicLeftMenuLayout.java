package com.pisen.ott.launcher.localplayer.music;

import android.content.Context;
import android.util.AttributeSet;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.slide.MenuLayout;

/**
 * 音乐播放左侧菜单布局
 * @author Liuhc
 * @version 1.0 2015年3月5日 下午2:50:19
 */
public class MusicLeftMenuLayout extends MenuLayout {

	
	public MusicLeftMenuLayout(Context context) {
		this(context, null);
	}

	public MusicLeftMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs, R.anim.slide_left_appear, R.anim.slide_left_disappear);
	}
	
}
