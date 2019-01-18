package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.izy.os.EnvironmentUtils;
import android.izy.widget.BaseListAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.BrowserView.ViewMode;
import com.pisen.ott.launcher.utils.FileUtils;

/**
 * 文件二级适配器
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:10:56
 */
public class SubFileBrowserAdapter extends BaseListAdapter<AlbumData> {

	public interface OnItemClickListener {
		void onItemClick(int position, View view, AlbumData item);
	}

	Context context;
	private ViewMode mode;
	private OnItemClickListener itemClickListener;

	public SubFileBrowserAdapter(Context context, List<AlbumData> objects) {
		this.context = context;
		addAll(objects);
	}

	public void setViewMode(ViewMode mode) {
		this.mode = mode;
	}

	public ViewMode getViewMode() {
		return this.mode;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		this.itemClickListener = l;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		return getListView(position, convertView, parent);
	}

	private View getListView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.file_browser_item_file, null);
			convertView.setTag(holder = new ViewHolder());
			holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
			holder.imgArrows = (ImageView) convertView.findViewById(R.id.imgArrows);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final AlbumData item = getItem(position);
		holder.txtName.setText(item.title);
		File f = new File(item.path);
		String name = f.getName();
		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (item.isDirectory) {
			if (EnvironmentUtils.isRootPath(item.getPath())) {
				holder.txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.local_usb, 0, 0, 0);
			} else {
				holder.txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.local_ic_dir, 0, 0, 0);
			}
		} else {
			if (FileUtils.getFileType(item.path) == AlbumData.Image) {
				holder.txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.local_ic_image, 0, 0, 0);
			} else if (FileUtils.getFileType(item.path) == AlbumData.Music) {
				holder.txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.local_ic_music, 0, 0, 0);
			} else if (FileUtils.getFileType(item.path) == AlbumData.Video) {
				holder.txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.local_ic_video, 0, 0, 0);
			} 
//			else if (LocalPalyerPagerViewBase.ApkType.contains(end)) {//APK
//				BitmapDrawable bd  = (BitmapDrawable) showUninstallAPKIcon(item.path);
//				bd.setBounds(0, 0, 64,64);
//				holder.txtName.setCompoundDrawables(bd, null, null, null);
//			} 
			else {
				holder.txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.local_ic_doc, 0, 0, 0);
			}
		}
		holder.imgArrows.setVisibility(item.isDirectory ? View.VISIBLE : View.INVISIBLE);

		final View view = convertView;
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (itemClickListener != null) {
					itemClickListener.onItemClick(position, view, item);
				}
			}
		});
		return convertView;
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	static class ViewHolder {
		TextView txtName;
		ImageView imgPhoto;
		TextView txtCount;
		ImageView imgArrows;
	}

    /**
     * 获取未安装apk文件的图标 
     */
	private Drawable showUninstallAPKIcon(String apkPath) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// apk包的文件路径
			// 这是一个Package 解释器, 是隐藏的
			// 构造函数的参数只有一个, apk文件的路径
			// PackageParser packageParser = new PackageParser(apkPath);
			Class pkgParserCls = Class.forName(PATH_PackageParser);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
			// 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			typeArgs = new Class[4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[3] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
			valueArgs = new Object[4];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = apkPath;
			valueArgs[2] = metrics;
			valueArgs[3] = 0;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
			// 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
			// uid 输出为"-1"，原因是未安装，系统未分配其Uid。
			Class assetMagCls = Class.forName(PATH_AssetManager);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			CharSequence label = null;
			if (info.labelRes != 0) {
				label = res.getText(info.labelRes);
			}
			Log.d("ANDROID_LAB", "label=" + label);
			// 这里就是读取一个apk程序的图标
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				return icon;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
