package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

import com.pisen.ott.launcher.R;

/**
 * 媒体二级页面GridView
 */
public class BrowserGridViewSub extends GridView implements OnItemSelectedListener {
	private View lastSelectedView;
	private int curPos=-1;
	public BrowserGridViewSub(Context context, AttributeSet attrs) {
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

				} else {
					View view = getSelectedView();
					if (view != null) {
						view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_image_border);
						ViewPropertyAnimator animator = view.findViewById(R.id.imgPhoto).animate();
						animator.scaleX(1f);
						animator.scaleY(1f);
						animator.setDuration(50);
						animator.start();
					}
				}
			}
		});
		lastSelectedView = getChildAt(0);
		curPos= 0 ;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			if (lastSelectedView != null) {
				//setSelection(getPositionForView(lastSelectedView));
				setSelection(curPos);
			}
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (lastSelectedView == null) {
			setSelection(0);
			lastSelectedView  =  getChildAt(0);
			curPos = 0;
			onItemSelected(this,lastSelectedView, 0, 0);
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// 前一个选中的Item,还原未选中状态的背景，触发缩小动画
		if (lastSelectedView != null) {
			lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_image_border);
			ViewPropertyAnimator animator1 = lastSelectedView.findViewById(R.id.imgPhoto).animate();
			animator1.scaleX(1f);
			animator1.scaleY(1f);
			animator1.setDuration(50);
			animator1.start();
		}
		// 选中的Item,设置选中状态的图片背景,触发放大动画
		if (view != null) {
			view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_image_border_sel);
			ViewPropertyAnimator animator = view.findViewById(R.id.imgPhoto).animate();
			animator.scaleX(1.1f);
			animator.scaleY(1.1f);
			animator.setDuration(250);
			animator.start();
			lastSelectedView = view;
			curPos = position;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
