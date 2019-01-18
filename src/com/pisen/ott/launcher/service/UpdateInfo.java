package com.pisen.ott.launcher.service;

import java.util.List;

import android.izy.util.LogCat;
import android.izy.util.parse.GsonUtils;

/**
 * 版本升级信息
 * 
 * @author yangyp
 * @version 1.0, 2015年1月14日 下午3:37:17
 */
public class UpdateInfo {

	public System System;
	public List<Content> Content;

	static public class System {
		// apk更新url
		public String Apk;
		// MD5校验码
		public String MD5;
		// 系统版本号（硬件版本号）
		public String SystemVersion;
		// 内部版本号（软件版本号）
		public int InternalVersion;
		// 内容版本号
		public String UiVersion;
		// 版本描述
		public String Description;
		// 发布时间
		public String ReleaseDate;
	}

	static public class Content {
		public String UiVersion; // "1.0.1",
		public String Config; // "http://file.pisen.com.cn/ott/SB0001/Content/1.0.1/content.20150108093624.json",
		public String ConentVersion; // "20150108093624",
		public String MD5; // "621364c73adb4fb4b4d1482d9a86f41c",
		public String Description; // null

		/**
		 * 判断内容是否有更新
		 * 
		 * @param conentVersion
		 * @return
		 */
		public boolean hasContentUpdate(String conentVersion) {
			LogCat.i(("ConentVersion:"+ConentVersion + " conentVersion:"+conentVersion));
			return ConentVersion != null ? !ConentVersion.equals(conentVersion) : false;
		}
	}

	/**
	 * 判断是否有新版本APK
	 * 
	 * @return
	 */
	public boolean checkUpdate(int internalVersion) {
		return System != null ? System.InternalVersion > internalVersion : false;
	}

	/**
	 * 获取新版本内版本号
	 * @return
	 */
	public String getNewInnerVersion(){
		return System.InternalVersion+"";
	}
	
	/**
	 * 根据uiVersion获取内容更新信息
	 * 
	 * @param uiVersion
	 * @return
	 */
	public Content getContent(String uiVersion) {
		if (Content != null) {
			for (Content c : Content) {
				if (uiVersion.equals(c.UiVersion)) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * 获取内容版本号
	 * 
	 * @return
	 */
	public String getConentVersion() {
		return getContent(System.UiVersion).ConentVersion;
	}

	public static UpdateInfo json2bean(String json) {
		return GsonUtils.jsonDeserializer(json, UpdateInfo.class);
	}

}
