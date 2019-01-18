package com.pisen.ott.launcher.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.utils.FileUtils;
import com.pisen.ott.launcher.utils.HttpUtils;

import android.content.Context;
import android.content.Intent;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;

/**
 * 图片管理加载器
 * 处理网络文件的异步下载和本地图片的加载
 * @author Liuhc
 * @version 1.0 2015年1月8日 下午5:04:34
 */
public class ImageDownLoader {
	
	//下载完成广播更新action
	public final static String ACTION_UIVERSION_UPDATE = "android.action.ottbox.uiversion";
	public final static String KEY_UIVERSION = "uiversion";
		
	private static ImageDownLoader mDownLoader;
	
	/** 保存正在下载或等待下载的URL和相应失败下载次数（初始为0），防止多次下载 */
	private static Map<String, Integer> taskCollection;
	private static Map<String, UiContent> uiCollection;
	/** 线程池 */
	private ExecutorService threadPool;
	/** 存储文件目录 （如无SD卡，则data目录下） 包含了UIVersion*/
	private String saveFilePath;
	/** 图片下载失败重试次数 */
	private static UiVersionInfo info;
	private String contentVersion;
	private static final int IMAGE_DOWNLOAD_FAIL_TIMES = 2;
	private Context mContext;
	
	public static ImageDownLoader getDownLoader(Context context) {
		if(mDownLoader == null){
			mDownLoader = new ImageDownLoader(context);
		}
		return mDownLoader;
	}
	
	private ImageDownLoader(Context context) {
		mContext = context;
		// 创建线程数10个
		threadPool = Executors.newFixedThreadPool(10);
		saveFilePath = FileUtils.getImagePath(context);
	}

	/**
	 * 更新所有图片
	 * @param contentVersion
	 * @param uiVersionInfo
	 */
	public void updateAllImages(String contentVersion,UiVersionInfo uiVersionInfo){
		this.contentVersion = contentVersion;
		this.info = uiVersionInfo;
		parseUICollection(uiVersionInfo.getHome(),uiVersionInfo.Content.ContentViewCode);
	}
	
	/**
	 * 解析下载任务
	 * @param contentViewCode 
	 */
	private void parseUICollection(List<UiContent> uiContentList, String contentViewCode){
		if (uiContentList != null && !uiContentList.isEmpty()) {
			for (UiContent uiContent : uiContentList) {
				if (!StringUtils.isEmpty(uiContent.Image)) {
					uiContent.ParentViewCode = contentViewCode;
					addTaskThread(uiContent);
				}
				parseUICollection(uiContent.ChildContent,uiContent.ContentViewCode);
			}
		}
		
	}
	
