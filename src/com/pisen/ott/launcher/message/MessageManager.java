package com.pisen.ott.launcher.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.izy.database.ICursor;
import android.izy.database.sqlite.ISQLiteWapper.RowQuery;
import android.os.Bundle;
import android.text.TextUtils;
import cn.jpush.android.api.JPushInterface;

import com.pisen.ott.launcher.base.NavigationActivity;
import com.pisen.ott.launcher.base.OttBaseActivity;
import com.pisen.ott.launcher.utils.DateUtils;

/**
 * @author  mahuan
 * @version 1.0 2015年2月12日 下午1:43:42
 */
public class MessageManager {
	private Context mContext;
	private MessageDbHelper dbHelper;
	private static MessageManager mInstance = null;
	
	/**
	 * @describtion 单例实例
	 * @param  context
	 * @return 消息管理对象
	 */
	public static MessageManager getInstance(Context context){
		if (null == mInstance){
			mInstance = new MessageManager(context);
		}
		return mInstance;
	}
	
	/**
	 * 构造器,初始化构建数据库
	 * @param context
	 */
	public MessageManager(Context context){
		this.mContext = context;
		dbHelper = new MessageDbHelper(context);
	}
	
	/**
	 * @describtion  插入消息
	 * @param info
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertMessage(MessageInfo info){
		SQLiteDatabase  db = dbHelper.getWritableDatabase();
		ContentValues   cv = new ContentValues();
		cv.put(MessageInfo.Table.MSG_TITLE, info.title);
		cv.put(MessageInfo.Table.MSG_CONTENT, info.content);
		cv.put(MessageInfo.Table.MSG_TYPE, info.type);
		cv.put(MessageInfo.Table.MSG_RECV_TIME, info.recv_time);
		cv.put(MessageInfo.Table.MSG_READ_FLAG, info.read_flag);
		cv.put(MessageInfo.Table.MSG_READ_TIME, info.read_time);
		return db.insert(MessageInfo.Table.TABLE_NAME, null, cv);
	}
	
	/**
	 * @describtion 删除信息 
	 * @param ids
	 * @return 受影响的条目数
	 */
	public int deleteMessage(long... ids){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(MessageInfo.Table.TABLE_NAME, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
	}
	
	/**
	 * @describtion
	 * @param ids
	 * @return 受影响条目数
	 */
	public int updateMessage(String updateColumn,long updateValue,long... ids){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues  cv = new ContentValues();
		cv.put(updateColumn, updateValue);
		return db.update(MessageInfo.Table.TABLE_NAME, cv, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
	}
	
	/**
	 * @describtion
	 * @return   限制在2000条数据信息,显示在ListView面板上
	 * @asc 升序       desc 降序,装载所有消息时 要求按组（ 已读,未读）,打开的时间序列 排序
	 */
	public List<MessageInfo> getAllMessage(){
		return dbHelper.query(MessageInfo.Table.TABLE_NAME, null, null,
				MessageInfo.Table.MSG_RECV_TIME + " desc", "2000", new RowQuery<MessageInfo>(){
					@Override
					public MessageInfo rowQuery(ICursor cursor, int arg1) {
						return MessageInfo.cursor2bean(cursor);
					}
		});
	}
	
	/**
	 * @describtion 获取分类消息,要求按组（未读）,打开的时间序列 排序
	 * @return 所有消息数据
	 */
	public List<MessageInfo> getUnReadMessage(){
		String 	selection  = MessageInfo.Table.MSG_READ_FLAG + " = '"+MessageInfo.MESSGAE_UNREAD+"' ";
		String 	orderBy    = MessageInfo.Table._ID + " desc";
		return dbHelper.query(MessageInfo.Table.TABLE_NAME, selection, null, orderBy, "2000", new RowQuery<MessageInfo>() {
			@Override
			public MessageInfo rowQuery(ICursor cursor, int arg1) {
				return MessageInfo.cursor2bean(cursor);
			}
		});
	}
	
	/**
	 * @describtion 获取分类消息,要求按组（已读）,打开的时间序列 排序
	 * @return 所有消息数据
	 */
	public List<MessageInfo> getReadMessage(){
		String 	selection  = MessageInfo.Table.MSG_READ_FLAG + " = '"+MessageInfo.MESSAGE_READ+"' ";
		String 	orderBy    = MessageInfo.Table._ID + " desc";
		return dbHelper.query(MessageInfo.Table.TABLE_NAME, selection, null, orderBy, "2000", new RowQuery<MessageInfo>() {
			@Override
			public MessageInfo rowQuery(ICursor cursor, int arg1) {
				return MessageInfo.cursor2bean(cursor);
			}
		});
	}
	
	/**
	 * @describtion 获取分类消息,要求按组（ 已读,未读）,打开的时间序列 排序
	 * @return 所有消息分组数据
	 */
	public List<MessageInfo> getNewSortMessage(){
		List<MessageInfo> hs = new ArrayList<MessageInfo>(getUnReadMessage());
		hs.addAll(getReadMessage());
		return hs;
	}
	
	/**
	 * @describtion 查询条件
	 * @param ids
	 * @return
	 */
	protected  String  getWhereClauseForIds(long... ids) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < ids.length; i++){
			if (i > 0){
				sb.append(" OR ");
			}
			sb.append(MessageInfo.Table._ID);
			sb.append(" = ? ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * @describtion 查询参数 
	 * @param ids
	 * @return
	 */
	protected  String [] getWhereArgsForIds(long... ids) {
		String [] sArgs = new String[ids.length];
		for (int i = 0; i < ids.length ; i++){
			sArgs[i] = Long.toString(ids[i]);
		}
		return sArgs;
	}
	
	/**
	 * @describtion  将推送信息加入数据库
	 * @param intent
	 */
	public void addMessage(Intent intent){
		MessageInfo info = new MessageInfo();
		Bundle bundle  = intent.getExtras();
		info.read_flag = MessageInfo.MESSGAE_UNREAD;
		info.title     = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
		info.content   = bundle.getString(JPushInterface.EXTRA_ALERT);
		info.recv_time = DateUtils.getCurrentTimeMillis();
		String extra   = bundle.getString(JPushInterface.EXTRA_EXTRA);
		if (!TextUtils.isEmpty(extra)){
			JSONObject obj = null;//JSONObject.parseObject(extra);
			//info.type = obj.getInt("type");
			//info.parameter = obj.getString("para");
//			info.recv_time = obj.getString("time");
		}
		info.id = insertMessage(info);
//		具体操作有其它判断
		if (MessageCenterActivity.isForeground) {//消息中心 ,消息在前端
			Intent msgIntent = new Intent(MessageCenterActivity.MESSAGE_RECEIVED_ACTION);
			msgIntent.putExtra(MessageInfo.MESSAGE_NEW, info);
			mContext.sendBroadcast(msgIntent);
		}else{//消息不在前端,Intent是发往哪里的 OttBaseActivity 
			Intent msgIntent = new Intent(OttBaseActivity.MESSAGE_RECEIVED_P);
			msgIntent.putExtra(MessageInfo.MESSAGE_NEW, info);
			mContext.sendBroadcast(msgIntent);
		}
	}
	
	/**
	 * @describtion  将推送信息加入数据库
	 * @param intent
	 */
	public void addWeatherMessage(Intent intent){
		MessageInfo info = new MessageInfo();
		Bundle bundle  = intent.getExtras();
		info.read_flag = MessageInfo.MESSGAE_UNREAD;
		info.title     = bundle.getString(NavigationActivity.KEY_WEATHER_BROADCAST_TITLE);
		info.content   = bundle.getString(NavigationActivity.KEY_WEATHER_BROADCAST_CONTENT);
		info.recv_time = DateUtils.getCurrentTimeMillis();
		info.id = insertMessage(info);
//		具体操作有其它判断
		if (MessageCenterActivity.isForeground) {//消息中心 ,消息在前端
			Intent msgIntent = new Intent(MessageCenterActivity.MESSAGE_RECEIVED_ACTION);
			msgIntent.putExtra(MessageInfo.MESSAGE_NEW, info);
			mContext.sendBroadcast(msgIntent);
		}else{//消息不在前端,Intent是发往哪里的 OttBaseActivity 
			Intent msgIntent = new Intent(OttBaseActivity.MESSAGE_RECEIVED_P);
			msgIntent.putExtra(MessageInfo.MESSAGE_NEW, info);
			mContext.sendBroadcast(msgIntent);
		}
	}
	
	/**
	 * @describtion
	 * @return   数据库存在没有 阅读的消息 
	 */
	public boolean haveNewMessage(){
		String  selection  = MessageInfo.Table.MSG_READ_FLAG + " = '"+MessageInfo.MESSGAE_UNREAD+"' ";
		String  orderBy    = MessageInfo.Table.MSG_RECV_TIME + " desc";
		return dbHelper.query(MessageInfo.Table.TABLE_NAME, selection, null, orderBy, "2000", new RowQuery<Boolean>() {
			@Override
			public Boolean rowQuery(ICursor arg0, int arg1) {
				return true;
			}
		}).size()>0;
	}
	
	/**
	 * @describtion
	 * @return   数据库存在没有  阅读的消息数量
	 */
	public int haveNewMessageCount(){
		String  selection  = MessageInfo.Table.MSG_READ_FLAG + " = '"+MessageInfo.MESSGAE_UNREAD+"' ";
		String  orderBy    = MessageInfo.Table.MSG_RECV_TIME + " desc";
		return dbHelper.query(MessageInfo.Table.TABLE_NAME, selection, null, orderBy, "2000", new RowQuery<Integer>() {
			@Override
			public Integer rowQuery(ICursor arg0, int arg1) {
				return 0;
			}
		}).size();
	}
}
