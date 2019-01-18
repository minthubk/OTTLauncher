package com.pisen.ott.launcher.voice;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.pisen.ott.launcher.search.SearchActivity;
import com.pisen.ott.launcher.utils.HttpUtils;
import com.pisen.ott.launcher.voice.IflytekMananger.OnSpeechListener;
import com.qiyi.tv.voice.VoiceClient;
import com.qiyi.tv.voice.VoiceEvent;
import com.qiyi.tv.voice.VoiceEventFactory;

public class VoiceService extends Service implements OnSpeechListener {

	private IflytekMananger iflytekMananger;
	private VioceInputDialog mDialog;
	private boolean testRun = false;
	private Handler handler;
	private VoiceRulesHandler rulesHandler;

	private String pkgName, activityName;
	static final String QIYI_PKGNAME = "com.gitvvideo";
	static final String QIYI_SEARCH_PAGE = "com.qiyi.video.ui.search.QSearchActivity";
	static final String QIYI_PLAYING_PAGE = "com.qiyi.video.ui.newdetail.AlbumDetailActivity";

	private VoiceClient mVoiceClient;
	private String TAG = VoiceService.class.getSimpleName();
	private VoiceBinder myBinder = new VoiceBinder();
	private int action = -1;

	public class VoiceBinder extends Binder {

		public VoiceService getService() {
			return VoiceService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return myBinder;
	}

