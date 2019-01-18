package com.pisen.ott.launcher.movie;

import java.lang.reflect.Field;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.ott.launcher.movie.QiyiManager.QiyiLisenter;

/**
 * @Description 基类
 * @author hegang
 *
 */
public abstract class BaseFragment extends Fragment implements QiyiLisenter {

	protected QiyiManager mQiyiManager;
	protected MovieCacheManager cacheManager;
	private boolean qiyiResumed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private Handler mHandler;

	public BaseFragment(Handler handler) {
		this.mHandler = handler;
	}

	public void procTaskBackground(Runnable runnable) {
		mHandler.post(runnable);
	}

	public void procTaskBackgroundAtFront(Runnable runnable) {
		mHandler.postAtFrontOfQueue(runnable);
	}

	public void procTaskBackgroundDelay(Runnable runnable, long delayMillis) {
		mHandler.postDelayed(runnable, delayMillis);
	}

	@Override
	public void onAttach(Activity activity) {
		debug("onAttach "+getLogTag());
		mQiyiManager = QiyiManager.getInstance(activity.getApplication());
		mQiyiManager.addQiyiLisenter(this);
		cacheManager = MovieCacheManager.getInstance(activity.getApplication());
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		debug("onActivityCreated mQiyiManager.isAvailable() = " + mQiyiManager.isAvailable());
		if (mQiyiManager.isAvailable()) {
			mQiyiManager.ensureLoad();
			onQiyiResume();
		} else {
			mQiyiManager.init();
		}
	}
	
	@Override
	public void onResume() {
		debug("onResume "+getLogTag());
		super.onResume();
	}
	
	@Override
	public void onPause() {
		debug("onPause "+getLogTag());
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		debug("onDestroyView "+getLogTag());
		qiyiResumed = false;
		super.onDestroyView();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		debug("onCreateView "+getLogTag());
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		debug("onDestroy "+getLogTag());
		super.onDestroy();
	}

	public void runOnUI(Runnable runnable) {
		if(getActivity()!=null)
			getActivity().runOnUiThread(runnable);
	}

	public abstract View getDefaultFocusView();

	public View getScrollFocusView() {
		return null;
	}

	@Override
	public void onDetach() {
		mQiyiManager.removeQiyiLisenter(this);
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onAuthSuccess() {
		onQiyiResume();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub

	}

	protected void onQiyiResume() {
		if (qiyiResumed) {
			debug("method onQiyiResume() has been invoked");
			return;
		}
		qiyiResumed = true;
	}

	public void onPageSelected(boolean pageRequestFocus) {
		View view = getDefaultFocusView();
		if (view != null && pageRequestFocus) {
			view.requestFocus();
		}
	}

	public abstract String getLogTag();

	protected void debug(String msg) {
		Log.d("hegang-" + getLogTag(), msg);
	}

}
