package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class LoopFaddingImageView extends ImageView {
	Animation alphaAnimation;
	public LoopFaddingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		 alphaAnimation= new AlphaAnimation(0.3f,0.15f);
		 alphaAnimation.setDuration(2000);
		 alphaAnimation.setRepeatCount(Animation.INFINITE);
		 alphaAnimation.setRepeatMode(Animation.REVERSE);
		 this.setAnimation(alphaAnimation);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		startAnim();
	}
	
	private void startAnim(){
		if(!alphaAnimation.hasStarted())
			alphaAnimation.startNow();
	}

}
