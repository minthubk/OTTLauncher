package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.izy.os.EnvironmentUtils;
import android.izy.util.URLUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

/** 资源类，包含文件、视频、图片、音乐 */
public class AlbumData implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int File = 0;
	public static final int Video = 1;
	public static final int Image = 2;
	public static final int Music = 3;

	public String id;// id
	public String bucketId;// 组id
	public String path;// 路径
	public String title; // 标题
	public String thumbnailUrl; // 缩略图
	public long updated; // 更新时间
	public int count; // 下级资源数量
	public boolean isDirectory; // 是否目录
	public int fileType;// 资源类型
	public String local; //本地rom盘

	private ContentResolver mResolver = null;
	private Context mContext;

	/**
	 * @param fileType
	 */
	public AlbumData(Context context) {
		this.count = 0;
		this.mContext = context;
		this.mResolver = context.getContentResolver();

	}

	public AlbumData(int fileType, Context context) {
		super();
		this.count = 0;
		this.fileType = fileType;
		this.mContext = context;
		this.mResolver = mContext.getContentResolver();
	}

	public AlbumData getParent() {
		File file = new File(path);
		AlbumData result = new AlbumData(fileType, this.mContext);
		result.path = file.getParent();
		result.id = result.path;
		result.isDirectory = true;
		result.fileType = fileType;
		result.title = file.getParentFile().getName();

		return result;
	}

	/**
	 * 获取当前路径
	 * 
	 * @return
	 */
	public String getPath() {
		if (isDirectory) {
			return path;
		} else {
			return URLUtils.getParentURI(path);
		}
	}

	/**
	 * 获取下级资源列表
	 * 
	 * @return
	 */
	public List<AlbumData> listFile() {
		// 返回的数据
		List<AlbumData> results = new ArrayList<AlbumData>();
		// 文件二级数据
		if (this.fileType == File) {
			results = getSecondLevelFiles();
		}
		// 视频二级数据
		if (this.fileType == Video) {
			results = getSecondLevelVideos();
		}
		// 音乐二级数据
		if (this.fileType == Music) {
			results = getSecondLevelMusics();
		}
		// 图片二级数据
		if (this.fileType == Image) {
			results = getSecondLevelImages();
		}
		return results;
	}

	/** 获取文件二级数据 */
	public List<AlbumData> getSecondLevelFiles() {
		List<AlbumData> results = new ArrayList<AlbumData>();
		List<AlbumData> fileList = new ArrayList<AlbumData>();
		List<AlbumData> dirList = new ArrayList<AlbumData>();
		File dir = new File(getPath());
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				AlbumData data = new AlbumData(this.mContext);
				data.id = f.getPath();
				data.path = f.getPath();
				data.fileType = File;
				data.title = f.getName();
				data.thumbnailUrl = f.getPath();
				data.updated = f.lastModified();
				data.isDirectory = f.isDirectory();

				if (data.isDirectory) {
					dirList.add(data);
				} else {
					fileList.add(data);
				}
			}
			// 排序，目录在前，文件在后
			results.addAll(dirList);
			results.addAll(fileList);
		}
		return results;
	}

	/** 获取图片二级数据 */
	public List<AlbumData> getSecondLevelImages() {
		List<AlbumData> results = new ArrayList<AlbumData>();
		HashMap<String, AlbumData> foundAlbums = new HashMap<String, AlbumData>();
		boolean internal = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		Uri uri = internal ? MediaStore.Images.Media.INTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.MediaColumns.DATA, //
				MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, //
				MediaStore.Images.Media.TITLE, //
				MediaStore.Images.Media.DATE_TAKEN };
		String selection = MediaStore.Images.Media.BUCKET_ID + "=?";
		String[] selectionArgs = new String[] { this.bucketId };
		Cursor cursor = mResolver.query(uri, projection, selection, selectionArgs, null);
		if (cursor != null) {
			cursor.moveToPosition(-1);

			int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			int bucketIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
			int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
			int updatedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

			while (cursor.moveToNext()) {
				AlbumData data = new AlbumData(this.mContext);
				int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
				data.isDirectory = false;
				data.bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
				data.fileType = Image;
				data.path = cursor.getString(dataIndex);
				data.thumbnailUrl = getImageThembnailUrl(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
				data.title = cursor.getString(nameIndex);
				data.updated = cursor.getLong(updatedIndex);
				data.count = data.count + 1;
				foundAlbums.put(id + "", data);
			}
			cursor.close();
		}
		results = new ArrayList<AlbumData>(foundAlbums.values());
		return results;
	}

	/** 获取视频二级数据 */
	public List<AlbumData> getSecondLevelVideos() {
		List<AlbumData> results = new ArrayList<AlbumData>();
		HashMap<String, AlbumData> foundAlbums = new HashMap<String, AlbumData>();
		boolean internal = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		Uri uri = internal ? MediaStore.Video.Media.INTERNAL_CONTENT_URI : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.MediaColumns.DATA, //
				MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID, //
				MediaStore.Video.Media.TITLE, //
				MediaStore.Video.Media.DATE_TAKEN };
		String selection = MediaStore.Video.Media.BUCKET_ID + "=?";
		String[] selectionArgs = new String[] { this.bucketId };
		Cursor cursor = mResolver.query(uri, projection, selection, selectionArgs, null);
		if (cursor != null) {
			cursor.moveToPosition(-1);

			int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			int bucketIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
			int nameIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
			int updatedIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);

			while (cursor.moveToNext()) {
				AlbumData data = new AlbumData(this.mContext);
				int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
				data.isDirectory = false;
				data.fileType = Video;
				// File file = new File(cursor.getString(dataIndex));
				// data.path = file.getParent();//文件的父目录路径
				data.bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
				data.path = cursor.getString(dataIndex);//
				data.title = cursor.getString(nameIndex);
				data.thumbnailUrl = getVideoThembnailUrl(cursor.getColumnIndex(MediaStore.Video.Media._ID));// 缩略图
				data.updated = cursor.getLong(updatedIndex);
				data.count = data.count + 1;
				foundAlbums.put(id + "", data);
			}
			cursor.close();
		}
		results = new ArrayList<AlbumData>(foundAlbums.values());
		return results;
	}

	/** 获取音乐二级数据 */
	public List<AlbumData> getSecondLevelMusics() {
		List<AlbumData> results = new ArrayList<AlbumData>();
		HashMap<String, AlbumData> foundAlbums = new HashMap<String, AlbumData>();
		boolean internal = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		Uri uri = internal ? MediaStore.Audio.Media.INTERNAL_CONTENT_URI : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.MediaColumns.DATA, //
				MediaStore.Audio.Media.ALBUM_ID, //
				MediaStore.Audio.Media._ID, MediaStore.Audio.AudioColumns.IS_MUSIC,
				// MediaStore.Images.Media.BUCKET_DISPLAY_NAME, //
				MediaStore.Audio.Media.DATE_MODIFIED };
		String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " = '1'";
		Cursor cursor = mResolver.query(uri, projection, selection, null, null);
		if (cursor != null) {
			cursor.moveToPosition(-1);

			int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			int index = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
			// int nameIndex =
			// cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
			int updatedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
			int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

			String parentPath = this.isDirectory ? this.path : (new File(this.path)).getParent();
			while (cursor.moveToNext()) {
				String path = "";
				File file = new File(cursor.getString(dataIndex));
				path = file.getParent();// 文件的父目录路径

				if (path.equals(parentPath)) {// 该目录下的文件
					AlbumData data = new AlbumData(this.mContext);
					data.isDirectory = false;
					// data.id = id;//ID
					int id = cursor.getInt(index);
					data.fileType = Music;
					data.path = cursor.getString(dataIndex);// 路径
					data.thumbnailUrl = getAudioThembnailUrl(cursor.getInt(albumIndex));// 缩略图
					data.title = file.getName();
					data.updated = cursor.getLong(updatedIndex);
					data.count = data.count + 1;
					foundAlbums.put(id + "", data);
				}
			}
			cursor.close();
		}
		results = new ArrayList<AlbumData>(foundAlbums.values());
		return results;
	}

	/** 获取图片缩略图 */
	public String getImageThembnailUrl(int id) {
		String[] thumbColumns = new String[] { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };
		String selection = MediaStore.Images.Thumbnails.IMAGE_ID + "=?";
		String[] selectionArgs = new String[] { id + "" };
		Cursor thumbCursor = mResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
		if (thumbCursor.moveToFirst()) {
			String ret = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
			thumbCursor.close();
			return ret;
		}
		thumbCursor.close();
		return "";
	}

