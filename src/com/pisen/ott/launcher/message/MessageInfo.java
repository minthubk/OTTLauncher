package com.pisen.ott.launcher.message;

import java.io.Serializable;

import com.pisen.ott.launcher.utils.DateUtils;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * @author  mahuan
 * 消息实体构造,数据字段,实体对象传输 
 */
public class MessageInfo implements Serializable{
	public static final long serialVersionUID = 1L;
//	新消息
	public  static final String MESSAGE_NEW		= "message_new";
//	消息已读
	public  static final int 	MESSAGE_READ	= 1;
//	消息未读
	public  static final int 	MESSGAE_UNREAD	= 0;
	
//	消息系统当前时间
	public  static final long   MESSAGE_TIME = DateUtils.getCurrentTimeMillis();
	
//	消息id (数据库ID)
	public long 				id;
//	消息标题
	public String				title;
//	消息内容
	public String				content;
//	消息类型  :10 普通消息  20 超文本消息
	public int     				type;
//	接收消息时间
	public long 				recv_time;
//	消息状态:0  未读       1 已读
	public int 					read_flag;
//	阅读消息时间
	public String  				read_time;
//	扩展参数
	public String 				parameter; 					
	
	
	/**
	 * @author  mahuan
	 * 消息表设计
	 */
	public static final class Table implements BaseColumns{
//		表名
		public static final String TABLE_NAME 		= "sys_message";
//		消息标题
		public static final String MSG_TITLE  		= "msg_title";
//		消息内容
		public static final String MSG_CONTENT 		= "msg_content";
//		消息类型 
		public static final String MSG_TYPE    		= "msg_type";
//		消息接收时间
		public static final String MSG_RECV_TIME 	="msg_recv_time";
//		消息阅读状态
		public static final String MSG_READ_FLAG 	= "msg_read_flag";
//		消息阅读时间
		public static final String MSG_READ_TIME 	= "msg_read_time";
//		拓展参数 
		public static final String EXPAND_PARAMETER = "expand_parameter";
	}
	

	/**
	 * @describtion 生成传输对象
	 * @param cursor
	 * @return 消息实体
	 */
	public static MessageInfo cursor2bean(Cursor cursor){
		MessageInfo info = new MessageInfo();
		info.id = cursor.getLong(cursor.getColumnIndexOrThrow(MessageInfo.Table._ID));
		info.title = cursor.getString(cursor.getColumnIndexOrThrow(MessageInfo.Table.MSG_TITLE));
		info.content = cursor.getString(cursor.getColumnIndexOrThrow(MessageInfo.Table.MSG_CONTENT));
		info.type = cursor.getInt(cursor.getColumnIndexOrThrow(MessageInfo.Table.MSG_TYPE));
		info.recv_time = cursor.getLong(cursor.getColumnIndexOrThrow(MessageInfo.Table.MSG_RECV_TIME));
		info.read_flag = cursor.getInt(cursor.getColumnIndexOrThrow(MessageInfo.Table.MSG_READ_FLAG));
		info.read_time = cursor.getString(cursor.getColumnIndexOrThrow(MessageInfo.Table.MSG_READ_TIME));
		info.parameter = cursor.getString(cursor.getColumnIndexOrThrow(MessageInfo.Table.EXPAND_PARAMETER));
		return info;
	}
}
