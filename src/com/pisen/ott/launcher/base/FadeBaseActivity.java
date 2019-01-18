package com.pisen.ott.launcher.base;

import android.izy.app.ActivitySupport;
import android.os.Bundle;
import android.view.KeyEvent;

import com.pisen.ott.launcher.LauncherApplication;

public abstract class FadeBaseActivity extends ActivitySupport {

	@Override
	public LauncherApplication getApplicationContext() {
		return (LauncherApplication) super.getApplicationContext();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	protected void onDestroy() {
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		super.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				onMenuKeyEvent(event);
				return true;
			}
		}

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (onBackKeyEvent()) {
				return true;
			}
		}

		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
			onVolumeKeyEvent(event);
			return true;
		}

		return executeKeyEvent(event) || super.dispatchKeyEvent(event);
	}

	public boolean onBackKeyEvent() {
		return false;
	}

	/**
	 * 菜单
	 * 
	 * @param event
	 */
	public void onMenuKeyEvent(KeyEvent event) {

	}

	public boolean executeKeyEvent(KeyEvent event) {
		return false;
	}

	/**
	 * @音量键处理
	 * @param event
	 */
	public void onVolumeKeyEvent(KeyEvent event) {

	}
}
