package com.pisen.ott.launcher.widget;

/**
 * 明细接口
 * 
 * @author yangyp
 * @version 1.0, 2015年2月27日 上午11:07:39
 */
public interface IDetailContent extends IRequestFocus {

	void setMasterTitle(IMasterTitle masterTitle);

	/**
	 * 判断明细是否有数据
	 * 
	 * @return
	 */
	boolean hasData();
}
