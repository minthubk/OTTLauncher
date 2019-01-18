package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * 主界面块焦点切换动画
 * 
 * @author yangyp
 * @version 1.0, 2015年1月5日 下午2:34:14
 */
public class RecommendScrollView extends ScrollView implements OnBorderListener{

	public RecommendScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecommendScrollView(Context context) {
		super(context);
	}

	

	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(android.graphics.Rect rect) {
		if (getChildCount() == 0)
			return 0;

		int height = getHeight();
		int screentop = getScrollY();

		int fadingEdge = getVerticalFadingEdgeLength();

		// leave room for left fading edge as long as rect isn't at very left
		if (rect.left > 0) {
			screentop += fadingEdge;
		}

		int scrollYDelta = 0;

		int screenMiddle = screentop + height / 2;
		int nextFocusedMiddle = rect.left + rect.width() / 2;

		if (nextFocusedMiddle > screenMiddle) {
			scrollYDelta += (nextFocusedMiddle - screenMiddle);
		} else if (nextFocusedMiddle < screenMiddle) {
			scrollYDelta -= (screenMiddle - nextFocusedMiddle);
		}

		// doScrollAnim(rect);

		return scrollYDelta;
	}

	@Override
	public void onBottom() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTop() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
