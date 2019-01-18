package com.pisen.ott.launcher.localplayer.video;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.MediaController.OnVideoPlayerControl;
import io.vov.vitamio.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsSeekBar;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.localplayer.music.MusicLeftMenuLayout;
import com.pisen.ott.launcher.localplayer.video.VideoBottomMenuLayout.OnItemBottonClickListener;
import com.pisen.ott.launcher.utils.DateUtils;

/**
 * 每次进来的时候,音量都是静音,是否存在不妥. 视频播放器
 */
@SuppressLint("HandlerLeak")
public class VideoPlayActivity extends DefaultActivity implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener, OnCompletionListener, OnInfoListener, OnItemClickListener, OnVideoPlayerControl,
		OnItemBottonClickListener {

	private long mCurrent = -1;
	private Uri mUri;
	private VideoAdapter mVideoAdapter;
	private List<VideoInfo> mVideoList = new ArrayList<VideoInfo>();
	private int mTotalVideoCount;
	private int mCurrentPlayPosition = 0;

	private VideoView mVideoView;
	private ImageView imgPause;
	private MediaController mMC;
	private String mVideoName = null;

	private VideoBottomMenuLayout bottomMenuLayout;
	private MusicLeftMenuLayout leftMenuLayout;
	private VideoListView lstMenuItem;
	// 扫描接收路径
	static List<String> lstVideoAbsolutePath;
	static int index;
	// 初始化播放控件
	private ImageView imgPlayOrPause;
	private TextView txtVideoName;
	private TextView txtCurrentTime;
	private TextView txtTotalTime;
	private ProgressBar volumeBar;
	private SeekBar seekbar;
	private int maxVolum, curVolum;
	private Boolean bMute = false;
	private long mVideoDuration, mVideoCurrentPosition;
	private TextView txtVideoRoad, txtVideoCount;
	private ImageView imgVolumIcon;
	private AudioManager audioMgr;

	private Boolean mDragging = false;
	private final static int SHOW_PROGRESS = 0x1;
	private final static int HIDE_LEFTMENU = 0x2;
	// private final static int DEFAULT_TIMEOUT = 2 * 1000;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			long pos;
			switch (msg.what) {
			case SHOW_PROGRESS:
				pos = setProgress();
				if (!mDragging && bottomMenuLayout.isVisible()) {
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));
				}
				break;
			case HIDE_LEFTMENU:
				if (null != leftMenuLayout && leftMenuLayout.isVisible()) {
					leftMenuLayout.toggleMenu();
				}
				break;
			}
		}
	};
	
	public static void start(Context context, List<String> playbacklist, int currIndex) {
		VideoPlayActivity.lstVideoAbsolutePath = playbacklist;
		VideoPlayActivity.index = currIndex;		
		context.startActivity(new Intent(context, VideoPlayActivity.class));
	}

	@Override
	protected void onDestroy() {
		VideoPlayActivity.lstVideoAbsolutePath = null;
		VideoPlayActivity.index = -1;
		
		if (mVideoView != null) {
			mVideoView.stopPlayback();
		}
		super.onDestroy();
	}
	
	
	private void parseIntent() {
		String mCurrentPath = null;
		Intent intent = getIntent();
		String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) { // from outer file manager
			Uri uri = intent.getData();
			if ("file".equals(uri.getScheme())) {
				mCurrentPath = uri.getPath();
			} else {
				mCurrentPath = uri.toString();
			}
		}
		if (mCurrentPath != null) {
			lstVideoAbsolutePath = new ArrayList<String>();
			lstVideoAbsolutePath.add(mCurrentPath);
			index = 0;
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.videoplayer_activity);
		initView();
		parseIntent();
		UdiskReally();
	}

	/**
	 * 实际调用项
	 * 
	 * @describtion
	 */
	private void UdiskReally() {
		initReceiverData();
		bindingListener();
	}

	/**
	 * 绑定监听器
	 * 
	 * @describtion
	 */
	private void bindingListener() {
		mMC.setOnVideoPlayerControl(this);
		mVideoView.setOnInfoListener(this);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		imgPlayOrPause.requestFocus();
	}

	/**
	 * 初始化接收到的数据
	 * 
	 * @describtion
	 */
	@SuppressWarnings("unchecked")
	private void initReceiverData() {
		if (lstVideoAbsolutePath == null || index >= lstVideoAbsolutePath.size()) {
			Toast.makeText(this, "播放出错", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (index != -1) {
			mUri = getVideoUri(lstVideoAbsolutePath.get(index));
		}
		//Intent intent = getIntent();
		//lstVideoAbsolutePath = (List<String>) intent.getSerializableExtra("list");
		//mCurrentPlayPosition = intent.getIntExtra("index", -1);
		mCurrentPlayPosition = index;
//		if (mCurrentPlayPosition != -1) {
//			mUri = getVideoUri(lstVideoAbsolutePath.get(mCurrentPlayPosition));
//		}

		for (int i = 0; i < lstVideoAbsolutePath.size(); i++) {
			AddVideoDataItem(lstVideoAbsolutePath.get(i));
		}
		mVideoAdapter.setData(mVideoList);
		//lstMenuItem.setSelection(mCurrentPlayPosition);// 设置左菜单选中项
		mTotalVideoCount = mVideoAdapter.getCount();
		txtVideoCount.setText(mTotalVideoCount + "");
		setPlayVideoUri(mUri);
	}

	/**
	 * @describtion 设置播放路径,暂时将 判读 暂停标志的 方法于此方法体内
	 * @param mUri
	 */
	private void setPlayVideoUri(Uri mUri) {
		lstMenuItem.setSelection(mCurrentPlayPosition);
		mVideoAdapter.playViewPosition(mCurrentPlayPosition);
		mVideoView.setVideoURI(mUri);
		dismissPauseBigIcon();		
		initVideoInfo();
	}

	/**
	 * 初始化View 控件
	 * 
	 * @describtion
	 */
	private void initView() {
		audioMgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mVideoView = (VideoView) findViewById(R.id.surface);

		lstMenuItem = (VideoListView) findViewById(R.id.lstMenuItem);
		mVideoAdapter = new VideoAdapter(this);

		lstMenuItem.setAdapter(mVideoAdapter);
		lstMenuItem.setOnItemClickListener(this);
		// lstMenuItem.setOnItemSelectedListener(mVideoAdapter);

		leftMenuLayout = (MusicLeftMenuLayout) findViewById(R.id.leftMenuLayout);
		bottomMenuLayout = (VideoBottomMenuLayout) findViewById(R.id.BottomControlLayout);
		bottomMenuLayout.setOnItemBottonClickListener(this);

		imgPlayOrPause = (ImageView) findViewById(R.id.imgPlayOrPause);
		imgPause = (ImageView) findViewById(R.id.imgPause);
		txtVideoName = (TextView) findViewById(R.id.txt_musicname);

		txtCurrentTime = (TextView) findViewById(R.id.txtCurrentTime);
		txtTotalTime = (TextView) findViewById(R.id.txtTotalTime);
		txtVideoRoad = (TextView) findViewById(R.id.txtVideoRoad);
		txtVideoCount = (TextView) findViewById(R.id.txtVideoCount);

		imgVolumIcon = (ImageView) findViewById(R.id.imgVolumIcon);
		volumeBar = (ProgressBar) findViewById(R.id.volumeBar);
		mMC = MediaController.getInstance(this);

		mVideoView.setMediaController(mMC);
		curVolum = mMC.getCurrentVolum();
		maxVolum = mMC.getMaxVolum();
		volumeBar.setMax(maxVolum);

		seekbar = (SeekBar) findViewById(R.id.seekbar);
		if (seekbar instanceof AbsSeekBar) {
			AbsSeekBar bar = (AbsSeekBar) seekbar;
			bar.setKeyProgressIncrement(10);
		}
		seekbar.setOnSeekBarChangeListener(mSeekListener);
		seekbar.setMax(1000);
	}

	/**
	 * 左右键 滑动 进度条变化监听
	 */
	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (!fromUser) {
				return;
			}
			dismissPauseBigIcon();
			long newPostion = (mVideoDuration * progress) / 1000;
//			Toast.makeText(VideoPlayActivity.this, DateUtils.generateTime(newPostion), Toast.LENGTH_SHORT).show();
			txtCurrentTime.setText(DateUtils.generateTime(newPostion));
			mVideoView.seekTo(newPostion);
			// 为什么会重头开始播放
			if (newPostion >= mVideoView.getDuration()) {
				slidShow();
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};

	/**
	 * 音量键 按键处理
	 */
	@Override
	public void onVolumeKeyEvent(KeyEvent event) {
		super.onVolumeKeyEvent(event);
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			setVolumeAdjust(setVolumeAdd());
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			setVolumeAdjust(setVolumeSub());
			break;
		}
	}

	/**
	 * 菜单键 处理
	 */
	@Override
	public void onMenuKeyEvent(KeyEvent event) {
		if (bottomMenuLayout.isVisible()) {
			bottomMenuLayout.hideMenu();
		}
		leftMenuLayout.toggleMenu();
	}

	/**
	 * 返回按键的监听
	 */
	@Override
	public boolean onBackKeyEvent() {
		if (bottomMenuLayout.isVisible()) {
			bottomMenuLayout.hideMenu();
			return true;
		}
		if (leftMenuLayout.isVisible()) {
			leftMenuLayout.hideMenu();
			return true;
		}
		// stopPlayer();
		return super.onBackKeyEvent();
	}

	/**
	 * 五行键盘处理
	 */
	@Override
	public boolean executeKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (!leftMenuLayout.isVisible()) {
					seekbar.setFocusable(false);
					if (bottomMenuLayout.toggleMenu()) {
						imgPlayOrPause.requestFocus();
						mHandler.sendEmptyMessage(SHOW_PROGRESS);
						initVideoInfo();
					} else {
						mHandler.removeMessages(SHOW_PROGRESS);
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				setSeekBarAdjust();
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				setSeekBarAdjust();
				break;
			case KeyEvent.KEYCODE_ENTER:
				if (!leftMenuLayout.isVisible() && !bottomMenuLayout.isVisible()) {
					isPauseOrPlay();
				}
				break;
			}
		}

		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (seekbar.isFocused()) {
					if (isPlaying()) {
						mHandler.removeMessages(SHOW_PROGRESS);
						mDragging = false;
						mHandler.sendEmptyMessage(SHOW_PROGRESS);
					}
				}
				break;
			}
		}
		return super.executeKeyEvent(event);
	}

	/**
	 * 初始化时间显示控件
	 */
	private void initVideoInfo() {
		txtVideoName.setText(mVideoView.getVideoShortName());
		txtCurrentTime.setText(DateUtils.generateTime(mVideoView.getCurrentPosition()));
		txtTotalTime.setText(DateUtils.generateTime(mVideoView.getDuration()));
		if (curVolum == 0) {
			bMute = true;
			imgVolumIcon.setImageDrawable(getResources().getDrawable(R.drawable.videoplayer_sound_mute));
		}
		if (!bMute) {
			volumeBar.setProgress(curVolum);
			audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			mVideoView.setVolume(curVolum, curVolum);
		} else {
			audioMgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		}
	}

	/**
	 * SeekBar 进度步长调节
	 */
	public void setSeekBarAdjust() {
		if (!leftMenuLayout.isVisible() && !bottomMenuLayout.isVisible()) {
			initVideoInfo();
			mDragging = true;
			seekbar.setFocusable(true);
			bottomMenuLayout.toggleMenu();
		}
	}

	/**
	 * @describtion 显示底部控制栏
	 * @return
	 */
	public Boolean showBottomMenu() {
		if (!bottomMenuLayout.isVisible()) {
			initVideoInfo();
			bottomMenuLayout.toggleMenu();
			mHandler.removeMessages(SHOW_PROGRESS);
			mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
			return true;
		}
		return false;
	}

	/**
	 * 统一进行音量调整
	 * 
	 * @describtion
	 * @param curVolum
	 */
	public void setVolumeAdjust(int curVolum) {
		showBottomMenu();
		volumeBar.setProgress(curVolum);
		mVideoView.setVolume(curVolum, curVolum);
	}

	/**
	 * 设置音量 ++
	 * 
	 * @describtion
	 */
	public int setVolumeAdd() {
		if (++curVolum > maxVolum) {
			curVolum = maxVolum;
		}
		if (curVolum > 0) {
			bMute = false;
			audioMgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			imgVolumIcon.setImageDrawable(getResources().getDrawable(R.drawable.videoplayer_volum_selector));
		}
		return curVolum;
	}

	/**
	 * 设置音量--
	 * 
	 * @describtion
	 */
	public int setVolumeSub() {
		if (--curVolum <= 0) {
			curVolum = 0;
			bMute = true;
			imgVolumIcon.setImageDrawable(getResources().getDrawable(R.drawable.videoplayer_sound_mute));
		}
		return curVolum;
	}

	/**
	 * 暂停播放
	 * @describtion
	 */
	private void stopPlayer() {
		if (mVideoView != null) {
			mVideoView.pause();
		}
	}

	/**
	 * 开始播放
	 * 
	 * @describtion
	 */
	private void startPlayer() {
		if (mVideoView != null) {
			mVideoView.start();
		}
	}

	/**
	 * 判断是否在播放
	 * 
	 * @describtion
	 * @return
	 */
	private boolean isPlaying() {
		return mVideoView != null && mVideoView.isPlaying();
	}

	/**
	 * 显示暂停大图标
	 * 
	 * @describtion
	 */
	private Boolean showImgPause() {
		imgPause.setVisibility(View.VISIBLE);
		return true;
	}

	/**
	 * 隐藏暂停大图标
	 * 
	 * @describtion
	 */
	private Boolean hideImgPause() {
		imgPause.setVisibility(View.GONE);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != mHandler) {
			mHandler.removeMessages(SHOW_PROGRESS);
		}
		if (mVideoView != null) {
			mCurrent = mVideoView.getCurrentPosition();
			mVideoView.pause();
		}
	}

	@Override
	protected void onResume() {
		if (mVideoView != null) {
			mVideoView.resume();
			if (mCurrent >= 0) {
				mVideoView.seekTo(mCurrent);
			}
		}
		initPlayState();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mCurrent = -1;
		new AlertDialog.Builder(this).setTitle("不能播放视频").setMessage("对不起,这个视频不能播放")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				}).setCancelable(false).show();
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.setPlaybackSpeed(1.0f);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		slidShow();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			if (isPlaying()) {
				stopPlayer();
			}
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			startPlayer();
			break;
		}
		return true;
	}

	/**
	 * 根据绝对路径来转换为uri
	 * 
	 * @param uri
	 */
	public void AddVideoDataItem(String url) {
		if (url != null) {
			ContentResolver cr = this.getContentResolver();
			StringBuffer buff = new StringBuffer();
			buff.append("(").append(Video.VideoColumns.DATA).append("=").append("'" + url + "'").append(")");
			String[] projection = { Video.VideoColumns.DISPLAY_NAME, Video.VideoColumns.DURATION };
			// 媒体外部内容URI
			Cursor cur = cr.query(Video.Media.EXTERNAL_CONTENT_URI, projection, buff.toString().trim(), null, null);
			if (cur.moveToFirst()) {
				mVideoDuration = cur.getInt(cur.getColumnIndex(Video.VideoColumns.DURATION));
				String displayName = cur.getString(cur.getColumnIndex(Video.VideoColumns.DISPLAY_NAME));
				mVideoName = displayName.substring(0, displayName.lastIndexOf('.'));
			} else {
				mVideoName = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
			}
			if (mVideoDuration >= 0) {
				VideoInfo video = new VideoInfo();
				video.setName(mVideoName);
				video.setUrl(url);
				video.setDuration(mVideoDuration);
				mVideoList.add(video);
			}
		}
	}

	/**
	 * @describtion 解析String 路径成 Uri
	 * @param uriString
	 * @return uristring -->uri
	 */
	public Uri getVideoUri(String uriString) {
		return Uri.parse(uriString);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		VideoInfo videoItem = (VideoInfo) parent.getItemAtPosition(position);
		mCurrentPlayPosition = position;
		setPlayVideoUri(getVideoUri(videoItem.getUrl()));
		// if (null != mHandler) {
		// mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE_LEFTMENU),
		// DEFAULT_TIMEOUT);
		// }
	}

	/**
	 * 底层提供接口(上一视频)
	 */
	@Override
	public void previous() {
		if (mCurrentPlayPosition == 0) {
			mCurrentPlayPosition = mTotalVideoCount - 1;
		} else {
			mCurrentPlayPosition--;
		}
		setPlayVideoUri(getVideoUri(mVideoList.get(mCurrentPlayPosition).getUrl()));
	}

	/**
	 * 底层提供接口（下一视频）
	 */
	@Override
	public void next() {
		if (mCurrentPlayPosition == mTotalVideoCount - 1) {
			mCurrentPlayPosition = 0;
		} else {
			mCurrentPlayPosition++;
		}
		setPlayVideoUri(getVideoUri(mVideoList.get(mCurrentPlayPosition).getUrl()));
	}

	/**
	 * 顺序播放
	 */
	public void slidShow() {
		if (mCurrentPlayPosition == mTotalVideoCount - 1) {
			finish();
			return;
		}
		next();
	}

	/**
	 * 播放控制栏控制事件触发
	 */
	@Override
	public void onItemBottonClick(View v) {
		switch (v.getId()) {
		case R.id.imgPrevious:
			previous();
			break;
		case R.id.imgNext:
			next();
			break;
		case R.id.imgPlayOrPause:
			isPauseOrPlay();
			break;
		default:
			break;
		}
	}

	/**
	 * 暂时播放状态切换
	 * 
	 * @describtion
	 */
	public void isPauseOrPlay() {
		if (isPlaying()) {
			showImgPause();
			imgPlayOrPause.setImageResource(R.drawable.videoplayer_play_selector);
			stopPlayer();
		} else {
			hideImgPause();
			imgPlayOrPause.setImageResource(R.drawable.videoplayer_pause_selector);
			startPlayer();
		}
	}

	/**
	 * @describtion 设置播放进度信息
	 * @return 返回视频当前播放位置
	 */
	public long setProgress() {
		if (null == mVideoView || mDragging) {
			return 0;
		}
		long position = mVideoView.getCurrentPosition();
		long duration = mVideoView.getDuration();
		if (seekbar != null) {
			if (duration > 0) {
				long pos = 1000L * position / duration;
				seekbar.setProgress((int) pos);
			}
		}

		mVideoDuration = duration;
		if (txtTotalTime != null)
			txtTotalTime.setText(DateUtils.generateTime(mVideoDuration));
		if (txtCurrentTime != null)
			txtCurrentTime.setText(DateUtils.generateTime(position));

		return position;
	}

	/**
	 * @des 初始化成播放状态
	 */
	public void initPlayState() {
		if (isPlaying()) {
			imgPlayOrPause.setImageResource(R.drawable.videoplayer_pause_selector);
		}
	}

	/**
	 * 通常情况下,快进,快退,上一视频,下一视频
	 * 
	 * @describtion
	 */
	public void dismissPauseBigIcon() {
		if (showImgPause()) {
			hideImgPause();
			imgPlayOrPause.setImageResource(R.drawable.videoplayer_pause_selector);
		}
	}
}
