package com.pisen.ott.launcher;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.pisen.ott.launcher.base.NavigationActivity;
import com.pisen.ott.launcher.utils.AppUtils;

public class ChatMainActivity extends NavigationActivity implements OnClickListener{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLeftMenuEnable(false);
		setContentView(R.layout.activity_chat);
		findViewById(R.id.chat_qinyouyue).setOnClickListener(this);
		findViewById(R.id.chat_qq).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.chat_qinyouyue:
			try {
				AppUtils.openApk(this, "com.lenovo.vcs.familycircle.tv");
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "打开应用失败", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.chat_qq:
			try {
				AppUtils.openApk(this, "com.tencent.deviceapp");
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "打开应用失败", Toast.LENGTH_LONG).show();
			}
			break;

		default:
			break;
		}
		
	}
}
