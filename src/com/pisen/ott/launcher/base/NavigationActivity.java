package com.pisen.ott.launcher.base;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.izy.os.EnvironmentUtils;
import android.izy.util.LogCat;
import android.izy.util.parse.GsonUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.ott.common.WeatherInfo;
import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.config.OnImageListener;
import com.pisen.ott.launcher.config.UiContentObservable;
import com.pisen.ott.launcher.config.UiContentObservable.UiContentObserver;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.utils.DateUtils;

/**
 * 导航基类(包含天气,USB,消息图标 等事件监听;不需要的继承DefaultActivity基类即可)
 * @version 1.0 2015年2月11日 上午9:18:06
 */
public abstract class NavigationActivity extends OttBaseActivity implements UiContentObserver {
	// 天气广播更新action
	public final static String ACTION_WEATHER_UPDATE = "android.action.ottbox.weather.update";
	public final static String ACTION_WEATHER_BROADCAST	 = "android.action.ottbox.weather.broadcast";
	public final static String KEY_WEATHER_BROADCAST_TITLE = "key.weather.broadcast.title";
	public final static String KEY_WEATHER_BROADCAST_CONTENT = "key.weather.broadcast.content";
	public final static String CONSTANT_WEATHER_UPDATE = "天气更新：";
	public final static String USB_VALIDE_ALLWINNER = "/storage/external_storage/sda2";
	public final static String USB_VALIDE_AMLOGIC = "/storage/external_storage/sda1";
	private UiContentObservable mObservable;
	private TextView txtUsbDevice;
	private TextView txtWifiState;
	private TextView txtMessageIcon;
	private TextView txtEthernetState;
	private TextView txtBluetooth;
	private LinearLayout weatherLayout;
	private TextView txtWeatherInfo;
	private ConnectivityManager mConnectMgr;
	private NetworkInfo mNetInfo;
	private BluetoothAdapter blutoothAdapter ;
	public static final int USB_STATE_ON    = 0x00021;
	public static final int USB_STATE_OFF   = 0x00022;
	public static final int WIFI_STATE_ON   = 0x00023;
	public static final int WIFI_STATE_OFF  = 0x00024;
	public static final int ETHER_STATE_ON  = 0x00025;
	public static final int ETHER_STATE_OFF = 0x00026;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarView(R.layout.launcher_action_bar);
		initUIActionBarView();
		mObservable = LauncherApplication.getConfig().getObservable();
		mObservable.registerObserver(this);
		mConnectMgr= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		registerNetWorkReceiver();
		registerUSBReceiver();
		registerWeatherReceiver(); 	// used for receiving weather info
		registerBluetoothReceiver();
		
		blutoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	
	public void initUIActionBarView() {
		View actionBar = getOttActionBar();
		txtUsbDevice = (TextView) actionBar.findViewById(R.id.txtUsbDevice);
		txtUsbDevice.setAlpha(0.5f);
		txtWifiState = (TextView) actionBar.findViewById(R.id.txtWifiState);
		txtWifiState.setAlpha(0.5f);
		txtEthernetState =  (TextView) actionBar.findViewById(R.id.txtEthernetState);
		txtEthernetState.setAlpha(0.5f);
		setNetWorkState(View.GONE, View.VISIBLE);
		
		weatherLayout = (LinearLayout) actionBar.findViewById(R.id.weatherLayout);
		txtWeatherInfo = (TextView) actionBar.findViewById(R.id.txtWeatherInfo);
		txtMessageIcon = (TextView)actionBar.findViewById(R.id.txtMessageIcon);
		txtBluetooth = (TextView) actionBar.findViewById(R.id.txtBluetooth);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		if (haveInsertUsbDevice() && null != mHandler){
//			final Message msg = mHandler.obtainMessage();
//			msg.what = USB_STATE_ON;
//			mHandler.sendMessage(msg);
//		}
		if (blutoothAdapter != null) {
			txtBluetooth.setVisibility(blutoothAdapter.isEnabled() ? View.VISIBLE : View.GONE);
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mObservable != null) {
			mObservable.unregisterObserver(this);
		}
		unregisterReceiver(usbStateDetect);
		unregisterReceiver(networkStateDetect);
		unregisterReceiver(weatherUpdateReceiver);
		unregisterReceiver(bluetoothReceiver);
		super.onDestroy();
	}

	@Override
	public void onChangedContent(UiContent obj) {
		
	}

	/**
	 * 刷新当前view背景图(及选中图片)
	 * 
	 * @param view
	 */
	protected void refreshImage(final View view, String drawablePath) {
		ImageLoader.loader(drawablePath, new OnImageListener() {
			@Override
			public void onSuccess(Bitmap response, boolean isCache) {
				if (view instanceof ImageView) {
					((ImageView) view).setImageBitmap(response);
					return;
				}
				view.setBackground(new BitmapDrawable(getResources(), response));
			}
		});
	}
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case USB_STATE_ON:
				txtUsbDevice.setVisibility(View.VISIBLE);
				showMenuUsb();
				break;
			case USB_STATE_OFF:
				txtUsbDevice.setVisibility(View.GONE);
				hideMenuUsb();
				break;
			case WIFI_STATE_ON:
				setCompoundDrawablesLeft(R.drawable.menu_wifi, txtWifiState);
				break;
			case ETHER_STATE_ON:
				setCompoundDrawablesLeft(R.drawable.menu_ic_cable, txtEthernetState);
				break;
			case ETHER_STATE_OFF:
				setCompoundDrawablesLeft(R.drawable.menu_ic_disconnected, txtEthernetState);
				break;
			}
			super.handleMessage(msg);
		}
	};

	public  BroadcastReceiver usbStateDetect = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {   
			String action = intent.getAction();
			final Message msg = mHandler.obtainMessage();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				msg.what = USB_STATE_ON;
			} else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
				msg.what = USB_STATE_OFF;
			}
			mHandler.sendMessage(msg);
		}
	};

	public BroadcastReceiver  networkStateDetect = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			final Message msg = mHandler.obtainMessage();
