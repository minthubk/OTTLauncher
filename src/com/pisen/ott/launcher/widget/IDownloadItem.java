package com.pisen.ott.launcher.widget;

import android.view.KeyEvent;

import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;

public interface IDownloadItem {

	void nextClick(UiContent content, GridScaleView grdContent);
	
	void setUiContent(UiContent content);

	boolean isShowControl();

	void showControlLayout();

	void hideControlLayout();

	boolean dispatchKeyEvent(KeyEvent event);

	boolean checkInstalled();

}
