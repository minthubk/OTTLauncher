package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pisen.ott.launcher.localplayer.AlbumData;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 资源浏览视图
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:11:42
 */
public class MusicLocalPalyerPagerView extends BaseLocalPalyerPagerView implements OnItemSelectedListener{
	
	public MusicLocalPalyerPagerView(Context context) {
		super(context);
		setNumColumns(5);
		setAdapter(new MusicLocalPalyerPagerAdapter(context));
		setOnItemSelectedListener(this);
		lastSelectedView = getChildAt(0);
		curPos=0;
	}
//	@Override
//    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
//        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//        if(gainFocus){
//			if (lastSelectedView != null) {
//				setSelection(curPos);
//			}
//        } else {
//        	// GridView失去焦点，清除选中项的背景并缩小,清除Selection
//			View view = getSelectedView();
//			if (view instanceof FileBrowserIconView) {
//				FileBrowserIconView iconView = (FileBrowserIconView)view;
//				iconView.startNotSelectedAmim();
//				setSelection(-1);
//			}		
//        }
//	}
//	
//	@Override
//	public void setAdapter(ListAdapter adapter) {
//		super.setAdapter(adapter);
//	}
//
//	@Override
//	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//		// 前一个选中的Item,还原未选中状态的背景，触发缩小动画
//		if (lastSelectedView != null&&lastSelectedView!=view) {
//			if (lastSelectedView instanceof FileBrowserIconView) {
//				FileBrowserIconView iconView = (FileBrowserIconView)lastSelectedView;
//				iconView.startNotSelectedAmim();
//			}			
//		}
//		
//		// 选中的Item,设置选中状态的图片背景,触发放大动画
//		if (view != null) {
//			if (view instanceof FileBrowserIconView) {
//				FileBrowserIconView iconView = (FileBrowserIconView)view;
//				iconView.startSelectedAmim();
//			}	
//		}
//		
//		lastSelectedView = view;
//		curPos = position;
//	}
//
//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//	}
	
	@Override
	public List<AlbumData> findAlbums(Context context) {
		HashMap<String, AlbumData> foundAlbums = new HashMap<String, AlbumData>();
		boolean internal = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		Uri uri = internal ? MediaStore.Audio.Media.INTERNAL_CONTENT_URI : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.MediaColumns.DATA, //
				MediaStore.Audio.Media.ALBUM_ID, 
				MediaStore.Audio.Media.DATE_MODIFIED };
		String selection = MediaStore.MediaColumns.DATA + " like '/mnt/%' or " + MediaStore.MediaColumns.DATA + " like '/storage/%'";
		Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
		if (cursor != null) {
			cursor.moveToPosition(-1);

			int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			int updatedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
			int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

			while (cursor.moveToNext()) {
				String path = "";
				File file = new File(cursor.getString(dataIndex));
				path = file.getParent();// 文件的父目录路径
				String id = getGroupId(uri, path);
				AlbumData data = foundAlbums.get(id);
				if (data == null) {
					data = new AlbumData(context);
					data.isDirectory = true;
					data.id = id;// ID
					data.fileType = AlbumData.Music;
					data.path = path;// 路径
					//data.thumbnailUrl = getAudioThembnailUrl(cursor.getInt(albumIndex), context);// 缩略图
					data.thumbnailUrl = "";
					data.title = file.getParentFile().getName();// 父目录的文件名
					foundAlbums.put(id, data);
				}
				// 如果图集还没有缩略图，刷新
				if (data.thumbnailUrl.equals("")) {
					data.thumbnailUrl = getAudioThembnailUrl(cursor.getInt(albumIndex), context);
				}
				data.updated = Math.max(data.updated, cursor.getLong(updatedIndex));
				data.count = data.count + 1;
			}
			cursor.close();
		}
		return new ArrayList<AlbumData>(foundAlbums.values());
	}

	/**
	 * 获取音频缩略图
	 * 
	 * @param id
	 * @param context
	 * @return
	 */
	private static String getAudioThembnailUrl(int id, Context context) {
		String[] thumbColumns = new String[] {
				// MediaStore.Audio.Albums.ALBUM_ID,
				MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART };
		String selection = MediaStore.Audio.Albums._ID + "=?";
		String[] selectionArgs = new String[] { id + "" };
		Cursor thumbCursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
		if (thumbCursor.moveToFirst()) {
			String ret = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			thumbCursor.close();
			return ret == null ? "" : ret;
		}
		thumbCursor.close();
		return "";
	}
}
