package com.pisen.ott.launcher.config;

import com.pisen.ott.launcher.config.ImageAsyncTask.TaskResult;

import android.content.Context;
import android.graphics.Bitmap;
import android.volley.BitmapLruCache;

public final class ImageLoader {

	private static ImageLoader instance;
	private Context mContext;
	private final BitmapLruCache mCache;

	private ImageLoader(Context context, BitmapLruCache cache) {
		mContext = context;
		mCache = cache;
	}

	synchronized public static void init(Context context, BitmapLruCache cache) {
		if (instance == null) {
			instance = new ImageLoader(context, cache);
		}
	}

	public static ImageTask loader(String uri, OnImageListener imageListener) {
		return instance.request(uri, imageListener);
	}

	public static void clear(String url) {
		instance.mCache.remove(url);
	}

	private ImageTask request(String uri, final OnImageListener imageListener) {
		final String cacheKey = getCacheKey(uri);
		Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
		if (cachedBitmap != null) {
			imageListener.onSuccess(cachedBitmap, true);
			return new ImageTask(null, uri);
		}

		ImageAsyncTask newRequest = new ImageAsyncTask(mContext, uri, new OnImageListener() {
			@Override
			public void onComplete(TaskResult result) {
				super.onComplete(result);
				imageListener.onComplete(result);
			}
			
			@Override
			public void onSuccess(Bitmap response, boolean isCache) {
				onGetImageSuccess(cacheKey, response, imageListener);
			}
			
			@Override
			public void onError(Throwable err) {
				onGetImageError(cacheKey, err, imageListener);
			}
		});

		newRequest.executeExt();
		return new ImageTask(newRequest, uri);
	}

	private void onGetImageSuccess(String cacheKey, Bitmap response, OnImageListener imageListener) {
		mCache.putBitmap(cacheKey, response);
		imageListener.onSuccess(response, false);
	}

	private void onGetImageError(String cacheKey, Throwable err, OnImageListener imageListener) {
		imageListener.onError(err);
	}

	private static String getCacheKey(String url) {
		return url;
	}

	public class ImageTask {
		private ImageAsyncTask mImageAsyncTask;
		private String mUri;

		public ImageTask(ImageAsyncTask task, String uri) {
			this.mImageAsyncTask = task;
			this.mUri = uri;
		}

		public String getUri() {
			return mUri;
		}

		public void cancel() {
			if (mImageAsyncTask != null) {
				mImageAsyncTask.cancel(false);
			}
		}
	}
}