//			Toast.makeText(context, "action:"+action,Toast.LENGTH_LONG).show();
			if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)||
				"android.net.ethernet.ETH_STATE_CHANGED".equals(action)){
				mNetInfo = mConnectMgr.getActiveNetworkInfo();
				if (mNetInfo != null && mNetInfo.isAvailable()){
					switch (mNetInfo.getType()) {
					case ConnectivityManager.TYPE_WIFI:
//						Toast.makeText(context, "EthConnectedState:"+isEthConnected(context),Toast.LENGTH_LONG).show();
						 if (!isEthConnected(context)){
							 setNetWorkState(View.VISIBLE, View.GONE);
							 msg.what = WIFI_STATE_ON;
							 mHandler.sendMessage(msg);
						 }
						 break;
					case ConnectivityManager.TYPE_ETHERNET:
						 setNetWorkState(View.GONE, View.VISIBLE);
						 msg.what = ETHER_STATE_ON;
						 mHandler.sendMessage(msg);
						 break;
					}
						
				}else {
					//  网络断开
						setNetWorkState(View.GONE, View.VISIBLE);
						msg.what = ETHER_STATE_OFF;
						mHandler.sendMessage(msg);
				}
			}
		}
	};
	
	/**
	 * @des                网络连接类型描述
	 * @param wifiState    wifi_icon  Visible|Gone
	 * @param etherState   ether_icon Gone | Visible
	 */
	private void setNetWorkState(int wifiState,int etherState){
		if (null != txtWifiState && null != txtEthernetState){
			txtWifiState.setVisibility(wifiState);
			txtEthernetState.setVisibility(etherState);
		}
	}
	
	/**
	 * @describtion 设置替换wifi图标
	 * @author mahuan
	 * @version 1.0 ,2015年1月7日 下午12:02:03
	 */
	public void setCompoundDrawablesLeft(int id, TextView tv) {
		Drawable drawable = getResources().getDrawable(id);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv.setCompoundDrawables(drawable, null, null, null);
	}

	/**
	 * @des USB接收器
	 */
	public void registerUSBReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		this.registerReceiver(usbStateDetect, filter);
	}

	
	/**
	 * @des 注册网络接收器
	 */
	private void registerNetWorkReceiver(){
		IntentFilter ifilter = new IntentFilter();
		ifilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		ifilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ifilter.addAction("android.net.ethernet.ETH_STATE_CHANGED");
		this.registerReceiver(networkStateDetect, ifilter);
	}
	
	/**
	 * @describtion
	 * @param context
	 * @return 以太网连接状态断读
	 */
	public  boolean isEthConnected(Context context) {
		 NetworkInfo info  = mConnectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		if (null != info && State.CONNECTED == info.getState()){
            return true;
        }else{
            return false;
        }
	}

	/**
	 * 刷新天气信息
	 * @param info
	 */
	public void refreshWeather(WeatherInfo info) {
		if (info != null) {
			LogCat.i("<update> refresh the weather info = "+info);
			if (DateUtils.isNight()) {
				txtWeatherInfo.setText(info.nightWeather + "  " + info.nightTemp + "℃ ("+info.cityName+")");
			} else {
				txtWeatherInfo.setText(info.dayWeather + "  " + info.dayTemp + "℃ ("+info.cityName+")");
			}
			if (info.dayWeatherNum != null && !"".equals(info.dayWeatherNum)) {
				int weatherNum = Integer.parseInt(info.dayWeatherNum);
				if (weatherNum == 0) {
					txtWeatherInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.weather_sunny, 0, 0, 0);
				} else if (weatherNum == 1) {
					txtWeatherInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.weather_cloudy, 0, 0, 0);
				} else if (weatherNum == 2) {
					txtWeatherInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.weather_overcast, 0, 0, 0);
				} else if (weatherNum == 3 || weatherNum == 21 || (7 <= weatherNum && weatherNum <= 9)) {
					txtWeatherInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.weather_rain, 0, 0, 0);
				} else if (weatherNum == 6 || (13 <= weatherNum && weatherNum <= 19) || (26 <= weatherNum && weatherNum <= 28)) {
					txtWeatherInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.weather_snow, 0, 0, 0);
				} else if (weatherNum == 4 || weatherNum == 5 || (10 <= weatherNum && weatherNum <= 12) || (22 <= weatherNum && weatherNum <= 25)) {
					txtWeatherInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.weather_bigrain, 0, 0, 0);
				}
				weatherLayout.setVisibility(View.VISIBLE);
			}
			LogCat.i("<update> refresh the weather info success");
		}
	}

	/**
	 * 注册天气监听广播
	 */
	public void registerWeatherReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(ACTION_WEATHER_UPDATE);
		registerReceiver(weatherUpdateReceiver, filter);
		LogCat.i("<register> register the Receiver for updating weather");
		setWeather(null);
	}

	/**
	 * 天气更新广播
	 */
	private BroadcastReceiver weatherUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogCat.i("<update> received the new weather info.");
			if (ACTION_WEATHER_UPDATE.equals(intent.getAction())) {
				// 天气json
				String ACTION_WEATHER_DATA = "WeatherInfo";
				String json = intent.getStringExtra(ACTION_WEATHER_DATA);
				if (!TextUtils.isEmpty(json)) {
					//保存天气到本地
					LauncherApplication.getConfig().setLastWeatherInfo(json);
					setWeather(context);
				}
			}
		}
	};
	
	/**
	 * 设置天气信息
	 */
	public void setWeather(Context ctx){
		String info = LauncherApplication.getConfig().getLastWeatherInfo();
		if (!TextUtils.isEmpty(info)) {
			WeatherInfo i = GsonUtils.jsonDeserializer(info, WeatherInfo.class);
			refreshWeather(i);
			if (ctx != null) {
				// 发广播给消息中心
				sendWeatherUpdateToMessage(i,ctx);
			}
		}
	}
	
	/**
	 * @des   将天气更新消息,插入本地数据库
	 * @param i
	 * @param context
	 */
	private void sendWeatherUpdateToMessage(WeatherInfo i,Context context){
		Bundle b = new Bundle();
		b.putString(KEY_WEATHER_BROADCAST_TITLE, CONSTANT_WEATHER_UPDATE + i.getCityName() + i.getReleaseTime());
		b.putString(KEY_WEATHER_BROADCAST_CONTENT, i.toString());
		context.sendBroadcast(new Intent(ACTION_WEATHER_BROADCAST).putExtras(b));
	}
	
	/**
	 * @describtion
	 * @return 是否开机插入Usb设备
	 */
	private Boolean haveInsertUsbDevice(){
		String [] paths = EnvironmentUtils.getExternalStorageDirectoryAll();
		return Arrays.asList(paths).contains(USB_VALIDE_AMLOGIC)||Arrays.asList(paths).contains(USB_VALIDE_ALLWINNER);
	}
	
	
	/**
	 * @des 显示消息图标
	 */
	public void showMessageIcon(){
		if (null != txtMessageIcon){
			txtMessageIcon.setVisibility(View.VISIBLE);
			txtMessageIcon.setAlpha(0.5f);
		}
	}
	
	/**
	 * @des 隐藏消息图标
	 */
	public void hideMessageIcon(){
		if (null != txtMessageIcon){
			txtMessageIcon.setVisibility(View.GONE);
		}
	}
	
	/**
	 * @des 接收到有极光推送消息
	 */
	@Override
	public void OnRecvJPushMessageListener() {
		if (getNewMessageCount() > 0){
			showMessageIcon();
		}else {
			hideMessageIcon();
		}
	}
	/**
	 * 注册蓝牙状态广播
	 */
	public void registerBluetoothReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(bluetoothReceiver, filter);
		LogCat.i("<register> register the Receiver for bluetooth state changed");
	}
	
	private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Broadcast receiver is always running on the UI thread here,
			// so we don't need consider thread synchronization.
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			switch (state) {
			case BluetoothAdapter.STATE_ON:
				txtBluetooth.setVisibility(View.VISIBLE);
				break;
			case BluetoothAdapter.STATE_OFF:
				txtBluetooth.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		}
	};
}
