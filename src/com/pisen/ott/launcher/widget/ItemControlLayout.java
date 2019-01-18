package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 主要用于控件选中后，子控件焦点问题
 * 
 * @author yangyp
 * @version 1.0, 2015年2月6日 上午11:04:21
 */
public class ItemControlLayout extends LinearLayout {

	public ItemControlLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event);
	}

	
	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (visibility == View.VISIBLE)
		{
			//requestFocus();
		}
	}
}
