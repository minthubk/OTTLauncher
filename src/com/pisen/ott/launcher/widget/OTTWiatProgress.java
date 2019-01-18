package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.pisen.ott.launcher.R;

/**
 * 公用等待进度圈
 * 
 * @author Liuhc
 * @version 1.0 2015年4月30日 下午3:51:05
 */
public class OTTWiatProgress extends ImageView {

	private Animation operatingAnim;

	public OTTWiatProgress(Context context) {
		super(context);
		setDefaultImage();
	}

	public OTTWiatProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultImage();
	}

	public OTTWiatProgress(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDefaultImage();
	}

	private void setDefaultImage() {
		if (getDrawable() == null) {
			setImageResource(R.drawable.public_waiting);
		}
	}

	public void cancel() {
		Log.i("testMsg", this.hashCode() + "cancel");
		clearAnimation();
		/*if (operatingAnim != null) {
			operatingAnim.cancel();
		}*/
		setVisibility(View.GONE);
	}

	public void show() {
		setVisibility(View.VISIBLE);
		Log.i("testMsg", this.hashCode() + "show");
		operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotation_wait);
		// LinearInterpolator lin = new LinearInterpolator();
		// operatingAnim.setInterpolator(lin);
		startAnimation(operatingAnim);
	}
	
	public boolean isShown(){
		return getVisibility() == View.VISIBLE;
	}
}
