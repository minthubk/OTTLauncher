package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.AlbumData;

/**
 * 一级浏览适配器
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:10:56
 */
public class VideoLocalPalyerPagerAdapter extends LocalPalyerPagerAdapter {

	public VideoLocalPalyerPagerAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(Context context, AlbumData item, ViewGroup parent) {
		FileBrowserIconView view = new FileBrowserIconView(context);
		view.setIconBound(context.getResources().getDimensionPixelSize(R.dimen.local_player_image_width)
				, context.getResources().getDimensionPixelSize(R.dimen.local_player_image_height));
		return view;
	}

	@Override
	public void bindView(final View view, Context context, final AlbumData item) {
		FileBrowserIconView iconView = (FileBrowserIconView) view;
		iconView.setIconText(item.title, item.count);

		if (!TextUtils.isEmpty(item.thumbnailUrl)) {
			Bitmap bitmap = BitmapFactory.decodeFile(item.thumbnailUrl);
			if (bitmap != null) {
				iconView.setIconImageBitmap(bitmap);
			} else {
				// 无缩略图,设置默认视频图片
				iconView.setIconImageBitmapRes(R.drawable.local_video);
			}
		} else {
			iconView.setIconImageBitmapRes(R.drawable.local_video);//先设置默认图片
			setThum(iconView.findViewById(R.id.imgIcon), item);//异步压缩并加载
		}
	}
	
	/**
	 * 异步获得视频集缩略图
	 * @param v
	 * @param item
	 */
	public static void setThum(final View v, final AlbumData item){
		if (v instanceof ImageView) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.what == 1) {
						Bitmap b = (Bitmap) msg.obj;
						if (b != null) {
							((ImageView)v).setImageBitmap(b);
						}else{
							// 无缩略图,设置默认图片
							((ImageView)v).setImageResource(R.drawable.local_video);
						}
					}
				}
			};
			if (!TextUtils.isEmpty(item.path)) {
				(new Thread() {
					@Override
					public void run() {
						Message m = new Message();
						m.what = 1;
						m.obj = ThumbnailUtils.createVideoThumbnail(item.path, Thumbnails.MINI_KIND);
						handler.sendMessage(m);
					}
				}).start();
			}else{
				// 无缩略图,设置默认音乐图片
				((ImageView)v).setImageResource(R.drawable.local_video);
			}
		}
	}
}
