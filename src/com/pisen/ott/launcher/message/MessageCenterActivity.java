package com.pisen.ott.launcher.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.izy.widget.BaseListAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.utils.DateUtils;
import com.pisen.ott.launcher.widget.FocusListView;

/**
 * 注册静态广播接收 {@link JPushInterface}
 * 
 * @author mahuan
 * @version 1.0 2015年2月12日 下午5:30:16
 * @updated [2015 下午5:30:16]:
 */
public class MessageCenterActivity extends DefaultActivity implements OnItemClickListener {
	public final static String MESSAGE_RECEIVED_ACTION = "com.pisen.ott.launcher.message.MESSAGE_RECEIVED_ACTION";
	public static final String KEY = "MSG";
	private FocusListView lstMsgCenter;
	private BaseListAdapter<MessageInfo> msgAdapter;
	private List<MessageInfo> mListInfos = new ArrayList<MessageInfo>();
	private TextView txtMsgTip;
	public static Boolean isForeground = false;
	private UpdateMsgReceiver mUpdateMsgReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_center);
		// 初始化View
		initView();
		// 异步加载数据
		new QueryMessageTask().execute();
		// 注册消息通知
		registerMessageReceiver();
	}

	
	private void initView() {
		lstMsgCenter = (FocusListView) findViewById(R.id.lvMsgCenter);
		txtMsgTip = (TextView) findViewById(R.id.txt_msg_tip);
		msgAdapter = new MsgAdapter(this);
		lstMsgCenter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lstMsgCenter.setAdapter(msgAdapter);
		lstMsgCenter.setOnItemClickListener(this);
	}

	
	public void registerMessageReceiver() {
		mUpdateMsgReceiver = new UpdateMsgReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		registerReceiver(mUpdateMsgReceiver, filter);
	}

	
	@Override
	protected void onResume() {
		isForeground = true;
		super.onResume();
	}

	
	@Override
	protected void onDestroy() {
		isForeground = false;
		unregisterReceiver(mUpdateMsgReceiver);
		super.onDestroy();
	}

	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MessageInfo info = (MessageInfo) parent.getItemAtPosition(position);
		MessageManager msgMgr = MessageManager.getInstance(MessageCenterActivity.this);
		msgMgr.updateMessage(MessageInfo.Table.MSG_READ_FLAG, MessageInfo.MESSAGE_READ, info.id);
		msgMgr.updateMessage(MessageInfo.Table.MSG_READ_TIME, DateUtils.getCurrentTimeMillis(), info.id);
		info.read_flag = MessageInfo.MESSAGE_READ;
		info.read_time = String.valueOf(DateUtils.getCurrentTimeMillis());
		msgAdapter.notifyDataSetChanged();
		startActivity(info);
	}
	

	/**
	 * @desc 消息中心在前端,接收新消息广播
	 */
	class UpdateMsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
				MessageInfo info = (MessageInfo) intent.getSerializableExtra(MessageInfo.MESSAGE_NEW);
				if (null != info && msgAdapter != null) {
					msgAdapter.getData().add(0, info);
				}
				    msgAdapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * @author mahuan
	 * @version 1.0 2015年2月12日 下午5:48:27
	 * @updated 适配器类
	 */
	class MsgAdapter extends BaseListAdapter<MessageInfo> {
		public MsgAdapter(Context context) {
			super();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder viewHolder;
			if (view == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messsage_center_item_file, null);
				view.setTag(viewHolder = new ViewHolder());
				viewHolder.imgMsgIcon = (ImageView) view.findViewById(R.id.imgMsgIcon);
				viewHolder.txtNewsTitle = (TextView) view.findViewById(R.id.txtNewsTitle);
				viewHolder.imgArrows = (ImageView) view.findViewById(R.id.imgArrows);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
			// 可以通过消息类型进一步判断
			viewHolder.txtNewsTitle.setText(getItem(position).title);
			MessageInfo info = getItem(position);
			if (info.read_flag == 1) {
				viewHolder.imgMsgIcon.setImageResource(R.drawable.msg_ic_gray);
				viewHolder.txtNewsTitle.setTextColor(Color.GRAY);
			} else {
				viewHolder.imgMsgIcon.setImageResource(R.drawable.msg_ic_light);
				viewHolder.txtNewsTitle.setTextColor(Color.WHITE);
			}
			return view;
		}

		class ViewHolder {
			ImageView imgArrows, imgMsgIcon;
			TextView txtNewsTitle;
		}
	}

	class QueryMessageTask extends AsyncTask<Void, Void, List<MessageInfo>> {
		@Override
		protected List<MessageInfo> doInBackground(Void... params) {
			return MessageManager.getInstance(MessageCenterActivity.this).getNewSortMessage();
		}

		@Override
		protected void onPostExecute(List<MessageInfo> result) {
			super.onPostExecute(result);
			mListInfos.clear();
			mListInfos.addAll(result);
			msgAdapter.setData(mListInfos);
		    lstMsgCenter.setEmptyView(txtMsgTip);
		}
	}

	/**
	 * @describtion 跳转详细消息
	 * @param obj
	 */
	public void startActivity(Serializable obj) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY, obj);
		intent.putExtras(bundle);
		intent.setClass(this, MessageDetailActivity.class);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

}