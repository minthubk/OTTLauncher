/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vov.vitamio.widget;

import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.StringUtils;
import java.lang.reflect.Method;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons
 * like "Play/Pause" and a progress slider. It takes care of synchronizing the
 * controls with the state of the MediaPlayer.
 * <p/>
 * The way to use this class is to a) instantiate it programatically or b)
 * create it in your xml layout.
 * <p/>
 * a) The MediaController will create a default set of controls and put them in
 * a window floating above your application. Specifically, the controls will
 * float above the view specified with setAnchorView(). By default, the window
 * will disappear if left idle for three seconds and reappear when the user
 * touches the anchor view. To customize the MediaController's style, layout and
 * controls you should extend MediaController and override the {#link
 * {@link #makeControllerView()} method.
 * <p/>
 * b) The MediaController is a FrameLayout, you can put it in your layout xml
 * and get it through {@link #findViewById(int)}.
 * <p/>
 * NOTES: In each way, if you want customize the MediaController, the SeekBar's
 * id must be mediacontroller_progress, the Play/Pause's must be
 * mediacontroller_pause, current time's must be mediacontroller_time_current,
 * total time's must be mediacontroller_time_total, file name's must be
 * mediacontroller_file_name. And your resources must have a pause_button
 * drawable and a play_button drawable.
 * <p/>
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout.
 */
public class MediaController extends FrameLayout{
  private  static final int sDefaultTimeout = 5000;
  private static final int FADE_OUT = 1;
  private static final int SHOW_PROGRESS = 2;
  public  MediaPlayerControl mPlayer;
  private Context mContext;
  private PopupWindow mWindow;
  private int mAnimStyle;
  private View mAnchor;
  private View mRoot;
  private SeekBar mProgress;
  public  static ProgressBar mVolumBar;
  private int mCurVolum,mMaxVolum;
  public  LinearLayout mediacontroller_llayout;
  public  TextView mEndTime, mCurrentTime;
  public  TextView mFileName;
  private OutlineTextView mInfoView;
  public  String mTitle;
  private long mDuration;
  private boolean mShowing;
  private boolean mDragging;
  private boolean mInstantSeeking = false;
  private boolean mFromXml = false;
  public  ImageButton mPauseButton;
  public  ImageButton mPreviousButton;
  public  ImageButton mNextButton;
  public  AudioManager mAM;
  private OnShownListener mShownListener;
  private OnHiddenListener mHiddenListener;
  private OnShownSystemTime mShownSystemTime;
  public  OnVideoPlayerControl mVideoPlayercontrol;
  private static MediaController mMC;
  
  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      long pos;
      switch (msg.what) {
        case FADE_OUT:
          hide();
          break;
        case SHOW_PROGRESS:
          pos = setProgress();
          if (!mDragging && mShowing) {
            msg = obtainMessage(SHOW_PROGRESS);
            sendMessageDelayed(msg, 1000 - (pos % 1000));
            updatePausePlay();
          }
          break;
      }
    }
  };
  
  
  /**
   * SeekBar发生变化的触发方法 
   */
  private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
    public void onStartTrackingTouch(SeekBar bar) {
      mDragging = true;
      show(3600000);
      mHandler.removeMessages(SHOW_PROGRESS);
      if (mInstantSeeking)
        mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
      if (mInfoView != null) {
        mInfoView.setText("");
        mInfoView.setVisibility(View.VISIBLE);
      }
    }

    public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
      if (!fromuser)
        return;

      long newposition = (mDuration * progress) / 1000;//mDuration  Video long position
      String time = StringUtils.generateTime(newposition);
      if (mInstantSeeking)
        mPlayer.seekTo(newposition);
      if (mInfoView != null)
        mInfoView.setText(time);
      if (mCurrentTime != null)
        mCurrentTime.setText(time);//拖放在那里当前时间 发生变化
    }

    public void onStopTrackingTouch(SeekBar bar) {
      if (!mInstantSeeking)
    	  mPlayer.seekTo((mDuration * bar.getProgress()) / 1000);
      if (mInfoView != null) {
    	  mInfoView.setText("");
    	  mInfoView.setVisibility(View.GONE);
      }
      show(sDefaultTimeout);//在手动拖动的时候在显示进度条信息 
      mHandler.removeMessages(SHOW_PROGRESS);
      mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
      mDragging = false;
      mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
    }
  };

  
  /**
   * 如果此构造器被调用, 则表示控制器View 是来自xml 自定义视图
   * @param context
   * @param attrs
   */
