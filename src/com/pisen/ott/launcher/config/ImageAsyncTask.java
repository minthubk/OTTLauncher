package com.pisen.ott.launcher.config;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.izy.os.AsyncTaskExt;
import android.izy.os.EnvironmentUtils;
import android.izy.util.LogCat;
import android.util.DisplayMetrics;
import android.webkit.URLUtil;

import com.pisen.ott.launcher.config.ImageAsyncTask.TaskResult;

public class ImageAsyncTask extends AsyncTaskExt<Void, Void, TaskResult> {

	static final String ASSET_BASE = "file:///android_asset/";
	private Context mContext;
	private int mMaxWidth;
	private int mMaxHeight;
	private String mUri;
	private OnImageListener mOnImageListener;

	public ImageAsyncTask(Context context, String uri, OnImageListener l) {
		mContext = context;
		mUri = uri;
		mOnImageListener = l;
		// Bounds
		computeScreenSize();
	}

	private void computeScreenSize() {
		DisplayMetrics metrics = EnvironmentUtils.getResolution(mContext);
		mMaxWidth = metrics.widthPixels;
		mMaxHeight = metrics.heightPixels;
	}

	public OnImageListener getOnImageListener() {
		return mOnImageListener;
	}

	@Override
	protected TaskResult doInBackground(Void... params) {
		if (URLUtil.isAssetUrl(mUri)) {
			Bitmap assetBitmap = getImageFromAssetsFile(mContext, mUri.substring(ASSET_BASE.length()));
			return new TaskResult(mUri, assetBitmap, false, null);
		}

		Bitmap bitmap = null;
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		decodeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mUri, decodeOptions);

		int actualWidth = decodeOptions.outWidth;
		int actualHeight = decodeOptions.outHeight;
		if (actualWidth > mMaxWidth || actualHeight > mMaxHeight) {
			int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight, actualWidth, actualHeight);
			int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth, actualHeight, actualWidth);
			decodeOptions.inJustDecodeBounds = false;
			decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
			Bitmap tempBitmap = BitmapFactory.decodeFile(mUri, decodeOptions);
			if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)) {
				bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
				tempBitmap.recycle();
			} else {
				bitmap = tempBitmap;
			}
		} else {
			decodeOptions.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(mUri, decodeOptions);
		}

		if (bitmap == null) {
			String errorMessage = "图片加载失败: " + mUri;
			LogCat.e(errorMessage);
			return new TaskResult(mUri, bitmap, false, new NullPointerException(errorMessage));
		}

		return new TaskResult(mUri, bitmap, false, null);
	}

	@Override
	protected void onPostExecute(TaskResult result) {
		super.onPostExecute(result);
		if (mOnImageListener != null) {
			mOnImageListener.onComplete(result);
		}
	}

	/**
	 * 获取Assets图片
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap bitmap = null;
		AssetManager asset = context.getResources().getAssets();
		try {
			InputStream in = asset.open(fileName);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
		// If no dominant value at all, just return the actual.
		if (maxPrimary == 0 && maxSecondary == 0) {
			return actualPrimary;
		}

		// If primary is unspecified, scale primary to match secondary's
		// scaling ratio.
		if (maxPrimary == 0) {
			double ratio = (double) maxSecondary / (double) actualSecondary;
			return (int) (actualPrimary * ratio);
		}

		if (maxSecondary == 0) {
			return maxPrimary;
		}

		double ratio = (double) actualSecondary / (double) actualPrimary;
		int resized = maxPrimary;
		if (resized * ratio > maxSecondary) {
			resized = (int) (maxSecondary / ratio);
		}
		return resized;
	}

	static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
		double wr = (double) actualWidth / desiredWidth;
		double hr = (double) actualHeight / desiredHeight;
		double ratio = Math.min(wr, hr);
		float n = 1.0f;
		while ((n * 2) <= ratio) {
			n *= 2;
		}

		return (int) n;
	}

	static public class TaskResult {

		public final String uri;
		public final Bitmap bitmap;
		public final boolean isCache;
		public final Throwable err;

		public TaskResult(String uri, Bitmap bitmap, boolean isCache, Throwable err) {
			super();
			this.uri = uri;
			this.bitmap = bitmap;
			this.isCache = isCache;
			this.err = err;
		}

		public boolean isSuccess() {
			return bitmap != null;
		}

	}
}
