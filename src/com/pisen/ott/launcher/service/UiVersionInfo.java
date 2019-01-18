package com.pisen.ott.launcher.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.izy.util.StringUtils;
import android.izy.util.parse.GsonUtils;

public class UiVersionInfo {

	public String UiVersion; // "1.0.1",
	public String ContentVersion; // "20150108093624",
	public UiContent Content; // 主界面

	@SuppressWarnings("serial")
	static public class UiContent implements Serializable {
		public int Id; // 44,
		public String Name; // "瑙嗗惂鐩掑瓙锛堥珮閰嶇増锛�",
		public String Image; // "http://file.pisen.com.cn/ott/img/20150106/153245_1626.png",
		public String Type; // "ContentView",
		public String ApkFile;
		public String SelectedImage;//选中图片
		public String StartParameters; // null,
		public String DisplayCode; // "0",
		public String ContentViewCode; // "YS_GP",
		public String ParentViewCode; // "YS_GP",
		public int ParentID; // 0,
		public int LayerLevel; // 等级分类 （1：首页; 2:二级页面; 3:详细(列表)页面）
		public String Description; // null,
		public List<UiContent> ChildContent; // null

		public StartType getStartType() {
			try {
				return (StartType) Enum.valueOf(StartType.class, Type);
			} catch (IllegalArgumentException e) {
				return StartType.Unknown;
			}
		}

		static public enum StartType {

			/** 内容 */
			ContentView,

			/** 浏览器 */
			Bowser,

			/** App */
			App, Unknown;
		}
	}

	/**
	 * 更新UiContent
	 * 
	 * @param newContent
	 */
	public void updateUiContent(UiContent newContent) {
		UiContent content = getContent(newContent.DisplayCode);
		if (content != null) {
			content.Id = newContent.Id;
			content.Name = newContent.Name;
			content.Image = newContent.Image;
			content.SelectedImage = newContent.SelectedImage;
			content.Type = newContent.Type;
			content.StartParameters = newContent.StartParameters;
			content.DisplayCode = newContent.DisplayCode;
			content.ContentViewCode = newContent.ContentViewCode;
			content.ParentID = newContent.ParentID;
			content.LayerLevel = newContent.LayerLevel;
			content.Description = newContent.Description;
		}
	}

	/**
	 * 获取Home内容
	 * 
	 * @return
	 */
	public List<UiContent> getHome() {
		return Content != null ? Content.ChildContent : null;
	}

	public UiContent getHome(String displayCode) {
		return findUiContent(getHome(), displayCode);
	}

	/**
	 * 查找displayCode对应的UiContent
	 * 
	 * @param displayCode
	 * @param banners
	 * @return
	 */
	private static UiContent findUiContent(List<UiContent> contentList, String displayCode) {
		if (contentList != null) {
			for (UiContent c : contentList) {
				if (displayCode != null && displayCode.equals(c.DisplayCode)) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * 广告二级内容
	 * 
	 * @param contentViewCode
	 * @return
	 */
	public List<UiContent> getBanner(String contentViewCode) {
		for (UiContent c : getHome()) {
			if (contentViewCode != null && c.LayerLevel == 1 
					&& contentViewCode.equals(c.ContentViewCode)) {
				return c.ChildContent;
			}
		}
		return null;
	}

	public UiContent getBanner(String bannerViewCode, String displayCode) {
		List<UiContent> banners = getBanner(bannerViewCode);
		return findUiContent(banners, displayCode);
	}

	/**
	 * 获取广告推荐内容分类
	 * 
	 * @param contentViewCode
	 * @return
	 */
	public List<UiContent> getBannerCategory(String contentViewCode) {
		List<UiContent> results = new ArrayList<UiContent>();
		List<UiContent> banners = getBanner(contentViewCode);
		if (banners != null) {
			for (UiContent c : banners) {
				if (c.DisplayCode != null && c.DisplayCode.startsWith("c")) {
					results.add(c);
				}
			}
		}
		return results;
	}

	public UiContent getUiContent(int id) {
		return getParentUiContent(getHome(), id);
	}

	private UiContent getParentUiContent(List<UiContent> contentList, int id) {
		if (contentList != null) {
			for (UiContent c : contentList) {
				if (c.Id == id) {
					return c;
				}
				UiContent result = getParentUiContent(c.ChildContent, id);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public UiContent getContent(String contentViewCode, String displayCode) {
		if (StringUtils.isEmpty(contentViewCode) || StringUtils.isEmpty(displayCode)) {
			return null;
		}
		return findContentByViewCode(getChildList(getHome(), contentViewCode), displayCode);
	}

	private List<UiContent> getChildList(List<UiContent> contentList, String contentViewCode) {
		if (contentList != null) {
			for (UiContent c : contentList) {
				if (contentViewCode.equals(c.ContentViewCode)) {
					return c.ChildContent;
				}
			}
		}
		return null;
	}

	/**
	 * 根据displayCode获取内容
	 * 
	 * @param displayCode
	 * @return
	 */
	public UiContent getContent(String displayCode) {
		return findContentByViewCode(getHome(), displayCode);
	}

	private static UiContent findContentByViewCode(List<UiContent> contentList, String displayCode) {
		if (contentList != null) {
			for (UiContent c : contentList) {
				if (displayCode != null && displayCode.equals(c.DisplayCode)) {
					return c;
				}
				UiContent result = findContentByViewCode(c.ChildContent, displayCode);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * 根据contentViewCode获取子级内容
	 * 
	 * @param contentViewCode
	 * @return
	 */
	public List<UiContent> getChildContent(String displayCode) {
		return findChildContentByViewCode(getHome(), displayCode);
	}

	private static List<UiContent> findChildContentByViewCode(List<UiContent> contentList, String displayCode) {
		if (contentList != null) {
			for (UiContent c : contentList) {
				if (displayCode != null && displayCode.equals(c.DisplayCode)) {
					return c.ChildContent;
				}
				List<UiContent> results = findChildContentByViewCode(c.ChildContent, displayCode);
				if (results != null) {
					return results;
				}
			}
		}
		return null;
	}

	public static UiVersionInfo json2bean(String json) {
		return GsonUtils.jsonDeserializer(json, UiVersionInfo.class);
	}

}
