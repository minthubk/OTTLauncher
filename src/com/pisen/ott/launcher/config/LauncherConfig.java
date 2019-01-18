package com.pisen.ott.launcher.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.izy.content.IntentUtils;
import android.izy.preference.PreferencesUtils;
import android.izy.util.parse.GsonUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.search.SearchUtils;
import com.pisen.ott.launcher.service.ImageDownLoader;
import com.pisen.ott.launcher.service.UiVersionInfo;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.service.UpdateInfo;

public class LauncherConfig extends BroadcastReceiver {

	static final String TAG = "LauncherConfig";
	static final String DEFAULT_CONFIG = "ui_content.json";
	private File initConfigFile;

	static final String PrefContentVersion = "ContentVersion";
	static final String WeatherInfo = "WeatherInfo";

	private LauncherApplication mApp;
	private UpdateInfo updateInfo;
	private UiVersionInfo uiVersion;
	private UiContentObservable mObservable;

	private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

	public final void runOnUiThread(Runnable action) {
		if (sWorkerThread.getThreadId() == Process.myTid()) {
			sWorker.post(action);
		} else {
			action.run();
		}
	}

	public LauncherConfig(LauncherApplication app) {
		mApp = app;
		mObservable = new UiContentObservable();
		initConfigFile = new File(app.getExternalFilesDir("config"), DEFAULT_CONFIG);
	}

	/**
	 * 加载系统配置文件
	 */
	public void initialize() {
		updateInfo = getUpdateInfo();
		loaduiVersion();
	}

	private UpdateInfo getUpdateInfo() {
		UpdateInfo result = null;
		try {
			InputStream inStream = mApp.getResources().openRawResource(R.raw.config_system);
			result = GsonUtils.jsonDeserializer(new InputStreamReader(inStream), UpdateInfo.class);
			inStream.close();
		} catch (Exception e) {
		} finally {

		}
		return result;
	}

	private void loaduiVersion() {
		if (initConfigFile.exists()) {
			try {
				InputStreamReader inStream = new FileReader(initConfigFile);
				uiVersion = GsonUtils.jsonDeserializer(inStream, UiVersionInfo.class);
				inStream.close();
			} catch (Exception e) {
			}
		}

		if (uiVersion == null) {
			InputStream inStream = mApp.getResources().openRawResource(R.raw.config_content);
			uiVersion = GsonUtils.jsonDeserializer(new InputStreamReader(inStream), UiVersionInfo.class);
		}
	}

	/**
	 * 配置文件目录
	 * 
	 * @return
	 */
	public File getConfigDir() {
		return initConfigFile.getParentFile();
	}

	/**
	 * 获取系统内部版本号
	 * 
	 * @return
	 */
	public int getInternalVersion() {
		return updateInfo.System.InternalVersion;
	}

	/**
	 * 获取系统Ui版本号
	 * 
	 * @return
	 */
	public String getUiVersionCode() {
		return updateInfo.System.UiVersion;
	}

	/**
	 * 获取内容更新版本号
	 * 
	 * @return
	 */
	public String getContentVersion() {
		return PreferencesUtils.getString(PrefContentVersion, updateInfo.getConentVersion());
	}

	/**
	 * 设置最新的内容版本号
	 * 
	 * @param contentVersion
	 */
	public void setContentVersion(String contentVersion) {
		PreferencesUtils.setString(PrefContentVersion, contentVersion);
	}
	
	/**
	 * 按条件搜索全局内容
	 * @param search
	 * @return
	 */
	public List<UiContent> searchContentList(String search){
		return getContentList(search, uiVersion.Content.ChildContent);
	}

	/**
	 * 搜索
	 * @param search 搜索条件
	 * @param objList 搜索对象
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private List<UiContent> getContentList(String search,List<UiContent> objList){
		List<UiContent> list = new ArrayList<UiVersionInfo.UiContent>();
		if (objList != null) {
			for (UiContent obj : objList) {
				if (obj.LayerLevel == 3) {
					if (SearchUtils.getPingYinShort(obj.Name).contains(search.toLowerCase())) {
						list.add(obj);
					}
				}else{
					if (obj.ChildContent != null && obj.ChildContent.size() > 0) {
						list.addAll(getContentList(search, obj.ChildContent));
					}
				}
			}
		}
		return list;
	}
	
	public List<UiContent> searchUninstalledContentList(String search){
		return getUninstalledContentList(search, uiVersion.Content.ChildContent);
	}
	private List<UiContent> getUninstalledContentList(String search,List<UiContent> objList){
		List<UiContent> list = new ArrayList<UiVersionInfo.UiContent>();
		if (objList != null) {
			for (UiContent obj : objList) {
				if (obj.LayerLevel == 3) {
					if (!IntentUtils.isInstalledApk(mApp, obj.ApkFile) && SearchUtils.getPingYinShort(obj.Name).contains(search.toLowerCase())) {
						list.add(obj);
					}
				}else{
					if (obj.ChildContent != null && obj.ChildContent.size() > 0) {
						list.addAll(getContentList(search, obj.ChildContent));
					}
				}
			}
		}
		return list;
	}
	
	
	/**
	 * 获取出厂配置的Ui内容
	 * 
	 * @return
	 */
	public UiVersionInfo getUiVersion() {
		return uiVersion;
	}

	public UiContentObservable getObservable() {
		return mObservable;
	}
	
	/**
	 * 获取广告推荐内容分类
	 * @param bannerViewCode
	 * @return
	 */
	public List<UiContent> getBannerCategory(String contentViewCode){
		return uiVersion.getBannerCategory(contentViewCode);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive intent=" + intent);

		String action = intent.getAction();
		if (ImageDownLoader.ACTION_UIVERSION_UPDATE.equals(action)) {
			UiContent recvUicontent = (UiContent) intent.getSerializableExtra(ImageDownLoader.KEY_UIVERSION);
			if (uiVersion != null) {
				uiVersion.updateUiContent(recvUicontent);
				mObservable.notifyChanged(recvUicontent);
//				updateConfig();
			}
		}
	}

	/**
	 * 更新配置文件
	 */
	public boolean updateConfig(UiVersionInfo uiVersion) {
		FileWriter writer = null;
		try {
			if (initConfigFile == null) {
				initConfigFile = new File(mApp.getExternalFilesDir("config"), DEFAULT_CONFIG);
			}
			if (initConfigFile.exists()) {
				initConfigFile.delete();
				initConfigFile.createNewFile();
			}
			String json = GsonUtils.jsonSerializer(uiVersion);
			writer = new FileWriter(initConfigFile);
			writer.write(json);
			writer.flush();
		} catch (IOException e) {
		} finally {
			try {
				writer.close();
				//更新缓存
				loaduiVersion();
				return true;
			} catch (IOException e) {
			}
		}
		return false;
	}
	
	/**
	 * @return
	 */
	public String getLastWeatherInfo() {
		return PreferencesUtils.getString(WeatherInfo, "");
	}

	/**
	 * 
	 * @param contentVersion
	 */
	public void setLastWeatherInfo(String info) {
		PreferencesUtils.setString(WeatherInfo, info);
	}
}
