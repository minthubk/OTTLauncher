package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.izy.util.FileUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.FocusFinder;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.AlbumData;
import com.pisen.ott.launcher.localplayer.AlbumDataSend;
import com.pisen.ott.launcher.localplayer.FileCategoryLayout;
import com.pisen.ott.launcher.localplayer.MediaBrowserActivity;
import com.pisen.ott.launcher.localplayer.SubBrowserActivity;
import com.pisen.ott.launcher.localplayer.image.ImageViewerActivity;
import com.pisen.ott.launcher.localplayer.music.MusicPlayerActivity;
import com.pisen.ott.launcher.localplayer.video.VideoPlayActivity;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

/**
 * 资源浏览视图
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:11:42
 */
public abstract class LocalPalyerPagerViewBase extends GridView implements OnItemClickListener {

	static String [] videos = new String[]{"3gp","avi","flv","mkv","mov","mp4","mpg","rmvb",/*"ts",*/"webm","wmv"};
	static String [] audios = new String[]{"aac","ac3","mp3",/*"flac",*/"wma","wav","mp2","amr"};
	public static final List<String> VideoType = Arrays.<String> asList(/*new String[] { "mp4", "rmvb", "rm", "mpg", "avi", "mpeg"}*/videos);
	public static final List<String> MusicType = Arrays.<String> asList(/*new String[] { "mp3" }*/audios);
	public static final List<String> ImageType = Arrays.<String> asList(new String[] { "jpg", "png", "jpeg", "bmp", "gif" });
	public static final List<String> ApkType = Arrays.<String> asList(new String[] { "apk" });

	public LocalPalyerPagerAdapter localpalyerAdapter;
	// private FileCategoryLayout menuLocalPlayer;
	private Handler handler;
	private Context mContext;

	private LocalPlayerActivity activity;
	private FileCategoryLayout menuLayout;
//	private List<LocalPalyerPagerViewBase> listPages;
	private ViewPager vPager;
	
	private GridViewPack parent;

