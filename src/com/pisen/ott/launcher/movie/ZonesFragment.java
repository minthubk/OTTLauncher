package com.pisen.ott.launcher.movie;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.CommonScaleFocusView;
import com.qiyi.tv.client.data.Channel;

/**
 * @Description 专区
 * @author hegang
 *
 */
public class ZonesFragment extends BaseFragment implements OnClickListener {

	private String TAG = ZonesFragment.class.getSimpleName();
	private List<View> ivMediaList;

	private CommonScaleFocusView focusView;

	@Override
	protected void onQiyiResume() {
		super.onQiyiResume();
	};

	public ZonesFragment(Handler handler) {
		super(handler);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ivMediaList = new ArrayList<View>();
		View view = inflater.inflate(R.layout.layout_movie_zones, container, false);
		ivMediaList.add(view.findViewById(R.id.movie_item_r1c1));
		ivMediaList.add(view.findViewById(R.id.movie_item_r1c2));
		ivMediaList.add(view.findViewById(R.id.movie_item_r1c3));
		ivMediaList.add(view.findViewById(R.id.movie_item_r1c4));
		ivMediaList.add(view.findViewById(R.id.movie_item_r1c5));
		ivMediaList.add(view.findViewById(R.id.movie_item_r2c1));
		ivMediaList.add(view.findViewById(R.id.movie_item_r2c2));
		ivMediaList.add(view.findViewById(R.id.movie_item_r2c3));
		ivMediaList.add(view.findViewById(R.id.movie_item_r2c4));
		ivMediaList.add(view.findViewById(R.id.movie_item_r2c5));
		ivMediaList.add(view.findViewById(R.id.movie_item_r3c1));
		ivMediaList.add(view.findViewById(R.id.movie_item_r3c2));
		ivMediaList.add(view.findViewById(R.id.movie_item_r3c3));
		ivMediaList.add(view.findViewById(R.id.movie_item_r3c4));
		ivMediaList.add(view.findViewById(R.id.movie_item_r3c5));
		for (View v : ivMediaList) {
			v.setOnClickListener(this);
		}
		focusView = (CommonScaleFocusView) view.findViewById(R.id.focusScrollView);
		return view;
	}

	/*
	 * { 音乐,体育，纪录片，少儿，娱乐 教育，搞笑，生活，财经，旅游 汽车，军事，法律，健康，3D }
	 */
	@Override
	public void onClick(View view) {
		int channelId = -1;
		switch (view.getId()) {
		case R.id.movie_item_r1c1:
			channelId = Channel.ID_MUSIC;
			break;
		case R.id.movie_item_r1c2:
			channelId = Channel.ID_SPORTS;
			break;
		case R.id.movie_item_r1c3:
			channelId = Channel.ID_DOCUMENTARY;
			break;
		case R.id.movie_item_r1c4:
			channelId = Channel.ID_KIDS;
			break;
		case R.id.movie_item_r1c5:
			channelId = Channel.ID_ENTERTAINMENT;
			break;
		case R.id.movie_item_r2c1:
			channelId = Channel.ID_EDUCATION;
			break;
		case R.id.movie_item_r2c2:
			channelId = Channel.ID_FUNNY;
			break;
		case R.id.movie_item_r2c3:
			channelId = Channel.ID_LIFE;
			break;
		case R.id.movie_item_r2c4:
			channelId = Channel.ID_FINANCE;
			break;
		case R.id.movie_item_r2c5:
			channelId = Channel.ID_TRAVEL;
			break;
		case R.id.movie_item_r3c1:
			channelId = Channel.ID_CAR;
			break;
		case R.id.movie_item_r3c2:
			channelId = Channel.ID_MILITARY;
			break;
		case R.id.movie_item_r3c3:
			channelId = Channel.ID_FASHION;
			break;
		case R.id.movie_item_r3c4:
			channelId = Channel.ID_NEWS;
			break;
		case R.id.movie_item_r3c5:
			channelId = Channel.ID_3D;
			break;

		default:
			break;
		}
		if (channelId != -1) {
			Channel ch = mQiyiManager.getChannelById(channelId);
			mQiyiManager.openChannel(ch, null);
		}
	}

	@Override
	public String getLogTag() {
		return TAG;
	}

	@Override
	public View getDefaultFocusView() {
		return ivMediaList.get(0);
	}

}
