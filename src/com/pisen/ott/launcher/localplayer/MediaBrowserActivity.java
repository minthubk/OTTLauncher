package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.localplayer.image.ImageViewerActivity;
import com.pisen.ott.launcher.localplayer.music.MusicPlayerActivity;
import com.pisen.ott.launcher.localplayer.video.VideoPlayActivity;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

/** 媒体二级页面Activity */
public class MediaBrowserActivity extends DefaultActivity implements OnItemClickListener {
	AlbumData rootItem;
	AlbumData currentItem;
	BrowserGridViewSub grdContent, grdContentMusic;
	TextView txtLocalSecondLevelNav;
	private MediaBrowserAdapter browserAdapter;
	private MusicBrowserAdapter musicAdapter;
	public OTTWiatProgress animView;

	public static long time1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlbumDataSend ads = (AlbumDataSend) getIntent().getSerializableExtra("item");
		AlbumData ad = toAlbumData(ads);
		this.rootItem = ad;
		this.currentItem = ad;
		setContentView(R.layout.file_browser_media);
		grdContent = (BrowserGridViewSub) findViewById(R.id.grdContent);
		grdContentMusic = (BrowserGridViewSub) findViewById(R.id.grdContentMusic);
		txtLocalSecondLevelNav = (TextView) findViewById(R.id.txtLocalSecondLevelNav);
		animView = (OTTWiatProgress) findViewById(R.id.animView);
		File file = new File(currentItem.path);
		String name;
		if (file.isDirectory()) {
			name = file.getName();
		} else {
			name = file.getParentFile().getName();
		}
		txtLocalSecondLevelNav.setText(name);
		browserAdapter = new MediaBrowserAdapter(this, currentItem);
		musicAdapter = new MusicBrowserAdapter(this, currentItem);
		grdContent.setAdapter(browserAdapter);
		grdContentMusic.setAdapter(musicAdapter);

		grdContent.setOnItemClickListener(this);
		grdContentMusic.setOnItemClickListener(this);

		if (currentItem != null && currentItem.fileType == AlbumData.Music) {
			grdContent.setVisibility(View.GONE);
			grdContentMusic.setVisibility(View.VISIBLE);
		}
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
		return ad;

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		time1 = System.currentTimeMillis();
		if (parent.getId() == R.id.grdContent) {
			currentItem = browserAdapter.getItem(position);
		} else if (parent.getId() == R.id.grdContentMusic) {
			currentItem = musicAdapter.getItem(position);
		} else {
			return;
		}
		if (currentItem == null) {
			return;
		}
		animView.show();
//		animView.bringToFront();
//		animView.setVisibility(View.VISIBLE);
		List<String> list = currentItem.listSameDirectoryFiles();
		int index = currentItem.getItemIndex(list);
		if (currentItem.isDirectory) {// 目录： 打开下级目录
			browserAdapter.setData(currentItem.listFile());
			browserAdapter.notifyDataSetInvalidated();
		} else {// 媒体文件: 打开适当的播放器
			switch (currentItem.fileType) {
			case AlbumData.Video:
				VideoPlayActivity.start(this, list, index);
				break;
			case AlbumData.Image:
				ImageViewerActivity.start(this, list, index);
				break;
			case AlbumData.Music:				
				MusicPlayerActivity.start(this, list, index);
				break;
			}
		}
		animView.cancel();
	}

}
