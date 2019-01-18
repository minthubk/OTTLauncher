package com.pisen.ott.launcher.localplayer;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.izy.widget.DefaultPagerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

/**
 * 本地播放
 * 
 * @author yangyp
 */
public class LocalPlayerActivity extends DefaultActivity implements OnPageChangeListener, OnItemClickListener {

	public FileCategoryLayout menuLayout;
	private ViewPager vPager;
	private DefaultPagerAdapter browserAdapter;

	private GridViewPack frmFile;
	private GridViewPack frmVideo;
	private GridViewPack frmImage;
	private GridViewPack frmMusic;

	public OTTWiatProgress animViewVideo;
	public OTTWiatProgress animViewImage;
	public OTTWiatProgress animViewMusic;

	private VideoLocalPalyerPagerView videoView;
	private FileLocalPalyerPagerView fileView;
	private ImageLocalPalyerPagerView imageView;
	private MusicLocalPalyerPagerView musicView;
//	private List<LocalPalyerPagerViewBase> listPages;// 保存4个GridView

	/**
	 * 刷新media数据
	 */
	private void refreshMediaChanged() {
		int pos = vPager.getCurrentItem();

		for (int i=1;i<browserAdapter.getCount();i++)
		{
			final GridViewPack page = (GridViewPack)	browserAdapter.getItem(i);
			final LocalPalyerPagerViewBase gridView = page.getPagerView();
			if (i == pos) {
				menuLayout.getChildAt(i).requestFocus();
			}
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					((LocalPalyerPagerAdapter) gridView.getAdapter()).setData((List<AlbumData>) msg.obj);
					//page.cancelAnim();
				}
			};
			(new Thread() {
				@Override
				public void run() {
					Message msg = new Message();
					msg.obj = gridView.findAlbums(LocalPlayerActivity.this);
					msg.setTarget(handler);
					handler.sendMessage(msg);
				}
			}).start();
		}
		
	}




	private void refreshFileChanged() {
		int pos = vPager.getCurrentItem();
		LocalPalyerPagerViewBase gridView =(LocalPalyerPagerViewBase) ((GridViewPack)browserAdapter.getItem(0)).getPagerView();
		((LocalPalyerPagerAdapter) gridView.getAdapter()).setData(gridView.findAlbums(this));
		vPager.setCurrentItem(pos);


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.localplayer);
		menuLayout = (FileCategoryLayout) findViewById(R.id.menuLocalPlayer);
		menuLayout.setOnItemClickListener(this);
		//frmFile = new GridViewPack(this);
		//frmVideo = new GridViewPack(this);
		//frmImage = new GridViewPack(this);
		//frmMusic = new GridViewPack(this);

		browserAdapter = new DefaultPagerAdapter();
		initImagePage(new FileLocalPalyerPagerView(this),GridViewPack.FRM_FILE_ID);
		initImagePage(new VideoLocalPalyerPagerView(this),GridViewPack.FRM_VIDEO_ID);
		initImagePage(new ImageLocalPalyerPagerView(this),GridViewPack.FRM_IMAGE_ID);
		initImagePage(new MusicLocalPalyerPagerView(this),GridViewPack.FRM_MUSIC_ID);
		
		vPager = (ViewPager) findViewById(R.id.vPager);
		vPager.setAdapter(browserAdapter);		
		vPager.setOffscreenPageLimit(browserAdapter.getCount() - 1);
		vPager.setOnPageChangeListener(this);
		menuLayout.setvPager(vPager);
		


		/*listPages = new ArrayList<LocalPalyerPagerViewBase>();
		listPages.add(fileView);
		listPages.add(videoView);
		listPages.add(imageView);
		listPages.add(musicView);*/
		// 注册Usb插拔广播
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		iFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		iFilter.addDataScheme("file");
		registerReceiver(usbReceiver, iFilter);
	}

	private void initFilePage() {
		LinearLayout emptyFile = (LinearLayout) getLayoutInflater().inflate(R.layout.empty, null, false);
		fileView = new FileLocalPalyerPagerView(this);
		frmFile.addView(fileView);
		frmFile.addView(emptyFile);
		frmFile.setId(GridViewPack.FRM_FILE_ID);
		fileView.setEmptyView(emptyFile);
		browserAdapter.add(frmFile);
	}

	private void initVideoPage() {
		LinearLayout emptyVideo = (LinearLayout) getLayoutInflater().inflate(R.layout.empty, null, false);
		animViewVideo = new OTTWiatProgress(this);
		animViewVideo.setVisibility(View.GONE);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		animViewVideo.setLayoutParams(lp);
		videoView = new VideoLocalPalyerPagerView(this);
		frmVideo.addView(videoView);
		frmVideo.addView(emptyVideo);
		frmVideo.addView(animViewVideo);
		frmVideo.setId(GridViewPack.FRM_VIDEO_ID);
		videoView.setEmptyView(emptyVideo);
		browserAdapter.add(frmVideo);
	}

	private void initImagePage(LocalPalyerPagerViewBase pageView,int id) {
		GridViewPack page = new GridViewPack(this, menuLayout, pageView, id);
		browserAdapter.add(page);
		pageView.asyncLoadData();
	}

	private void initMusicPage() {
		LinearLayout emptymusic = (LinearLayout) getLayoutInflater().inflate(R.layout.empty, null, false);
		animViewMusic = new OTTWiatProgress(this);
		animViewMusic.setVisibility(View.GONE);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		animViewMusic.setLayoutParams(lp);
		musicView = new MusicLocalPalyerPagerView(this);
		frmMusic.addView(musicView);
		frmMusic.addView(emptymusic);
		frmMusic.addView(animViewMusic);
		frmMusic.setId(GridViewPack.FRM_MUSIC_ID);
		musicView.setEmptyView(emptymusic);
		browserAdapter.add(frmMusic);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(usbReceiver);
		super.onDestroy();
	}

	private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				// 刷新
				refreshFileChanged();
			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				// 刷新
				refreshFileChanged();
				//加载媒体动画
				for (int i=1;i<browserAdapter.getCount();i++)
				{
					GridViewPack page = (GridViewPack)	browserAdapter.getItem(i);
					page.showAnim();
				}
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				// 刷新
				refreshMediaChanged();
				//取消媒体动画
				for (int i=1;i<browserAdapter.getCount();i++)
				{
					GridViewPack page = (GridViewPack)	browserAdapter.getItem(i);
					page.cancelAnim();
				}
			}
		}
	};

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		menuLayout.setIndex(position);
	}

	@Override
	public void onItemClick(View v) {
		switch (v.getId()) {
		case R.id.btnFileLocalPlayer:
			vPager.setCurrentItem(0);
			break;
		case R.id.btnVideoLocalPlayer:
			vPager.setCurrentItem(1);
			break;
		case R.id.btnImageLocalPlayer:
			vPager.setCurrentItem(2);
			break;
		case R.id.btnMusicLocalPlayer:
			vPager.setCurrentItem(3);
			break;
		}
	}

//	public List<LocalPalyerPagerViewBase> getListPages() {
//		return listPages;
//	}

	public ViewPager getPager() {
		return vPager;
	}


	public DefaultPagerAdapter getBrowserAdapter() {
		return browserAdapter;
	}

}
