package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.image.ImageViewerActivity;
import com.pisen.ott.launcher.localplayer.music.MusicPlayerActivity;
import com.pisen.ott.launcher.localplayer.video.VideoPlayActivity;
import com.pisen.ott.launcher.widget.ContentLayout;

/**
 * 资源浏览视图
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:11:42
 */
public class BrowserView extends FrameLayout {

//	public Browser browser;
//	public GridView browserGrid, browserGridFile, browserGridMusic;
//	public BrowserAdapter browserAdapter;
//	FileCategoryLayout menuLocalPlayer;
//	private String browserType;
//	Context mcontext;
//	private List<AlbumData> list = new ArrayList<AlbumData>();
//	private Handler handler;
//
//	public AlbumData item;

	public BrowserView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	enum ViewMode {
		FileGrid, FileList, ImageGrid, MusicGrid
	}

	public static String MUSIC = "music";
	public static String VIDEO = "video";
	public static String IMAGE = "image";
	public static String FILE = "file";

//	public String getBrowserType() {
//		return browserType;
//	}
//
//	public void setBrowserType(String browserType) {
//		this.browserType = browserType;
//	}
//
//	public BrowserView(Context context, Browser browser, String browserType) {
//		super(context);
//		this.browser = browser;
//		this.browserType = browserType;
//		this.mcontext = context;
//		initViews(context);
//		menuLocalPlayer = ((FileBrowserActivity) mcontext).menuLayout;
//	}
//
//	public Browser getBrowser() {
//		return browser;
//	}
//
//	public void setBrowser(Browser browser) {
//		this.browser = browser;
//	}
//
//	private void initViews(Context context) {
//		View.inflate(context, R.layout.file_browser_page, this);
//		browserGrid = (GridView) findViewById(R.id.browserGrid);
//		browserGridFile = (GridView) findViewById(R.id.browserGridFile);
//		browserGridMusic = (GridView) findViewById(R.id.browserGridMusic);
//
//		browserAdapter = new BrowserAdapter(getContext(), this, list);
//		
//        if(browserType.equals(FILE)){
//        	browserGridFile.setAdapter(browserAdapter);
//        }else if (browserType.equals(MUSIC)){
//        	browserGridMusic.setAdapter(browserAdapter);
//        }else {
//        	browserGrid.setAdapter(browserAdapter);
//        }
//		
//		// 异步加载数据
//		handler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				browserAdapter.setList((List<AlbumData>) msg.obj);
//				//browserAdapter.notifyDataSetChanged();
//			}
//		};
//
////		(new Thread(new Runnable() {
////			@Override
////			public void run() {
////				Message msg = new Message();
////				msg.obj = browser.findAlbums();
////				msg.setTarget(handler);
////				handler.sendMessage(msg);
////			}
//////		})).start();
////		(new Thread() {
////			@Override
////			public void run() {
////				Message msg = new Message();
////				msg.obj = browser.findAlbums();
////				msg.setTarget(handler);
////				handler.sendMessage(msg);
////			}
////		}).start();
////		browserGrid.setOnItemClickListener(this);
////		browserGridFile.setOnItemClickListener(this);
////		browserGridMusic.setOnItemClickListener(this);
////
////		setViewMode(browser.getViewMode());
////		if (browser.getViewMode() == ViewMode.FileGrid) {
////			browserGrid.setVisibility(View.GONE);
////			browserGridFile.setVisibility(View.VISIBLE);
////		}
////		if (browser.getViewMode() == ViewMode.MusicGrid) {
////			browserGrid.setVisibility(View.GONE);
////			browserGridMusic.setVisibility(View.VISIBLE);
////		}
//		
//	}
//
//	void setViewMode(ViewMode mode) {
//		//browserAdapter.setViewMode(mode);
//	}
//
//	/** AlbumData转变成方便serial的数据类 */
//	private AlbumDataSend toDataSend(AlbumData ad) {
//		AlbumDataSend ads = new AlbumDataSend();
//		ads.id = ad.id;
//		ads.bucketId = ad.bucketId;
//		ads.path = ad.path;
//		ads.title = ad.title;
//		ads.thumbnailUrl = ad.thumbnailUrl;
//		ads.updated = ad.updated;
//		ads.count = ad.count;
//		ads.isDirectory = ad.isDirectory;
//		ads.fileType = ad.fileType;
//		return ads;
//
//	}
//
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		//item = browserAdapter.getItem(position);
//		// menuLocalPlayer.setVisibility(View.GONE);
//		if (item.isDirectory) {// 目录
//			if (item.fileType == AlbumData.File) {
//				// 启动文件二级activity
//				Intent it = new Intent(mcontext, SubBrowserActivity.class);
//				Bundle mBundle = new Bundle();
//				AlbumDataSend sendItem = toDataSend(this.item);
//				mBundle.putSerializable("item", sendItem);
//				it.putExtras(mBundle);
//				mcontext.startActivity(it);
//			} else {
//				// 启动媒体二级activity
//				Intent it = new Intent(mcontext, MediaBrowserActivity.class);
//				Bundle mBundle = new Bundle();
//				AlbumDataSend sendItem = toDataSend(this.item);
//				mBundle.putSerializable("item", sendItem);
//				it.putExtras(mBundle);
//				mcontext.startActivity(it);
//			}
//		} else {// 文件
//			List<String> list = item.listSameDirectoryFiles();
//			int index = item.getItemIndex(list);
//			Intent it;
//			switch (item.fileType) {
//			case AlbumData.File:// 文件 ：根据扩展名选择播放器
//				List<String> fileList = new ArrayList<String>();
//				File f = new File(item.path);
//				String name = f.getName();
//				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
//				// 根据类型启动播放器
//				if (end.equals("mp4") || end.equals("rmvb") || end.equals("rm") || end.equals("mpg") || end.equals("avi") || end.equals("mpeg")) {
//					fileList = filterFileType(VIDEO, list);
//					index = item.getItemIndex(fileList);
//					it = new Intent(getContext(), VideoPlayActivity.class);
//					it.putExtra("list", (Serializable) fileList);
//					it.putExtra("index", index);
//					getContext().startActivity(it);
//				}
//				if (end.equals("mp3")/* || end.equals("wav") || end.equals("ogg") || end.equals("midi") || end.equals("wma")*/) {
//					it = new Intent(getContext(), MusicPlayerActivity.class);
//					fileList = filterFileType(MUSIC, list);
//					index = item.getItemIndex(fileList);
//					it.putExtra("list", (Serializable) fileList);
//					it.putExtra("index", index);
//					getContext().startActivity(it);
//				}
//				if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
//					fileList = filterFileType(IMAGE, list);
//					index = item.getItemIndex(fileList);
//					it = new Intent(getContext(), ImageViewerActivity.class);
//					it.putExtra("list", (Serializable) fileList);
//					it.putExtra("index", index);
//					it.putExtra("path", item.path);
//					getContext().startActivity(it);
//				}
//				break;
//			case AlbumData.Video:// 视频 -->视频播放器
//				it = new Intent(getContext(), VideoPlayActivity.class);
//				it.putExtra("list", (Serializable) list);
//				it.putExtra("index", index);
//				getContext().startActivity(it);
//				break;
//			case AlbumData.Image:// 图片-->图片浏览器
//				it = new Intent(getContext(), ImageViewerActivity.class);
//				it.putExtra("list", (Serializable) list);
//				it.putExtra("index", index);
//				it.putExtra("path", item.path);
//				getContext().startActivity(it);
//				break;
//			case AlbumData.Music:// 音乐-->音乐播放器
//				it = new Intent(getContext(), MusicPlayerActivity.class);
//				it.putExtra("list", (Serializable) list);
//				it.putExtra("index", index);
//				getContext().startActivity(it);
//				break;
//			}
//		}
//	}
//
//	/** 过滤文件类型 */
//	private List<String> filterFileType(String fileType, List<String> list) {
//		List<String> results = new ArrayList<String>();
//		if (fileType.equals(MUSIC)) {
//			for (String path : list) {
//				File f = new File(path);
//				String name = f.getName();
//				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
//				if (end.equals("mp3") /*|| end.equals("wav") || end.equals("ogg") || end.equals("midi") || end.equals("wma")*/) {
//					results.add(path);
//				}
//			}
//			return results;
//		}
//		if (fileType.equals(VIDEO)) {
//			for (String path : list) {
//				File f = new File(path);
//				String name = f.getName();
//				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
//				if (end.equals("mp4") || end.equals("rmvb") || end.equals("rm") || end.equals("mpg") || end.equals("avi") || end.equals("mpeg")) {
//					results.add(path);
//				}
//			}
//			return results;
//
//		}
//		if (fileType.equals(IMAGE)) {
//			for (String path : list) {
//				File f = new File(path);
//				String name = f.getName();
//				String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
//				if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
//					results.add(path);
//				}
//			}
//			return results;
//
//		}
//		return null;
//	}
//
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		if (event.getAction() == KeyEvent.ACTION_DOWN) {
//			switch (event.getKeyCode()) {
//			case KeyEvent.KEYCODE_DPAD_UP:
//				if (!ContentLayout.hasNextFocused(this, View.FOCUS_UP)) {
//					// 一级界面，根据browserType,设置向上切换到相应的一级导航
//					View v = findFocus();
//					if (v == null) {
//						return super.dispatchKeyEvent(event);
//					}
//					if (MUSIC.equals(browserType)) {
//						v.setNextFocusUpId(menuLocalPlayer.getChildAt(3).getId());
//					}
//					if (VIDEO.equals(browserType)) {
//						v.setNextFocusUpId(menuLocalPlayer.getChildAt(1).getId());
//					}
//					if (IMAGE.equals(browserType)) {
//						v.setNextFocusUpId(menuLocalPlayer.getChildAt(2).getId());
//					}
//					if (FILE.equals(browserType)) {
//						v.setNextFocusUpId(menuLocalPlayer.getChildAt(0).getId());
//					}
//				}
//				break;
//			case KeyEvent.KEYCODE_DPAD_RIGHT:
//				if (!ContentLayout.hasNextFocused(this, View.FOCUS_RIGHT)) {
//					// 一级界面，根据browserType,阻止音乐向右滚动
//					View v = findFocus();
//					if (v == null) {
//						return super.dispatchKeyEvent(event);
//					}
//					if (MUSIC.equals(browserType)) {
//						// v.setNextFocusRightId(menuLocalPlayer.getChildAt(3).getId());
//						v.setNextFocusRightId(v.getId());
//					}
//				}
//				break;
//			}
//		}
//		return super.dispatchKeyEvent(event);
//	}

}
