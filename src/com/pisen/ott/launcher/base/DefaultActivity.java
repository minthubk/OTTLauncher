package com.pisen.ott.launcher.base;

import android.os.Bundle;

/**
 * 默认基类，没有通知栏，左导航
 * 
 * @author yangyp
 * @version 1.0, 2015年2月27日 上午10:56:17
 */
public abstract class DefaultActivity extends FadeBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

}
