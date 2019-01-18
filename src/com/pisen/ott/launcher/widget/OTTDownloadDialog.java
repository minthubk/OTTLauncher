package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.izy.util.StringUtils;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.ott.launcher.R;

/**
 * 下载对话框
 * @author Liuhc
 * @version 1.0 2015年3月27日 下午2:18:28
 */
public class OTTDownloadDialog extends OTTDialog {
	
	ProgressBar pbrDown;
	Button btnCancel;
	TextView txtProgress;
	
	public OTTDownloadDialog(Context context) {
		super(context, R.style.AppDialog_Alert);
		initView(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void initView(Context context) {
		setContentView(R.layout.home_update_progress);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		txtProgress = (TextView) findViewById(R.id.txtProgress);
		pbrDown = (ProgressBar) findViewById(R.id.pbrDown);
	}

	
	public ProgressBar getProgressBar(){
		return pbrDown;
	}
	
	public TextView getTxtProgress(){
		return txtProgress;
	}
	
	public void setCancelListener(View.OnClickListener listener) {
		btnCancel.setOnClickListener(listener);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
