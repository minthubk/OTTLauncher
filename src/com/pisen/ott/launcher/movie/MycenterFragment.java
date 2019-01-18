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
import com.qiyi.tv.client.feature.common.PageType;

/**
 * @Description 专区
 * @author hegang
 *
 */
public class MycenterFragment extends BaseFragment implements OnClickListener {

	private String TAG = MycenterFragment.class.getSimpleName();
	private List<View> ivMediaList;

	private CommonScaleFocusView focusView;

	public MycenterFragment(Handler handler) {
		super(handler);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ivMediaList = new ArrayList<View>();
		View view = inflater.inflate(R.layout.layout_movie_mycenter, container, false);
		ivMediaList.add(view.findViewById(R.id.movie_mycenter_history));
		ivMediaList.add(view.findViewById(R.id.movie_mycenter_favorite));
		ivMediaList.add(view.findViewById(R.id.movie_mycenter_offline));
		for (View v : ivMediaList) {
			v.setOnClickListener(this);
		}
		focusView = (CommonScaleFocusView) view.findViewById(R.id.focusScrollView);
		return view;
	}

	@Override
	public void onClick(View view) {
		int pageType = -1;
		switch (view.getId()) {
		case R.id.movie_mycenter_history:
			pageType = PageType.PAGE_HISTORY;
			break;
		case R.id.movie_mycenter_favorite:
			pageType = PageType.PAGE_FAVORITE;
			break;
		case R.id.movie_mycenter_offline:
			pageType = PageType.PAGE_OFFLINE;
			break;
		}
		if (pageType != -1) {
			mQiyiManager.getQiyiClient().open(pageType);
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
