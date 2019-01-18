package com.pisen.ott.launcher.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.izy.content.IntentUtils;
import android.izy.util.LogCat;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.ott.launcher.AppRecommendActivity;
import com.pisen.ott.launcher.ChatMainActivity;
import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.image.WebBrowserActivity;
import com.pisen.ott.launcher.message.MessageInfo;
import com.pisen.ott.launcher.message.MessageManager;
import com.pisen.ott.launcher.movie.MovieActivity;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.widget.ContentLayout;
import com.pisen.ott.launcher.widget.slide.BottomMenuLayout;
import com.pisen.ott.launcher.widget.slide.LeftMenuLayout;
import com.pisen.ott.launcher.widget.slide.NotificationLayout;

/**
 * Ott基类
 * 
 * @author yangyp
 * @version 1.0, 2015年2月9日 上午11:08:24
 */
public abstract class OttBaseActivity extends FadeBaseActivity {
	protected NotificationLayout notificationLayout;
	private BottomMenuLayout bottomMenuLayout;
	private LeftMenuLayout leftMenuLayout;
	private LinearLayout leftMenuLayoutGuide;
	private ViewGroup actionBar;
	private ContentLayout contentView;
	public final static String MESSAGE_RECEIVED_P = "com.pisen.ott.launcher.message.MESSAGE_RECEIVED_P";
	public final static int NOTIFICATION_SHOW_TIMEOUT = 5 * 1000;
	private TextView txtMenuMessage;
	private TextView txtMenuUsb;
	private View txtMenuUsbSpace;
	private UpdateMessageReceiver mUpdateMsgReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.launcher_base);
		initView();
	}

	private void initView() {
		notificationLayout = (NotificationLayout) superFindViewById(R.id.notificationLayout);
		txtMenuMessage = (TextView) notificationLayout.findViewById(R.id.txtMenuMessage);
		txtMenuUsb = (TextView) notificationLayout.findViewById(R.id.txtMenuUsb);
		txtMenuUsbSpace = notificationLayout.findViewById(R.id.txtMenuUsbSpace);
		hideMenuUsb();
		leftMenuLayoutGuide = (LinearLayout) superFindViewById(R.id.leftMenuLayout_Guide);
		leftMenuLayout = (LeftMenuLayout) superFindViewById(R.id.leftMenuLayout);
		bottomMenuLayout = (BottomMenuLayout) superFindViewById(R.id.bottomMenuLayout);

		actionBar = (ViewGroup) superFindViewById(R.id.actionBar);
		contentView = (ContentLayout) superFindViewById(R.id.frmContent);
		contentView.setActivity(this);
		leftMenuLayout.setActivity(this);
		registerMsgReceiver();
	}

	private View superFindViewById(int id) {
		return super.findViewById(id);
	}

	public static void startActivity(Context context, Class<?> cls) {
		context.startActivity(new Intent(context, cls));
	}

	public View getOttActionBar() {
		return actionBar;
	}

	public void setActionBarView(int layoutResID) {
		actionBar.removeAllViewsInLayout();
		actionBar.addView(View.inflate(this, layoutResID, null));
	}

	public NotificationLayout getNotificationLayout() {
		return notificationLayout;
	}

	public BottomMenuLayout getBottomMenuLayout() {
		return bottomMenuLayout;
	}

	public void setLeftMenuEnable(boolean enable){
		leftMenuLayout.setEnabled(enable);
		leftMenuLayoutGuide.setEnabled(enable);
		if(!enable){
			leftMenuLayoutGuide.setVisibility(View.GONE);
		}
	}

	public void requestContentFocus() {
		contentView.requestChildFocus();
	}

	/**
	 * @des 隐藏菜单Usb
	 */
	protected void hideMenuUsb() {
		txtMenuUsb.setVisibility(View.GONE);
		txtMenuUsbSpace.setVisibility(View.GONE);
		if( notificationLayout.isShown() ){
			notificationLayout.requestLayout();
		}
	}
	
	public boolean toggleLeftMenuGuide(boolean visible){
		if(visible){
			if(leftMenuLayoutGuide.isEnabled()){
				leftMenuLayoutGuide.setVisibility(View.VISIBLE);
			}else{
				return false;
			}
		}else{
			leftMenuLayoutGuide.setVisibility(View.GONE);
		}
		return true;
	}
	
	/**
	 * @des 显示菜单Usb
	 */
	protected void showMenuUsb() {
		txtMenuUsb.setVisibility(View.VISIBLE);
		txtMenuUsbSpace.setVisibility(View.VISIBLE);
		showNotificationBar();
		if( notificationLayout.isShown() ){
			notificationLayout.requestLayout();
			notificationLayout.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					txtMenuUsb.requestFocus();
					
				}
			},100);
		}
	}

	/**
	 * 显示状态栏
	 */
	public boolean showNotificationBar() {
		if (notificationLayout.isEnabled()) {
			notificationLayout.showMenu();
			return true;
		}
		return false;
	}

	/**
	 * 显示左菜单
	 */
	public boolean showLeftMenu() {
		// 只有主界面才能开户左菜单
		if (leftMenuLayout.isEnabled()) {
			leftMenuLayout.showMenu();
			leftMenuLayoutGuide.setVisibility(View.GONE);
			return true;
		}
		return false;
	}
	
	/**
	 * 隐藏左菜单
	 */
	public void hideLeftMenuAll() {
		// 只有主界面才能开户左菜单
		leftMenuLayout.hideMenu();
		leftMenuLayoutGuide.setVisibility(View.GONE);
	}

	@Override
	public void setContentView(int layoutResID) {
		setContentView(View.inflate(this, layoutResID, null));
	}

	@Override
	public void setContentView(View view) {
		setContentView(view, view.getLayoutParams());
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		contentView.removeAllViewsInLayout();
		if (params != null) {
			contentView.addView(view, params);
		} else {
			contentView.addView(view);
		}
	}

	@Override
	public View findViewById(int id) {
		return contentView.findViewById(id);
	}

	@Override
	public boolean onBackKeyEvent() {
		if (notificationLayout.isVisible()) {
			notificationLayout.hideMenu();
			return true;
		}
		if (leftMenuLayout.isVisible()) {
			leftMenuLayout.hideMenu();
			if (leftMenuLayoutGuide.isEnabled()) {
				leftMenuLayoutGuide.setVisibility(View.VISIBLE);
			}
			return true;
		}
		if (bottomMenuLayout.isVisible()) {
			bottomMenuLayout.hideMenu();
			return true;
		}
		return false;
	}

	/**
	 * 菜单
	 * 
	 * @param event
	 */
	@Override
	public void onMenuKeyEvent(KeyEvent event) {
		if (bottomMenuLayout.isVisible()) {
			requestContentFocus();
			bottomMenuLayout.hideMenu();
		} else {
			notificationLayout.hideMenu();
			leftMenuLayout.hideMenu();
			if (leftMenuLayoutGuide.isEnabled()) {
				leftMenuLayoutGuide.setVisibility(View.VISIBLE);
			}
			bottomMenuLayout.showMenu();
		}
	}

	/**
	 * 根据位置编号启动相关应用
	 * 
	 * @param viewCode
	 */
	public void startContainer(String displayCode) {
		UiContent uiContent = LauncherApplication.getConfig().getUiVersion()
				.getHome(displayCode);
		startNav(uiContent);
	}

	public void startContainer(String bannerViewCode, String displayCode) {
		UiContent uiContent = LauncherApplication.getConfig().getUiVersion()
				.getBanner(bannerViewCode, displayCode);
		startNav(uiContent);
	}

	private void startNav(UiContent uiContent) {
		if (uiContent != null) {
			if(uiContent.DisplayCode.equals("0012")){//打开影视二级页面
				startActivity(new Intent(this, MovieActivity.class));
				return;
			}else if(uiContent.DisplayCode.equals("5012")){//打开聊吧二级页面
				startActivity(new Intent(this, ChatMainActivity.class));
				return;
			}
			switch (uiContent.getStartType()) {
			case ContentView:
				AppRecommendActivity.start(this, uiContent);
				break;
			case App:
				startApp(this, uiContent);
				break;
			case Bowser:
				startActivity(new Intent(this, WebBrowserActivity.class));
				break;
			default:
				LogCat.e("未知启动类型(%s)", uiContent.Type);
				break;
			}
		}
	}

	/**
	 * 启动第三方应用，如果未安装那么提交安装；反之，则打开应用
	 * 
	 * @param uiContent
	 */
	public static void startApp(Context ctx, UiContent uiContent) {
		Intent mainIntent = IntentUtils.getLauncherMainIntent(ctx,
				uiContent.StartParameters);
		if (mainIntent == null) {
			// 应用未安装 // 下载apk安装
		} else {
			startExternalActivity(ctx, mainIntent);
		}
	}

	/**
	 * 启动第三方应用
	 * 
	 * @param packageName
	 * @param activityName
	 * @return
	 */
	public boolean startActivity(String packageName, String activityName) {
		try {
			Intent newIntent = IntentUtils.newIntent(packageName, activityName);
			startActivity(newIntent);
			return true;
		} catch (Exception e) {
			LogCat.i("应用打开失败，请重试：packageName=%s, activityName=%s", packageName,
					activityName);
			return false;
		}
	}

	public static void startExternalActivity(Context ctx, Intent intent) {
		try {
			ctx.startActivity(intent);
		} catch (Exception e) {
			LogCat.i("应用打开失败，请重试：" + intent);
		}
	}

	/**
	 * 注册广播接收器,当消息列表展示不在前端时,显示消息通知栏
	 * 
	 * @describtion
	 */
	private void registerMsgReceiver() {
		mUpdateMsgReceiver = new UpdateMessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_P);
		registerReceiver(mUpdateMsgReceiver, filter);
	}

	/**
	 * 收到来自极光推送信息 显示5秒后 隐藏
	 * 
	 * @describtion
	 */
	private void noticeNewMessage() {
		notificationLayout.showMenu();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				notificationLayout.hideMenu();
			}
		}, NOTIFICATION_SHOW_TIMEOUT);
	}

	class UpdateMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_P.equals(intent.getAction())) {
				MessageInfo info = (MessageInfo) intent
						.getSerializableExtra(MessageInfo.MESSAGE_NEW);
//				把天气消息转换成 消息中心消息	
				
				OnRecvJPushMessageListener(); // 极光推送来信息
				if (null != info) {
					int j = 0;
					if (!notificationLayout.isVisible()) {
						noticeNewMessage();
					}
					j = getNewMessageCount();
					if (j > 0) {
						txtMenuMessage.setText("有 " + j + " 条新消息");
					}
				}

			}
		}
	}

	protected int getNewMessageCount() {
		return MessageManager.getInstance(OttBaseActivity.this)
				.haveNewMessageCount();
	}

	@Override
	protected void onResume() {
		int k = 0;
		k = getNewMessageCount();
		if (k > 0) {
			txtMenuMessage.setText("有 " + k + " 条新消息");
		} else {
			txtMenuMessage.setText("没有新消息");
		}
		OnRecvJPushMessageListener();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mUpdateMsgReceiver);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		if (bottomMenuLayout != null &&  bottomMenuLayout.isShown()) {
			bottomMenuLayout.hideMenuWithOutAnimation();
		}
		if (leftMenuLayout != null &&  leftMenuLayout.isShown()) {
			leftMenuLayout.hideMenuWithOutAnimation();
		}
		
		super.onStop();
	}
	/**
	 * 设置子View Click事件
	 * 
	 * @param layout
	 * @param l
	 */
	public static void setOnLayoutClickListener(ViewGroup layout,
			View.OnClickListener l) {
		int count = layout.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = layout.getChildAt(i);
			child.setOnClickListener(l);
		}
	}

	public abstract void OnRecvJPushMessageListener();
}
