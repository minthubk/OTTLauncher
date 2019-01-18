package com.pisen.ott.launcher.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.LauncherApplication;
import com.pisen.ott.launcher.service.UpdateInfo.Content;
import com.pisen.ott.launcher.utils.FileUtils;
import com.pisen.ott.launcher.utils.HttpUtils;
import com.pisen.ott.launcher.widget.OTTAlertDialog;
import com.pisen.ott.launcher.widget.OTTDownloadDialog;

import android.content.Context;
import android.content.Intent;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.volley.RequestManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 应用程序更新工具包
 * @author Liuhc
 * @version 1.0 2014年12月17日 下午4:39:17
 */
public class UpdateManager {

	private static final int DOWN_NOSDCARD = 0;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
	
    //超时设置 10m
  	public static final int TIME_OUT_CONNECT = 10000;
  	public static final int TIME_OUT_READ = 10000;
  	
	private static UpdateManager updateManager;
	private Context mContext;
	//下载对话框
	private OTTDownloadDialog downloadDialog = null;
    //进度条
    private ProgressBar mProgress = null;
    //显示下载数值
    private TextView mProgressText = null;
    //进度值
    private int progress;
    //下载线程
    private Thread downLoadThread;
    //终止标记
    private boolean interceptFlag;
	//提示语
	private String updateMsg = "";
	//返回的安装包url
	private String apkUrl = "";
	//下载包保存路径
    private String savePath = "";
	//apk保存完整路径
	private String apkFilePath = "";
	//临时下载文件路径
	private String tmpFilePath = "";
	//下载文件大小
	private String apkFileSize;
	//已下载文件大小
	private String tmpFileSize;
	
//	private String curVersionName = "";
	private int curVersionCode;
	private UpdateInfo mUpdate;
    
