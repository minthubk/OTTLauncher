package com.pisen.ott.launcher.appmanage;

import android.content.Context;
import android.izy.widget.BaseListAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.AppManageItemView;

/**
 * 应用程序适配器
 * @author Liuhc
 * @version 1.0 2015年4月17日 下午5:02:27
 */
public class AppManageAdapter extends BaseListAdapter<AppInfo> {

	Context context;

	public AppManageAdapter(Context context) {
		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.app_manage_item, null);
		}
		AppManageItemView itemApp = (AppManageItemView) convertView.findViewById(R.id.itemAppManage);
		AppInfo info = getItem(position);
		if (info != null) {
			itemApp.setName(info.getAppName());
			if (info.getAppIcon() != null) {
//				itemApp.setBackground(info.getAppIcon());
				itemApp.setAppIcon(info.getAppIcon());
			}
		}
		return convertView;
	}
}