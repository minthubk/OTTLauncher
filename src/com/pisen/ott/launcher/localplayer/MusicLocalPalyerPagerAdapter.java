package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.AlbumData;

/**
 * 一级浏览适配器
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:10:56
 */
public class MusicLocalPalyerPagerAdapter extends LocalPalyerPagerAdapter {

	public MusicLocalPalyerPagerAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(Context context, AlbumData item, ViewGroup parent) {
		return new FileBrowserIconView(context);
	}

	@Override
	public void bindView(final View view, Context context, final AlbumData item) {
		FileBrowserIconView iconView = (FileBrowserIconView) view;
		iconView.setIconText(item.title, item.count);

		// 无缩略图,设置默认音乐图片
		if (TextUtils.isEmpty(item.thumbnailUrl)) {
			iconView.setIconImageBitmapRes(R.drawable.local_default_music);
		} else {
			iconView.setIconImageBitmapRes(R.drawable.local_default_music);//设置默认音乐图片
			Bitmap bitmap = BitmapFactory.decodeFile(item.thumbnailUrl);
			if (bitmap != null) {
				iconView.setIconImageBitmap(bitmap);
			} 
		}
	}
}
