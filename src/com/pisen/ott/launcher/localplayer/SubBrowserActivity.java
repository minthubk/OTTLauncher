package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.localplayer.image.ImageViewerActivity;
import com.pisen.ott.launcher.localplayer.music.MusicPlayerActivity;
import com.pisen.ott.launcher.localplayer.LocalPalyerPagerViewBase;
import com.pisen.ott.launcher.localplayer.video.VideoPlayActivity;
import com.pisen.ott.launcher.widget.OTTMessageDialog;

/** 文件二级页面Activity */
public class SubBrowserActivity extends DefaultActivity implements OnItemClickListener {
	AlbumData rootItem;
	AlbumData currentItem;
	BrowserListView lstContent;
	TextView txtLocalSecondLevelNav, txtEmpty;
	private SubFileBrowserAdapter browserAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlbumDataSend ads = (AlbumDataSend) getIntent().getSerializableExtra("item");
		AlbumData ad = toAlbumData(ads);
		this.rootItem = ad;
		this.currentItem = ad;
		setContentView(R.layout.file_browser_sub);
		lstContent = (BrowserListView) findViewById(R.id.lstContent);
		txtLocalSecondLevelNav = (TextView) findViewById(R.id.txtLocalSecondLevelNav);
		txtLocalSecondLevelNav.setText(getDirectoryName(currentItem.getPath()));
		txtEmpty = (TextView) findViewById(R.id.txtEmpty);
		browserAdapter = new SubFileBrowserAdapter(this, currentItem.listFile());
		lstContent.setAdapter(browserAdapter);
		lstContent.setOnItemClickListener(this);
		lstContent.requestFocus();
		checkEmptyDirectory();
	}

	public AlbumData toAlbumData(AlbumDataSend ads) {
		AlbumData ad = new AlbumData(this);
		ad.id = ads.id;
		ad.bucketId = ads.bucketId;
		ad.path = ads.path;
		ad.title = ads.title;
		ad.thumbnailUrl = ads.thumbnailUrl;
		ad.updated = ads.updated;
		ad.count = ads.count;
		ad.isDirectory = ads.isDirectory;
		ad.fileType = ads.fileType;
		ad.local = ads.local;
		return ad;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (currentItem.getPath().equals(rootItem.getPath())) {
				return super.onKeyDown(keyCode, event);
			}

			currentItem = currentItem.getParent();
			txtLocalSecondLevelNav.setText(getDirectoryName(currentItem.getPath()));
			browserAdapter.setData(currentItem.listFile());
			browserAdapter.notifyDataSetChanged();
			lstContent.setSelection(-1);
			checkEmptyDirectory();
			// lstContent.requestFocus();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AlbumData item = browserAdapter.getItem(position);
		currentItem = item;
		if (item.isDirectory) {
			txtLocalSecondLevelNav.setText(getDirectoryName(currentItem.getPath()));
			browserAdapter.setNotifyOnChange(false);
			browserAdapter.setData(item.listFile());
			lstContent.setSelection(0);
			browserAdapter.notifyDataSetChanged();
			checkEmptyDirectory();
		} else {// 打开文件
			List<String> list = currentItem.listSameDirectoryFiles();
			int index = currentItem.getItemIndex(list);

			List<String> fileList = new ArrayList<String>();
			File f = new File(item.path);
			String name = f.getName();
			String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
			Intent it;
			if (LocalPalyerPagerViewBase.VideoType.contains(end)) {
				fileList = LocalPalyerPagerViewBase.filterFileType(LocalPalyerPagerViewBase.VideoType, list);
				index = currentItem.getItemIndex(fileList);
//				it = new Intent(this, VideoPlayActivity.class);
//				it.putExtra("list", (Serializable) fileList);
//				it.putExtra("index", index);
//				this.startActivity(it);
				VideoPlayActivity.start(this, fileList, index);
			}
			else if (LocalPalyerPagerViewBase.MusicType.contains(end)) {
//				it = new Intent(this, MusicPlayerActivity.class);
				fileList = LocalPalyerPagerViewBase.filterFileType(LocalPalyerPagerViewBase.MusicType, list);
				index = currentItem.getItemIndex(fileList);
//				it.putExtra("list", (Serializable) fileList);
//				it.putExtra("index", index);
				MusicPlayerActivity.start(this, fileList, index);
//				this.startActivity(it);
			}
			else if (LocalPalyerPagerViewBase.ImageType.contains(end)) {
				fileList = LocalPalyerPagerViewBase.filterFileType(LocalPalyerPagerViewBase.ImageType, list);
				index = currentItem.getItemIndex(fileList);
//				it = new Intent(this, ImageViewerActivity.class);
//				it.putExtra("list", (Serializable) fileList);
//				it.putExtra("index", index);
//				it.putExtra("path", currentItem.path);
//				this.startActivity(it);
				ImageViewerActivity.start(this, fileList, index);
			}else if (LocalPalyerPagerViewBase.ApkType.contains(end)) {
				// fileList =
				// LocalPalyerPagerViewBase.filterFileType(LocalPalyerPagerViewBase.ImageType,
				// list);
				// index = currentItem.getItemIndex(fileList);
				// it = new Intent(this, ImageViewerActivity.class);
				// it.putExtra("list", (Serializable) fileList);
				// it.putExtra("index", index);
				// it.putExtra("path", currentItem.path);
				// this.startActivity(it);
				
				// 安装APK  
		        Intent intent = new Intent(Intent.ACTION_VIEW);  
		        intent.setDataAndType(Uri.fromFile(new File(currentItem.path)),  
		                "application/vnd.android.package-archive");  
		        this.startActivity(intent); 
		        
//		        Intent i = new Intent(Intent.ACTION_VIEW); 
//		        i.setDataAndType(Uri.parse("file://" + currentItem.path),"application/vnd.android.package-archive"); 
//		        this.startActivity(i);
			}else{//弹出提示框
				showNoticeDialog();
			}

		}
	}

	private void showNoticeDialog(){
		final OTTMessageDialog dialog = new OTTMessageDialog(this);
		dialog.setTitle("文件不可播放");
		dialog.setMessage("");
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOkListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				//showDownloadDialog();
			}
		});
		dialog.setCancelListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void checkEmptyDirectory() {
		if (lstContent.getAdapter().getCount() == 0) {
			txtEmpty.setVisibility(View.VISIBLE);
		} else {
			txtEmpty.setVisibility(View.GONE);
		}
	}

	/**
	 * 过滤目录的显示名称，截取掉目录路径的前边
	 */
	private String getDirectoryName(String path) {
		if (null != path) {
			String sub = "/" + rootItem.title;
			int len =sub.length();
			int idx = path.indexOf(sub);
			if(null!=rootItem.local){
				return "/"+rootItem.local+path.substring(idx+len)+"/";
			}else{
				return path.substring(idx) + "/";
			}
		}
		return null;
	}
}
