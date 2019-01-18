package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
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
public class ImageLocalPalyerPagerAdapter extends LocalPalyerPagerAdapter {

	public ImageLocalPalyerPagerAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(Context context, AlbumData item, ViewGroup parent) {
		FileBrowserIconView f = new FileBrowserIconView(context);
		f.setIconBound(context.getResources().getDimensionPixelSize(R.dimen.local_player_image_width)
				, context.getResources().getDimensionPixelSize(R.dimen.local_player_image_height));
		return f;
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
				// 无缩略图,设置默认图片
				iconView.setIconImageBitmapRes(R.drawable.local_image);
			}
		} else {
			iconView.setIconImageBitmapRes(R.drawable.local_image);
			setThum(iconView.findViewById(R.id.imgIcon), item);
		}
	}
	
	/**
	 * 异步获得第一张缩略图
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
							((ImageView)v).setImageResource(R.drawable.local_image);
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
						m.obj = getImageThumbnail(item.path, 300, 300);
						handler.sendMessage(m);
					}
				}).start();
			}else{
				// 无缩略图,设置默认图片
				((ImageView)v).setImageResource(R.drawable.local_image);
			}
		}
	}
	
	/** 获取图片文件的缩略图 */
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}
