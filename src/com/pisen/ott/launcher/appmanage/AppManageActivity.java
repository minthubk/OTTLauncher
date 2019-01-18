package com.pisen.ott.launcher.appmanage;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.widget.AppManageItemView;
import com.pisen.ott.launcher.widget.CategoryMenuLayout;
import com.pisen.ott.launcher.widget.GridScaleView;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

/**
 * 应用市场(加载模式已和其他模块统一)
 * @author Liuhc
 * @version 1.0 2015年4月17日 下午3:00:15
 */
public class AppManageActivity extends DefaultActivity implements OnItemClickListener {

	private static final String APP_TYPE_RECENT = "最近使用"; 		// 最近安装
	private static final String APP_TYPE_LAEST = "最近安装"; 		// 最近安装
	private static final String APP_TYPE_ALL = "全部应用"; 			// 所有应用
	private static final String APP_TYPE_UNINSTALL = "卸载应用"; 	// 卸载应用

	private String appType;
	private AppManageAdapter apkInforAdapter;
	private TextView txtItemName;
	private CategoryMenuLayout menuLayout;
	private GridScaleView gridView;
	private PackageManager packageMgr;
	private OTTWiatProgress progressLoading;
	private static final int MAX_RECENT_TASKS = 20;
	private SharedPreferences mSharedPreferences;
	public static final String PREFER_NAME = "recentappfile";
	public static final String PREFER_KEY = "recentapp";
//	private TextView tvTopbg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_manage);
		mSharedPreferences = getSharedPreferences(PREFER_NAME, Activity.MODE_PRIVATE);
		txtItemName = (TextView) findViewById(R.id.txtItemName);
		menuLayout = (CategoryMenuLayout) findViewById(R.id.categoryLayout);
		progressLoading = (OTTWiatProgress) findViewById(R.id.progressLoading);
		gridView = (GridScaleView) findViewById(R.id.grdAppManage);
		gridView.setAdapter(apkInforAdapter = new AppManageAdapter(this));
		gridView.setMasterTitle(menuLayout);
		gridView.setOnItemClickListener(this);
		packageMgr = AppManageActivity.this.getPackageManager();
		
