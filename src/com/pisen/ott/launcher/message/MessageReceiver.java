package com.pisen.ott.launcher.message;


import com.pisen.ott.launcher.base.NavigationActivity;

import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.izy.util.LogCat;
import android.os.Bundle;
import android.util.Log;

/**
 * @desc    极光推送消息接收器
 * @author  mahuan
 * @version 1.0 2015年3月5日 上午9:56:42
 * @updated [2015年3月5日 上午9:56:42]:
 */
public class MessageReceiver extends BroadcastReceiver {
	private static final String TAG = "[JPush]";
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		LogCat.d("%s, onReceive -%s, extras: %s\n", TAG, intent.getAction() , printBundle(bundle));
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			LogCat.d("%s, 接收Registration Id :%s\n", TAG, regId);
			// send the Registration Id to your server...
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			LogCat.d("%s, 接收到推送下来的自定义消息: %s\n", TAG, bundle.getString(JPushInterface.EXTRA_MESSAGE));
			MessageManager.getInstance(context).addMessage(intent);
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
			int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			LogCat.d("%s, 接收到推送下来的通知的ID:%s",TAG, notifactionId);
			MessageManager.getInstance(context).addMessage(intent);
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			LogCat.d("%s,%s\n",TAG, "用户点击打开了通知");//用户点击打开了通知,跳转到消息中心处理.
			JPushInterface.reportNotificationOpened(context,bundle.getString(JPushInterface.EXTRA_MSG_ID));
			// 打开自定义的Activity
			Intent i = new Intent(context, MessageCenterActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
			// 富媒体链接
			LogCat.d("%s,用户收到到RICH PUSH CALLBACK:%s\n",TAG, bundle.getString(JPushInterface.EXTRA_EXTRA));
		} else if (NavigationActivity.ACTION_WEATHER_BROADCAST.equals(intent.getAction())){
			// 收到天气更新广播
			MessageManager.getInstance(context).addWeatherMessage(intent);
		}else {
			LogCat.d("%s,Unhandled intent - %s\n",TAG, intent.getAction());
		}
	}

	/**
	 * 打印所有的 intent extra 数据
	 * @param bundle
	 * @return String
	 */
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
}
