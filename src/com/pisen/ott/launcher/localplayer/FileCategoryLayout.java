package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.launcher.R;

/**
 * 支持按键焦点浮层切换
 * 
 * @author yangyp
 * @version 1.0, 2014年12月16日 下午4:38:55
 */
public class FileCategoryLayout extends LinearLayout {

	private DefaultKeyFocus mKeyFocus;
	private ViewPager vPager;
	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
        //滚动导航		
		Rect outRect = new Rect();
		getChildAt(index).getHitRect(outRect);
	    mKeyFocus.onFocusChange(getChildAt(index), true);
	}

	public ViewPager getvPager() {
		return vPager;
	}

	public void setvPager(ViewPager vPager) {
		this.vPager = vPager;
	}

	public FileCategoryLayout(Context context) {
		super(context);
		initViews(context);
	}

	public FileCategoryLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public FileCategoryLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	protected void initViews(Context context) {
		mKeyFocus = new DefaultKeyFocus(this);
		mKeyFocus.setKeepFocus(true);
		mKeyFocus.setFocusImageResource(R.drawable.three_level_choice_highlight, R.dimen.banner_focus_border_menu);
		mKeyFocus.setOnItemFocusChangeListener(new DefaultKeyFocus.OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if(hasFocus && !isInEditMode()){
					switch (v.getId()) {
					case R.id.btnFileLocalPlayer:
						vPager.setCurrentItem(0);
						break;
					case R.id.btnVideoLocalPlayer:
						vPager.setCurrentItem(1);
						break;
					case R.id.btnImageLocalPlayer:
						vPager.setCurrentItem(2);
						break;
					case R.id.btnMusicLocalPlayer:
						vPager.setCurrentItem(3);
						break;
					}
				}else{
				}
				
			}
		});
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mKeyFocus.setOnItemClickListener(l);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mKeyFocus.layout(null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mKeyFocus.draw(canvas);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}

}
