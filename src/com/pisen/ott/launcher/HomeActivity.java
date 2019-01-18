package com.pisen.ott.launcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.appmanage.AppManageActivity;
import com.pisen.ott.launcher.base.NavigationActivity;
import com.pisen.ott.launcher.base.OttBaseActivity;
import com.pisen.ott.launcher.config.HomeIconCache;
import com.pisen.ott.launcher.config.HomeIconCache.IconItemCallback;
import com.pisen.ott.launcher.localplayer.LocalPlayerActivity;
import com.pisen.ott.launcher.message.MessageCenterActivity;
import com.pisen.ott.launcher.service.UIContentUpdateService;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.service.UpdateManager;
import com.pisen.ott.launcher.widget.HomeScrollView;
import com.pisen.ott.launcher.widget.IconReflectView;

/**
 * home
 * 
 * @author yangyp
 * @version 1.0, 2015年2月9日 下午4:34:45
 */
public class HomeActivity extends NavigationActivity implements OnClickListener, IconItemCallback {

	private HomeScrollView scrollView;
	private GridLayout blockLayout;
	private HomeIconCache iconCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_home);
		scrollView = (HomeScrollView) findViewById(R.id.horizontalScrollView1);
		blockLayout = (GridLayout) findViewById(R.id.blockLayout);
		setBlockLayoutOnClickListener();
		iconCache = new HomeIconCache(getApplicationContext());
		iconCache.initialize(this);

		// scrollView.setOnItemFocusChangeListener(this);

		// used for updating uicontent
		this.startService(new Intent(this, UIContentUpdateService.class));
		
		// used for updating lanucher
		UpdateManager.getUpdateManager().checkAppUpdate(HomeActivity.this, true);
		scrollView.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if(scrollView.canScrollHorizontally(-1)){
					toggleLeftMenuGuide(false);
				}else{
					toggleLeftMenuGuide(true);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		iconCache.removeAll();
		stopService(new Intent(this, UIContentUpdateService.class));
		super.onDestroy();
	}

	@Override
	protected void onResume() {
//		从通知栏 跳转Shortcuts 返回主菜单 隐藏通知栏
		notificationLayout.hideMenuWithOutAnimation();
		super.onResume();
		if(scrollView.canScrollHorizontally(-1)){
			toggleLeftMenuGuide(false);
		}else{
			toggleLeftMenuGuide(true);
		}
	}
	
	@Override
	public boolean executeKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		}
		return super.executeKeyEvent(event);
	}

	/**
	 * 设置Item点击事件
	 */
	private void setBlockLayoutOnClickListener() {
		for (int i = 0, N = blockLayout.getChildCount(); i < N; i++) {
			View child = blockLayout.getChildAt(i);
			child.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		hideLeftMenuAll();
		Object tag = v.getTag();
		if (tag != null) {
			startContainer(String.valueOf(v.getTag()));
		} else {
			switch (v.getId()) {
			case R.id.appSettings:
				startActivity("com.pisen.ott.settings", "com.pisen.ott.settings.SettingsActivity");
				break;
			case R.id.appMgr:
				OttBaseActivity.startActivity(this, AppManageActivity.class);
				break;
			case R.id.appMessageCenter:
				OttBaseActivity.startActivity(this, MessageCenterActivity.class);
				break;
			case R.id.appLocalPlayer:
				OttBaseActivity.startActivity(this, LocalPlayerActivity.class);
				break;
			}
		}
	}

	@Override
	public void onChangedContent(UiContent obj) {
		super.onChangedContent(obj);
		iconCache.update(obj, this);
	}

	@Override
	public void onIconItem(UiContent ui, Bitmap icon) {
		View itemView = blockLayout.findViewWithTag(ui.DisplayCode);
		if (itemView instanceof IconReflectView) {
			IconReflectView iconReflect = (IconReflectView) itemView;
			iconReflect.setIconImageBitmap(icon);
			iconReflect.setIconText(ui.Name);
		}
	}
	
	/*@Override
	public void onItemFocusChanged(View v, boolean hasFocus) {
		String viewCode = String.valueOf(v.getTag());
		CacheEntry entry = iconCache.get(viewCode);
		if (entry != null) {
			if (hasFocus) {
				if (v instanceof IconReflectView) {
					IconReflectView iconReflect = (IconReflectView) v;
					iconReflect.setIconImageBitmap(entry.icon);
				}
			} else {
				if (v instanceof IconReflectView) {
					IconReflectView iconReflect = (IconReflectView) v;
					iconReflect.setIconImageBitmap(entry.icon);
				}
			}
		}
	}*/

}
