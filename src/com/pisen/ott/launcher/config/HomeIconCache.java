package com.pisen.ott.launcher.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;

import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.config.ImageAsyncTask.TaskResult;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;

/**
 * 主页图标缓存
 * 
 * @author yangyp
 */
public class HomeIconCache {

	static public class CacheEntry {
		public Bitmap icon;
		//public String title;
		//public Bitmap normal;
		//public Bitmap selected;
	}

	public interface IconItemCallback {
		void onIconItem(UiContent ui, Bitmap icon);
	}

	private LauncherApplication appContext;
	private boolean isRemoveAll = false;
	private final HashMap<String, CacheEntry> mCache = new HashMap<String, CacheEntry>(10);

	public HomeIconCache(LauncherApplication context) {
		appContext = context;
	}

	public void initialize(IconItemCallback callback) {
		isRemoveAll = false;
		LauncherConfig config = LauncherApplication.getConfig();
		List<UiContent> uiHome = config.getUiVersion().getHome();
		if (uiHome != null) {
			for (UiContent ui : uiHome) {
				update(ui, callback);
			}
		}
	}

	public CacheEntry get(String displayCode) {
		return mCache.get(displayCode);
	}

	/**
	 * 根据UI更新icon
	 * 
	 * @param ui
	 * @param callback
	 */
	public void update(final UiContent ui, final IconItemCallback callback) {
		if (ui != null) {
			if (ui.LayerLevel == 1) {
				final CacheEntry entry = new CacheEntry();
				synchronized (mCache) {
					mCache.put(ui.DisplayCode, entry);
				}

				ImageLoader.loader(ui.Image, new OnImageListener() {
					@Override
					public void onComplete(TaskResult result) {
						super.onComplete(result);
						if (result.isSuccess()) {
							synchronized (mCache) {
								entry.icon = result.bitmap;
							}
						} 
						setIconItemCallback(ui, callback, entry);
					}
				});
			}
		}
	}

	/**
	 * 设置回调
	 * 
	 * @param ui
	 * @param callback
	 * @param entry
	 */
	private void setIconItemCallback(UiContent ui, IconItemCallback callback, CacheEntry entry) {
		if (callback != null && !isRemoveAll) {
			callback.onIconItem(ui, entry.icon);
		}
	}

	/**
	 * Empty out the cache.
	 */
	public void removeAll() {
		isRemoveAll = true;
		synchronized (mCache) {
			Collection<CacheEntry> entryValues = mCache.values();
			for (CacheEntry en : entryValues) {
				bitmapRecycle(en.icon);
				//bitmapRecycle(en.selected);
			}
			mCache.clear();
		}
	}

	private static void bitmapRecycle(Bitmap bit) {
		if (bit != null && bit.isRecycled()) {
			bit.recycle();
			bit = null;
		}
	}

}