    private Handler mHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				mProgressText.setText(tmpFileSize + "/" + apkFileSize);
				break;
			case DOWN_OVER:
				if (downloadDialog != null) {
					downloadDialog.dismiss();
					installApk();
				}
				break;
			case DOWN_NOSDCARD:
				downloadDialog.dismiss();
				Toast.makeText(mContext, mContext.getString(R.string.error_sdcard), Toast.LENGTH_SHORT).show();
				break;
			}
    	};
    };
    
	public static UpdateManager getUpdateManager() {
		if(updateManager == null){
			updateManager = new UpdateManager();
		}
		updateManager.interceptFlag = false;
		return updateManager;
	}
	
	/**
	 * 检查App更新
	 * @param context
	 * @param isUpdateContent 是否更新UI版本
	 */
	public void checkAppUpdate(Context context, final boolean isUpdateContent){
		this.mContext = context;
		getCurrentVersion();
		RequestManager.addRequest(new StringRequest(HttpUtils.URL_UPDATE, new Listener<String>() {
			@Override
			public void onResponse(String arg0) {
//				LogCat.i("<Update> check up inner version:"+arg0);
				if (!StringUtils.isEmpty(arg0)) {
					//系统版本信息
					mUpdate = UpdateInfo.json2bean(arg0);
					if(mUpdate != null){
						checkUIVersion();
						if (mUpdate.checkUpdate(curVersionCode)) {
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									apkUrl = mUpdate.System.Apk;
									updateMsg = mUpdate.System.Description;
									showNoticeDialog(isUpdateContent);
								}
							}, 5000);
						}
					}
				}
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				LogCat.i("<Update> check up new version failed:"+arg0.getMessage());
			}
		}), mContext);
		
	}	
	
	/**
	 * 检查UI更新
	 */
	private void checkUIVersion(){
		
		//是否存在内容更新
		Content content = mUpdate.getContent(mUpdate.System.UiVersion);
		boolean hasNewContent = false;
		if (content != null) {
			hasNewContent = content.hasContentUpdate(LauncherApplication.getConfig().getContentVersion());
		}
		
		if (!hasNewContent) {
			//内容无更新
			LogCat.i("<Update> content version newest");
			return;
		}
		
		//读取config文件中的UiVersion
		String configUrl = content.Config;
		if (!StringUtils.isEmpty(configUrl)) {
			RequestManager.addRequest(new StringRequest(configUrl, new Listener<String>() {
				@Override
				public void onResponse(String arg0) {
//					LogCat.i("<Update> check up ui-version:"+arg0);
					if (!StringUtils.isEmpty(arg0)) {
						//UI版本信息
						UiVersionInfo uiVersionInfo = UiVersionInfo.json2bean(arg0);
						if(uiVersionInfo != null){
							ImageDownLoader.getDownLoader(mContext).updateAllImages(mUpdate.getConentVersion(), uiVersionInfo);
						}
					}
				}
			}, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError arg0) {
					LogCat.i("<Update> check up ui-version failed:"+arg0.getMessage());
				}
			}), mContext);
		}
	}
	

	/**
	 * 获取当前客户端版本信息
	 */
	private void getCurrentVersion(){
		curVersionCode = LauncherApplication.getConfig().getInternalVersion();
	}
	
	/**
	 * 显示版本更新通知对话框
	 * @param isUpdateContent 
	 */
	private void showNoticeDialog(final boolean isUpdateContent){
//		Builder builder = new AlertDialog.Builder(mContext);  
//	    builder.setTitle("提示");  
//	    builder.setMessage("该下车了");  
//	    builder.setNegativeButton("取消", new OnClickListener() {  
//	        @Override  
//	        public void onClick(DialogInterface dialog, int which) {  
//	  
//	        }  
//	    });  
//	    builder.setPositiveButton("确定", new OnClickListener() {  
//	        @Override  
//	        public void onClick(DialogInterface dialog, int which) {  
//	  
//	        }  
//	    });  
//	    final AlertDialog dialog = builder.create();  
//	    dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));  
//	    dialog.show();
		
		final OTTAlertDialog dialog = new OTTAlertDialog(mContext);
		if (TextUtils.isEmpty(updateMsg)) {
			dialog.setTitle(mContext.getString(R.string.dialog_update_message));
//			dialog.setTitle("发现新版本："+mUpdate.getNewInnerVersion()+"  是否现在更新?");
		}else{
			dialog.setTitle(mContext.getString(R.string.dialog_update_title));
		}
		dialog.setMessage(updateMsg);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOkListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		dialog.setCancelListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (isUpdateContent) {
//					checkUIVersion();
				}
			}
		});
		dialog.show();
	}
	
	/**
	 * 显示下载对话框
	 */
	private void showDownloadDialog(){
		if (!HttpUtils.isNetworkAvailable(mContext)) {
			Toast.makeText(mContext, mContext.getString(R.string.error_net), 1).show();
			return;
		}
		downloadDialog = new OTTDownloadDialog(mContext);
		mProgress = downloadDialog.getProgressBar();
		mProgressText = downloadDialog.getTxtProgress();
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.setCancelListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadDialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadDialog.show();
		downloadApk();
	}
	
	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			FileOutputStream fos = null;
			InputStream is = null;
			try {
				String apkName = "OTTBox_"+mUpdate.System.SystemVersion+".apk";
				String tmpApk = "OTTBox_"+mUpdate.System.SystemVersion+".tmp";
				
				if(FileUtils.hasSdcard()){
					savePath = FileUtils.getUpdatePath();
					File file = new File(savePath);
					if(!file.exists()){
						file.mkdirs();
					}
					apkFilePath = savePath +File.separator+ apkName;
					tmpFilePath = savePath +File.separator+ tmpApk;
				}
				
				//没有挂载SD卡，无法下载文件
				if(apkFilePath == null || apkFilePath.equals("")){
					mHandler.sendEmptyMessage(DOWN_NOSDCARD);
					return;
				}
				
				File ApkFile = new File(apkFilePath);
				//是否已下载更新文件
				if(ApkFile.exists()){
					//通知安装
					mHandler.sendEmptyMessage(DOWN_OVER);
					return;
				}
				
				//输出临时下载文件
				File tmpFile = new File(tmpFilePath);
				fos = new FileOutputStream(tmpFile);
				
				apkUrl = URLEncoder.encode(apkUrl,"utf-8").replaceAll("\\+", "%20");
				apkUrl = apkUrl.replaceAll("%3A", ":").replaceAll("%2F", "/");
				URL url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(TIME_OUT_CONNECT);
				conn.setReadTimeout(TIME_OUT_READ);
				conn.connect();
				
				int length = conn.getContentLength();
				is = conn.getInputStream();
				
				//显示文件大小格式：2个小数点显示
		    	DecimalFormat df = new DecimalFormat("0.00");
		    	//进度条下面显示的总文件大小
		    	apkFileSize = df.format((float) length / 1024 / 1024) + "MB";
				
				int count = 0;
				byte buf[] = new byte[1024];
				
				do{
		    		int numread = is.read(buf);
		    		count += numread;
		    		//进度条下面显示的当前下载文件大小
		    		tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
		    		//当前进度值
		    	    progress =(int)(((float)count / length) * 100);
		    	    //更新进度
		    	    mHandler.sendEmptyMessage(DOWN_UPDATE);
		    		if(numread <= 0){	
		    			//下载完成 - 将临时下载文件转成APK文件
						if(tmpFile.renameTo(ApkFile)){
							//通知安装
							mHandler.sendEmptyMessage(DOWN_OVER);
						}
		    			break;
		    		}
		    		fos.write(buf,0,numread);
		    	}while(!interceptFlag);//点击取消就停止下载
				
			} catch(Exception e){
				e.printStackTrace();
				FileUtils.deleteFile(tmpFilePath);
				LogCat.e("<<UpdateManager>> down apk error... :"+e.toString());
			}finally{
				try {
					fos.close();
					is.close();
					if (interceptFlag) {
						FileUtils.deleteFile(tmpFilePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
					LogCat.e("<<UpdateManager>> close io error...:"+e.toString());
				}
			}
		}
	};
	
	/**
	* 下载apk
	* @param url
	*/	
	private void downloadApk(){
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}
	
	/**
    * 安装apk
    * @param url
    */
	private void installApk(){
		File apkfile = new File(apkFilePath);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive"); 
        mContext.startActivity(i);
	}
	
}
