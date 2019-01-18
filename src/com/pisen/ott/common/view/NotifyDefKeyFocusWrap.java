package com.pisen.ott.common.view;

import android.view.View;
import android.view.ViewGroup;

import com.pisen.ott.common.view.focus.DefaultKeyFocus;

/**
 * @author  mahuan
 * @version 1.0 2015年4月24日 上午11:44:24
 * @updated [2015年4月24日 上午11:44:24]:
 */
public class NotifyDefKeyFocusWrap extends DefaultKeyFocus{

	/**
	 * @param layout
	 */
	public NotifyDefKeyFocusWrap(ViewGroup layout) {
		super(layout);
	}

	@Override
	public void layout(View focus) {
		mCurrentRect.setEmpty();
		mCurrentView = null;
		super.layout(focus);
	}
}