	private boolean test;
	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
		registerReceiver(voiceReceiver, new IntentFilter("android.intent.action.Pisen.VoiceKeyEvent"));
		rulesHandler = new VoiceRulesHandler(getBaseContext());
		rulesHandler.init();
		iflytekMananger = new IflytekMananger(getBaseContext());
		iflytekMananger.setSpeechListener(this);
		VoiceClient.initialize(getBaseContext());
		mVoiceClient = VoiceClient.instance();
		mVoiceClient.setListener(new VoiceClient.ConnectionListener() {

			@Override
			public void onDisconnected(int arg0) {
				Log.d(TAG, "onDisconnected, code=" + arg0);
			}

			@Override
			public void onConnected() {
				Log.d(TAG, "onConnected");
			}
		});
		mVoiceClient.connect();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(testRun){
					getRunningTaskInfo();
					if(inQiyiPlayingPage()){
						Log.e(TAG, "in playing page....");
						test = !test;
						testPlay(test?"暂停":"播放");
					}
					SystemClock.sleep(10000);
				}
			}
		}).start();
	}

	Runnable startVoice = new Runnable() {
		public void run() {
			if (mDialog == null) {
				mDialog = new VioceInputDialog(getBaseContext());
				mDialog.setOnDismissListener(onDismissListener);
			}
//			iflytekMananger.stopRecord();
			if(iflytekMananger.startRecord()){
				if(!mDialog.isShowing()){
					mDialog.show();
					iflytekMananger.setVoiceViews(mDialog.getEdtResult(), mDialog.getImgListening());
				}
				mDialog.getEdtResult().setHint("正在识别语音");
			}else{
				showToast("启动语音识别失败");
			}
		}
	};
	
	/**
	 * 延迟1秒，避免用户误按
	 */
	private void startVoiceInput() {
		if (!HttpUtils.isNetworkAvailable(getBaseContext())) {
			handler.post(new Runnable() {
				@Override
				public void run() {
						Toast.makeText(getBaseContext(), "网络异常，无法使用语音识别", Toast.LENGTH_LONG).show();
				}
			});
			return;
		}
		getRunningTaskInfo();
		if(inQQVideoPage()){
			return;
		}
		long delay = 1000;
		if (mDialog != null && mDialog.isShowing()) {
			delay = 0;
		}
		handler.postDelayed(startVoice, delay);
	}

	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface arg0) {
			iflytekMananger.stopRecord();
		}
	};

	@Override
	public void onDestroy() {
		iflytekMananger.release();
		testRun = false;
		rulesHandler.release();
		unregisterReceiver(voiceReceiver);
		super.onDestroy();
	}

	@Override
	public void onSpeechFinished() {
		final String text = iflytekMananger.getEdtResult();
		Log.d(TAG, "onSpeechFinished() text = " + text);
		if (text == null || "".equals(text))
			return;
		handler.removeCallbacks(noVoiceTip);
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				processSearch(text);
				
			}
		}, 1000);
	}
	private void showToast(final String msg){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void processSearch(String text){
		boolean movieSearchPage = false;
		Log.d(TAG, "processSearch() text = " + text);
		if (text == null || "".equals(text)){
			mDialog.dismiss();			
			return;
		}
		boolean ret = false;
		// 爱奇艺界面，尝试其内部动作
		if (mVoiceClient.isConnected()) {
			getRunningTaskInfo();
			if (inQiyiSearchPage()) {
				ret = testSearch(text);
				movieSearchPage = true;
			} else if (inQiyiPlayingPage()) {
				ret = testPlay(text);
//				showToast("播放界面处理了语音？："+(ret?"已处理":"未处理"));
				return;//为了不影响播放，不继续处理语音
			} else if (inQiyi()) {
				ret = testKeyEvent(text);
			}
		}
		if (!ret) {
			// 打开应用
			ret = rulesHandler.processApp(text);
		}
		// 都没有处理，测跳转到全文搜索
		if (!ret && !movieSearchPage) {
			startSearchActivity(text);
		}
		mDialog.dismiss();
	}

	private void startSearchActivity(String text) {
		Log.d(TAG, "startSearchActivity() event = ");
		Intent intent = new Intent(getBaseContext(), SearchActivity.class);
		intent.putExtra(SearchActivity.PARAME_KEYWORD, text);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}

	public boolean testSearch(String text) {
		VoiceEvent voiceEvent = VoiceEventFactory.createSearchEvent(text);
		Log.d(TAG, "testSearch() event = " + voiceEvent);
		boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
		Log.d(TAG, "testSearch() result =  " + handled);
		if(!handled){
			showToast("未搜索到影视'"+text+"'");
		}
		return handled;
	}
	
	boolean handled = false;

	/**
	 * 外部调用影视搜索，因爱奇艺搜索页面在一定状态下才可以搜索，故增加循环和timeout处理
	 * @param text
	 * @param timeout
	 */
	public void searchFilmFromOutside(final String text, long timeout) {
		handled = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!handled) {
					VoiceEvent voiceEvent = VoiceEventFactory.createSearchEvent(text);
					Log.d(TAG, "testSearchAtDelay() event = " + voiceEvent);
					handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
					Log.d(TAG, "testSearchAtDelay() result =  " + handled);
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!handled){
					handled = true;
					Toast.makeText(getBaseContext(), "未搜索到'"+text+"'相关影视", Toast.LENGTH_SHORT).show();
				}
			}
		}, timeout);
	}

	/**
	 * 播放暂停
	 * 
	 * @param text
	 * @return
	 */
	private boolean testPlay(String text) {
		boolean handled = false;
		if("快进".equals(text)){
			VoiceEvent voiceEvent = VoiceEventFactory.createSeekOffsetEvent(100000);
	        handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
		}else if("快退".equals(text)){
			VoiceEvent voiceEvent = VoiceEventFactory.createSeekOffsetEvent(100000);
	        handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
		}else{
			VoiceEvent voiceEvent = VoiceEventFactory.createKeywordsEvent(text);
			Log.d(TAG, "testPlay() event = " + voiceEvent);
			handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
			Log.d(TAG, "testPlay() result =  " + handled);
		}
		return handled;
	}

	private boolean testKeyEvent(String text) {
		int keycode = 0;
		if (text.equals("上")) {
			keycode = KeyEvent.KEYCODE_DPAD_UP;
		} else if (text.equals("下")) {
			keycode = KeyEvent.KEYCODE_DPAD_DOWN;
		} else if (text.equals("左")) {
			keycode = KeyEvent.KEYCODE_DPAD_LEFT;
		} else if (text.equals("右")) {
			keycode = KeyEvent.KEYCODE_DPAD_RIGHT;
		} else if (text.equals("上一页")) {
			keycode = KeyEvent.KEYCODE_PAGE_UP;
		} else if (text.equals("下一页")) {
			keycode = KeyEvent.KEYCODE_PAGE_DOWN;
		}
		VoiceEvent voiceEvent = VoiceEventFactory.createKeyEvent(keycode);
		Log.d(TAG, "testSearch() event = " + voiceEvent);
		boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
		Log.d(TAG, "testSearch() result =  " + handled + ", keycode=" + keycode);
		return handled;
	}

	private void getRunningTaskInfo() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		RunningTaskInfo taskInfo = activityManager.getRunningTasks(1).get(0);
		activityName = taskInfo.topActivity.getClassName();
		pkgName = taskInfo.topActivity.getPackageName();
		Log.d(TAG, "getRunningTaskInfo() activityName = " + activityName + "; pkgName = " + pkgName);
	}

	private boolean inQiyiSearchPage() {
		return QIYI_PKGNAME.equals(pkgName) && QIYI_SEARCH_PAGE.equals(activityName);
	}

	private boolean inQiyi() {
		return QIYI_PKGNAME.equals(pkgName);
	}

	private boolean inQiyiPlayingPage() {
		return QIYI_PKGNAME.equals(pkgName) && QIYI_PLAYING_PAGE.equals(activityName);
	}
	
	
	private static final String pkgQQ = "com.tencent.deviceapp";
	private static final String actQQVideoChat = "com.tencent.deviceapp.VideoActivity";
	private static final String actQQVideoMsg = "com.tencent.deviceapp.VideoMessageActivity";
	private boolean inQQVideoPage(){
		return pkgQQ.equals(pkgName)&&(actQQVideoChat.equals(activityName)||actQQVideoMsg.equals(activityName));
	}
	
	private BroadcastReceiver voiceReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			if(b!=null){
				KeyEvent event = b.getParcelable("KeyEvent");
				if(event!=null){
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(action != event.getAction()){
							startVoiceInput();
						}
					}else if(event.getAction() == KeyEvent.ACTION_UP){
						if (mDialog == null || !mDialog.isShowing()) {
							handler.removeCallbacks(startVoice);
						}else{
							iflytekMananger.stopRecord();
							handler.removeCallbacks(noVoiceTip);
							handler.postDelayed(noVoiceTip, 4000);
						}
//						if(mDialog == null || mDialog.getEdtResult() == null)
//							return ; 
//						if(TextUtils.isEmpty(mDialog.getEdtResult().getText())){
//							mDialog.getEdtResult().addTextChangedListener(textWatcher);
//						}else{
//							String text = mDialog.getEdtResult().getText().toString();
//							processSearch(text);
//						}
					}
					action = event.getAction(); 
					
				}
			}
		}
		
	};
	
	Runnable noVoiceTip = new Runnable() {

		@Override
		public void run() {
			if (mDialog != null && mDialog.isShowing()) {
				String text = iflytekMananger.getEdtResult();
				if (text == null || "".equals(text)) {
					mDialog.getEdtResult().setHint("未识别到语音");
				}
			}

		}
	};

}
