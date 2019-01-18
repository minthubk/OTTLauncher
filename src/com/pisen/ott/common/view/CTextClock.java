package com.pisen.ott.common.view;

import android.content.Context;
import android.izy.view.ICustomView;
import android.util.AttributeSet;
import android.widget.TextClock;

public class CTextClock extends TextClock implements ICustomView {

	public CTextClock(Context context) {
		super(context);
	}

	public CTextClock(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CTextClock(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean is24HourModeEnabled() {
		return true; // super.is24HourModeEnabled();
	}

}
