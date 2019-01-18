package com.pisen.ott.launcher.movie;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.qiyi.tv.client.ConnectionListener;
import com.qiyi.tv.client.ErrorCode;
import com.qiyi.tv.client.QiyiClient;
import com.qiyi.tv.client.Result;
import com.qiyi.tv.client.data.Channel;

public class QiyiManager {

	private String TAG = QiyiManager.class.getSimpleName();
	private QiyiClient mQiyiClient = null;
	private Context context;
	private List<Channel> channelList;
	private static QiyiManager instance;
	private static final Object mutex = new Object();
	private int errorCode = ErrorCode.SUCCESS;
	private SparseArray<Channel> channels = new SparseArray<Channel>();
	private static boolean loaded = false;
	private List<QiyiLisenter> lisenters = new ArrayList<QiyiLisenter>();
	private static boolean inited = false;

	public int getErrorCode() {
		return errorCode;
	}

	public static QiyiManager getInstance(Application app) {
		synchronized (mutex) {
			if (instance == null) {
				instance = new QiyiManager(app);
			}
			return instance;
		}
	}

	public void addQiyiLisenter(QiyiLisenter qiyiLisenter) {
		synchronized (lisenters) {
			lisenters.add(qiyiLisenter);
		}
	}
	public void removeQiyiLisenter(QiyiLisenter qiyiLisenter) {
		synchronized (lisenters) {
			lisenters.remove(qiyiLisenter);
		}
	}

	private QiyiManager(Context context) {
		this.context = context;
	}

	private ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onAuthSuccess() {
			ensureLoad();
			Log.d(TAG, "onAuthSuccess()");
			synchronized (lisenters) {
				for (QiyiLisenter qiyiLisenter : lisenters) {
					qiyiLisenter.onAuthSuccess();
				}
			}
		}

		@Override
		public void onError(int code) {
			Log.d(TAG, "onError()" + ", code = " + code);
			errorCode = code;
		}

		@Override
		public void onDisconnected() {
			Log.d(TAG, "onDisconnected()");
			synchronized (lisenters) {
				for (QiyiLisenter qiyiLisenter : lisenters) {
					qiyiLisenter.onDisconnected();
				}
			}
		}

		@Override
		public void onConnected() {
			Log.d(TAG, "onConnected()");
			synchronized (lisenters) {
				for (QiyiLisenter qiyiLisenter : lisenters) {
					qiyiLisenter.onConnected();
				}
			}
		}
	};
	

	public void ensureLoad() {
		if (loaded)
			return;
		new Thread(new Runnable() {

			@Override
			public void run() {
				loaded = true;
				Result<List<Channel>> result = mQiyiClient.getChannelList();
				Log.d(TAG, "getChannelList()" + ", code = " + result.code);
				if (result.code == ErrorCode.SUCCESS) {
					channelList = result.data;
					for(Channel ch: channelList){
						Log.d(TAG, "  ch "+ch.getName()+"; id = "+ch.getId());
					}
				}else{
					loaded = false;
				}
			}
		}).start();
	}
	
	public List<Channel> getChannelList() {
		return channelList;
	}
	public void ensureLoad(boolean force){
		loaded = false;
		ensureLoad();
	}

	public QiyiClient getQiyiClient() {
		return mQiyiClient;
	}

	public Channel getChannelById(int id) {
		Channel ch = channels.get(id);
		if (ch != null)
			return ch;
		if (channelList == null) {
			return null;
		}
		for (Channel chl : channelList) {
			if (chl.getId() == id) {
				channels.put(id, chl);
				return chl;
			}
		}
		return null;
	}
	
	public boolean isAvailable() {
		return inited && isConnected() && isAuthSuccess();
	}
	
	public boolean isConnected() {
		return mQiyiClient == null ? false : mQiyiClient.isConnected();
	}
	
	public boolean isAuthSuccess() {
		return mQiyiClient == null ? false : mQiyiClient.isAuthSuccess();
	}

	public void init() {
		if(inited){
			return;
		}
		inited = true;
		mQiyiClient = QiyiClient.instance();
		mQiyiClient.initialize(context, IQiyiConfig.SIGNATURE);
		mQiyiClient.setListener(mConnectionListener);
		mQiyiClient.connect();
	}

	public void release() {
		inited = false;
		loaded = false;
		lisenters.clear();
		if (mQiyiClient != null) {
			if (mQiyiClient.isConnected())
				mQiyiClient.disconnect();
			mQiyiClient.release();
		}
	}

	public interface QiyiLisenter {
		public void onAuthSuccess();

		public void onDisconnected();

		public void onConnected();
	}

	public int openChannel(Channel channel, String title) {
		if(channel == null)
			return -1;
		return mQiyiClient.openChannel(channel, title);

	}
	
	public int openChannel(Channel channel, String classTag, String title) {
		if(channel == null)
			return -1;
		return mQiyiClient.openChannel(channel, classTag, title);

	}
}