	public LocalPalyerPagerViewBase(Context context) {
		super(context);
		this.mContext = context;
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		this.setId(11);
		setClipChildren(false);
		setClipToPadding(false);
		setHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.block_category_item_horizontal_spacing));
		setVerticalSpacing(getResources().getDimensionPixelSize(R.dimen.block_category_item_vertical_spacing));
		int pading = getResources().getDimensionPixelSize(R.dimen.home_focus_border);
		setPadding(pading, pading, pading, pading);

		setOnItemClickListener(this);
		
		activity = (LocalPlayerActivity) mContext;
		menuLayout = activity.menuLayout;
		
	}
	
	public void setParent(GridViewPack parent) {
		this.parent = parent;
	}
	
	public void setAdapter(LocalPalyerPagerAdapter adapter) {
		localpalyerAdapter = adapter;
		super.setAdapter(adapter);
	}

	public void asyncLoadData() {
		// Handler异步更新
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// setData()时，如果gridView
				// 数据为空，emptyView将得到焦点，setData()会导致UI移动到PagerView的第一个Tab :
				// 设置焦点到上部导航可解决此Bug
				//listPages = activity.getListPages();
				vPager = activity.getPager();
				int pos = vPager.getCurrentItem();
				
				for (int i=0;i<activity.getBrowserAdapter().getCount();i++)
				{
					final GridViewPack page = (GridViewPack)activity.getBrowserAdapter().getItem(pos);
					final LocalPalyerPagerViewBase gridView = page.getPagerView();
					if (i == pos && ((ViewGroup) LocalPalyerPagerViewBase.this.getParent()).hasFocus()) {
						menuLayout.getChildAt(i).requestFocus();
					}
				}
				hideLoadingAnimation();
				localpalyerAdapter.setData((List<AlbumData>) msg.obj);
			}
		};
		showLoadingAnimation();
		// 新线程加载数据
		(new Thread() {
			@Override
			public void run() {
				Message msg = new Message();
				msg.obj = findAlbums(getContext());
				msg.setTarget(handler);
				handler.sendMessage(msg);
			}
		}).start();
	}

	/**
	 * 显示数据加载动画
	 */
	private void showLoadingAnimation() {
//		if (this instanceof ImageLocalPalyerPagerView) {
//			activity.animViewImage.show();
//		}
//		if (this instanceof VideoLocalPalyerPagerView) {
//			activity.animViewVideo.show();
//		}
//		if (this instanceof MusicLocalPalyerPagerView) {
//			activity.animViewMusic.show();
//		}
		((GridViewPack)this.getParent()).showAnim();
	}

	/**
	 * 隐藏数据加载动画
	 */
	private void hideLoadingAnimation() {
//		if (this instanceof ImageLocalPalyerPagerView) {
//			activity.animViewImage.cancel();
//		}
//		if (this instanceof VideoLocalPalyerPagerView) {
//			activity.animViewVideo.cancel();
//		}
//		if (this instanceof MusicLocalPalyerPagerView) {
//			activity.animViewMusic.cancel();
//		}
		((GridViewPack)this.getParent()).cancelAnim();
	}

	public abstract List<AlbumData> findAlbums(Context context);

	public static String getGroupId(Uri uri, String bucketId) {
		return String.format("%s#%s", uri, bucketId);
	}

	// @Override
	// public boolean dispatchKeyEvent(KeyEvent event) {
	// Rect r = new Rect();
	// this.getHitRect(r);
	// if (event.getAction() == KeyEvent.ACTION_DOWN) {
	// switch (event.getKeyCode()) {
	// case KeyEvent.KEYCODE_DPAD_UP:
	// FocusFinder.getInstance().findNextFocusFromRect((ViewGroup)
	// this.getParent(), r, FOCUS_UP);
	// if (FocusFinder.getInstance().findNextFocusFromRect((ViewGroup)
	// this.getParent(), r, FOCUS_UP) == null && this.hasFocus()) {
	// // 一级界面，根据browserType,设置向上切换到相应的一级导航
	// View v = findFocus();
	// if (!(v instanceof LocalPalyerPagerViewBase)) {
	// return super.dispatchKeyEvent(event);
	// }
	//
	// if (this instanceof MusicLocalPalyerPagerView) {
	// v.setNextFocusUpId(menuLocalPlayer.getChildAt(3).getId());
	// }
	//
	// if (this instanceof
	// VideoLocalPalyerPagerView||((View)v.getParent()).getId()==33) {
	// v.setNextFocusUpId(menuLocalPlayer.getChildAt(1).getId());
	// }
	// if (this instanceof ImageLocalPalyerPagerView) {
	// v.setNextFocusUpId(menuLocalPlayer.getChildAt(2).getId());
	// }
	// if (this instanceof FileLocalPalyerPagerView) {
	// v.setNextFocusUpId(menuLocalPlayer.getChildAt(0).getId());
	// }
	//
	// }
	// break;
	// case KeyEvent.KEYCODE_DPAD_RIGHT:
	// if (FocusFinder.getInstance().findNextFocusFromRect((ViewGroup)
	// this.getParent(), r, FOCUS_RIGHT) == null && this.hasFocus()) {
	// // 一级界面,阻止音乐向右滚动
	// View v = findFocus();
	// if (!(v instanceof LocalPalyerPagerViewBase)) {
	// return super.dispatchKeyEvent(event);
	// }
	//
	// if (this instanceof MusicLocalPalyerPagerView) { //
	// // v.setNextFocusRightId(menuLocalPlayer.getChildAt(3).getId());
	// v.setNextFocusRightId(v.getId());
	// }
	// }
	// break;
	// }
	// }
	// return super.dispatchKeyEvent(event);
	// }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AlbumData item = localpalyerAdapter.getItem(position);
		onItemClick(getContext(), view, item);
	}

	public void onItemClick(Context context, View view, AlbumData item) {
		if (item.isDirectory) {// 目录
			if (item.fileType == AlbumData.File) {
				// 启动文件二级activity
				Intent it = new Intent(context, SubBrowserActivity.class);
				Bundle mBundle = new Bundle();
				AlbumDataSend sendItem = toDataSend(item);
				mBundle.putSerializable("item", sendItem);
				it.putExtras(mBundle);
				context.startActivity(it);
			} else {
				// 启动媒体二级activity
				Intent it = new Intent(context, MediaBrowserActivity.class);
				Bundle mBundle = new Bundle();
				AlbumDataSend sendItem = toDataSend(item);
				mBundle.putSerializable("item", sendItem);
				it.putExtras(mBundle);
				context.startActivity(it);
			}
		} else {// 文件
			List<String> list = item.listSameDirectoryFiles();
			int index = item.getItemIndex(list);
			Intent it;
			switch (item.fileType) {
			case AlbumData.File:// 文件 ：根据扩展名选择播放器
				List<String> fileList = new ArrayList<String>();
				File f = new File(item.path);
				String end = FileUtils.getExtension(f.getName());
				// 根据类型启动播放器
				if (VideoType.contains(end)) {
					fileList = filterFileType(VideoType, list);
					index = item.getItemIndex(fileList);
					it = new Intent(getContext(), VideoPlayActivity.class);
					it.putExtra("list", (Serializable) fileList);
					it.putExtra("index", index);
					getContext().startActivity(it);
				}
				if (MusicType.contains(end)) {
					it = new Intent(getContext(), MusicPlayerActivity.class);
					fileList = filterFileType(MusicType, list);
					index = item.getItemIndex(fileList);
					it.putExtra("list", (Serializable) fileList);
					it.putExtra("index", index);
					getContext().startActivity(it);
				}
				if (ImageType.contains(end)) {
					fileList = filterFileType(ImageType, list);
					index = item.getItemIndex(fileList);
					it = new Intent(getContext(), ImageViewerActivity.class);
					it.putExtra("list", (Serializable) fileList);
					it.putExtra("index", index);
					it.putExtra("path", item.path);
					getContext().startActivity(it);
				}
				break;
			case AlbumData.Video:// 视频 -->视频播放器
				it = new Intent(getContext(), VideoPlayActivity.class);
				it.putExtra("list", (Serializable) list);
				it.putExtra("index", index);
				getContext().startActivity(it);
				break;
			case AlbumData.Image:// 图片-->图片浏览器
				it = new Intent(getContext(), ImageViewerActivity.class);
				it.putExtra("list", (Serializable) list);
				it.putExtra("index", index);
				it.putExtra("path", item.path);
				getContext().startActivity(it);
				break;
			case AlbumData.Music:// 音乐-->音乐播放器
				it = new Intent(getContext(), MusicPlayerActivity.class);
				it.putExtra("list", (Serializable) list);
				it.putExtra("index", index);
				getContext().startActivity(it);
				break;
			}
		}
	}

	/**
	 * AlbumData转变成方便serial的数据类
	 * 
	 * @param ad
	 * @return
	 */
	private AlbumDataSend toDataSend(AlbumData ad) {
		AlbumDataSend ads = new AlbumDataSend();
		ads.id = ad.id;
		ads.bucketId = ad.bucketId;
		ads.path = ad.path;
		ads.title = ad.title;
		ads.thumbnailUrl = ad.thumbnailUrl;
		ads.updated = ad.updated;
		ads.count = ad.count;
		ads.isDirectory = ad.isDirectory;
		ads.fileType = ad.fileType;
		ads.local = ad.local;
		return ads;
	}

	/** 过滤文件类型 */
	public static List<String> filterFileType(List<String> filerType, List<String> list) {
		List<String> results = new ArrayList<String>();
		for (String path : list) {
			File f = new File(path);
			String end = FileUtils.getExtension(f.getName());
			if (filerType.contains(end)) {
				results.add(path);
			}
		}
		return results;
	}

}
