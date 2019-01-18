package com.pisen.ott.launcher.localplayer.music;

import java.util.List;

import com.pisen.ott.launcher.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.izy.widget.BaseListAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 音乐列表的adapter
 * 
 */
public class MusicAdapter extends BaseListAdapter<MusicInifo>{
	
	int playPosition = -1;
	int prePlayPosition = -1;
	public AnimationDrawable animationControl;
	public Context ctx;
	//当前播放状态是否暂停
	public boolean isStop = false;
	
	public MusicAdapter(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Holder holder;
		if (view == null) {
			holder = new Holder();
			view = LayoutInflater.from(ctx).inflate(R.layout.musicplayer_musiclist_item, null);
			holder.headImageview = (ImageView) view.findViewById(R.id.imageHead);
			holder.txtName = (TextView) view.findViewById(R.id.txt_musicname);
			holder.txtSize = (TextView) view.findViewById(R.id.txt_musicTime);
			holder.txtsongid = (TextView) view.findViewById(R.id.txtsongid);
			view.setTag(holder);
		} else {
			holder = (Holder) view.getTag();
		}
		
		MusicInifo info = getItem(position);
		holder.txtsongid.setText((position+1)+".");
		holder.txtName.setText(info.getName());
		holder.txtSize.setText(getStrTime(info.getDuration()));

		if (playPosition == position) {
			holder.txtsongid.setVisibility(View.GONE);
			holder.headImageview.setVisibility(View.VISIBLE);
			if (isGetFocus && position == selectPosition) {
				holder.txtName.setTextColor(ctx.getResources().getColor(R.color.blacklight));
			} else {
				holder.txtName.setTextColor(ctx.getResources().getColor(R.color.lightblue));
			}
			animationControl = (AnimationDrawable) holder.headImageview.getBackground();
			holder.headImageview.setImageDrawable(null);
			animationControl.start();
			if (isStop) {
				animationControl.stop();
			}
		} else {
			holder.headImageview.setVisibility(View.GONE);
			holder.txtsongid.setVisibility(View.VISIBLE);
			if (isGetFocus && position == selectPosition) {
				isGetFocus = false;
			} 
			holder.txtName.setTextColor(ctx.getResources().getColor(R.color.blacklight));
			AnimationDrawable animation = (AnimationDrawable) holder.headImageview.getBackground();
			animation.stop();
		}
		
		if (position % 2 == 0) {
			view.setBackgroundResource(R.color.graywhite);
		}else{
			view.setBackgroundResource(R.color.white);
		}
		return view;
	}
	
	class Holder {
		ImageView headImageview;
		TextView txtsongid, txtName, txtSize;
	}

	public void stopAnimator(boolean isStop) {
		this.isStop = isStop;
		if (animationControl != null) {
			if (isStop)
				animationControl.stop();
			else
				animationControl.start();
		}
	}
	
	/**
	 * 格式化时间
	 * @param dura
	 * @return
	 */
	private String getStrTime(long dura) {
		long duration = dura;
		int minute = (int) (duration / 1000 / 60);
		int sec = (int) ((duration / 1000) - minute * 60);
		String minutestr = "" + minute;
		String secstr = "" + sec;
		if (minute < 10)
			minutestr = "0" + minutestr;
		if (sec < 10)
			secstr = "0" + secstr;
		return minutestr + ":" + secstr;
	}


	/**
	 * 切换音乐刷新
	 * @param playPosition
	 */
	public void playView(int playPosition) {
		prePlayPosition = this.playPosition;
		this.playPosition = playPosition;
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

}