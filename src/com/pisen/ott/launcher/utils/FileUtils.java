package com.pisen.ott.launcher.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import com.pisen.ott.launcher.localplayer.BrowserView;
import com.pisen.ott.launcher.localplayer.image.ImageViewerActivity;
import com.pisen.ott.launcher.localplayer.music.MusicPlayerActivity;
import com.pisen.ott.launcher.localplayer.video.VideoPlayActivity;
import com.pisen.ott.launcher.service.UpdateManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 文件操作工具包
 * 
 * @author Liuhc
 * @version 1.0 2014年11月21日 下午4:33:54
 */
public class FileUtils {

	// SD卡存放Lancher相关文件根目录
	public final static String APPROOT = "Android/data/com.pisen.ott.launcher/files";
	// SD卡存放Lancher升级更新文件目录
	public final static String UPDATE = "Update";
	// SD卡存放Lancher内容图片更新目录
	public final static String IMAGE = "Image";
	
	public static final int Video = 1;
	public static final int Image = 2;
	public static final int Music = 3;
	public static final int Unknow = -1;
	
	
	/**
	 * 获得Lancher根目录
	 * 
	 * @return
	 */
	public static String getAppPath() {
		return Environment.getExternalStorageDirectory() + File.separator + APPROOT;
	}

	/**
	 * 获得Lancher系统更新目录
	 * 
	 * @return
	 */
	public static String getUpdatePath() {
		return getAppPath() + File.separator + UPDATE ;
	}

	/**
	 * 获得Lancher系统更新UI目录 file : UIVersion/级数
	 * 
	 * @return
	 */
	public static String getUpdatePath(String file) {
		return getUpdatePath() + File.separator + file ;
	}

	/**
	 * 检查是否存在SD卡
	 * 
	 * @return
	 */
	public static boolean hasSdcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获得系统更新文件目录对象
	 * 
	 * @param context
	 * @return
	 */
	public static String getUpdateFile(Context context) {
		String filePath = "";
		if (hasSdcard()) {
			filePath = getUpdatePath();
		} else {
			filePath = context.getCacheDir().getPath() + File.separator + UPDATE ;
		}
		
		if (!isExists(filePath)) {
			boolean isCreate = createDirectory(filePath);
			LogCat.i("<<FileUtils>> " + filePath + " has created. " + isCreate);
		}
		return filePath;
	}

	
	public static String getImageLocalPath(Context context, String netPath) {
		return getImagePath(context) + File.separator + getFileName(netPath);

	}
	
