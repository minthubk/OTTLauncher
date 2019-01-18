package com.pisen.ott.launcher;

import io.vov.vitamio.Vitamio;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.izy.ApplicationSupport;
import android.izy.util.LogCat;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.volley.BitmapLruCache;
import cn.jpush.android.api.JPushInterface;

import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.config.LauncherConfig;
import com.pisen.ott.launcher.movie.QiyiManager;
import com.pisen.ott.launcher.service.ImageDownLoader;
import com.pisen.ott.launcher.voice.VoiceService;

/**
 * 
 * @author yangyp
 * @version 1.0, 2015年1月12日 下午5:45:40
 */
public class LauncherApplication extends ApplicationSupport {

	private static LauncherConfig mConfig;

	@Override
	public void onCreate() {
		super.onCreate();
		LogCat.setTag("Ott");
		ImageLoader.init(this, BitmapLruCache.getInstance(this));
		mConfig = new LauncherConfig(this);
		mConfig.initialize();

		// Register intent receivers
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mConfig, filter);

		filter = new IntentFilter();
		filter.addAction(ImageDownLoader.ACTION_UIVERSION_UPDATE);
		registerReceiver(mConfig, filter);

		// 极光推送初始化
		try {
			JPushInterface.setDebugMode(true);
			JPushInterface.init(this);
			// Set<String> setlink = new LinkedHashSet<String>();
			// setlink.add("tag_formal_android_ott_20150305");
			// JPushInterface.setTags(this, setlink, new TagAliasCallback() {
			// @Override
			// public void gotResult(int arg0, String arg1, Set<String> arg2) {
			// if (arg0 == 0) {
			// LogCat.i("message set success. and tag= %s\n",arg2);
			// }else{
			// LogCat.e("%s\n","message set failed.");
			// }
			// }
			// });
			LogCat.d("%s\n", "[JPushInterface] onCreate");
		} catch (Exception e) {
			LogCat.e("%s\n", "JPushInterface init error: ", e);
		}
		// Loader videoPlay libs
		new libsLoadsAsyncTask().execute();
		
		//注册广播  
        registerReceiver(homeKeyEventReceiver, new IntentFilter(  
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        
        QiyiManager.getInstance(this).init();
        QiyiManager.getInstance(this).ensureLoad();
        startService(new Intent(getBaseContext(), VoiceService.class));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterReceiver(mConfig);
	}

	public static LauncherConfig getConfig() {
		return mConfig;
	}

	private BroadcastReceiver homeKeyEventReceiver = new BroadcastReceiver() {
		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";
		String SYSTEM_HOME_KEY_LONG = "recentapps";
		 
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
					 //表示按了home键,程序到了后台
					Intent intt = new Intent(getApplicationContext(),HomeActivity.class);
					intt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intt);
				}else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
					//表示长按home键,显示最近使用的程序列表
				}
			} 
		}
	};

	protected class libsLoadsAsyncTask extends AsyncTask<Object, Object, Boolean> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			return Vitamio.initialize(getApplicationContext(), getResources().getIdentifier("libarm", "raw", getPackageName()));
		}

		@Override
		protected void onPostExecute(Boolean inited) {
		}
	}
}
