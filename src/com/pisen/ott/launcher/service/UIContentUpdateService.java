package com.pisen.ott.launcher.service;

import com.pisen.ott.launcher.HomeActivity;

import android.content.Context;
import android.izy.service.WifiService;
import android.net.wifi.WifiInfo;

/**
 * 1、定时检查内容是否有更新
 * 2、下载图片
 * @author Liuhc
 * @version 1.0 2015年1月9日 下午2:14:15
 */
public class UIContentUpdateService extends WifiService {

	private Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mContext = getApplication();
	}

	@Override
	public void onWifiConnected(WifiInfo info) {
		// 检查是否有等待下载任务
		ImageDownLoader.getDownLoader(mContext).checkQueue();
	}

	@Override
	public void onWifiDisconnected(WifiInfo info) {
		ImageDownLoader.getDownLoader(mContext).cancelTasks();
	}
	
	@Override
	public void onDestroy() {
		ImageDownLoader.getDownLoader(mContext).cancelTasks();
		super.onDestroy();
	}

}