	/**
	 * 下载任务添加到线程池
	 * @param uiContent
	 * @param listener 图片下载完成后调用接口
	 */
	private void addTaskThread(final UiContent uiContent) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				down(uiContent,false);
				if (uiContent.LayerLevel == 1) {
					//只下载一级菜单背景图片
					down(uiContent,true);
				}
			}
		};
		
		if (taskCollection == null) {
			taskCollection = new HashMap<String, Integer>();
		}
		if (uiCollection == null) {
			uiCollection = new HashMap<String, UiContent>();
		}
		
		taskCollection.put(uiContent.Image, 0);
		uiCollection.put(uiContent.Image, uiContent);
		if (uiContent.LayerLevel == 1) {
			taskCollection.put(uiContent.SelectedImage, 0);
			uiCollection.put(uiContent.SelectedImage, uiContent);
		}
		
		if (HttpUtils.isNetworkAvailable(mContext)) {
			if (threadPool == null) {
				threadPool = Executors.newFixedThreadPool(10);
			}
			threadPool.execute(runnable);
			return;
		}
		
		LogCat.i(" network error!,please check it");
	}


	/**
	 * 下载背景图片或者选中图片
	 * @param b 是否下载选中图片
	 * @return
	 */
	private void down(UiContent uiContent, boolean b) {
		String url = "";
		String saveImgName = "";
		if (b) {
			url = uiContent.SelectedImage;
			saveImgName = FileUtils.getFileName(url);
		}else{
			url = uiContent.Image;
			saveImgName = uiContent.ParentViewCode+"_"+uiContent.DisplayCode+"."+FileUtils.getFileFormat(url);
		}
		
		boolean isSuccess = downloadFile(url, saveFilePath, saveImgName);
		
		if (taskCollection != null && taskCollection.containsKey(url)) {
			if (!isSuccess) {
				// 下载失败，再重新下载
				int times = taskCollection.get(url);
				if (times < IMAGE_DOWNLOAD_FAIL_TIMES) {
					times++;
					taskCollection.put(url, times);
					down(uiContent,b);
					LogCat.e(" download failed:" + url);
				}
			}else{
				// 下载成功
				taskCollection.remove(url);
				if (uiCollection != null && uiCollection.containsKey(url)) {
					if (uiContent.LayerLevel == 1 || uiContent.LayerLevel == 2) {
						if (b) {
							uiContent.SelectedImage = saveFilePath + File.separator + saveImgName;
						}else{
							uiContent.Image = saveFilePath + File.separator + saveImgName;
							sendRefreshBroadCast(uiContent);
						}
					}else{
						uiContent.Image = saveFilePath + File.separator + saveImgName;
					}
					
					uiCollection.remove(url);
					
					if (taskCollection.isEmpty() && !StringUtils.isEmpty(contentVersion)) {
						LauncherApplication.getConfig().setContentVersion(contentVersion);
						updateConfAndCache(info.Content.ChildContent);
						boolean isRefresh = LauncherApplication.getConfig().updateConfig(info);
						//清空对象
						info = null;
						contentVersion = "";
						LogCat.i(" update local contentVersion "+isRefresh);
					}
				}
			}
			LogCat.i(" 下载队列数："+taskCollection.size());
		}
	}

	/**
	 * 下载成功后
	 * 发送系统广播通知更新UI
	 * @param uiContent 下载文件网络url
	 */
	private void sendRefreshBroadCast(UiContent uiContent){
		Intent intent = new Intent(ACTION_UIVERSION_UPDATE);
		intent.putExtra(KEY_UIVERSION, uiContent);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 更新配置和缓存中的图片路径
	 * @param childContent 
	 */
	private void updateConfAndCache(List<UiContent> childContent){
		if (childContent == null || childContent.isEmpty()) {
			return;
		}
		for (int i = 0; i < childContent.size(); i++) {
			UiContent con = childContent.get(i);
			String localPath = con.ParentViewCode+"_"+con.DisplayCode+"."+FileUtils.getFileFormat(con.Image);
			con.Image = saveFilePath + File.separator + localPath;
			
			//清空缓存
			ImageLoader.clear(con.Image);
			
			updateConfAndCache(con.ChildContent);
		}
	}
	
	/**
	 * 下载文件
	 * @param fileUrl 下载路径
	 * @param folder  存储目录
	 * @param fileName 存储文件名
	 * @return 下载成功返回true
	 */
	public boolean downloadFile(String fileUrl, String folder, String fileName) {
		boolean writeSucc = false;
		FileOutputStream fos = null;
		InputStream is = null;
		File tmpFile = null;
		try {
			//没有挂载SD卡，无法下载文件
			if(StringUtils.isEmpty(folder) || StringUtils.isEmpty(fileName)){
				return false;
			}
			String tmpName = fileName.substring(0, fileName.lastIndexOf("."))+".tmp";
			
			if(FileUtils.hasSdcard()){
				File file = new File(folder);
				if(!file.exists()){
					file.mkdirs();
				}
			}
			
			File normalFile = new File(folder , fileName);
			//是否已下载更新文件
			if(normalFile.exists()){
				LogCat.e(" the img exist:"+fileName);
				return true;
			}
			
			//输出临时下载文件
			tmpFile = new File(folder , tmpName);
			fos = new FileOutputStream(tmpFile);
			
			URL url = new URL(fileUrl);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(10000);
			conn.connect();
			
			int length = conn.getContentLength();
			int count = 0;
			
			is = conn.getInputStream();
			
			byte buf[] = new byte[2*4096];
			int numread = 0;
    		while((numread = is.read(buf)) != -1){
    			count += numread;
    			fos.write(buf,0,numread);
    		}
    		
    		if(numread <= 0){
    			if (length != count) {
    				tmpFile.delete();
    				return false;
				}
    			//下载完成 - 将临时下载文件转成APK文件
				if(tmpFile.renameTo(normalFile)){
					writeSucc = true;
				}
    		}
		} catch(Exception e){
			e.printStackTrace();
			LogCat.e(" down file error... :"+e.toString());
			if(tmpFile != null)
				tmpFile.delete();
		}finally{
			try {
				if(fos != null)
					fos.close();
				if(is != null)
					is.close();
			} catch (IOException e) {
				LogCat.e(" close io error...:"+e.toString());
				e.printStackTrace();
			}
		}
		return writeSucc;
	}
	
	/**
	 * 检查队列,如果存在没有下载或者未下载成功的继续下载
	 */
	public void checkQueue(){
		if (taskCollection != null && !taskCollection.isEmpty()) {
			LogCat.i(" checkQueue 等待下载队列数："+taskCollection.size());
			synchronized (taskCollection) {
				for (String url : taskCollection.keySet()) {
					int times = taskCollection.get(url);
					if (times < IMAGE_DOWNLOAD_FAIL_TIMES) {
						addTaskThread(uiCollection.get(url));
					}
				}
			}
		}
	}
	
	/**
	 * 取消正在下载的任务
	 */
	public synchronized void cancelTasks() {
		if (threadPool != null) {
			threadPool.shutdownNow();
			threadPool = null;
		}
	}
	
}