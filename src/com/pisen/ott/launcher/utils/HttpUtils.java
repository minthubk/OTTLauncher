package com.pisen.ott.launcher.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 网络请求URL地址
 * 
 * @author Liuhc
 * @version 1.0 2014年12月3日 下午1:56:43
 */
public class HttpUtils {

	/**
	 * 更新服务和菜单
	 */
//	public static final String URL_UPDATE = "http://file.pisen.com.cn/ott/OTT001/system/system.json";
//	public static final String URL_UPDATE = "http://file.v3.huiyuanti.com:9212/OTT/OTT001/system/system.json";
	 public static final String URL_UPDATE = "http://test.file.pisen.com.cn:9212/OTT/OTT001/system/system.json";

	/**
	 * 用来判断网络是否可用
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo[] infoList = cm.getAllNetworkInfo();
			for (NetworkInfo info : infoList) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	//判断是wifi网络
	public static boolean isWifi(Context context) {   
        ConnectivityManager cm = (ConnectivityManager) context   
                .getSystemService(Context.CONNECTIVITY_SERVICE);   
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();   
        if (networkINfo != null   
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {   
            return true;   
        }   
        return false;   
    }
	
	//判断WIFI是否打开
	public static boolean isWifiEnabled(Context context) {
		ConnectivityManager mgrConn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mgrTel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return ((mgrConn.getActiveNetworkInfo() != null && mgrConn.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
	}
	// /**
	// * 检测网络是否连接
	// * @return
	// */
	// public static boolean isNetworkConnected(Context ctx) {
	// ConnectivityManager cm = (ConnectivityManager)
	// ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	// if (cm == null) {
	// return false;
	// }
	// NetworkInfo ni = cm.getActiveNetworkInfo();
	// if (ni == null) {
	// return false;
	// }
	// return ni.isAvailable();
	// }
}
