package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;

/**
 * 支持下载安装的控件
 * 
 * @author yangyp
 * @version 1.0, 2015年2月12日 下午3:31:16
 */
public class DownloadIconReflectView extends IconReflectView implements IDownloadItem {

	private DownloadItemView downloadItemView;

	public DownloadIconReflectView(Context context) {
		super(context, null);
	}

	public DownloadIconReflectView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public View onInflateView(Context context) {
		downloadItemView = new DownloadItemView(context);
		return downloadItemView;
	}

	@Override
	public void setUiContent(UiContent content) {
		downloadItemView.setUiContent(content);
	}

	@Override
	public void setIconText(String name) {
		super.setIconText(name);
		downloadItemView.setName(name);
	}

	@Override
	public boolean isShowControl() {
		return downloadItemView.isShowControl();
	}

	@Override
	public void showControlLayout() {
		downloadItemView.showControlLayout();
	}

	@Override
	public void hideControlLayout() {
		downloadItemView.hideControlLayout();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (downloadItemView.isShowControl()) {
			downloadItemView.dispatchKeyEvent(event);
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean checkInstalled() {
		return downloadItemView.checkInstalled();
	}

	@Override
	public void nextClick(UiContent uiContent, GridScaleView grdContent) {
		downloadItemView.nextClick(uiContent,grdContent);
	}

}
