package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pisen.ott.launcher.R;
/**GridView的Item对象，实现了获取图片位置Rect方法，暂未使用*/
public class BrowserItemView extends LinearLayout {

	public BrowserItemView(Context context) {
		super(context);
	}

	public BrowserItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void getHitRect(Rect outRect) {
		super.getHitRect(outRect);
		ImageView iv = (ImageView) findViewById(R.id.imgPhoto);
		// outRect.top += getPaddingTop();
		outRect.bottom -= (getHeight() - iv.getHeight() + 10);
		outRect.left += 10;
		outRect.right -= 10;
	}

}
