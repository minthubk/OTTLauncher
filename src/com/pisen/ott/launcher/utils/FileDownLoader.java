package com.pisen.ott.launcher.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.utils.FileUtils;
import com.pisen.ott.launcher.utils.HttpUtils;

import android.content.Context;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;

/**
 * 文件异步下载工具类
 * @author Liuhc
 * @version 1.0 2015年1月8日 下午5:04:34
 */
public class FileDownLoader {
	
	private static FileDownLoader mDownLoader;
	public String downing_error = "";
	/** 线程池 */
	private ExecutorService threadPool;
	/** 存储文件目录 （如无SD卡，则data目录下）*/
	private String saveFilePath;
	private Context mContext;
	//终止标记
    private boolean interceptFlag;
    
	public static FileDownLoader getDownLoader(Context context) {
		if(mDownLoader == null){
			mDownLoader = new FileDownLoader(context);
		}
		return mDownLoader;
	}
	
	private FileDownLoader(Context context) {
		mContext = context;
		threadPool = Executors.newFixedThreadPool(10);
		saveFilePath = FileUtils.getUpdateFile(context);
	}

	/**
	 * 异步下载
	 * @param url 下载文件地址
	 * @param listener 文件下载过程调用接口
	 */
	public void download(final String url, final AsyncLoaderListener listener) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (!downloadFile(url,listener)) {
					listener.onFailed(downing_error);
				}
			}
		};
		
		if (HttpUtils.isNetworkAvailable(mContext)) {
			if (threadPool == null) {
				threadPool = Executors.newFixedThreadPool(10);
			}
			threadPool.execute(runnable);
		}else{
			LogCat.i("<<DownLoader>> network error!,please check it");
			listener.onFailed(mContext.getString(R.string.error_net));
		}
	}


	/**
	 * 下载文件
	 * @param fileUrl 下载路径
	 * @param listener 异步下载文件接口
	 * @return 下载成功返回true
	 */
	private boolean downloadFile(String fileUrl, AsyncLoaderListener listener) {
		interceptFlag = false;
		boolean isSuccess = false;
		//下载包保存路径
		String folder = saveFilePath;
		//下载文件名
		String fileName = FileUtils.getFileName(fileUrl);
		//下载文件大小
		String totalFileSize = "";
		//已下载文件大小
		String tmpFileSize = "";
		//进度值
		int progress = 0;
		
		FileOutputStream fos = null;
		InputStream is = null;
		File tmpFile = null;
		HttpURLConnection conn = null;
		try {
			if(StringUtils.isEmpty(folder) || StringUtils.isEmpty(fileName)){
				downing_error = mContext.getString(R.string.error_down_path);
				LogCat.e("本地存储路径或下载路径出错...本地："+folder+" fileName:"+fileName);
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
				LogCat.e("<<DownLoader>> the file exist:"+fileName);
				listener.onSuccess(normalFile);
				return true;
			}
			
			//输出临时下载文件
			tmpFile = new File(folder , tmpName);
			fos = new FileOutputStream(tmpFile);
			
			
			fileUrl = URLEncoder.encode(fileUrl,"utf-8").replaceAll("\\+", "%20");
			fileUrl = fileUrl.replaceAll("%3A", ":").replaceAll("%2F", "/");
			URL url = new URL(fileUrl);
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.connect();
			
			if (conn.getResponseCode() == 200) {
				int length = conn.getContentLength();
				is = conn.getInputStream();
				
				if (length > 0) {
					listener.onStart();
				}
				
				//显示文件大小格式：2个小数点显示
		    	DecimalFormat df = new DecimalFormat("0.00");
		    	//进度条下面显示的总文件大小
		    	totalFileSize = df.format((float) length / 1024 / 1024) + "MB";
		    	
				byte buf[] = new byte[1024];
				int count = 0;
				int numread = 0;
	    		while(interceptFlag == false && (numread = is.read(buf)) != -1){
	    			count += numread;
	    			fos.write(buf,0,numread);
		    		tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
		    	    progress =(int)(((float)count / length) * 100);
		    	    listener.onDownLoading(progress, tmpFileSize, totalFileSize);
	    		}
	    		
	    		if(numread <= 0){
	    			if (length != count) {
	    				tmpFile.delete();
	    				downing_error = mContext.getString(R.string.error_down_package);
	    				return false;
					}
	    			//下载完成 - 将临时下载文件转成APK文件
					if(tmpFile.renameTo(normalFile)){
						isSuccess = true;
						listener.onSuccess(normalFile);
					}
	    		}
			}else{
				downing_error = mContext.getString(R.string.error_net);
			}
		} catch(Exception e){
			e.printStackTrace();
			LogCat.e("<<DownLoader>> down file error... :"+e.toString());
			downing_error = mContext.getString(R.string.error_net);
			if(tmpFile != null)
				tmpFile.delete();
		}finally{
			try {
				if(fos != null){
					fos.close();
				}
				if(is != null){
					is.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
				if (interceptFlag) {
					tmpFile.delete();
					downing_error = mContext.getString(R.string.error_down);
				}
			} catch (Exception e) {
				LogCat.e("<<DownLoader>> close io error...:"+e.toString());
				e.printStackTrace();
				downing_error = mContext.getString(R.string.error_net);
			}
		}
		return isSuccess;
	}
	
	/**
	 * 取消正在下载的任务
	 */
	public synchronized void cancelTasks() {
		interceptFlag = true;
		if (threadPool != null) {
			threadPool.shutdownNow();
			threadPool = null;
		}
	}


	/** 异步下载文件接口 */
	public interface AsyncLoaderListener {
		void onStart();
		//下载失败,返回异常信息
		void onFailed(String erroInfo);
		//下载成功,返回下载后文件对象;
		void onSuccess(File file);
		//正在下载;返回下载进度,缓存文件大小,总文件大小;
		void onDownLoading(int progress, String temSize,String totalSize);
	}

}