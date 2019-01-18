package com.pisen.ott.launcher.utils;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;

/**
 * 应用程序操作相关工具类
 * 
 * @author Liuhc
 * @version 1.0 2015年2月12日 下午4:29:12
 */
public class AppUtils {

	/**
	 * 安装应用程序
	 * 
	 * @param apkfile
	 */
	public static void installApk(Context ctx, File apkfile) {
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		ctx.startActivity(i);
	}

	/**
	 * 判断APK是否安装，如果安装了直接启动
	 * 
	 * @param ctx
	 * @param obj
	 * @return
	 */
	public static boolean isInstalledApk(Context ctx, UiContent obj) {
		String packageName = getPackName(ctx, FileUtils.getFileName(obj.ApkFile));
		boolean hasInstalled = false;
		if (packageName != null) {
			PackageManager pm = ctx.getPackageManager();
			List<PackageInfo> list = pm.getInstalledPackages(PackageManager.PERMISSION_GRANTED);
			for (PackageInfo p : list) {
				if (packageName != null && packageName.equals(p.packageName)) {
					hasInstalled = true;
					openApk(ctx, packageName);
					break;
				}
			}
		}
		return hasInstalled;
	}

	/**
	 * 判断是否安装，不启动
	 * @param ctx
	 * @param obj
	 * @return
	 */
	public static boolean checkInstalled(Context ctx, UiContent obj) {
		String packageName = getPackName(ctx, FileUtils.getFileName(obj.ApkFile));
		boolean hasInstalled = false;
		if (packageName != null) {
			PackageManager pm = ctx.getPackageManager();
			List<PackageInfo> list = pm.getInstalledPackages(PackageManager.PERMISSION_GRANTED);
			for (PackageInfo p : list) {
				if (packageName != null && packageName.equals(p.packageName)) {
					hasInstalled = true;
					break;
				}
			}
		}
		return hasInstalled;
	}
	
	/**
	 * 打开应用程序
	 * 
	 * @param packName
	 */
	public static void openApk(Context ctx, String packName) {
		Intent LaunchIntent = ctx.getPackageManager().getLaunchIntentForPackage(packName);
		ctx.startActivity(LaunchIntent);
	}
	
	public static Drawable loadAppIcon(Context ctx,String pkgName){
		PackageManager pm = ctx.getPackageManager();
		try {
			return pm.getApplicationIcon(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 应用程序已安装直接打开
	 * @param ctx
	 * @param obj
	 */
	public static void startApk(Context ctx,  UiContent obj) {
		String packageName = getPackName(ctx, FileUtils.getFileName(obj.ApkFile));
		openApk(ctx, packageName);
	}
	
	/**
	 * 获取apk包名
	 * 
	 * @param apkName
	 * @return
	 */
	public static String getPackName(Context ctx, String apkName) {
		apkName = FileUtils.getUpdateFile(ctx) + "/" + FileUtils.getFileName(apkName);
		PackageInfo info = ctx.getPackageManager().getPackageArchiveInfo(apkName, PackageManager.GET_ACTIVITIES);
		return info != null ? info.packageName : null;
	}

}
