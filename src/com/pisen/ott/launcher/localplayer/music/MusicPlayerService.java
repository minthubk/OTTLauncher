package com.pisen.ott.launcher.localplayer.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pisen.ott.launcher.LauncherApplication;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MusicPlayerService extends Service implements
		OnBufferingUpdateListener, OnCompletionListener,
		MediaPlayer.OnPreparedListener, OnInfoListener, OnErrorListener {
	public static final String PLAY_STATE_previous = "previous";
	public static final String PLAY_STATE_next = "next";
	public static final String PLAY_STATE_play = "play";
	public static final String PLAY_STATE_pause = "pause";
	public static final String PLAY_STATE_function = "function";
	public static MediaPlayer mediaPlayer;
	public static List<MusicInifo> musicList = new ArrayList<MusicInifo>();
//	int Max;
	int curMusic = 0;
	int bufferingProgress;
	int curFunction = 0;
//	LauncherApplication mApp;
    private int currentposition = 0;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mHandler.sendEmptyMessageDelayed(1, 500);
			switch (msg.what) {
			case 1:
				if (mediaPlayer == null)
					return;
				int position = mediaPlayer.getCurrentPosition();
				int duration = mediaPlayer.getDuration();
				int percent = position * 100 / duration;

				Intent intent = new Intent(
						MusicPlayerActivity.BROADCAST_REFRESH_PROGRESS);
				intent.putExtra("curMusic", curMusic);
				intent.putExtra("curPercent", percent);
				intent.putExtra("curPostion", position);
				intent.putExtra("secondaryProgress", bufferingProgress);
				sendBroadcast(intent);

				break;
			}
		}

	};
    
	public static int getCurrentTime()
	{
		if(mediaPlayer != null)
			return mediaPlayer.getCurrentPosition();
		else
			return 0;
	}
	@Override
	public void onCreate() {
//		mApp = (LauncherApplication) getApplication();
//		musicList = mApp.getMusicList();
//		if (musicList != null) {
//			Max = musicList.size();
//		}
		super.onCreate();
	}

	/**
	 * 发送更新进度条
	 */
	private void sendMessage() {
		if (mHandler.hasMessages(1)) {
			mHandler.removeMessages(1);
		}
		mHandler.sendEmptyMessage(1);
	}

	/**
	 * 移除定时器handler
	 */
	private void removeMessage() {
		if (mHandler.hasMessages(1)) {
			mHandler.removeMessages(1);
		}
	}

	/**
	 * 播放
	 */
	public void play() {
		releaseMediaPlay();
		if(curMusic <= musicList.size()){
			try {
				if (!MusicPlayerActivity.isPlaying) {
					stopSelf();
					return;
				}
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setOnBufferingUpdateListener(this);
				mediaPlayer.setOnPreparedListener(this);
				mediaPlayer.setOnCompletionListener(this);
				mediaPlayer.reset();
				mediaPlayer.setDataSource(musicList.get(curMusic).getUrl());
				mediaPlayer.prepare();
				Log.i("time", "time: " + mediaPlayer.getDuration());
				sendChangeMusicBroadcast();
	
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 切换歌曲广播
	 */
	private void sendChangeMusicBroadcast() {
		removeMessage();
		if (mediaPlayer == null)
			return;
		int position = mediaPlayer.getCurrentPosition();
		int duration = mediaPlayer.getDuration();
		int percent = position * 100 / duration;

		Intent intent = new Intent(MusicPlayerActivity.BROADCAST_CHANGE_MUSIC);
		intent.putExtra("curMusic", curMusic);
		intent.putExtra("curPercent", percent);
		intent.putExtra("secondaryProgress", bufferingProgress);
		sendBroadcast(intent);

		mHandler.sendEmptyMessageDelayed(1, 500);
	}

	/**
	 * 暂停
	 */
	public void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			//removeMessage();
		}
	}

	/**
	 * 重放
	 */
	public void resumePlay() {
		if (mediaPlayer == null)
			return;
	
		if (currentposition > 0) {
			mediaPlayer.seekTo(currentposition);
			mediaPlayer.start();
			sendMessage();
		} else {
			play();
		}

	}

	/**
	 * 停止
	 */
	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			removeMessage();
			mediaPlayer = null;
		}
	}

	/**
	 * 上一首
	 */
	public void previous() {
		if (curMusic == 0) {
			curMusic = musicList.size() - 1;
			play();
		} else {
			curMusic--;
			play();
		}
	}

	/**
	 * 下一首
	 */
	public void next() {
		if (curMusic + 1 == musicList.size()) {
			curMusic = 0;
			play();
		} else {
			curMusic++;
			play();
		}
	}

	/**
	 * 顺序播放
	 */
	public void slidShow() {
		curMusic++;
		if (curMusic == musicList.size()) {
			releaseMediaPlay();
			removeMessage();
		} else {
			play();
		}
	}
	public void releaseMediaPlay() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.reset();
			}
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		arg0.start();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		switch (curFunction) {
		case 0:
			slidShow();
			break;
		}

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {

		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
		if (!MusicPlayerActivity.isPlaying) {
			pause();
			releaseMediaPlay();
			stopSelf();
			return;
		}
		this.bufferingProgress = bufferingProgress;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		next();
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		String state = intent.getStringExtra("state");
		int position = intent.getIntExtra("position", -1);
		if (position != -1)
			curMusic = position;
		if (state.trim().equals(PLAY_STATE_previous)) {
			previous();
		} else if (state.trim().equals(PLAY_STATE_next)) {
			next();
		} else if (state.trim().equals(PLAY_STATE_play)) {
			
			currentposition = intent.getIntExtra("currentposition", 0);
			resumePlay();
			if (position == -1)
				return;
		} else if (state.trim().equals(PLAY_STATE_pause)) {
			pause();
			return;
		} else if (state.trim().equals(PLAY_STATE_function)) {
			return;
		}
		play();
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		stop();
		if (!musicList.isEmpty()) {
			musicList.clear();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}