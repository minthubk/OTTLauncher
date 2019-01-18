package com.pisen.ott.launcher.movie;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pisen.ott.launcher.R;
import com.qiyi.tv.client.Result;
import com.qiyi.tv.client.data.Media;
import com.qiyi.tv.client.feature.common.PageType;
import com.qiyi.tv.client.feature.common.RecommendationType;

/**
 * @Description 电影
 * @author hegang
 *
 */
public class RecommendFragment extends BaseFragment implements OnClickListener {

	private String TAG = RecommendFragment.class.getSimpleName();
	private List<ImageView> ivRecomendList;// 7个普通大小的
	private List<ImageView> ivBigestList;// 2个最大的，
	private List<ImageView> ivMidlleList;// 3个中等的，
	private View viewHistory, viewFavorite, viewSearch;
	private boolean inited = false;
	public enum RecommendImgType {
		Biggest, Middle, Common
	}


	public RecommendFragment(Handler handler) {
		super(handler);
	}

	class BitmapMedia {
		Media media;
		Bitmap bm;

		public BitmapMedia(Media media, Bitmap bm) {
			this.media = media;
			this.bm = bm;
		}
	}

	// not ui thread
	private void getRecommendation() {
		debug( "getRecommendation() begin");
		int position = RecommendationType.COMMON;
		Result<List<Media>> result = mQiyiManager.getQiyiClient().getRecommendation(position);
		final int code = result.code;
		List<Media> mediaList = result.data;
		debug( "getRecommendation() end" + ", result code = " + code + ", data size = " + mediaList + ", position = " + position);
		if (mediaList == null) {
			return;
		}
		cacheManager.setRecommendNormalList(mediaList);
		updateNormalRecommend(mediaList);
	}

	private void updateNormalRecommend(List<Media> list) {
		debug("updateNormalRecommend----");
		final List<BitmapMedia> middleList = new ArrayList<BitmapMedia>();
		final List<BitmapMedia> commonList = new ArrayList<BitmapMedia>();
		Bitmap bm = null;
		for (Media media : list) {
			bm = cacheManager.getBitmap(media);
			if (bm == null) {
				bm = media.getImage().getBitmap();
				cacheManager.addRecommendNormalCache(media, bm);
			}
			if (bm == null) {
				continue;
			}
			debug("bm = " + bm.getWidth() + "; " + bm.getHeight());
			if (bm.getWidth() == bm.getHeight()) {
				middleList.add(new BitmapMedia(media, bm));
			} else {
				commonList.add(new BitmapMedia(media, bm));
			}
		}
		runOnUI(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < middleList.size() && i < ivMidlleList.size(); i++) {
					ivMidlleList.get(i).setTag(middleList.get(i).media);
					ivMidlleList.get(i).setImageBitmap(middleList.get(i).bm);
				}
				for (int i = 0; i < commonList.size() && i < ivRecomendList.size(); i++) {
					ivRecomendList.get(i).setTag(commonList.get(i).media);
					ivRecomendList.get(i).setImageBitmap(commonList.get(i).bm);
				}

			}
		});

	}

	private void getLargeRecommendation() {
		debug( "getRecommendation() begin");
		int position = RecommendationType.EXTRUDE;
		Result<List<Media>> result = mQiyiManager.getQiyiClient().getRecommendation(position);
		final int code = result.code;
		List<Media> mediaList = result.data;
		debug( "getRecommendation() end" + ", result code = " + code + ", data size = " + mediaList + ", position = " + position);
		if (mediaList == null) {
			return;
		}
		cacheManager.setRecommendLargeList(mediaList);
		updateLargeRecommend(mediaList);
	}

	private void updateLargeRecommend(List<Media> mediaList) {
		Bitmap bm = null;
		for (int i = 0; i < mediaList.size() && i < ivBigestList.size(); i++) {
			final Media media = mediaList.get(i);
			bm = cacheManager.getBitmap(media);
			if (bm == null) {
				bm = media.getImage().getBitmap();
				cacheManager.addRecommendLargeCache(media, bm);
			}
			final int index = i;
			final Bitmap bpm = bm;
			runOnUI(new Runnable() {

				@Override
				public void run() {
					ivBigestList.get(index).setTag(media);
					ivBigestList.get(index).setImageBitmap(bpm);
				}
			});
		}
	}

	@Override
	protected void onQiyiResume() {
		super.onQiyiResume();
		initDate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ivRecomendList = new ArrayList<ImageView>();
		View view = inflater.inflate(R.layout.layout_movie_recommend, container, false);
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item1));
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item2));
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item3));
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item4));
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item5));
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item6));
		ivRecomendList.add((ImageView) view.findViewById(R.id.movie_recommend_item7));
		ivBigestList = new ArrayList<ImageView>();
		ivBigestList.add((ImageView) view.findViewById(R.id.movie_recommend_item_biggest_1));
		ivBigestList.add((ImageView) view.findViewById(R.id.movie_recommend_item_biggest_2));
		ivMidlleList = new ArrayList<ImageView>();
		ivMidlleList.add((ImageView) view.findViewById(R.id.movie_recommend_item_middle_1));
		ivMidlleList.add((ImageView) view.findViewById(R.id.movie_recommend_item_middle_2));
		ivMidlleList.add((ImageView) view.findViewById(R.id.movie_recommend_item_middle_3));

		viewHistory = view.findViewById(R.id.movie_revommend_fram_history);
		viewFavorite = view.findViewById(R.id.movie_revommend_fram_favorite);
		viewSearch = view.findViewById(R.id.movie_revommend_fram_search);
		viewHistory.setOnClickListener(this);
		viewFavorite.setOnClickListener(this);
		viewSearch.setOnClickListener(this);
		for(ImageView v :ivBigestList){
			v.setOnClickListener(this);
		}
		for(ImageView v :ivMidlleList){
			v.setOnClickListener(this);
		}
		for(ImageView v :ivRecomendList){
			v.setOnClickListener(this);
		}
		return view;
	}

	private void initDate() {
		debug("initDate == inited "+inited);
		if(inited)
			return;
		inited = true;
		procTaskBackgroundAtFront(new Runnable() {
			@Override
			public void run() {
				List<Media> list = cacheManager.getRecommendLargeList();
				if (list == null || list.isEmpty()) {
					getLargeRecommendation();
				} else {
					updateLargeRecommend(list);
				}
			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<Media> list = cacheManager.getRecommendNormalList();
				if (list == null || list.isEmpty()) {
					getRecommendation();
				} else {
					debug("initDate----");
					updateNormalRecommend(list);
				}

			}
		}).start();
	}

	@Override
	public void onClick(View view) {
		if (view == viewHistory) {
			mQiyiManager.getQiyiClient().open(PageType.PAGE_HISTORY);
		} else if (view == viewFavorite) {
			mQiyiManager.getQiyiClient().open(PageType.PAGE_FAVORITE);
		} else if (view == viewSearch) {
			mQiyiManager.getQiyiClient().open(PageType.PAGE_SEARCH);
		} else {
			Object object = view.getTag();
			if(object instanceof Media){
				mQiyiManager.getQiyiClient().openMedia((Media) object);
			}
		}
	}

	@Override
	public View getDefaultFocusView() {
		return viewHistory;
	}

	@Override
	public String getLogTag() {
		return TAG;
	}
	@Override
	public void onDestroyView() {
		inited = false;
		super.onDestroyView();
	}

}
