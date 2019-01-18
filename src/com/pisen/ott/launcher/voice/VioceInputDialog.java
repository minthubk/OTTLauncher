package com.pisen.ott.launcher.voice;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.pisen.ott.launcher.R;

public class VioceInputDialog extends AlertDialog {

	private EditText edtResult;
	private ImageView imgListening;
	
	public EditText getEdtResult() {
		return edtResult;
	}

	public ImageView getImgListening() {
		return imgListening;
	}

	protected VioceInputDialog(Context context) {
		super(context,R.style.AppDialog_Voice);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_voiceinput);
		edtResult = (EditText) findViewById(R.id.voice_edt_input);
		imgListening = (ImageView) findViewById(R.id.voice_img_input);
		Log.e("hegang", "onCreate edtResult = "+edtResult);
//		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	}
	

	@Override
	public void show() {
		super.show();
		edtResult.setText(null);
		imgListening.setImageResource(R.drawable.search_record_0);
	}
}
