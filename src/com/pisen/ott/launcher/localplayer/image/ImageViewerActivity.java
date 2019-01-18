package com.pisen.ott.launcher.localplayer.image;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.izy.util.LogCat;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.volley.BitmapLruCache;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.pisen.ott.launcher.AppRecommendActivity;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.config.OnImageListener;
import com.pisen.ott.launcher.localplayer.MediaBrowserActivity;
import com.pisen.ott.launcher.localplayer.music.MusicPlayerActivity;

//import com.pisen.ott.launcher.config.ImageAsyncTask;

/**
 * 图片浏览器Activity
 */
public class ImageViewerActivity extends Activity implements ViewFactory {
	
	private static List<String> imgPathList;// 图片路径列表
	private static int currIndex = -1;

	private ImageSwitcher imgViewer;
	private TextView txtStatus;

	private BitmapLruCache cache;
	private ImageAsyncTask newRequest;
	Bitmap bm;
	String filePath = "";

	static final String DirectionLeft = "left";
	static final String DirectionRight = "right";
	
	public static void start(Context context, List<String> playbacklist, int currIndex) {
		ImageViewerActivity.imgPathList = playbacklist;
		ImageViewerActivity.currIndex = currIndex;		
		context.startActivity(new Intent(context, ImageViewerActivity.class));
	}

	@Override
	protected void onDestroy() {
		ImageViewerActivity.imgPathList = null;
		ImageViewerActivity.currIndex = -1;
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_viewer_act);
		imgViewer = (ImageSwitcher) findViewById(R.id.imgViewer);
		imgViewer.setFactory(this);
		imgViewer.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		imgViewer.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		txtStatus = (TextView) findViewById(R.id.txtStatus);

		// 建立缓存
		cache = BitmapLruCache.getInstance(this);
		showImageView(currIndex);
	}

	// 获得系统可用内存信息
	private String getSystemAvaialbeMemorySize() {
		// 获得MemoryInfo对象
		MemoryInfo memoryInfo = new MemoryInfo();
		// 获得系统可用内存，保存在MemoryInfo对象上
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActivityManager.getMemoryInfo(memoryInfo);
		long memSize = memoryInfo.availMem;

		// 字符类型转换
		String availMemStr = formateFileSize(memSize);

		return availMemStr;
	}

	// 调用系统函数，字符串转换 long -String KB/MB
	private String formateFileSize(long size) {
		return Formatter.formatFileSize(this, size);
	}

	public void prevImage() {
		currIndex = getCurrentIndex(--currIndex);
		showImageView(currIndex);
	}

	public void nextImage() {
		currIndex = getCurrentIndex(++currIndex);
		showImageView(currIndex);
	}

	private int getCurrentIndex(int index) {
		if (index < 0) {
			index = imgPathList.size() - 1;
		} else if (index > imgPathList.size() - 1) {
			index = 0;
		}
		return index;
	}

	private void showImageView(int index) {
		if (imgPathList == null || currIndex >= imgPathList.size()) {
			Toast.makeText(this, "播放出错", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (index < 0) {
			index = 0;
		} else if (index > imgPathList.size() - 1) {
			index = imgPathList.size() - 1;
		}

		LogCat.i("image index = %s", index);
		final String filePath = imgPathList.get(index);
		txtStatus.setText(String.format("%s/%s", index + 1, imgPathList.size()));
		if (cache.get(filePath) != null) {
			Bitmap cacheBm = cache.get(filePath);
			setImg(cacheBm);
		} else {
			if (newRequest != null/*
								 * &&newRequest.getStatus() ==
								 * android.os.AsyncTask.Status.RUNNING
								 */) {
				newRequest.cancel(true);
				// newRequest = null;
			}
			// imgViewer.setImageDrawable(null);
			newRequest = new ImageAsyncTask(this, filePath, new OnImageListener() {
				@Override
				public void onSuccess(Bitmap response, boolean isCache) {
					if (response != null) {
						Log.i("testMsg", "add cache " + currIndex);
						// WeakReference<Bitmap> wrf = new
						// WeakReference<Bitmap>(response);
						cache.put(filePath, response);
						setImg(response);
					}
				}
			});
			// newRequest.executeExt();
			newRequest.execute();
		}
	}

	private void setImg(Bitmap b) {
		BitmapDrawable id = new BitmapDrawable(getResources(), b);
		imgViewer.setImageDrawable(id);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Toast.makeText(this, " cacheSize = "+cache.size(),
		// Toast.LENGTH_SHORT).show();
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			prevImage();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			nextImage();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public View makeView() {
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0xFF000000);
		i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return i;
	}
}
