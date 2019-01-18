package com.pisen.ott.launcher.search;

import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

/**
 * 字符串工具类
 * 
 * @author Liuhc
 * @version 1.0 2015年1月27日 上午11:43:23
 */
public class SearchUtils {

	/**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input) {
		if (input == null || "".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断一个字符是否是中文
	 * 
	 * @param ch
	 * @return
	 */
	public static boolean isChinese(String ch) {
		Pattern p = Pattern.compile("[\\u4E00-\\u9FA5]+");
		return p.matcher(ch).matches();
	}

	/**
	 * 判断是否是数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 将字符串中的中文转化为拼音,并提取首字母
	 * 
	 * @param inputString
	 * @return
	 */
	public static String getPingYinShort(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();// 把字符串转化成字符数组
		String shortString = "";
		try {
			for (int i = 0; i < input.length; i++) {
				// u4E00是unicode编码，判断是不是中文
				if (java.lang.Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
					// 将汉语拼音的全拼存到temp数组
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
					// 取拼音的第一个读音
					shortString += temp[0].substring(0, 1);
				} else {
					// 是否过滤其他非中文字符串
					shortString += input[i];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return shortString.toLowerCase();
	}

	/**
	 * 语音搜索解析
	 * 
	 * @param json
	 * @return
	 */
	public static String parseIatResult(String json) {
		StringBuffer ret = new StringBuffer();
		try {
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);

			JSONArray words = joResult.getJSONArray("ws");
			for (int i = 0; i < words.length(); i++) {
				// 转写结果词，默认使用第一个结果
				JSONArray items = words.getJSONObject(i).getJSONArray("cw");
				JSONObject obj = items.getJSONObject(0);
				ret.append(obj.getString("w"));
				// 如果需要多候选结果，解析数组其他字段
				// for(int j = 0; j < items.length(); j++)
				// {
				// JSONObject obj = items.getJSONObject(j);
				// ret.append(obj.getString("w"));
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret.toString();
	}
}
