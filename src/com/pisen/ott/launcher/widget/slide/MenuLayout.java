package com.pisen.ott.launcher.widget.slide;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.pisen.ott.launcher.base.OttBaseActivity;

/**
 * 自动收缩菜单栏
 * 
 * @author yangyp
 * @version 1.0, 2015年1月22日 下午4:51:25
 */
public abstract class MenuLayout extends LinearLayout implements OnFocusChangeListener, OnClickListener {

	private int enterAnim;
	private int exitAnim;

	public MenuLayout(Context context, AttributeSet attrs, int enter, int exit) {
		super(context, attrs);
		this.enterAnim = enter;
		this.exitAnim = exit;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		setDefaultFocusedChild(this);
	}

	protected void setDefaultFocusedChild(View view) {
		if (view.isShown()){
			if (view instanceof ViewGroup) {
				ViewGroup layout = (ViewGroup) view;
				for (int i = 0, N = layout.getChildCount(); i < N; i++) {
					View child = layout.getChildAt(i);
					setDefaultFocusedChild(child);
				}
			} else {
				if (view.isFocusable()) {
					 view.setOnFocusChangeListener(this);
					view.setOnClickListener(this);
				}
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return executeKeyEvent(event) || super.dispatchKeyEvent(event);
	}

	public boolean executeKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				hideMenu();
				break;
			}
		}
		return false;
	}

	/**
	 * 是否显示
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return getVisibility() == View.VISIBLE;
	}

	public Boolean toggleMenu() {
		if (isVisible()) {
			hideMenu();
			return false;
		} else {
			showMenu();
			return true;
		}
	}

	/**
	 * 显示菜单
	 */
	public void showMenu() {
		if (isEnabled()) {
			if (getVisibility() != View.VISIBLE) {
				setVisibility(View.VISIBLE);
				requestFocus();
				startAnimation(AnimationUtils.loadAnimation(getContext(), enterAnim));
			}
		}
	}

	/**
	 * 隐藏菜单
	 */
	public void hideMenu() {
		if (getVisibility() == View.VISIBLE) {
			Context context = getContext();
			if (context instanceof OttBaseActivity) {
				((OttBaseActivity) context).requestContentFocus();
			}

			setVisibility(View.GONE);
			startAnimation(AnimationUtils.loadAnimation(getContext(), exitAnim));
		}
	}
	
	/**
	 * 隐藏菜单(不带动画)
	 */
	public void hideMenuWithOutAnimation() {
		if (getVisibility() == View.VISIBLE) {
			Context context = getContext();
			if (context instanceof OttBaseActivity) {
				((OttBaseActivity) context).requestContentFocus();
			}
			setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			if (findFocus() == null) {
				// hideMenu();
			}
		}
	}

	@Override
	public void onClick(View v) {
		onItemClick(v);
	}

	public void onItemClick(View v) {
	}

}
