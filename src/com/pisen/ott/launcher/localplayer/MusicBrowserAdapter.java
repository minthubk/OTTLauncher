package com.pisen.ott.launcher.localplayer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.izy.widget.BaseListAdapter;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.localplayer.BrowserView.ViewMode;

/**
 * 音乐二级适配器
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:10:56
 */
public class MusicBrowserAdapter extends BaseListAdapter<AlbumData> {

	public interface OnItemClickListener {
		void onItemClick(int position, View view, AlbumData item);
	}

	Context mContext;
	private ViewMode mode;
	private OnItemClickListener itemClickListener;
	private AlbumData curItem;

	public MusicBrowserAdapter(Context context,  AlbumData item) {
		this.mContext = context;
		this.curItem = item;
		//addAll(item.listFile());
		addAll(new ArrayList<AlbumData>());//设置空数据
		//异步加载数据
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				setData((List<AlbumData>)msg.obj);
				((MediaBrowserActivity) mContext).animView.cancel();
			}
		};
		if(curItem!=null&&curItem.fileType==AlbumData.Music){
			(new Thread(){
				 @Override
				public void run() {
					 ((MediaBrowserActivity) mContext).animView.show();
					 Message msg = new Message();
					 msg.obj = curItem.listFile();
					 msg.setTarget(handler);
					 handler.sendMessage(msg);
				}
			}).start();
		}
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

		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.file_browser_music_item, null);
			convertView.setTag(holder = new ViewHolder());
			holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
			holder.imgPhoto = (ImageView) convertView.findViewById(R.id.imgPhoto);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final AlbumData item = getItem(position);
		holder.txtName.setText(item.title);
		// 设置缩略图

		if (item.fileType == AlbumData.Music) {// 音乐
			if (item.isDirectory) {// 目录获取第一个Video的缩略图
				holder.imgPhoto.setBackgroundResource(R.drawable.local_album_border);
			} else {// 文件
				holder.imgPhoto.setBackgroundResource(R.drawable.local_image_border);
			}
			if ("".equals(item.thumbnailUrl) || null == item.thumbnailUrl) {// 无缩略图,设置默认音乐图片
				holder.imgPhoto.setImageResource(R.drawable.local_default_music);

			} else {// 有缩略图，加载缩略图
				Bitmap bitmap = BitmapFactory.decodeFile(item.thumbnailUrl);
				if(null!=bitmap){
					holder.imgPhoto.setImageBitmap(bitmap);
				}else{
					holder.imgPhoto.setImageResource(R.drawable.local_default_music);
				}
			}
		}

		return convertView;
	}

	static class ViewHolder {
		TextView txtName;
		ImageView imgPhoto;
		TextView txtCount;
		ImageView imgArrows;
	}
}
