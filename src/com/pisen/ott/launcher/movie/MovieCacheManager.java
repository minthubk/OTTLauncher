package com.pisen.ott.launcher.movie;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.qiyi.tv.client.data.Media;

public class MovieCacheManager {

	private static ImageMemoryCache imageMemoryCache;


	private static MovieCacheManager instance;
	private static Object mutex = new Object();

	//推荐 
	private List<Media> recommendNormalList = new ArrayList<Media>();
	private List<Media> recommendMiddleList = new ArrayList<Media>();
	private List<Media> recommendLargeList = new ArrayList<Media>();
	
	private SparseArray<List<Media>> channelList = new SparseArray<List<Media>>();
//	private List<Media> filmList = new ArrayList<Media>();

	
	private MovieCacheManager() {
	}

	public static MovieCacheManager getInstance(Application app) {
		synchronized (mutex) {
			if (instance == null) {
				instance = new MovieCacheManager();
				imageMemoryCache = new ImageMemoryCache(app);
			}
			return instance;
		}
	}

	public void addRecommendNormalCache(Media media, Bitmap bm) {
		if (!recommendNormalList.contains(media)) {
			recommendNormalList.add(media);
		}
		imageMemoryCache.addBitmapToCache(media, bm);
	}

	public void addRecommendMiddleCache(Media media, Bitmap bm) {
		if (!recommendMiddleList.contains(media)) {
			recommendMiddleList.add(media);
		}
		imageMemoryCache.addBitmapToCache(media, bm);
	}

	public void addRecommendLargeCache(Media media, Bitmap bm) {
		if (!recommendLargeList.contains(media)) {
			recommendLargeList.add(media);
		}
		imageMemoryCache.addBitmapToCache(media, bm);
	}
	
	public void setRecommendNormalList(List<Media> recommendNormalList) {
		this.recommendNormalList = recommendNormalList;
	}

	public void setRecommendMiddleList(List<Media> recommendMiddleList) {
		this.recommendMiddleList = recommendMiddleList;
	}

	public void setRecommendLargeList(List<Media> recommendLargeList) {
		this.recommendLargeList = recommendLargeList;
	}
	
	public List<Media> getRecommendNormalList() {
		return recommendNormalList;
	}

	public List<Media> getRecommendMiddleList() {
		return recommendMiddleList;
	}

	public List<Media> getRecommendLargeList() {
		return recommendLargeList;
	}

	public List<Bitmap> getRecommendNormalCache(int requiredSize) {
		List<Bitmap> ret = new ArrayList<Bitmap>();
		int size = recommendNormalList.size();
		int count = 0;
		Bitmap bm = null;
		for (int i = 0; i < size; i++) {
			bm = imageMemoryCache.getBitmapFromCache(recommendNormalList.get(i));
			if (bm != null) {
				count++;
				ret.add(bm);
				if (count == requiredSize)
					return ret;
			}
		}
		return ret;
	}
	
	public List<Bitmap> getRecommendMiddleCache(int requiredSize) {
		List<Bitmap> ret = new ArrayList<Bitmap>();
		int size = recommendMiddleList.size();
		int count = 0;
		Bitmap bm = null;
		for (int i = 0; i < size; i++) {
			bm = imageMemoryCache.getBitmapFromCache(recommendMiddleList.get(i));
			if (bm != null) {
				count++;
				ret.add(bm);
				if (count == requiredSize)
					return ret;
			}
		}
		return ret;
	}
	
	public List<Bitmap> getRecommendLargeCache(int requiredSize) {
		List<Bitmap> ret = new ArrayList<Bitmap>();
		int size = recommendLargeList.size();
		int count = 0;
		Bitmap bm = null;
		for (int i = 0; i < size; i++) {
			bm = imageMemoryCache.getBitmapFromCache(recommendLargeList.get(i));
			if (bm != null) {
				count++;
				ret.add(bm);
				if (count == requiredSize)
					return ret;
			}
		}
		return ret;
	}
	
	public List<Media> getCachedChannelList(int channelId) {
		return channelList.get(channelId);
	}

	public void setCachedChannelList(List<Media> list,int channelId) {
		channelList.put(channelId, list);
	}

	public void addChannelCache(Media media, Bitmap bm,int channelId) {
		List<Media> filmList = getCachedChannelList(channelId);
		if(filmList == null){
			filmList = new ArrayList<Media>();
			filmList.add(media);
		}else{
			if (!filmList.contains(media)) {
				filmList.add(media);
			}
		}
		imageMemoryCache.addBitmapToCache(media, bm);
	}
	
	public Bitmap getBitmap(Media media){
		return imageMemoryCache.getBitmapFromCache(media);
	}

}