public MediaController(Context context, AttributeSet attrs) {
    super(context, attrs);
    mRoot = this;
    mFromXml = true;
    initController(context);
  }

  public MediaController(Context context) {
    super(context);
    if (!mFromXml && initController(context)){
         initFloatingWindow();
       }
  }

  private boolean initController(Context context) {
    mContext = context;
    mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    mMaxVolum = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    return true;
  }
  
  /**
   * 媒体播放控制器的单实例
   * @describtion
   * @param context
   * @return  单态对象
   */
  public static MediaController getInstance(Context context){
	    if (null == mMC){
	    	mMC = new MediaController(context);
	    }
	    return mMC;
  }

  /**
   * 返回当前音频设备最大音量
   * @describtion
   * @return
   */
  public int getMaxVolum(){
	  return 0 != mMaxVolum ? mMaxVolum : -1;
  }
  
  /**
   * @describtion
   * @return 返回音频管理设备对象
   */
  public AudioManager getAudioManager(){
	  return null!= mAM ? mAM:null;
  }
  
  /**
   * @describtion
   * @return  返回音频设备当前音量
   */
  public int getCurrentVolum(){
	  return null != mAM ? mAM.getStreamVolume(AudioManager.STREAM_MUSIC) : -1;
  }
  
  
  @Override
  public void onFinishInflate() {
    if (mRoot != null)
      initControllerView(mRoot);
  }

  private void initFloatingWindow() {
    mWindow = new PopupWindow(mContext);
    mWindow.setFocusable(false);//PopupWindow 必须可以可能获得焦点,才可以对控制进度条进行 焦点处理
    mWindow.setBackgroundDrawable(null);
    mWindow.setOutsideTouchable(true);
    mAnimStyle = android.R.style.Animation;
  }
  
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setWindowLayoutType() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			try {
				mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
				Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", new Class[] { int.class });
				setWindowLayoutType.invoke(mWindow, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
			} catch (Exception e) {
				Log.e("setWindowLayoutType", e);
			}
		}
	}

  /**
   * Set the view that acts as the anchor for the control view. This can for
   * example be a VideoView, or your Activity's main view.
   *
   * @param view The view to which to anchor the controller when it is visible.
   */
  public void setAnchorView(View view) {
    mAnchor = view;
    if (!mFromXml) {
      removeAllViews();
      mRoot = makeControllerView();
      mWindow.setContentView(mRoot);
      mWindow.setWidth(LayoutParams.MATCH_PARENT);
      mWindow.setHeight(LayoutParams.WRAP_CONTENT);
    }
    initControllerView(mRoot);
  }

  /**
   * Create the view that holds the widgets that control playback. Derived
   * classes can override this to create their own.
   *
   * @return The controller view.
   */
  protected View makeControllerView() {
    return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("mediacontroller", "layout", mContext.getPackageName()), this);
  }

  /**
   * 在初始化控件时,在Button 按钮上绑定了事件,触事件执行
   * @describtion,要控件产生焦点事件,也需要监听焦点的变化.
   * @param v
   */
  public void initControllerView(View v) {
    mPauseButton = (ImageButton) v.findViewById(getResources().getIdentifier("mediacontroller_play_pause", "id", mContext.getPackageName()));
    
    mProgress = (SeekBar) v.findViewById(getResources().getIdentifier("mediacontroller_seekbar", "id", mContext.getPackageName()));
    if (mProgress != null) {
      if (mProgress instanceof SeekBar) {
        SeekBar seeker = (SeekBar) mProgress;
        seeker.setOnSeekBarChangeListener(mSeekListener);
      }
      mProgress.setMax(1000);
    }

    mEndTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time_total", "id", mContext.getPackageName()));
    mCurrentTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time_current", "id", mContext.getPackageName()));
//  必须要初始化
    mFileName = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_file_name", "id", mContext.getPackageName()));
    if (mFileName != null)
      mFileName.setText(mTitle);
    mVolumBar = (ProgressBar) v.findViewById(getResources().getIdentifier("volumeBar", "id", mContext.getPackageName()));
    mVolumBar.setMax(mMaxVolum); 
  }

  public void setMediaPlayer(MediaPlayerControl player) {
	    mPlayer = player;
	    updatePausePlay();
  }

  
  /**
   * Control the action when the seekbar dragged by user
   *
   * @param seekWhenDragging True the media will seek periodically
   */
  public void setInstantSeeking(boolean seekWhenDragging) {
    mInstantSeeking = seekWhenDragging;
  }

  public void show() {
    show(sDefaultTimeout);
  }

  /**
   * Set the content of the file_name TextView
   * @param name
   */
  public void setFileName(String name) {
    mTitle = name;
    if (mFileName != null)
      mFileName.setText(mTitle);
  }

  /**
   * 传给调用都  正在播放视频文件名
   */
  public String passToVideoName(){
	  return null != mTitle ? mTitle : null;
  }
  
  /**
   * Set the View to hold some information when interact with the
   * MediaController
   *
   * @param v
   */
  public void setInfoView(OutlineTextView v) {
    mInfoView = v;
  }

  /**
   * <p>
   * Change the animation style resource for this controller.
   * </p>
   * <p/>
   * <p>
   * If the controller is showing, calling this method will take effect only the
   * next time the controller is shown.
   * </p>
   *
   * @param animationStyle animation style to use when the controller appears
   *                       and disappears. Set to -1 for the default animation, 0 for no animation, or
   *                       a resource identifier for an explicit animation.
   */
  public void setAnimationStyle(int animationStyle) {
    mAnimStyle = animationStyle;
  }

  /**
   * Show the controller on screen. It will go away automatically after
   * 'timeout' milliseconds of inactivity.
   *
   * @param timeout The timeout in milliseconds. Use 0 to show the controller
   *                until hide() is called.
   */
  public void show(int timeout) {
    if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {

      if (mFromXml) {
        setVisibility(View.VISIBLE);
      } else {
        int[] location = new int[2];

        mAnchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

        mWindow.setAnimationStyle(mAnimStyle);
        setWindowLayoutType();
        mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
      }
      mShowing = true;
    }
//    updatePausePlay();
    mHandler.sendEmptyMessage(SHOW_PROGRESS);
    if (timeout != 0) {
      mHandler.removeMessages(FADE_OUT);
      mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
    }
  }

  public boolean isShowing() {
    return mShowing;
  }

  public void hide() {
    if (mAnchor == null)
      return;

    if (mShowing) {
      try {
        mHandler.removeMessages(SHOW_PROGRESS);
        if (mFromXml)
          setVisibility(View.GONE);
        else
          mWindow.dismiss();
      } catch (IllegalArgumentException ex) {
        Log.d("MediaController already removed");
      }
      mShowing = false;
    }
  }

  public void setOnShownListener(OnShownListener l) {
	  mShownListener = l;
  }
  
  public void setOnHiddenListener(OnHiddenListener l) {
	  mHiddenListener = l;
  }

  public void setOnShownSystemTime(OnShownSystemTime l){
	  mShownSystemTime = l;
  }
  
  public void setOnVideoPlayerControl(OnVideoPlayerControl l){
	  mVideoPlayercontrol = l;
  }
  
  public long setProgress() {
    if (mPlayer == null || mDragging)
      return 0;

    long position = mPlayer.getCurrentPosition();
    long duration = mPlayer.getDuration();
    if (mProgress != null) {
      if (duration > 0) {
        long pos = 1000L * position / duration;
        mProgress.setProgress((int) pos);
      }
      int percent = mPlayer.getBufferPercentage();
      mProgress.setSecondaryProgress(percent * 10);
    }

    mDuration = duration;

    if (mEndTime != null){
      mEndTime.setText(StringUtils.generateTime(mDuration));
//      Log.e("MC","mEndTime = " +StringUtils.generateTime(mDuration));
    }
    if (mCurrentTime != null){
      mCurrentTime.setText(StringUtils.generateTime(position));
//      Log.e("MC","mCurrentTime = "+ StringUtils.generateTime(position));
    }

    return position;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
   	show(sDefaultTimeout);
    return true;
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {
	Log.e("onTouchEvent",ev.getAction());
    show(sDefaultTimeout);
    return false;
  }

  /**
   * 派发按键事件,过滤
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return super.dispatchKeyEvent(event);
  }

  public void updatePausePlay() {
    if (mRoot == null || mPauseButton == null) {
		return;
	}
    if (mPlayer.isPlaying()){
    	mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", mContext.getPackageName()));
    }else{
    	mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_play", "drawable", mContext.getPackageName()));
    }
  }

  public void doPauseResume() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
		} else {
		    mPlayer.start();
		}
		updatePausePlay();
	}

  @Override
  public void setEnabled(boolean enabled) {
    if (mPauseButton != null)
      mPauseButton.setEnabled(enabled);
    if (mProgress != null)
      mProgress.setEnabled(enabled);
    super.setEnabled(enabled);
  }

  public void showImgPause() {
		if (mShownListener != null) {
			 mShownListener.onShown();
		   }else {
	      Log.e("mShownListener==null");
		}
	}
  
  public void hideImgPause() {
	  if (mHiddenListener != null) {
		  mHiddenListener.onHidden();
	  }else {
	  Log.e("mHiddenListener==null");
	  }
  }
  
  public void showSystemTime(){
	  if (mShownSystemTime != null) {
		  mShownSystemTime.onShowSystemTime();
	  }else {
	    Log.e("mShownSystemTime==null");
	  }
  }
  
  public interface OnShownListener {
    public void onShown();
  }

  public interface OnHiddenListener {
    public void onHidden();
  }

  public interface OnShownSystemTime{
	public void onShowSystemTime();
  }
  
  public interface OnVideoPlayerControl{
	  void previous();
	  void next();
  }
  
  public interface MediaPlayerControl {
    void start();

    void pause();
    
    long getDuration();

    long getCurrentPosition();

    void seekTo(long pos);

    boolean isPlaying();

    int getBufferPercentage();
   }
}
