package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.izy.util.StringUtils;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pisen.ott.launcher.R;

/**
 * 文件提示对话框
 * @author fq
 */
public class OTTMessageDialog extends OTTDialog {
	Button btnOk;
	Button btnCancel;
	TextView txtTitle;
	TextView txtMessage;
	
	public OTTMessageDialog(Context context) {
		super(context, R.style.AppDialog_Alert);
		initView(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void initView(Context context) {
		setContentView(R.layout.ui_dialog_localplay_alert);
		btnOk = (Button) findViewById(R.id.btnOk);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtMessage = (TextView) findViewById(R.id.txtMessage);
	}

	public void setTitle(String title){
		txtTitle.setText(title);
	}
	
	public void setMessage(String content){
		if (StringUtils.isEmpty(content)) {
			txtMessage.setVisibility(View.GONE);
		}else{
			txtMessage.setVisibility(View.VISIBLE);
		}
		txtMessage.setText(content);
	}
	
	public void setOkListener(View.OnClickListener listener) {
		btnOk.setOnClickListener(listener);
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
