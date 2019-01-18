package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author  mahuan
 * @version 1.0 2015年3月3日 下午2:33:50
 * @updated [2015年3月3日 下午2:33:50]:跑马灯  
 */
public class MarqueeTextView extends TextView {

	/**
	 * @param context
	 */
	public MarqueeTextView(Context context) {
		super(context);
	}

	public MarqueeTextView(Context context,AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MarqueeTextView(Context context,AttributeSet attrs,int defStyle){
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean isFocused() {
		return true;
	}
}
