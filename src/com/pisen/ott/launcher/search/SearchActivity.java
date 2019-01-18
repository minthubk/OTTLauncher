package com.pisen.ott.launcher.search;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.izy.content.IntentUtils;
import android.izy.os.AsyncTaskExt;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.movie.QiyiManager;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.utils.AppUtils;
import com.pisen.ott.launcher.utils.HttpUtils;
import com.pisen.ott.launcher.voice.VoiceService;
import com.pisen.ott.launcher.voice.VoiceService.VoiceBinder;
import com.pisen.ott.launcher.widget.GridScaleView;
import com.pisen.ott.launcher.widget.OTTWiatProgress;
import com.pisen.ott.launcher.widget.SearchDownloadItemView;
import com.qiyi.tv.client.feature.common.PageType;

/**
 * 搜索
 * 
 * @author Liuhc
 * @version 1.0 2015年1月26日 下午3:42:56
 */
public class SearchActivity extends DefaultActivity {

	public static final String PREFER_NAME = "com.iflytek.setting";
	public static final String PARAME_KEYWORD = "keywords";

	private EditText etSearch;
	private TextView txtSearchResult;
	private TextView txtVoice;
	private ImageView imgSearchRecord;
	private TextView txtEmpty;

	private LinearLayout voiceSearchLayout;
	private SearchMenuLayout voiceControlLayout;

	private LinearLayout keyboardSearchLayout;
	private SearchMenuLayout keyboardControlLayout;

	private GridScaleView grdvSearch;
	private SearchAdapter searchAdapter;
	private SearchDownloadItemView itemDownload;
	private OTTWiatProgress progressLoading;

	private SpeechRecognizer mIat;
	private SharedPreferences mSharedPreferences;

	PackageManager packageMgr;
	static final String TYPE_INSTALLED = "type_installed";

	private TextView txtFilmResult;

	private VoiceService bindService;
	private boolean connected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_search);
		this.initViews();
