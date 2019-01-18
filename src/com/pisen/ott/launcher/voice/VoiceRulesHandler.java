package com.pisen.ott.launcher.voice;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class VoiceRulesHandler {

	/**影视频道-打开*/
	private String [] filmKeys = new String[]{"影视","电影","电视剧","综艺","动漫"};
	
	private HashMap<String, String> apps = new HashMap<String, String>();
	private Context context;
	
	
	public VoiceRulesHandler(Context context) {
		this.context = context;
	}
	
	public void init(){
		getInstallApps();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		context.registerReceiver(appReceiver, filter);
	}

	public void release(){
		context.unregisterReceiver(appReceiver);
	}
	private void getInstallApps(){
		apps.clear();
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_ACTIVITIES);
		for (ApplicationInfo info : listAppcations) {
			apps.put(info.loadLabel(pm).toString(), info.packageName);
		}
		
	}
	
	/**
	 * 尝试打开应用
	 * @param text
	 * @return
	 */
	public boolean processApp(String text){
		boolean ret = false;;
		String str = text;
		if(str.startsWith("打开")){
			str = str.replace("打开", "");
		}else if(str.startsWith("打开应用")){
			str = str.replace("打开应用", "");
		}else if(str.endsWith("应用")){
			str = str.replace("应用", "");
		}
		Set<String> set = apps.keySet();
		for(String app:set){
			if(app.equals(str)){
//			if(app.contains(str)){
				ret = openApkNewTaks(apps.get(app));
			}
		}
		return ret;
	}
	
	BroadcastReceiver appReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)||intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
				getInstallApps();
			}
		}
	};
	
	/**
	 * 打开应用程序
	 * 
	 * @param packName
	 */
	private boolean openApkNewTaks(String packName) {
		try {
			Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(packName);
			LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(LaunchIntent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
}