//	/** 获取同类型的一级目录数据，返回上级时调用 */
//	public List<AlbumData> listParentDirectorys() {
//		switch (this.fileType) {
//		case Video:
//			return Browser.findVideoAlbums(mContext);
//		case Music:
//			return Browser.findMusicAlbums(mContext);
//		case Image:
//			return Browser.findImageAlbums(mContext);
//		case File:// 返回文件上级目录数据
//			return findParentFileAlbums();
//		}
//		return null;
//	}

	/** 获取文件上级数据 */
	public List<AlbumData> findParentFileAlbums() {
		List<AlbumData> results = new ArrayList<AlbumData>();
		File dir = null;
		if (EnvironmentUtils.isRootPath(path)) {
			dir = new File(this.path);
		} else {
			dir = (new File(this.path)).getParentFile();
		}
		if (dir != null) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files) {
					AlbumData data = new AlbumData(this.mContext);
					data.id = f.getPath();
					data.path = f.getPath();
					data.fileType = File;
					data.title = f.getName();
					data.thumbnailUrl = f.getPath();
					data.updated = f.lastModified();
					data.isDirectory = f.isDirectory();
					results.add(data);
				}
			}
		}
		return results;
	}

	/** 获取当前目录下所有同类型数据,用于传递到播放器 */
	public List<String> listSameDirectoryFiles() {
		switch (this.fileType) {
		case Video:
			return findVideoFiles();
		case Music:
			return findMusicFiles();
		case Image:
			return findImageFiles();
		case File:
			return findfileFiles();
		}
		return null;
	}

	/** 返回当前文件在List中的索引 */
	public int getItemIndex(List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(this.path)) {
				return i;
			}
		}
		return -1;
	}

	private List<String> findVideoFiles() {
		return toStringList(getSecondLevelVideos());
	}

	private List<String> findMusicFiles() {
		return toStringList(getSecondLevelMusics());
	}

	private List<String> findImageFiles() {
		return toStringList(getSecondLevelImages());
	}

	private List<String> findfileFiles() {
		List<String> imgs = new ArrayList<String>();
		String p = "";
		String path = this.path;
		// 如果path是文件,扫描上级目录;如果是目录，扫描该目录
		File file = new File(path);
		if (file != null && file.isFile()) {
			p = file.getParent();
		} else if (file != null && file.isDirectory()) {
			p = path;
		} else {
			return null;
		}
		// 获取目录下所有文件
		file = new File(p);
		File[] files = file.listFiles();
		String name;
		// 过滤资源类型
		for (File f : files) {
			name = f.getName();
			// 文件扩展名
			String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
			switch (this.fileType) {
			case Video:
				if (end.equals("mp4") || end.equals("rmvb") || end.equals("rm") || end.equals("mpg") || end.equals("avi") || end.equals("mpeg")) {
					imgs.add(f.getAbsolutePath());
				}
				break;
			case Music:
				if (end.equals("mp3") || end.equals("wav") || end.equals("ogg") || end.equals("midi") || end.equals("wma")) {
					imgs.add(f.getAbsolutePath());
				}
				break;
			case Image:
				if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
					imgs.add(f.getAbsolutePath());
				}
				break;
			case File:
				imgs.add(f.getAbsolutePath());
				break;
			}
		}
		// 返回结果
		return imgs;
	}

	/** AlbumData_List to String_List */
	public List<String> toStringList(List<AlbumData> target) {
		List<String> results = new ArrayList<String>();
		for (AlbumData ad : target) {
			results.add(ad.path);
		}
		return results;
	}

	/** 获取视频 缩略图 */
	public String getVideoThembnailUrl(int id) {
		String[] thumbColumns = new String[] { MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID };
		String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
		String[] selectionArgs = new String[] { id + "" };
		Cursor thumbCursor = mResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
		if (thumbCursor.moveToFirst()) {
			String ret = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
			thumbCursor.close();
			return ret;
		}
		thumbCursor.close();
		return "";
	}

	/** 获取音乐缩略图 */
	private String getAudioThembnailUrl(int id) {
		String[] thumbColumns = new String[] {
				// MediaStore.Audio.Albums.ALBUM_ID,
				MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART };
		String selection = MediaStore.Audio.Albums._ID + "=?";
		String[] selectionArgs = new String[] { id + "" };
		Cursor thumbCursor = mResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
		if (thumbCursor.moveToFirst()) {
			String ret = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			thumbCursor.close();
			return ret;
		}
		thumbCursor.close();
		return "";
	}

}