		menuLayout.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if (hasFocus && menuLayout.hasNewChildFocus()) {
					gridView.setSelection(-1);
					new AppInfoAsyncTask().execute(((String)v.getTag()));
				}
			}
		});

		initMenuLayout();
		new AppInfoAsyncTask().execute(APP_TYPE_RECENT);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addAction("android.intent.action.PACKAGE_ADDED");
	  	filter.addDataScheme("package");
		registerReceiver(unBootReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(unBootReceiver);
	}
	
	private void initMenuLayout() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int margin = getResources().getDimensionPixelSize(R.dimen.banner_category_layout_margin);
		lp.setMargins(margin, margin, margin, margin);
		ColorStateList whiteColor = getResources().getColorStateList(R.color.item_category_text);
		String[] menus = new String[]{APP_TYPE_RECENT,APP_TYPE_LAEST,APP_TYPE_ALL,APP_TYPE_UNINSTALL};
		int padding_h = getResources().getDimensionPixelSize(R.dimen.banner_category_item_padding_horizontal);
		int padding_v = getResources().getDimensionPixelSize(R.dimen.banner_category_item_padding_vertical);
		for (final String name : menus) {
			Button newButton = new Button(this);
			newButton.setPadding(padding_h, padding_v, padding_h, padding_v);
			newButton.setText(name);
			newButton.setTextSize(24);
			newButton.setTextColor(whiteColor);
			newButton.setBackground(null);
			newButton.setTag(name);
			menuLayout.addView(newButton, lp);
			if (name.equals(APP_TYPE_RECENT)) {
				menuLayout.setChildFocusedView(newButton);
			}
		}
	}
	
	private AppInfo getAppInfo(String pkgName){
		AppInfo appInfo = null;
		try {
			ApplicationInfo app = packageMgr.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
			appInfo = new AppInfo();
			appInfo.setAppLabel((String) app.loadLabel(packageMgr));
			appInfo.setAppIcon(app.loadIcon(packageMgr));
			appInfo.setPkgName(pkgName);
			appInfo.setAppName(app.loadLabel(packageMgr).toString());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appInfo;
	}
	private void saveRecentPackages(List<AppInfo> appInfos) {
		if (appInfos == null) {
			return;
		}
		if (appInfos.size() <= 0)
			return;
		StringBuffer sb = new StringBuffer();
		for (AppInfo info : appInfos) {
			sb.append(info.getPkgName());
			sb.append("#");
		}
		sb.deleteCharAt(sb.lastIndexOf("#"));
		mSharedPreferences.edit().putString(PREFER_KEY, sb.toString()).commit();
	}
	
	/**
	 * 根据条件查询应用程序信息
	 * @param filter
	 * @return
	 */
	private List<AppInfo> queryFilterAppInfo(String type) {
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		if (!TextUtils.isEmpty(type)) {
			ApplicationInfo self = AppManageActivity.this.getApplicationInfo();
			if(APP_TYPE_RECENT.equals(type)){
				List<String> names = new ArrayList<String>();
				final ActivityManager tasksManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				final List<ActivityManager.RecentTaskInfo> recentTasks = tasksManager.getRecentTasks(MAX_RECENT_TASKS, 0);
				final int count = recentTasks.size();
				for (int i = count - 1; i >= 0; i--) {
					final Intent intent = recentTasks.get(i).baseIntent;
					if (Intent.ACTION_MAIN.equals(intent.getAction()) && !intent.hasCategory(Intent.CATEGORY_HOME)) {
						AppInfo info = turn2AppInfo(intent, -1, null);
						if (info != null) {
							if (!info.getPkgName().equals(self.packageName)) {
								names.add(info.getPkgName());
								appInfos.add(0,info);
							}
						}
					}
				}
				String pkgs = mSharedPreferences.getString(PREFER_KEY, null);
				if (pkgs != null) {
					String[] pkgArray = pkgs.split("#");
					if (pkgArray != null) {
						for (String pkg : pkgArray) {
							if (appInfos.size() >= MAX_RECENT_TASKS)
								break;
							if (!names.contains(pkg)) {
								AppInfo info = getAppInfo(pkg);
								if (info != null) {
									appInfos.add(info);
								}
							}
						}
					}
				}
				saveRecentPackages(appInfos);
			}else{
				List<ApplicationInfo> listAppcations = packageMgr.getInstalledApplications(PackageManager.GET_ACTIVITIES);
				Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(packageMgr));
				for (ApplicationInfo app : listAppcations) {
					Intent i = packageMgr.getLaunchIntentForPackage(app.packageName);
					if (i != null) {
						if (self.packageName.equals(app.packageName)) {
							continue;
						}
						if (type.equals(APP_TYPE_LAEST) || type.equals(APP_TYPE_UNINSTALL)) {
							// 非系统程序
							if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
								appInfos.add(turn2AppInfo(app,-1,null));
							}
							// 本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
							else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
								appInfos.add(turn2AppInfo(app,-1,null));
							}
							// 安装在SDCard的应用程序
							else if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {  
								appInfos.add(turn2AppInfo(app,-1,null));
							} 
						} else if (type.equals(APP_TYPE_ALL)) {
							appInfos.add(turn2AppInfo(app,-1,null));
						} 
					}
				}
			}
		}
		return appInfos;
	}
	

	private AppManageItemView itemApp;
	private int lastPosition;
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppInfo item = (AppInfo) parent.getItemAtPosition(position);
		itemApp = (AppManageItemView) view.findViewById(R.id.itemAppManage);
		if (!appType.equals(APP_TYPE_UNINSTALL)) {
			itemApp.openApk(item.getPkgName());
		} else {
			lastPosition = position;
			gridView.lockItem();
			itemApp.nextClick(item);
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (gridView.isLockItem()) {
					return true;
				}
			}
		}
		if (itemApp != null && itemApp.isShowControl()) {
			return itemApp.dispatchKeyEvent(event);
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onBackKeyEvent() {
		gridView.unlockItem();
		if (itemApp != null && itemApp.isShowControl()) {
			itemApp.hideControlLayout();
			itemApp = null;
			return true;
		}
		return super.onBackKeyEvent();
	}

	// 构造一个AppInfo对象 ，并赋值
	private AppInfo turn2AppInfo(Intent intent, int pid, String processName) {
		final ResolveInfo resolveInfo = packageMgr.resolveActivity(intent, 0);
		if (resolveInfo == null) {
			return null;
		}
		AppInfo appInfo = new AppInfo();
		final ActivityInfo activityInfo = resolveInfo.activityInfo;
		appInfo.setAppLabel(activityInfo.loadLabel(packageMgr).toString());
		appInfo.setAppIcon(activityInfo.loadIcon(packageMgr));
		appInfo.setPkgName(activityInfo.packageName);
		appInfo.setAppName(activityInfo.loadLabel(packageMgr).toString());
		if (pid > 0) {
			appInfo.setPid(pid);
		}
		if (!StringUtils.isEmpty(processName)) {
			appInfo.setProcessName(processName);
		}
		return appInfo;
	}

	// 构造一个AppInfo对象 ，并赋值
	private AppInfo turn2AppInfo(ApplicationInfo app, int pid, String processName) {
		AppInfo appInfo = new AppInfo();
		appInfo.setAppLabel((String) app.loadLabel(packageMgr));
		appInfo.setAppIcon(app.loadIcon(packageMgr));
		appInfo.setPkgName(app.packageName);
		appInfo.setAppName(app.loadLabel(packageMgr).toString());
		if (pid > 0) {
			appInfo.setPid(pid);
		}
		if (!StringUtils.isEmpty(processName)) {
			appInfo.setProcessName(processName);
		}
		return appInfo;
	}

	public class AppInfoAsyncTask extends AsyncTask<String, Integer, List<AppInfo>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressLoading.show();
			gridView.setVisibility(View.GONE);
		}
		
		@Override
		protected List<AppInfo> doInBackground(String... params) {
			appType = params[0];
			return queryFilterAppInfo(appType);
		}

		@Override
		protected void onPostExecute(List<AppInfo> result) {
			super.onPostExecute(result);
			progressLoading.cancel();
			apkInforAdapter.setData(result);
			gridView.clearFocus();
			gridView.setVisibility(View.VISIBLE);
			txtItemName.setText(appType+"  |  " + result.size());
			if (result.isEmpty()) {
				menuLayout.requestChildFocus();
			}
		}

	}
		
	/**
	 * 程序卸载添加监听
	 */
	private BroadcastReceiver unBootReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			//接收安装广播 
	        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {   
	            String packageName = intent.getDataString();
	            LogCat.wtf("安装了:" +packageName + "包名的程序");
	        }
	        
	        //接收卸载广播  
	        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {   
	            String packageName = intent.getDataString();   
	            if (itemApp != null && !TextUtils.isEmpty(packageName)) {
            		gridView.unlockItem();
            		if (itemApp.isShowControl()) {
            			itemApp.hideControlLayout();
            		}
            		
            		if (lastPosition == (apkInforAdapter.getCount() -1)) {
            			apkInforAdapter.remove(itemApp.getItem());
            			txtItemName.setText(appType+"  |  " + apkInforAdapter.getCount());
        				handler.sendEmptyMessageDelayed(0, 500);
        				return;
        			}
            		apkInforAdapter.remove(itemApp.getItem());
        			txtItemName.setText(appType+"  |  " + apkInforAdapter.getCount());
        			itemApp = null;
            		
            		LogCat.wtf("卸载了:"  + packageName + "包名的程序");
				}
	        }
		}
		
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0) {
				@SuppressWarnings("unused")
				int a = gridView.getChildCount();
				gridView.setSelectionFocused(gridView.getChildAt(a -1));
    			itemApp = null;
			}
		}
	};
}
