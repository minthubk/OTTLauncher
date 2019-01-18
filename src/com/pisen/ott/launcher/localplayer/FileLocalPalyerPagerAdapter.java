package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;

/**
 * 一级浏览适配器
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:10:56
 */
public class FileLocalPalyerPagerAdapter extends LocalPalyerPagerAdapter {

	public FileLocalPalyerPagerAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(Context context, AlbumData item, ViewGroup parent) {
		return View.inflate(context, R.layout.file_browser_item_usb, null);
	}

	@Override
	public void bindView(final View view, Context context, final AlbumData item) {
		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			view.setTag(holder);
			holder.txtName = (TextView) view.findViewById(R.id.txtName);
			holder.imgPhoto=(ImageView) view.findViewById(R.id.imgPhoto);
		}
        if(null!=item.local){
        	holder.txtName.setText(item.local);
        	holder.imgPhoto.setBackgroundResource(R.drawable.local_dir_normal);
        	holder.isUsb = false; 
        }else{
        	holder.txtName.setText(item.title);
        	holder.imgPhoto.setBackgroundResource(R.drawable.local_usb_normal);
        	holder.isUsb = true; 
        }
	}

	static class ViewHolder {
		ImageView imgPhoto;
		TextView txtName;
		boolean isUsb;
	}

}
