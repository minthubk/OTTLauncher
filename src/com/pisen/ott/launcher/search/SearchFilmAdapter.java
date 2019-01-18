package com.pisen.ott.launcher.search;

import android.content.Context;
import android.izy.widget.BaseListAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.qiyi.tv.client.data.Media;

/**
 * 查询结果适配器
 * 
 * @author Liuhc
 * @version 1.0 2015年1月28日 下午4:39:59
 */
public class SearchFilmAdapter extends BaseListAdapter<Media> {

	Context context;

	/**
	 * @param context
	 */
	public SearchFilmAdapter(Context context) {
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHoldler viewHoldler = null;
		if (convertView == null) {
			viewHoldler = new ViewHoldler();
			convertView = LayoutInflater.from(context).inflate(R.layout.search_result_film_itm, null);
			viewHoldler.filmName = (TextView) convertView.findViewById(R.id.search_result_film_txt_name);
			convertView.setTag(viewHoldler);
		}else{
			viewHoldler = (ViewHoldler) convertView.getTag();
		}
		Media item = getItem(position);
		viewHoldler.filmName.setText(item.getName());
		return convertView;
	}

	class ViewHoldler{
		TextView filmName;
	}
}
