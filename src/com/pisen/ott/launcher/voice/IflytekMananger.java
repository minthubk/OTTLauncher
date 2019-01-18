package com.pisen.ott.launcher.voice;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.search.SearchUtils;
import com.pisen.ott.launcher.utils.HttpUtils;

public class IflytekMananger {

	private static final String TAG = IflytekMananger.class.getSimpleName();
	public static final String PREFER_NAME = "com.iflytek.setting";
	private Context mContext;
	private SpeechRecognizer mIat;
	private SharedPreferences mSharedPreferences;
	private EditText edtResult;
	private ImageView imgListening;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	private OnSpeechListener speechListener;

	public IflytekMananger(Context mContext) {
		this.mContext = mContext;
		initSpeech();
	}

	public void setVoiceViews(EditText txtResult, ImageView imgListening) {
		this.edtResult = txtResult;
		this.imgListening = imgListening;
	}

	public void setSpeechListener(OnSpeechListener speechListener) {
		this.speechListener = speechListener;
	}
	/**
	 * 初始化语音
	 */
	private void initSpeech() {
		SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=557e7932");
		mSharedPreferences = mContext.getSharedPreferences(PREFER_NAME, Activity.MODE_PRIVATE);
		mIat = SpeechRecognizer.createRecognizer(mContext, mInitListener);
	}

	/**
	 * 开始说话（录音）
	 */
	public boolean startRecord() {
		setParam();
		if(mIat.isListening())
			return true;
		int code = mIat.startListening(mRecognizerListener);
		return code == ErrorCode.SUCCESS;
	}

	/**
	 * 说完了（结束录音）
	 */
	public void stopRecord() {
		if (mIat != null) {
			if(mIat.isListening()){
				mIat.stopListening();
			}
//			if (edtResult != null) {
//				edtResult.setHint(mContext.getString(R.string.search_speaking));
//			}
//			if (imgListening != null) {
				imgListening.setImageResource(R.drawable.search_record_0);
//			}
		}
	}
	
	public String getEdtResult() {
		if (edtResult != null && edtResult.getText() != null)
			return edtResult.getText().toString();
		return null;
	}

	/**
	 * 初期化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showToastLong("网络异常,请检查后再试...");
			}
		}
	};

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
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
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));
		// 设置音频保存路径，保存音频格式仅为pcm，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/iflytek/wavaudio.pcm");
		// 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
		// 注：该参数暂时只对在线听写有效
		mIat.setParameter(SpeechConstant.ASR_DWA, mSharedPreferences.getString("iat_dwa_preference", "1"));
	}

	/**
	 * 识别回调。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onVolumeChanged(int volume) {
			Log.i(TAG, "onVolumeChanged volume ：" + volume);
			if (imgListening == null)
				return;
			switch (volume / 4) {
			case 0:
				imgListening.setImageResource(R.drawable.search_record_0);
				break;
			case 1:
				imgListening.setImageResource(R.drawable.search_record_1);
				break;
			case 2:
				imgListening.setImageResource(R.drawable.search_record_2);
				break;
			case 3:
				imgListening.setImageResource(R.drawable.search_record_3);
				break;
			case 4:
				imgListening.setImageResource(R.drawable.search_record_4);
				break;
			case 5:
				imgListening.setImageResource(R.drawable.search_record_5);
				break;
			case 6:
				imgListening.setImageResource(R.drawable.search_record_6);
				break;
			case 7:
				imgListening.setImageResource(R.drawable.search_record_7);
				break;
			default:
				imgListening.setImageResource(R.drawable.search_record_0);
				break;
			}
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = SearchUtils.parseIatResult(results.getResultString());
			
//			if (edtResult != null) {
//				edtResult.setText(text);
//				edtResult.setSelection(edtResult.length());
//			}
			Log.i(TAG, "results：" + text+"; isLast = "+isLast);
			printResult(results);
			if (isLast) {
//				stopRecord();
				if (speechListener != null) {
					speechListener.onSpeechFinished();
				}
			}
		}

		@Override
		public void onError(SpeechError error) {
			Log.e(TAG, error.getPlainDescription(true));
		}

		@Override
		public void onBeginOfSpeech() {
			Log.i(TAG, "onBeginOfSpeech");
		}

		@Override
		public void onEndOfSpeech() {
			Log.i(TAG, "onEndOfSpeech");
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());
		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mIatResults.put(sn, text);
		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}
		Log.i(TAG, "printResult resultBuffer ：" + resultBuffer.toString());
		if (edtResult != null) {
			edtResult.setText(resultBuffer.toString());
			edtResult.setSelection(edtResult.length());
		}
	}
	
	private Handler handler = new Handler();
	private void showToastLong(final String msg){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	private void showToastShort(final String msg){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void release() {
		mIat.cancel();
		mIat.destroy();
	}
	
	public interface OnSpeechListener{
		public void onSpeechFinished();
	}
}