	public static void saveBitmap(File f,Bitmap bm) { 
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获得系统更新图片目录对象
	 * @param context
	 * @return
	 */
	public static String getImagePath(Context context) {
		String filePath = "";
		if (hasSdcard()) {
			filePath = getAppPath() + File.separator + IMAGE ;
		} else {
			filePath = context.getCacheDir().getPath() + File.separator + IMAGE ;
		}
		
		if (!isExists(filePath)) {
			boolean isCreate = createDirectory(filePath);
			LogCat.i("<<FileUtils>> " + filePath + " has created. " + isCreate);
		}
		return filePath;
	}
	
	/**
	 * 获取文件大小，单位为byte（若为目录，则包括所有子目录和文件）
	 * 
	 * @param file
	 * @return
	 */
	public static long getFileSize(File file) {
		long size = 0;
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] subFiles = file.listFiles();
				if (subFiles != null) {
					int num = subFiles.length;
					for (int i = 0; i < num; i++) {
						size += getFileSize(subFiles[i]);
					}
				}
			} else {
				size += file.length();
			}
		}
		return size;
	}

	public static boolean isExists(File dir, String fileName) {
		return new File(dir, fileName).exists();
	}

	public static boolean isExists(String dir, String fileName) {
		return new File(dir, fileName).exists();
	}

	public static boolean isExists(String filePath) {
		return new File(filePath).exists();
	}

	/**
	 * 读取文本文件
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String read(Context context, String fileName) {
		try {
			FileInputStream in = context.openFileInput(fileName);
			return readInStream(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (StringUtils.isEmpty(filePath))
			return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}

	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return "";
		}
		int point = filePath.lastIndexOf('.');
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1, point);
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return "";
		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 * 获取文件大小
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		long size = 0;
		File file = new File(filePath);
		if (file != null && file.exists()) {
			size = file.length();
		}
		return size;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param size
	 *            字节
	 * @return
	 */
	public static String getFileSize(long size) {
		if (size <= 0)
			return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		} else {
			return df.format(temp) + "K";
		}
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取目录文件大小
	 * 
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // 递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * 获取目录文件个数
	 * 
	 * @param f
	 * @return
	 */
	public long getFileList(File dir) {
		long count = 0;
		File[] files = dir.listFiles();
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);// 递归
				count--;
			}
		}
		return count;
	}

	/**
	 * 向App写图片
	 * 
	 * @param buffer
	 * @param folder
	 * @param fileName
	 * @return 成功返回True
	 */
	public static boolean writeFile(byte[] buffer, String folder, String fileName) {
		boolean writeSucc = false;

		if (!StringUtils.isEmpty(folder)) {
			File fileDir = new File(folder);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}

			File file = new File(folder , fileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(buffer);
				writeSucc = true;
			} catch (Exception e) {
				e.printStackTrace();
				writeSucc = false;
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
					writeSucc = false;
				}
			}
		}
		return writeSucc;
	}

	public static byte[] readFile(String imgUrl) throws IOException {
		URL url = new URL(imgUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(UpdateManager.TIME_OUT_CONNECT);
		InputStream inStream = conn.getInputStream();
		// 调用readStream方法
		return readStream(inStream);
	}

	public static byte[] readStream(InputStream inStream) {
		// 把数据读取存放到内存中去
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outSteam.close();
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outSteam.toByteArray();
	}

	public static String readInStream(FileInputStream inStream) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			Log.i("FileTest", e.getMessage());
		}
		return null;
	}

	public static String readInStream(InputStream inStream) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			Log.i("FileTest", e.getMessage());
		}
		return null;
	}
	
	/**
	 * 计算SD卡的剩余空间
	 * 
	 * @return 返回-1，说明没有安装sd卡
	 */
	public static long getFreeDiskSpace() {
		String status = Environment.getExternalStorageState();
		long freeSpace = 0;
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				freeSpace = availableBlocks * blockSize / 1024;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return -1;
		}
		return (freeSpace);
	}

	/**
	 * 新建目录
	 * 
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String directoryName) {
		boolean status;
		if (!directoryName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + directoryName);
			status = newPath.mkdir();
			status = true;
		} else
			status = false;
		return status;
	}

	public static File createFile(String folderPath, String fileName) {
		File destDir = new File(folderPath);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return new File(folderPath, fileName);
	}

	/**
	 * 删除目录(包括：目录里的所有文件)
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean deleteDirectory(String fileName) {
		boolean status = false;
		SecurityManager checker = new SecurityManager();
		if (!fileName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isDirectory()) {
				String[] listfile = newPath.list();
				// delete all files within the specified directory and then
				// delete the directory
				try {
					for (int i = 0; i < listfile.length; i++) {
						File deletedFile = new File(newPath.toString() + "/" + listfile[i].toString());
						deletedFile.delete();
					}
					newPath.delete();
					Log.i("deleteDirectory", fileName);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return status;
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean deleteFile(String fileName) {
		boolean status = false;
		SecurityManager checker = new SecurityManager();
		if (!fileName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isFile()) {
				try {
					Log.i("deleteFile", fileName);
					newPath.delete();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return status;
	}
	
	public static int getFileType(String path){
		File f = new File(path);
		String name = f.getName();
		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		Intent it;
		// 根据类型启动播放器
		if (end.equals("mp4") || end.equals("rmvb") || end.equals("rm") || end.equals("mpg") || end.equals("avi") || end.equals("mpeg")) {
		    return Video;
		}
		if (end.equals("mp3") || end.equals("wav") || end.equals("ogg") || end.equals("midi") || end.equals("wma")) {
		   return Music;
		}
		if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
		   return Image;
		}
		return Unknow;
	}
}