//		this.initSpeech();
		packageMgr = SearchActivity.this.getPackageManager();
		bindVocieService();
		processExtras();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		processExtras();
	}

	private void processExtras() {
		Intent intent = getIntent();
		if (intent != null) {
			String keywords = intent.getStringExtra(PARAME_KEYWORD);
			if (keywords != null) {
				etSearch.setText(keywords);
			}
		}
	}

	@Override
	protected void onDestroy() {
		unbindVocieService();
		super.onDestroy();
		if (mIat != null) {
			// 退出时释放连接
			mIat.cancel();
			mIat.destroy();
		}
	}

	@Override
	protected void onResume() {
		if (grdvSearch.isLockItem()) {
			if (itemDownload != null) {
				if (itemDownload.checkInstalled()) {
					grdvSearch.unlockItem();
				}
			}
		}
		super.onResume();
	}

	@Override
	public boolean onBackKeyEvent() {
		grdvSearch.unlockItem();
		if (itemDownload != null && itemDownload.isShowControl()) {
			itemDownload.cancelDownload();
			itemDownload.hideControlLayout();
			itemDownload = null;
			return true;
		}
		return super.onBackKeyEvent();
	}

	private void initViews() {
		txtSearchResult = (TextView) findViewById(R.id.txtSearchResult);
		txtVoice = (TextView) findViewById(R.id.txtVoice);
		txtVoice.setEnabled(false);
		progressLoading = (OTTWiatProgress) findViewById(R.id.progressLoading);
		txtEmpty = (TextView) findViewById(R.id.txtEmpty);
		grdvSearch = (GridScaleView) findViewById(R.id.grdvSearch);
		grdvSearch.setEmptyView(txtEmpty);
		grdvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.e("hegang", "onItemClick ---");
				UiContent item = searchAdapter.getItem(position);
				if (TYPE_INSTALLED.equals(item.Type)) {
					AppUtils.openApk(getBaseContext(), item.ApkFile);
					return;
				}
				if (IntentUtils.isInstalledApk(SearchActivity.this, item.ApkFile)) {
					IntentUtils.startApk(SearchActivity.this, item.ApkFile);
				} else {
					grdvSearch.lockItem();
					itemDownload = (SearchDownloadItemView) view.findViewById(R.id.itemDownload);
					itemDownload.nextClick(item, grdvSearch);
				}
			}
		});

		etSearch = (EditText) findViewById(R.id.etSearch);
		imgSearchRecord = (ImageView) findViewById(R.id.imgSearchRecord);

		voiceSearchLayout = (LinearLayout) findViewById(R.id.voiceSearchLayout);
		voiceControlLayout = (SearchMenuLayout) findViewById(R.id.voiceControlLayout);
		voiceControlLayout.setOnItemClickListener(controlListener);
		keyboardSearchLayout = (LinearLayout) findViewById(R.id.inputSearchLayout);
		keyboardControlLayout = (SearchMenuLayout) findViewById(R.id.keyboardControlLayout);
		keyboardControlLayout.setOnItemClickListener(controlListener);

		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Log.e("hegang", "onTextChanged s= " + s.toString());
				new AsyncSearchTask().execute(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		final GridFocusLayout keyboardLayout = (GridFocusLayout) findViewById(R.id.keyboardLayout);
		keyboardLayout.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(View v) {
				if (v instanceof TextView) {
					String value = ((TextView) v).getText().toString();
					etSearch.append(value);
				}
			}
		});

		keyboardLayout.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if (hasFocus) {
					grdvSearch.setMasterTitle(keyboardLayout);
				}
			}
		});
		voiceControlLayout.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if (hasFocus) {
					grdvSearch.setMasterTitle(voiceControlLayout);
				}
			}
		});
		keyboardControlLayout.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if (hasFocus) {
					grdvSearch.setMasterTitle(keyboardControlLayout);
				}
			}
		});

		txtFilmResult = (TextView) findViewById(R.id.txtFilmResult);
		grdvSearch.setUpFocusView(txtFilmResult);
		txtFilmResult.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keycode, KeyEvent keyevent) {
				if (keyevent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keycode) {
					case KeyEvent.KEYCODE_DPAD_DOWN:
						grdvSearch.requestChildFocus();
						return true;
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						int ret = QiyiManager.getInstance(getApplication()).getQiyiClient().open(PageType.PAGE_SEARCH);
						if (ret == com.qiyi.tv.client.ErrorCode.SUCCESS) {
							Log.e("hegang","bindService = "+bindService);
							if (bindService != null) {
								bindService.searchFilmFromOutside(etSearch.getText().toString(), 3000);
							}
						}
						break;

					default:
						break;
					}
				}
				return false;
			}
		});
	}

	/**
	 * 初始化语音
	 */
	private void initSpeech() {
		SpeechUtility.createUtility(SearchActivity.this, SpeechConstant.APPID + "=54754404");
		mSharedPreferences = getSharedPreferences(PREFER_NAME, Activity.MODE_PRIVATE);
		setSearchType(getIntent().getExtras() != null);
	}

	private OnItemClickListener controlListener = new OnItemClickListener() {
		@Override
		public void onItemClick(View v) {
			switch (v.getId()) {
			case R.id.txtClear:
				etSearch.setText("");
				break;
			case R.id.txtSpace:
				etSearch.append(" ");
				break;
			case R.id.txtBack:
				String txt = etSearch.getText().toString();
				if (!StringUtils.isEmpty(txt)) {
					etSearch.setText(txt.substring(0, txt.length() - 1));
				}
				// setSearchType(true);
				break;
			case R.id.txtVoice:
				String name = txtVoice.getText().toString();
				if (name.equals(getString(R.string.search_speaking))) {
					startRecord();
				} else {
					stopRecord();
				}
				break;
			case R.id.txtKeyboard:
				setSearchType(false);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 设置搜索类型
	 * 
	 * @param isVoiceSearch
	 *            true:语音搜索 false:键盘搜索
	 */
	private void setSearchType(boolean isVoiceSearch) {
		if (isVoiceSearch) {
			voiceSearchLayout.setVisibility(View.VISIBLE);
			keyboardSearchLayout.setVisibility(View.GONE);
			startRecord();
		} else {
			voiceSearchLayout.setVisibility(View.GONE);
			keyboardSearchLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 开始说话（录音）
	 */
	private void startRecord() {
		if (!HttpUtils.isNetworkAvailable(SearchActivity.this)) {
			Toast.makeText(SearchActivity.this, "网络异常,请检查后再试...", 1).show();
			return;
		}

		if (mIat == null) {
			mIat = SpeechRecognizer.createRecognizer(SearchActivity.this, mInitListener);
		} else {
			setParam();
			int code = mIat.startListening(mRecognizerListener);
			if (code == ErrorCode.SUCCESS) {
				txtVoice.setEnabled(true);
				etSearch.setText("");
				txtVoice.setText(getString(R.string.search_speakend));
			} else {
				LogCat.i("听写失败,错误码：" + code);
				txtVoice.setEnabled(false);
			}
		}
	}

	/**
	 * 说完了（结束录音）
	 */
	private void stopRecord() {
		if (mIat != null) {
			mIat.stopListening();
			txtVoice.setText(getString(R.string.search_speaking));
			imgSearchRecord.setImageResource(R.drawable.search_record_0);
			// pgbSearch.setVisibility(View.GONE);
			// grdvSearch.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	public void setParam() {
		mIat.setParameter(SpeechConstant.PARAMS, null);
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/iflytek/wavaudio.pcm");
	}

	/**
	 * 识别回调。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onVolumeChanged(int volume) {
			switch (volume / 4) {
			case 0:
				imgSearchRecord.setImageResource(R.drawable.search_record_0);
				break;
			case 1:
				imgSearchRecord.setImageResource(R.drawable.search_record_1);
				break;
			case 2:
				imgSearchRecord.setImageResource(R.drawable.search_record_2);
				break;
			case 3:
				imgSearchRecord.setImageResource(R.drawable.search_record_3);
				break;
			case 4:
				imgSearchRecord.setImageResource(R.drawable.search_record_4);
				break;
			case 5:
				imgSearchRecord.setImageResource(R.drawable.search_record_5);
				break;
			case 6:
				imgSearchRecord.setImageResource(R.drawable.search_record_6);
				break;
			case 7:
				imgSearchRecord.setImageResource(R.drawable.search_record_7);
				break;
			default:
				imgSearchRecord.setImageResource(R.drawable.search_record_0);
				break;
			}
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = SearchUtils.parseIatResult(results.getResultString());
			LogCat.i("results：" + text);
			etSearch.append(text);
			if (isLast) {
				stopRecord();
			}
		}

		@Override
		public void onError(SpeechError error) {
			LogCat.e(error.getPlainDescription(true));
		}

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onEndOfSpeech() {
			// pgbSearch.setVisibility(View.VISIBLE);
			// grdvSearch.setVisibility(View.GONE);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};

	/**
	 * 初期化监听器。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code == ErrorCode.SUCCESS) {
				LogCat.i("SpeechRecognizer SUCCESS code");
				txtVoice.setEnabled(true);
				startRecord();
			} else {
				LogCat.i("SpeechRecognizer init() code = " + code);
				txtVoice.setEnabled(false);
			}
		}
	};

	/**
	 * 异步查询任务,刷新
	 * 
	 * @author Liuhc
	 * @version 1.0 2015年1月27日 下午2:57:13
	 */
	private class AsyncSearchTask extends AsyncTaskExt<String, String, List<UiContent>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txtEmpty.setVisibility(View.GONE);
			grdvSearch.setVisibility(View.GONE);
			txtFilmResult.setVisibility(View.GONE);
			if (!progressLoading.isShown())
				progressLoading.show();
		}

		@Override
		protected List<UiContent> doInBackground(String... params) {
			String search = params[0];
			if (SearchUtils.isEmpty(search)) {
				return null;
			}
			search = SearchUtils.getPingYinShort(search);
			List<UiContent> list = new ArrayList<UiContent>();
			// 搜索本地已安装app
			List<ApplicationInfo> listAppcations = packageMgr.getInstalledApplications(PackageManager.GET_ACTIVITIES);
			for (ApplicationInfo app : listAppcations) {
				Intent i = packageMgr.getLaunchIntentForPackage(app.packageName);
				if (i != null) {
					if (SearchActivity.this.getPackageName().equals(app.packageName)) {
						continue;
					}
					UiContent content = createUiContent(app);
					String appName = SearchUtils.getPingYinShort(content.Name);
					if (appName.contains(search)) {
						list.add(content);
					}
				}
			}

			// 搜索服务器推荐app
			list.addAll(LauncherApplication.getConfig().searchUninstalledContentList(search));
			// if (SearchUtils.isChinese(search)) {
			// }
			return list;
		}

		@Override
		protected void onPostExecute(List<UiContent> result) {
			super.onPostExecute(result);
			progressLoading.cancel();
			txtEmpty.setVisibility(View.GONE);
			grdvSearch.setVisibility(View.VISIBLE);
			txtFilmResult.setVisibility(View.VISIBLE);

			if (searchAdapter == null) {
				searchAdapter = new SearchAdapter(SearchActivity.this);
				grdvSearch.setAdapter(searchAdapter);
			}
			if (result == null) {
				searchAdapter.clear();
			} else {
				searchAdapter.setData(result);
			}
			txtSearchResult.setText(searchAdapter.getCount() + "");
		}

	}

	private UiContent createUiContent(ApplicationInfo app) {
		UiContent content = new UiContent();
		content.Name = app.loadLabel(packageMgr).toString();
		content.ApkFile = app.packageName;
		content.Type = TYPE_INSTALLED;
		return content;
	}

	private void bindVocieService() {
		Intent intent = new Intent(this, VoiceService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	private void unbindVocieService() {
		if (connected == true) {
			unbindService(conn);
			connected = false;
		}
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bindService = null;
			connected = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			VoiceBinder binder = (VoiceBinder) service;
			bindService = binder.getService();
			connected = true;
		}
	};

}
