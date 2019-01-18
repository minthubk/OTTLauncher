package com.pisen.ott.launcher.localplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pisen.ott.launcher.localplayer.AlbumData;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 资源浏览视图
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:11:42
 */
public class VideoLocalPalyerPagerView extends BaseLocalPalyerPagerView implements OnItemSelectedListener {

	public VideoLocalPalyerPagerView(Context context) {
		super(context);
		setNumColumns(4);
		setAdapter(new VideoLocalPalyerPagerAdapter(context));
		setOnItemSelectedListener(this);
		lastSelectedView = getChildAt(0);
		curPos = 0;
	}

//	@Override
//	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
//		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//		if(gainFocus){
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
		Uri uri = internal ? MediaStore.Video.Media.INTERNAL_CONTENT_URI : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.MediaColumns.DATA, //
				MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID, //
				MediaStore.Video.Media.BUCKET_DISPLAY_NAME, //
				MediaStore.Video.Media.DATE_TAKEN };
		String selection = MediaStore.MediaColumns.DATA + " like '/mnt/%' or " + MediaStore.MediaColumns.DATA + " like '/storage/%'";
		Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
		if (cursor != null) {
			cursor.moveToPosition(-1);

			int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			int bucketIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
			int nameIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
			int updatedIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);

			while (cursor.moveToNext()) {
				String id = getGroupId(uri, cursor.getString(bucketIndex));
				AlbumData data = foundAlbums.get(id);
				if (data == null) {
					data = new AlbumData(context);
					data.id = id;
					data.bucketId = cursor.getString(bucketIndex);
					data.isDirectory = true;
					data.fileType = AlbumData.Video;
					data.path = cursor.getString(dataIndex);// 第一个视频的路径
					data.title = cursor.getString(nameIndex);
					//data.thumbnailUrl = getVideoThembnailUrl(cursor.getColumnIndex(MediaStore.Video.Media._ID), context);// 缩略图
					data.thumbnailUrl="";
					foundAlbums.put(id, data);
				}
				data.updated = Math.max(data.updated, cursor.getLong(updatedIndex));
				data.count = data.count + 1;
			}

			cursor.close();
		}
		return new ArrayList<AlbumData>(foundAlbums.values());
	}

	/**
	 * 获取视频缩略图
	 * 
	 * @param id
	 * @param mContext
	 * @return
	 */
	public static String getVideoThembnailUrl(int id, Context mContext) {
		String[] thumbColumns = new String[] { MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID };
		String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
		String[] selectionArgs = new String[] { id + "" };
		Cursor thumbCursor = mContext.getContentResolver()
				.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
		if (thumbCursor.moveToFirst()) {
			String ret = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
			thumbCursor.close();
			return ret;
		}
		thumbCursor.close();
		return "";
	}

}
