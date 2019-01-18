package com.pisen.ott.launcher.localplayer.video;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.izy.widget.BaseListAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.utils.DateUtils;

/**
 * 视频adapter
 */
public class VideoAdapter extends BaseListAdapter<VideoInfo> {
	int playPosition = -1;
	//int prePlayPositon = -1;
    private Context ctx;

    public VideoAdapter(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		Holder holder;
		if (view == null) {
			holder = new Holder();
			view = LayoutInflater.from(ctx).inflate(R.layout.videoplayer_videolist_item, null);
			holder.imgPlayIcon = (ImageView) view.findViewById(R.id.imageHead);
			holder.txtVideoNo = (TextView) view.findViewById(R.id.txtvideoid);
			holder.txtVideoName = (TextView) view.findViewById(R.id.txt_videoname);
			holder.txtVideoDuration = (TextView) view.findViewById(R.id.txt_videoTime);
			view.setTag(holder);
		} else {
			holder = (Holder) view.getTag();
		}
		holder.txtVideoNo.setText((position+1)+".");
		holder.txtVideoName.setText(getItem(position).getName());
		holder.txtVideoDuration.setText(DateUtils.generateTime(getItem(position).getDuration()));
		
		if (playPosition == position) {
			holder.txtVideoNo.setVisibility(View.GONE);
			holder.imgPlayIcon.setVisibility(View.VISIBLE);
			holder.txtVideoName.setTextColor(ctx.getResources().getColor(R.color.white));	
			holder.txtVideoDuration.setTextColor(ctx.getResources().getColorStateList(R.color.white));
		} else {
			holder.imgPlayIcon.setVisibility(View.GONE);
			holder.txtVideoNo.setVisibility(View.VISIBLE);		
			holder.txtVideoNo.setTextColor(ctx.getResources().getColorStateList(R.color.lightblue));
			holder.txtVideoName.setTextColor(ctx.getResources().getColor(R.color.blacklight));
			holder.txtVideoDuration.setTextColor(ctx.getResources().getColorStateList(R.color.gray));
		}
		
		if (position % 2 == 0) {
			view.setBackgroundResource(R.color.graywhite);
		}else{
			view.setBackgroundResource(R.color.white);
		}
		return view;
	}

	class Holder {
		ImageView imgPlayIcon;
		TextView  txtVideoNo,txtVideoName,txtVideoDuration;
	}
    
	/**
	 * 刷新
	 * @param playPosition
	 */
	public void playViewPosition(int position) {
		//prePlayPositon = playPosition;
		playPosition = position;
		notifyDataSetChanged();
	}

	public int selectPosition = -1;
	public int lastPosition = -1;
	public boolean isLostFocus = false;
	public boolean isGetFocus = false;

	public void setLostFocus() {
		this.isLostFocus = true;
	}

	public void setGetFocus(boolean gainFocus) {
		this.isGetFocus = gainFocus;
	}
	
//	@Override
//	public void onItemSelected(AdapterView<?> parent, View view, int position,
//			long id) {
//	}
//
//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//	}

	public void setTextViewColor(View txtVideoNo,View txtVideoName,View txtVideoDuration,int colors){
			((TextView)txtVideoNo).setTextColor(colors);
			((TextView)txtVideoName).setTextColor(colors);
			((TextView)txtVideoDuration).setTextColor(colors);
	}
}
