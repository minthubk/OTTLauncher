package com.pisen.ott.launcher.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.izy.content.IntentUtils;
import android.izy.util.StringUtils;
import android.izy.widget.BaseListAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.config.OnImageListener;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.utils.AppUtils;
import com.pisen.ott.launcher.widget.SearchDownloadItemView;

/**
 * 查询结果适配器
 * 
 * @author Liuhc
 * @version 1.0 2015年1月28日 下午4:39:59
 */
public class SearchAdapter extends BaseListAdapter<UiContent> {

	Context context;

	/**
	 * @param context
	 */
	public SearchAdapter(Context context) {
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout./*launcher_category_detail_grid_item*/launcher_search_item, null);
		}
		final SearchDownloadItemView itemDownload = (SearchDownloadItemView) convertView.findViewById(R.id.itemDownload);
		UiContent item = getItem(position);
		itemDownload.setName(item.Name);
		if(SearchActivity.TYPE_INSTALLED.equals(item.Type)){
			Drawable drawable = AppUtils.loadAppIcon(context, item.ApkFile);
			if (drawable != null) {
				itemDownload.setAppIcon(drawable);
			}
		}else{
			String imgUrl = item.Image;
			if (!StringUtils.isEmpty(imgUrl)) {
				ImageLoader.loader(imgUrl, new OnImageListener() {
					@Override
					public void onSuccess(Bitmap response, boolean isCache) {
//					itemDownload.setBackground(new BitmapDrawable(context.getResources(), response));
						itemDownload.setAppIcon(response);
					}
				});
			}
		}

		return convertView;
	}

}
