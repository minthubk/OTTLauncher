package com.pisen.ott.launcher.message;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.izy.database.sqlite.SQLiteOpenHelper;

/**
 * @author  mahuan
 * 
 * 数据库设计
 */
public class MessageDbHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME 	= "ott_message.db";
	private static final int    VERSION 		=  20150212;

	/**
	 * @param context
	 * @param dbName
	 * @param version
	 */
	public MessageDbHelper(Context context) {
		super(context, DATABASE_NAME, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTableInfo(db);
	}
	
	public void createTableInfo(SQLiteDatabase db){
		db.execSQL("create table " + MessageInfo.Table.TABLE_NAME + "("
				+ MessageInfo.Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MessageInfo.Table.MSG_TITLE + " TEXT,"
				+ MessageInfo.Table.MSG_CONTENT + " TEXT,"
				+ MessageInfo.Table.MSG_TYPE + " INTEGER,"
				+ MessageInfo.Table.MSG_RECV_TIME + " TEXT,"
				+ MessageInfo.Table.MSG_READ_FLAG + " INTEGER,"
				+ MessageInfo.Table.MSG_READ_TIME + " TEXT,"
				+ MessageInfo.Table.EXPAND_PARAMETER + " TEXT"
				+ ")"
				);
		}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
