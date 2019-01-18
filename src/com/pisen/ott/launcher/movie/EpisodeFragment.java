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
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.CommonScaleFocusView;
import com.qiyi.tv.client.Result;
import com.qiyi.tv.client.data.Channel;
import com.qiyi.tv.client.data.Media;
import com.qiyi.tv.client.feature.common.PictureSize;
import com.qiyi.tv.client.feature.common.PictureType;

/**
 * @Description 电影
 * @author hegang
 *
 */
public class EpisodeFragment extends BaseFragment implements OnClickListener {

	private String TAG = EpisodeFragment.class.getSimpleName();
	private List<ImageView> ivMediaList;
	private View viewTabTop, viewTabCenter, viewTabBottom;

	private CommonScaleFocusView focusView;
	private boolean inited = false;
	private static final int MAX_NUM = 9;

	private static final int CHANNEL_ID = Channel.ID_EPISODE;

	@Override
	protected void onQiyiResume() {
		super.onQiyiResume();
		initDate();
	};

	public EpisodeFragment(Handler handler) {
		super(handler);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ivMediaList = new ArrayList<ImageView>();
		View view = inflater.inflate(R.layout.layout_movie_episode, container, false);
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r1c1));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r1c2));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r1c3));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r2c1));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r2c2));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r2c3));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r3c1));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r3c2));
		ivMediaList.add((ImageView) view.findViewById(R.id.movie_item_r3c3));
		for (ImageView v : ivMediaList) {
			v.setOnClickListener(this);
		}
		viewTabTop = view.findViewById(R.id.movie_tab_top);
		viewTabCenter = view.findViewById(R.id.movie_tab_center);
		viewTabBottom = view.findViewById(R.id.movie_tab_bottom);
		viewTabTop.setOnClickListener(this);
		viewTabCenter.setOnClickListener(this);
		viewTabBottom.setOnClickListener(this);
		focusView = (CommonScaleFocusView) view.findViewById(R.id.focusScrollView);
		return view;
	}

	@Override
	public void onClick(View view) {
		if (view instanceof ImageView) {
			Object object = view.getTag();
			if (object instanceof Media) {
				mQiyiManager.getQiyiClient().openMedia((Media) object);
			}
		} else {
			Channel ch = mQiyiManager.getChannelById(CHANNEL_ID);
			if (ch == null) {
				debug("channel is null");
				return;
			}
			if (view == viewTabTop) {
				mQiyiManager.openChannel(ch, null);
			} else if (view == viewTabCenter || view == viewTabBottom) {
				TextView tv = (TextView) view;
				String text = tv.getText().toString();
				mQiyiManager.openChannel(ch, IQiyiConfig.getEpisodeTag(text), text);
			}
		}
	}

	@Override
	public View getDefaultFocusView() {
		return viewTabTop;
	}

	private void initDate() {
		debug("initDate == inited "+inited);
		if (inited)
			return;
		inited = true;
		procTaskBackgroundAtFront(task);
	}

	Runnable task = new Runnable() {

		@Override
		public void run() {
			List<Media> list = cacheManager.getCachedChannelList(CHANNEL_ID);
			if (list == null || list.isEmpty()) {
				getMedias();
			} else {
				updateMedias(list);
			}
		}
	};

	private void getMedias() {
		Channel channel = mQiyiManager.getChannelById(CHANNEL_ID);
		if (channel != null) {
			Result<List<Media>> result = mQiyiManager.getQiyiClient().getChannelRecommendedMediaForTab(channel, MAX_NUM);
			debug("getChannelMedia == " + channel.getName() + "; result.code = " + result.code);
			if (result.data != null) {
				for (int i = 0; i < result.data.size(); i++) {
					Media media = result.data.get(i);
					Result<String> ret = mQiyiManager.getQiyiClient().getPictureUrl(media, PictureType.ALBUM_PIC, PictureSize.SIZE_480_270);
					debug("getPicUrl ret = " + ret.code + "; ret.data = " + ret.data);
					if (ret.data != null)
						media.setPicUrl(ret.data);
				}
				cacheManager.setCachedChannelList(result.data, CHANNEL_ID);
				updateMedias(result.data);
			}
		} else {
			inited = false;
			if (mQiyiManager.getChannelList() == null) {
				mQiyiManager.ensureLoad(true);
				procTaskBackgroundDelay(task, 5000);
			}
		}
	}

	private void updateMedias(List<Media> data) {
		if (data == null || data.isEmpty())
			return;
		Bitmap bm = null;
		for (int i = 0; i < data.size(); i++) {
			final Media media = data.get(i);
			bm = cacheManager.getBitmap(media);
			if (bm == null) {
				String url = getPictureUrl(media);
				if (url != null) {
					media.setPicUrl(url);
				}
				bm = media.getImage().getBitmap();
				cacheManager.addChannelCache(media, bm, CHANNEL_ID);
				if (bm == null) {
					continue;
				}
				debug("getBitmap = "+bm.getWidth()+" x "+bm.getHeight()); 
			}
			final Bitmap bmp = bm;
			final int index = i;
			runOnUI(new Runnable() {
				@Override
				public void run() {
					ivMediaList.get(index).setTag(media);
					ivMediaList.get(index).setImageBitmap(bmp);
				}
			});
		}

	}

	private String getPictureUrl(Media media) {
        debug( "getPictureUrl() begin");
        Result<String> result = mQiyiManager.getQiyiClient().getPictureUrl(media, PictureType.RESOURCE_TYPE, PictureSize.SIZE_480_270);
        debug( "getPictureUrl() end" + ", result code = " + result.code + ", url = " + result.data);
        return result.data;
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
