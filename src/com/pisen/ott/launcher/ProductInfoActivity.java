package com.pisen.ott.launcher;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.config.OnImageListener;
import com.pisen.ott.launcher.service.ImageDownLoader;
import com.pisen.ott.launcher.utils.FileUtils;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

public class ProductInfoActivity extends Activity {
	private ImageView image;
	private String selectedImage;
	private static final int MSG_SUCCESS = 1, MSG_FAILED = 2;

	public static final String PARAM_KEY = "image";
	private OTTWiatProgress progressLoading;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressLoading.cancel();
			progressLoading.setVisibility(View.GONE);
			switch (msg.what) {
			case MSG_SUCCESS:
				String url = (String) msg.obj;
				ImageLoader.loader(url, new OnImageListener() {
					@Override
					public void onSuccess(Bitmap response, boolean isCache) {
						image.setImageBitmap(response);
					}

					@Override
					public void onError(Throwable err) {
						super.onError(err);
						Toast.makeText(ProductInfoActivity.this, "出错", Toast.LENGTH_SHORT).show();
						finish();
					}
				});
				break;
			case MSG_FAILED:
				Toast.makeText(ProductInfoActivity.this, "出错", Toast.LENGTH_SHORT).show();
				finish();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_productinfo);
		progressLoading = (OTTWiatProgress) findViewById(R.id.progressLoading);
		progressLoading.show();
		image = (ImageView) findViewById(R.id.productinfo_image);
		selectedImage = getIntent().getStringExtra(PARAM_KEY);
		if (selectedImage != null) {
			final String url = FileUtils.getImageLocalPath(ProductInfoActivity.this, selectedImage);
			final File f = new File(url);
			final boolean exists = f.exists();
			if (!exists) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						ImageDownLoader donwDownLoader = ImageDownLoader.getDownLoader(ProductInfoActivity.this);
						boolean ret = donwDownLoader.downloadFile(selectedImage, FileUtils.getImagePath(ProductInfoActivity.this), FileUtils.getFileName(url));
						Message msg;
						if (ret) {
							msg = handler.obtainMessage(MSG_SUCCESS);
							msg.obj = url;
						} else {
							msg = handler.obtainMessage(MSG_FAILED);
						}
						msg.sendToTarget();
					}
				}).start();
			} else {
				Message msg = handler.obtainMessage(MSG_SUCCESS);
				msg.obj = url;
				msg.sendToTarget();
			}

		} else {
			Toast.makeText(ProductInfoActivity.this, "出错", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}
