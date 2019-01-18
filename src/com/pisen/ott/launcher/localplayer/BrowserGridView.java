package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.pisen.ott.launcher.R;

/**
 * 本地播放一级GridView,选中放大并切换背景
 */
public class BrowserGridView extends GridView implements OnItemSelectedListener {
	private View lastSelectedView;
	private int curPos=-1;
	
	public BrowserGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		setClipToPadding(false);
		setVerticalFadingEdgeEnabled(false);
		setChildrenDrawingOrderEnabled(true);
		setOnItemSelectedListener(this);
		setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
				} else {// GridView失去焦点，清除选中项的背景并缩小,清除Selection
					View view = getSelectedView();
					if (view != null) {
						view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_album_border);
						itemNarrow(view.findViewById(R.id.imgFrame));
						setSelection(-1);
					}
				}
			}
		});
		lastSelectedView = getChildAt(0);
		curPos=0;
	}

	@Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
			if (lastSelectedView != null) {
				setSelection(curPos);
			}
        }
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// 前一个选中的Item,还原未选中状态的背景，触发缩小动画
		if (lastSelectedView != null&&lastSelectedView!=view) {
			itemNarrow(lastSelectedView.findViewById(R.id.imgFrame));
			lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_album_border);
		}
		// 选中的Item,设置选中状态的图片背景,触发放大动画
		if (view != null) {
			view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_album_border_sel);
			itemZoom(view.findViewById(R.id.imgFrame));
			lastSelectedView = view;
			curPos = position;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
	
	/**
	 * 放大
	 * 
	 * @param selectedView
	 */
	private void itemZoom(View selectedView) {
		ViewPropertyAnimator animator = selectedView.animate();
		animator.scaleX(1.1f);
		animator.scaleY(1.1f);
		animator.setDuration(250);
		animator.start();
	}
	
	/**
	 * 选中项缩小
	 * 
	 * @param selectedView
	 */
	private void itemNarrow(View selectedView) {
		ViewPropertyAnimator animator = selectedView.animate();
		animator.scaleX(1f);
		animator.scaleY(1f);
		animator.setDuration(50);
		animator.start();
	}
}
