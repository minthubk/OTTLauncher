package com.pisen.ott.launcher.widget;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.utils.AppUtils;
import com.pisen.ott.launcher.utils.FileDownLoader;
import com.pisen.ott.launcher.utils.FileDownLoader.AsyncLoaderListener;
import com.pisen.ott.launcher.utils.FileUtils;
import com.pisen.ott.launcher.utils.HttpUtils;


/**
 * 支持下载安装的控件
 * 
 * @author yangyp
 * @version 1.0, 2015年2月12日 下午3:31:16
 */
public class DownloadItemView extends FrameLayout implements OnClickListener, IDownloadItem {

	private static final int DWON_FAILED = 0x01;
	private static final int DWON_START = 0x02;
	private static final int DWON_LODING = 0x03;
	private static final int DWON_FINISH = 0x04;
	private TextView txtName;
	private LinearLayout controlLayout;
	private Button btnInstall;
	private UiContent uiContent;
	private Context context;
	private GridScaleView gridView;

	public DownloadItemView(Context context) {
		this(context, null);
	}

	public DownloadItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		View.inflate(context, R.layout.icon_reflect_download, this);
		txtName = (TextView) findViewById(R.id.txtName);
		controlLayout = (LinearLayout) findViewById(R.id.controlLayout);
		btnInstall = (Button) findViewById(R.id.btnInstall);
		btnInstall.setOnClickListener(this);
	}

	@Override
	public void setUiContent(UiContent content) {
		this.uiContent = content;
	}

	public void setName(String name) {
		txtName.setText(name);
		if (StringUtils.isEmpty(name)) {
			txtName.setVisibility(View.GONE);
		}else{
			txtName.setVisibility(View.VISIBLE);
		}
	}

	public boolean checkInstalled() {
		if (AppUtils.checkInstalled(context, uiContent)) {
			if (gridView != null) {
				gridView.unlockItem();
			}
			hideControlLayout();
//			showControlLayout();
//			btnInstall.setText("已安装");
//			setDrawbleLeft(btnInstall, R.drawable.app_statu_exist);
			return true;
		}
		return false;
	}

	@Override
	public void nextClick(UiContent uiContent, GridScaleView grdContent) {
		this.gridView = grdContent;
		if (!HttpUtils.isNetworkAvailable(context)) {
			Toast.makeText(context, getResources().getString(R.string.error_net), 1).show();
			return;
		}
		
		this.uiContent = uiContent;
		// 判断是否安装,如果安装了直接启动
		if (!AppUtils.isInstalledApk(context, uiContent)) {
			if (!isShowControl()) {
				showControlLayout();
				if (FileUtils.isExists(FileUtils.getUpdateFile(context), FileUtils.getFileName(uiContent.ApkFile))) {
					btnInstall.setText(getResources().getString(R.string.install_now));
				}
			} else {
				// Install Click
				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
			}
		}else{
			if (gridView != null) {
				gridView.unlockItem();
			}
		}
	}

	public boolean isShowControl() {
		return controlLayout.getVisibility() == View.VISIBLE;
	}

	@Override
	public void showControlLayout() {
		if (!isShowControl()) {
			int id = getId();
			setNextFocusUpId(id);
			setNextFocusDownId(id);
			setNextFocusLeftId(id);
			setNextFocusRightId(id);
			controlLayout.setVisibility(View.VISIBLE);
			btnInstall.setVisibility(View.VISIBLE);
			btnInstall.setText(getResources().getString(R.string.install_down));
			setDrawbleLeft(btnInstall, R.drawable.app_statu_down);
		}
	}

	@Override
	public void hideControlLayout() {
		if (isShowControl()) {
			setNextFocusUpId(View.NO_ID);
			setNextFocusDownId(View.NO_ID);
			setNextFocusLeftId(View.NO_ID);
			setNextFocusRightId(View.NO_ID);
			controlLayout.setVisibility(View.GONE);
			btnInstall.setText(getResources().getString(R.string.install_down));
			setDrawbleLeft(btnInstall, R.drawable.app_statu_down);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (isShowControl()) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
				hideControlLayout();
				cancelDownload();
				return true;
			}

			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return true;
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					if (btnInstall.getVisibility() == View.VISIBLE) {
						btnInstall.performClick();
					}
					return true;
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View v) {
		if (!HttpUtils.isNetworkAvailable(context)) {
			Toast.makeText(context, getResources().getString(R.string.error_net), 1).show();
			return;
		}
		
		if (!AppUtils.isInstalledApk(context, uiContent)) {
			String btnVaule = btnInstall.getText().toString();
			if (btnVaule.equals(getResources().getString(R.string.install_now))) {
				File file = new File(FileUtils.getUpdateFile(context), FileUtils.getFileName(uiContent.ApkFile));
				if (file.exists()) {
					if (gridView != null) {
						gridView.unlockItem();
					}
					AppUtils.installApk(context, file);
				} else {
					downLoadApk(uiContent.ApkFile);
					LogCat.e(getResources().getString(R.string.error_down_package_lose));
				}
			} 
//			else if(btnVaule.equals("已安装")){
//				AppUtils.isInstalledApk(context, uiContent);
//			} 
			else {
				downLoadApk(uiContent.ApkFile);
			}
		}
		
	}

	private void downLoadApk(String url) {
		if (!HttpUtils.isNetworkAvailable(context)) {
			Toast.makeText(context, getResources().getString(R.string.error_net), 1).show();
			return;
		}
		if (StringUtils.isEmpty(url)) {
			return;
		}
		
		FileDownLoader.getDownLoader(context).download(url, new AsyncLoaderListener() {
			@Override
			public void onStart() {
				updateHanlder.sendEmptyMessage(DWON_START);
			}
			
			@Override
			public void onSuccess(File file) {
				LogCat.i("FileDownLoader:onSuccess");
				Message msg = new Message();
				msg.what = DWON_FINISH;
				msg.arg1 = 0;
				updateHanlder.sendMessage(msg);
				// 启动安装
				AppUtils.installApk(context, file);
			}

			@Override
			public void onFailed(String erroInfo) {
				LogCat.e("下载失败--FileDownLoader:" + erroInfo);
				// 下载出错，恢复到下载安装界面
				Message msg = new Message();
				msg.what = DWON_FAILED;
				msg.arg1 = 0;
				msg.obj = erroInfo;
				updateHanlder.sendMessage(msg);
			}

			@Override
			public void onDownLoading(int progress, String temSize, String totalSize) {
				sendUpdateProgress(progress);
			}
		});
	}

	/**
	 * 取消下载任务
	 */
	public void cancelDownload() {
		FileDownLoader.getDownLoader(context).cancelTasks();
	}

	/**
	 * 根据progress进度移动imageview
	 */
	private final Handler updateHanlder = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) controlLayout.getLayoutParams();
			if (msg.what == DWON_FAILED) {
				lp.bottomMargin = 0;
				hideControlLayout();
				String info = (String) msg.obj;
				Toast.makeText(getContext(), info, 1).show();
			} else if (msg.what == DWON_LODING) {
				int margin = ((controlLayout.getHeight()+lp.bottomMargin) * msg.arg1) / 100;
				if (margin > lp.bottomMargin) {
					lp.bottomMargin = margin;
				}
				if (msg.arg1 == 100) {
					controlLayout.setVisibility(View.GONE);
				}
			} else if (msg.what == DWON_FINISH) {
				lp.bottomMargin = 0;
				showControlLayout();
				btnInstall.setText(getResources().getString(R.string.install_now));
			}else if (msg.what == DWON_START) {
				btnInstall.setVisibility(View.GONE);
				controlLayout.setVisibility(View.VISIBLE);
			}
			controlLayout.setLayoutParams(lp);
		}

	};

	/**
	 * animView所在的整体布局必须限制大小，否则会出现整个布局往下拉伸的效果，而不是当前view向上移动
	 * 
	 * @param progress
	 * @param animView
	 */
	public void sendUpdateProgress(int progress) {
		Message msg = new Message();
		msg.what = DWON_LODING;
		msg.arg1 = progress;
		updateHanlder.sendMessage(msg);
	}

	private void setDrawbleLeft(Button v,int imgId){
		Resources res = getResources();
		Drawable d = res.getDrawable(imgId);
		d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
		v.setCompoundDrawables(d, null, null, null); //设置左图标
	}
}
