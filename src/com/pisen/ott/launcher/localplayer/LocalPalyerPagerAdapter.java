package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.izy.widget.BaseListAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.ott.launcher.localplayer.AlbumData;

/**
 * 一级浏览适配器
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:10:56
 */
public abstract class LocalPalyerPagerAdapter extends BaseListAdapter<AlbumData> {

	public interface OnItemClickListener {
		void onItemClick(int position, View view, AlbumData item);
	}

	Context context;
	private OnItemClickListener itemClickListener;

	public LocalPalyerPagerAdapter(Context context) {
		this.context = context;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		this.itemClickListener = l;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AlbumData item = getItem(position);
		View v;
		if (convertView == null) {
			v = newView(context, item, parent);
		} else {
			v = convertView;
		}
		bindView(v, context, item);
		return v;
	}

	public abstract View newView(Context context, AlbumData item, ViewGroup parent);

	public abstract void bindView(View view, Context context, AlbumData item);

}
