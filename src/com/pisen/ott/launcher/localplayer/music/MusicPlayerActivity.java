package com.pisen.ott.launcher.localplayer.music;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;

/**
 * 加入了侧滑菜单的音乐播放器
 * @author mugabutie
 */
public class MusicPlayerActivity extends DefaultActivity implements
		OnItemClickListener, OnClickListener {

	public static final int PLAY_RESET = 0x00;
	public static final int PLAY_START = 0x01;
	public static final int PLAY_UPDATE_TITLE = 0x02;
	public static final int PLAY_UPDATE_PROGRESS = 0x03;
	public static final int PLAY_UPDATE_BITMAP = 0x04;
	
	private MusicContorlLayout musicLayout;//控制布局
	private MusicLeftMenuLayout slideListLayout; // 列表布局
	private MusicListView lstMusic;
	private TextView txtSongNameTitle;
	private TextView txtCurrentPosition;
	private TextView txtTotalPosition;
	// 当前播放音乐的路径,可以在代码中改变。
	public static String ExternalDivecePath = "/storage/sdcard0/Music";
	public static final String PLAY_STATE_previous = "previous";
	public static final String PLAY_STATE_next = "next";
	public static final String PLAY_STATE_play = "play";
	public static final String PLAY_STATE_pause = "pause";
	public static final String PLAY_STATE_function = "function";
	public static final String BROADCAST_REFRESH_PROGRESS = "com.music.refreshprogress";
	public static final String BROADCAST_CHANGE_MUSIC = "com.music.changemusic";
	private static final Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private MusicAdapter musicAdapter;
//	private LauncherApplication mApp;
	private Button btnPrevious;
	private Button btnPlayOrPause;
	private Button btnNext;
	private Button btnList;
	private Button btnSound;
	private SeekBar seekBar;
	private ImageView RotationAbulmView;
	private ImageView RotationAbulmViewBg;
	private ImageView RotationAbulmViewHandler;
	private TextView  txtSongCounter;
//	private boolean isPaused = false;
	private int curMusic = 0;
	private int curPercent = 0;
	private int secondaryProgress = 0;
	private List<MusicInifo> musicList = new ArrayList<MusicInifo>();
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private MusicBroadcastReceiver recevier;
	private int currentPosition = 0;
//	private boolean isSoundMute = false;
	
	private int maxVolume = 50; // 最大音量值
    private int curVolume = 20; // 当前音量值
    private int stepVolume = 0; // 每次调整的音量幅度  
    private AudioManager audioMgr = null; // Audio管理器，用了控制音量  
    public static boolean isPlaying = false;
    private AnsyLoadMusicListTask loadTask = null;
	// 更新seekbar
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PLAY_UPDATE_PROGRESS:
				seekBar.setProgress(curPercent);
				seekBar.setSecondaryProgress(secondaryProgress);
				break;
			case PLAY_UPDATE_BITMAP:
				RotationAbulmView.setImageBitmap(getRoundedCornerBitmap(getArtworkFromFile(
										MusicPlayerActivity.this,
										getTempUri(musicList.get(curMusic).url),
										-1), 1));
				musicAdapter.playView(curMusic);
				break;
			case PLAY_START:
				if (msg.obj != null) {
					updateMusicList((List<MusicInifo>) msg.obj);
				}
				break;
			case PLAY_RESET:
				startPlay(songindex);
    			startAnimalImage();
				break;
			case PLAY_UPDATE_TITLE:
				if (msg.obj != null) {
					txtSongCounter.setText(""+(Integer) msg.obj);
				}
				break;
			}
		}
	};
	
	static List<String> list ;
	static int songindex =-1;
	
	public static void start(Context context, List<String> playbacklist, int currIndex) {
		MusicPlayerActivity.list = playbacklist;
		MusicPlayerActivity.songindex = currIndex;		
		context.startActivity(new Intent(context, MusicPlayerActivity.class));
	}

	@Override
	protected void onDestroy() {
		isPlaying = false;
		MusicPlayerActivity.list = null;
		MusicPlayerActivity.songindex = -1;
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.musicplayer_activity);
		parseIntent();
		initView();
		initVolume();
		loadTask = new AnsyLoadMusicListTask();
		loadTask.execute(getIntent());
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
			list = new ArrayList<String>();
			list.add(mCurrentPath);
			songindex = 0;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		isPlaying = true;
	}
	
	@Override
	protected void onPause() {
		stopService(new Intent(this, MusicPlayerService.class));
		if (loadTask != null) {
			loadTask.cancel(true);
			loadTask = null;
		}
		if (recevier != null) {
			this.unregisterReceiver(recevier);
		}
		super.onPause();
	}
	
	private void initView() {
		musicLayout = (MusicContorlLayout) findViewById(R.id.musicLayout);
		slideListLayout = (MusicLeftMenuLayout) findViewById(R.id.leftLayout);
		slideListLayout.hideMenu();
		btnPrevious = (Button) findViewById(R.id.btn_previous);
		btnPlayOrPause = (Button) findViewById(R.id.btn_pause_play);
		btnNext = (Button) findViewById(R.id.btn_next);
		btnList = (Button) findViewById(R.id.btn_list);
		btnSound = (Button) findViewById(R.id.btn_sound);
		seekBar = (SeekBar) findViewById(R.id.seekbar);
		RotationAbulmView = (ImageView) findViewById(R.id.musicabulm);
		RotationAbulmViewBg = (ImageView) findViewById(R.id.musicabulmbg);
		RotationAbulmViewHandler = (ImageView) findViewById(R.id.musicabulm_handler);
		txtSongNameTitle = (TextView) findViewById(R.id.txtSongTitle);
		txtCurrentPosition = (TextView) findViewById(R.id.txtcurrnttime);
		txtTotalPosition = (TextView) findViewById(R.id.txttotaltime);
		txtSongCounter = (TextView) findViewById(R.id.txtmusicnumber);
		
		btnList.setOnClickListener(this);
		btnPrevious.setOnClickListener(this);
		btnPlayOrPause.setOnClickListener(this);
		btnPlayOrPause.requestFocus();
		btnNext.setOnClickListener(this);
		btnSound.setOnClickListener(this);
		
		musicLayout.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if (hasFocus) {
					musicLayout.setChildFocusedView(v);
				}
			}
		});
		
		lstMusic = (MusicListView) findViewById(R.id.lstMusic);
		lstMusic.setAdapter(musicAdapter = new MusicAdapter(this));
		lstMusic.setOnItemClickListener(this);
		lstMusic.setFocusable(false);
		lstMusic.setMasterTitle(musicLayout);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		RotationAbulmView.setImageBitmap(getRoundedCornerBitmap(BitmapFactory
				.decodeResource(getResources(),
						R.drawable.musicplayer_abulm_test), 1));
		
		recevier = new MusicBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_REFRESH_PROGRESS);
		intentFilter.addAction(BROADCAST_CHANGE_MUSIC);
		this.registerReceiver(recevier, intentFilter);
		resetHandler(300);
	}

	/**
	 * 初始化转柄
	 * @param time
	 */
	private void resetHandler(long time){
		RotationAbulmViewHandler.clearAnimation();
		RotateAnimation animation =new RotateAnimation(0f,27f,Animation.RELATIVE_TO_SELF, 
                0.845f,Animation.RELATIVE_TO_SELF,0.5f); 
		animation.setDuration(time);
	    animation.setFillAfter(true);
	    RotationAbulmViewHandler.setAnimation(animation);
	}
	
	public void startAnimalImage() {
//		isPaused = true;
		btnPlayOrPause.setBackgroundResource(R.drawable.musicplayer_playbtn);
		
		if (MusicPlayerService.mediaPlayer == null || !MusicPlayerService.mediaPlayer.isPlaying()) {
			rotationImageView(RotationAbulmViewHandler, R.anim.musicplayer_rotation_handler_left);
			rotationImageView(RotationAbulmViewBg, R.anim.musicplayer_rotation);
		}
		
		rotationImageView(RotationAbulmView,R.anim.musicplayer_couterclockwise_rotation_animal);
		musicAdapter.stopAnimator(false);
	}
	
	/**
	 * 开始播放歌曲
	 * @param index
	 */
	public void startPlay(int index) {
		Intent intent = new Intent(this, MusicPlayerService.class);
		intent.putExtra("state", PLAY_STATE_play);
		intent.putExtra("position", index);
		intent.putExtra("function", -1);
		startService(intent);
	}
	
	public void stoprotationAnimal(){
		resetHandler(500);
		btnPlayOrPause.setBackgroundResource(R.drawable.musicplayer_pausebtn);
		musicAdapter.stopAnimator(true);
		
        float rotation = 0.0f;
        int time = MusicPlayerService.getCurrentTime();
        if(time > 8000){
        	rotation = ((float)(((time * 1.0) % 8000)) / 8000 ) * 360;
        }else{
        	rotation = ((float)(time * 1.0) / 8000) * 360;
        }
        
        RotationAbulmView.clearAnimation();
		RotationAbulmView.setRotation(rotation);
        
        RotationAbulmViewBg.clearAnimation();
//		RotationAbulmViewBg.setRotation(rotation);
		
	}

	/**
	 * 设置旋转动画
	 * @param v
	 */
	public void rotationImageView(View v, int rotationDir) {
		Animation operatingAnim = AnimationUtils.loadAnimation(this,rotationDir);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		operatingAnim.setFillAfter(true);
		v.startAnimation(operatingAnim);
	}

	/**
	 * 图像圆形
	 */
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap, float ratio) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio,
				bitmap.getHeight() / ratio, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.musicplayer_abulm_test);
		float scaleWidth = ((float) bmp.getWidth()) / bitmap.getWidth();
		float scaleHeight = ((float) bmp.getHeight()) / bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(output, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		return resizedBitmap;
	}

	/**
	 * 通过系统的MediaPlayer来获取到时间
	 * @param url
	 */
	public void parseMusicInfo(String url) {
		int duration = 0;
		String name = "";
		
		ContentResolver cr = this.getContentResolver();
		StringBuffer buff = new StringBuffer();
		buff.append("(").append(Audio.AudioColumns.DATA).append("=").append("\"" + url + "\"").append(")");
		String[] projection = { Audio.AudioColumns.DISPLAY_NAME, Audio.AudioColumns.DURATION };
		
		Cursor cur = cr.query(Audio.Media.EXTERNAL_CONTENT_URI, projection, buff.toString().trim(), null, null);
		if (cur.moveToFirst()) {
			duration = cur.getInt(cur.getColumnIndex(Audio.AudioColumns.DURATION));
			String displayName = cur.getString(cur.getColumnIndex(Audio.AudioColumns.DISPLAY_NAME));
			name = displayName.substring(0, displayName.lastIndexOf('.'));
		} else {
			name = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
		}
		
		if (duration > 0) {
			MusicInifo music = new MusicInifo();
			music.setName(name);
			music.setUrl(url);
			music.setDuration(duration);
			musicList.add(music);
			if (musicList.size() == (songindex+1)) {
				Message msg = mHandler.obtainMessage(PLAY_START);
				msg.obj = musicList;
				mHandler.sendMessage(msg);
			}else if(musicList.size() > (songindex+1)){
				Message msg = mHandler.obtainMessage(PLAY_UPDATE_TITLE);
				msg.obj = musicList.size();
				mHandler.sendMessage(msg);
			}
		}
		
	}

	/**
	 * 获取歌曲名
	 * 
	 * @param songname
	 * @return
	 */
	public String getSongName(String songname) {
		int firstPosition = songname.lastIndexOf("/");
		int lastPosition = songname.indexOf(".");
		return songname.substring(firstPosition + 1, lastPosition);
	}

	@Override
	public boolean onBackKeyEvent() {
		if (slideListLayout.isVisible()) {
			slideListLayout.toggleMenu();
			return true;
		}
		return super.onBackKeyEvent();
	}
	

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, MusicPlayerService.class);
		intent.putExtra("position", -1);
		if (v.getId() == R.id.btn_previous) {
			startAnimalImage();
			intent.putExtra("state", PLAY_STATE_previous);
			intent.putExtra("function", -1);
			startService(intent);
		} else if (v.getId() == R.id.btn_pause_play) {
			if (MusicPlayerService.mediaPlayer.isPlaying()) {
				intent.putExtra("state", PLAY_STATE_pause);
				stoprotationAnimal();
			} else {
				startAnimalImage();
				intent.putExtra("currentposition", currentPosition);
				intent.putExtra("state", PLAY_STATE_play);
			}
			intent.putExtra("function", -1);
			startService(intent);
		} else if (v.getId() == R.id.btn_next) {
			startAnimalImage();
			intent.putExtra("state", PLAY_STATE_next);
			intent.putExtra("function", -1);
			startService(intent);
		} else if (v.getId() == R.id.btn_list) {
			slideListLayout.toggleMenu();
			if (slideListLayout.isVisible()) {
				lstMusic.requestChildFocus();
			} else {
				lstMusic.setFocusable(false);
			}

		} 
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		startAnimalImage();
		Intent intent = new Intent(this, MusicPlayerService.class);
		intent.putExtra("state", PLAY_STATE_play);
		intent.putExtra("position", arg2);
		intent.putExtra("function", -1);
		startService(intent);
		lstMusic.setSelectionFromTop(arg2,arg1.getTop());
	}

	/**
	 * 获取mp3内置专辑图
	 * 
	 * @param context
	 * @param songid
	 * @param albumid
	 * @return
	 */
	private Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (FileNotFoundException ex) {

		}
		if (bm == null) {
			bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.musicplayer_abulm_test);
		}
		return bm;
	}

	/**
	 * 格式化时间
	 * 
	 * @param dura
	 * @return
	 */
	private String getStrTime(long dura) {
		long duration = dura;
		int minute = (int) (duration / 1000 / 60);
		int sec = (int) ((duration / 1000) - minute * 60);
		String minutestr = "" + minute;
		String secstr = "" + sec;
		if (minute < 10)
			minutestr = "0" + minutestr;
		if (sec < 10)
			secstr = "0" + secstr;
		return minutestr + ":" + secstr;
	}


	/**
	 * 根据绝对路径来转换为uri
	 * @param uri
	 */
	public int getTempUri(String uri) {
		File file = new File(uri);
		int index = 0;
		if (file.exists()) {
			Log.i("tag", "file exist...");
		}

		if (uri.contains("'")) {
			String tempStr = uri.substring(0, uri.indexOf("'")) + "''"
					+ uri.subSequence(uri.indexOf("'") + 1, uri.length());
			uri = tempStr;
		}
		if (uri != null) {
			ContentResolver cr = this.getContentResolver();
			StringBuffer buff = new StringBuffer();
			buff.append("(").append(Audio.AudioColumns.DATA).append("=")
					.append("'" + uri + "'").append(")");
			Cursor cur = cr.query(Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { Audio.AudioColumns._ID }, buff.toString(),
					null, null);

			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				index = cur.getColumnIndex(Audio.AudioColumns._ID);
				// set _id value
				index = cur.getInt(index);
			}
			if (index == 0) {
				// do nothing
			} else {
				Uri uri_temp = Uri
						.parse("content://media/external/audio/media/" + index);
				Log.d("TAG", "uri_temp is " + uri_temp);
				if (uri_temp != null) {

				}
			}
		}
		return index;
	}


	/**
	 * 菜单键  处理
	 */
	@Override
	public void onMenuKeyEvent(KeyEvent event) {
		slideListLayout.toggleMenu();
		if (slideListLayout.isVisible()) {
			lstMusic.requestChildFocus();
		} else {
			lstMusic.setFocusable(false);
		}
	}

	/**
	 * 音量键 按键处理
	 */
	@Override
	public void onVolumeKeyEvent(KeyEvent event) {
		super.onVolumeKeyEvent(event);
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			int volume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (volume == 0) {
				//关闭静音
				adjustVolume(curVolume);
			}else{
				//静音
				adjustVolume(0);
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			addVolume();
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			reduceVolume();
			break;
		}
	}
	
	/** 
     * 初始化播放器、音量数据等相关工作 
     */  
    private void initVolume() {  
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);  
        // 获取最大音乐音量  
        maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  
        // 初始化音量大概为最大音量的1/2  
        curVolume = maxVolume / 2;  
        // 每次调整的音量大概为最大音量的1/6  
        stepVolume = maxVolume / 5;
        adjustVolume(curVolume);
    }  
    
    /** 
     * 调整音量 
     */  
    private void adjustVolume(int volume) {
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume,  
                AudioManager.FLAG_PLAY_SOUND);
        if (volume <= 0) {
			//静音状态
        	btnSound.setBackgroundResource(R.drawable.musicplayer_volumn_mute);
		}else if(volume >= (maxVolume / 2)){
			//大音量状态
			btnSound.setBackgroundResource(R.drawable.musicplayer_volumn_big);
		}else{
			//小音量状态
			btnSound.setBackgroundResource(R.drawable.musicplayer_volumn_small);
		}
    }  
    
    /**
     * 增加音量
     */
    private void addVolume(){
    	curVolume += stepVolume;  
        if (curVolume >= maxVolume) {  
            curVolume = maxVolume;  
        }
        adjustVolume(curVolume);
    }
    
    /**
     * 减少音量
     */
    private void reduceVolume(){
    	curVolume -= stepVolume;  
        if (curVolume <= 0) {  
            curVolume = 0;  
        }
        adjustVolume(curVolume);
    }
    
    /**
	 * 接收service中变化的时间
	 * @author mugabutie
	 *
	 */
    private class MusicBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_REFRESH_PROGRESS)) {
				curMusic = intent.getIntExtra("curMusic", 0);
				curPercent = intent.getIntExtra("curPercent", 0);
				secondaryProgress = intent.getIntExtra("secondaryProgress", 0);
				txtCurrentPosition.setText(""
						+ getStrTime(intent.getIntExtra("curPostion", 0)));
				mHandler.sendEmptyMessage(PLAY_UPDATE_PROGRESS);
			} else if (intent.getAction().equals(BROADCAST_CHANGE_MUSIC)) {
				curMusic = intent.getIntExtra("curMusic", 0);
				curPercent = intent.getIntExtra("curPercent", 0);
				secondaryProgress = intent.getIntExtra("secondaryProgress", 0);
				txtSongNameTitle.setText(musicList.get(curMusic).name + "");
				txtCurrentPosition.setText(""
						+ getStrTime(intent.getIntExtra("curPostion", 0)));
				txtTotalPosition.setText(""
						+ getStrTime((int) musicList.get(curMusic).duration));
				mHandler.sendEmptyMessage(PLAY_UPDATE_BITMAP);
			}
			currentPosition = intent.getIntExtra("curPostion", 0);
		}
	}
	
    /**
     * 更新音乐播放列表
     * @param list
     */
    private void updateMusicList(List<MusicInifo> list){
    	if (list != null && !list.isEmpty()) {
    		txtSongCounter.setText(""+list.size());
			MusicPlayerService.musicList.clear();
			MusicPlayerService.musicList.addAll(list);
    		if (musicAdapter != null ) {
    			if (musicAdapter.isEmpty()) {
        			musicAdapter.setData(list);
        			//设置默认
        			lstMusic.setSelection(songindex);
        			mHandler.sendEmptyMessageDelayed(PLAY_RESET, 500);
    			}else{
        			musicAdapter.setData(list);
    			}
			}
		}
    }
    
    /**
	 * 异步加载解析音频信息
	 * @author Liuhc
	 * @version 1.0 2015年4月21日 下午5:12:49
	 */
	private class AnsyLoadMusicListTask extends AsyncTask<Intent, Void,List<MusicInifo>>{

		@Override
		protected List<MusicInifo> doInBackground(Intent... params) {
			Intent intent = params[0];
			//List<String> list = (List<String>) intent.getSerializableExtra("list");
			//songindex = intent.getIntExtra("index", 0);
			if (list == null || songindex >= list.size()) {
				Toast.makeText(MusicPlayerActivity.this, "播放出错", Toast.LENGTH_SHORT).show();
				MusicPlayerActivity.this.finish();
				return musicList;
			}
			for (String url : list) {
				parseMusicInfo(url);
			}
			return musicList;
		}

		@Override
		protected void onPostExecute(List<MusicInifo> result) {
			super.onPostExecute(result);
			updateMusicList(result);
		}
	}
}
