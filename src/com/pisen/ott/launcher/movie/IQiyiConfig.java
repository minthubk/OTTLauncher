package com.pisen.ott.launcher.movie;

import java.util.HashMap;

public class IQiyiConfig {

	public static final String SIGNATURE = "50q4vlmrd900l0b6nxjsuirv47&d&y5vf9ph65ld82oshvrv";

	public static final HashMap<String, String> filmTags = new HashMap<String, String>();
	public static final HashMap<String, String> episodeTags = new HashMap<String, String>();
	public static final HashMap<String, String> cartoonsTags = new HashMap<String, String>();
	public static final HashMap<String, String> varietyTags = new HashMap<String, String>();
	static {
		filmTags.put("精彩好莱坞", "CT1999413583396");
		filmTags.put("华语院线", "CT1999413583196");
		filmTags.put("欧陆经典", "CT1999413582996");
		filmTags.put("动作剧场", "CT1999413590596");
		filmTags.put("开心喜剧", "CT1999413588796");
		filmTags.put("动画电影", "CT1999400021196");
		filmTags.put("浪漫爱情", "CT1999413685996");
		filmTags.put("恐怖惊悚", "CT1999413583796");
		filmTags.put("热映特辑", "CT1999408163596");
		
		episodeTags.put("同步跟播", "CT1999414280396");
		episodeTags.put("青春偶像", "CT1999413578196");
		episodeTags.put("古装言情", "CT1999413578596");
		episodeTags.put("搞笑喜剧", "CT1999406044396");
		episodeTags.put("年代传奇", "CT1999406042996");
		episodeTags.put("军旅谍战", "CT1999406042796");
		episodeTags.put("神话科幻", "CT1999406041996");
		episodeTags.put("乡村生活", "CT1999406040996");
		episodeTags.put("养眼韩剧", "CT1999413580596");
		episodeTags.put("清新台剧", "CT1999413579596");
		episodeTags.put("独播日剧", "CT1999413579196");
		
		cartoonsTags.put("国产精选", "CT1999413574196");
		cartoonsTags.put("日本动画", "CT1999413574796");
		cartoonsTags.put("欧美经典", "CT1999413573596");
		cartoonsTags.put("原创动漫", "CT1999413573396");
		cartoonsTags.put("剧场版", "CT1999413572796");
		
		varietyTags.put("强档推荐", "CT1999407947196");
		varietyTags.put("特色综艺", "CT1999413775796");
		varietyTags.put("内地综艺", "CT1999413244796");
		varietyTags.put("港台综艺", "CT1999413243996");
		varietyTags.put("日韩综艺", "CT1999413243796");
		varietyTags.put("小品集锦", "CT1999398463796");
		
	}
	
	
	public static String getFilmTag(String title){
		return filmTags.get(title);
	}
	
	public static String getVarietyTag(String title){
		return varietyTags.get(title);
	}
	public static String getCartoonsTag(String title){
		return cartoonsTags.get(title);
	}
	public static String getEpisodeTag(String title){
		return episodeTags.get(title);
	}

	/** 可能可用的频道 */
	/**
	 * 实体频道 : 电影 电视剧 动漫 少儿 综艺 娱乐 音乐 教育 体育 旅游 时尚 搞笑 生活 财经 纪录片 军事 汽车 资讯
	 * 特殊频道 : 4K H265  杜比 3D 每日资讯 1080P
	 */
}
