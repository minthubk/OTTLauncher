package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.pisen.ott.launcher.localplayer.FileCategoryLayout;
import com.pisen.ott.launcher.widget.ContentLayout;

public class packageView extends FrameLayout {
	private String browserType;
	private FileCategoryLayout menuLocalPlayer;
	private Context mcontext;
	public static String MUSIC = "music";
	public static String VIDEO = "video";
	public static String IMAGE = "image";
	public static String FILE = "file";

	public packageView(Context context, String browserType) {
		super(context);
		this.browserType = browserType;
		this.mcontext = context;
		menuLocalPlayer = ((LocalPlayerActivity) mcontext).menuLayout;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:
				if (!ContentLayout.hasNextFocused(this, View.FOCUS_UP)) {
					// 一级界面，根据browserType,设置向上切换到相应的一级导航
					View v = findFocus();
					if (v == null) {
						return super.dispatchKeyEvent(event);
					}
					if (MUSIC.equals(browserType)) {
						v.setNextFocusUpId(menuLocalPlayer.getChildAt(3).getId());
					}
					if (VIDEO.equals(browserType)) {
						v.setNextFocusUpId(menuLocalPlayer.getChildAt(1).getId());
					}
					if (IMAGE.equals(browserType)) {
						v.setNextFocusUpId(menuLocalPlayer.getChildAt(2).getId());
					}
					if (FILE.equals(browserType)) {
						v.setNextFocusUpId(menuLocalPlayer.getChildAt(0).getId());
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!ContentLayout.hasNextFocused(this, View.FOCUS_RIGHT)) {
					// 一级界面，根据browserType,阻止音乐向右滚动
					View v = findFocus();
					if (v == null) {
						return super.dispatchKeyEvent(event);
					}
					if (MUSIC.equals(browserType)) {
						// v.setNextFocusRightId(menuLocalPlayer.getChildAt(3).getId());
						v.setNextFocusRightId(v.getId());
					}
				}
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

}
