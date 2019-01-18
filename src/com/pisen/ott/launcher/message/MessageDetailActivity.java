package com.pisen.ott.launcher.message;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import com.pisen.ott.launcher.R;

/**
 * @author  mahuan
 * @date    2015年1月23日 上午11:15:37
 */
public class MessageDetailActivity extends Activity {
	private TextView txtMsgTitle;
	private TextView txtgMsgContent;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_center_item_info_detail);
		initView();
	}

	   
	public void initView() {
	    Bundle bundle = this.getIntent().getExtras();
	    MessageInfo msg  = (MessageInfo) bundle.getSerializable(MessageCenterActivity.KEY);
		txtMsgTitle = (TextView) findViewById(R.id.txtMsgTitle);
		txtgMsgContent = (TextView) findViewById(R.id.txtMsgContent);
//		txtMsgTitle.setText(msg.getMsgTitle());
//		txtgMsgContent.setText(msg.getMsgContent());
		txtMsgTitle.setText(msg.title);
		txtgMsgContent.setText(msg.content);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
