package com.pisen.ott.launcher.movie;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.NavigationActivity;
import com.pisen.ott.launcher.widget.CommonScaleFocusView;

public class MovieActivity extends NavigationActivity {
	private ViewPager vpager;
	private FragAdapter adapter;
	private List<Fragment> fragments;
	private BaseFragment rf;
	private RadioGroup titleGroup;
	private static final String TAG = "hegang";
	
	private HandlerThread mThread;
	private Handler mHandler;
	private FixedSpeedScroller scroller;
	private CommonScaleFocusView focusView;
	
	private boolean resumed = false;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLeftMenuEnable(false);
		QiyiManager.getInstance(getApplication()).init();
		setContentView(R.layout.activity_movie);
		titleGroup = (RadioGroup) findViewById(R.id.movie_title_group);
		vpager = (ViewPager) findViewById(R.id.pager);
		focusView = (CommonScaleFocusView) findViewById(R.id.home_titil_focus);
		mThread = new HandlerThread(MovieActivity.class.getSimpleName());
		mThread.start();
		mHandler = new Handler(mThread.getLooper());
		Bundle args = new Bundle();
		fragments = new ArrayList<Fragment>();
		int index = 0;
		rf = new RecommendFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);
		
		rf = new FilmFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);
		rf = new EpisodeFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);
		rf = new VarietyFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);
		rf = new CartoonsFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);
		rf = new ZonesFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);
		rf = new MycenterFragment(mHandler);
		args.putInt("num", index++);
		rf.setArguments(args);
		fragments.add(rf);

		adapter = new FragAdapter(getSupportFragmentManager(), fragments);
		vpager.setAdapter(adapter);
		vpager.setCurrentItem(0);
		((RadioButton) titleGroup.getChildAt(0)).setTextColor(Color.GREEN);
//		 vpager.setPageTransformer(true, new DepthPageTransformer());
		try {
			Field field = ViewPager.class.getDeclaredField("mScroller");
			field.setAccessible(true);
			scroller = new FixedSpeedScroller(
					vpager.getContext(), new AccelerateInterpolator());
			field.set(vpager, scroller);
			scroller.setmDuration(600);
		} catch (Exception e) {
			e.printStackTrace();
		}
		focusView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (!resumed && vpager != null)
					return;
				if(hasFocus){
					for (int i = 0; i < titleGroup.getChildCount(); i++){
						if(view == titleGroup.getChildAt(i)){
							vpager.setCurrentItem(i, true);
							return;
						}
					}
				}
			}
		});
		for (int i = 0; i < titleGroup.getChildCount(); i++) {
			final int idx = i;
			View v = titleGroup.getChildAt(i);
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
//					//if(ISTV){
//						whiteBorder.clearAnimation();
//						whiteBorder.setVisibility(View.INVISIBLE);
//					//}
					vpager.setCurrentItem(idx, true);
				}
			});
		}
		vpager.setOnPageChangeListener(new OnPageChangeListener() {

			/**
			 * 滑动viewPage页面获取焦点时更新导航标记
			 */
			@Override
			public void onPageSelected(int position) {
				int size = titleGroup.getChildCount();
				if (position < size) {
					for (int i = 0; i < size; i++) {
						if (i == position) {
							((RadioButton) titleGroup.getChildAt(i)).setTextColor(Color.GREEN);
						} else {
							((RadioButton) titleGroup.getChildAt(i)).setTextColor(Color.WHITE);
						}
					}
				}
				BaseFragment rf = (BaseFragment) fragments.get(position);
				Log.e("hegang", "titleGroup.findFocus() = "+titleGroup.findFocus());
				rf.onPageSelected(titleGroup.findFocus()==null);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}
		});
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(!scroller.isFinished())//翻页动画未完毕
			return true;
		return super.dispatchKeyEvent(event);
	}
	@Override
	protected void onResume() {
		super.onResume();
		resumed = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		resumed = false;
	}
	
	@Override
	protected void onDestroy() {
		mThread.quit();
		QiyiManager.getInstance(getApplication()).release();
		super.onDestroy();
	}
}
