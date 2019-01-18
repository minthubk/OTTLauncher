package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;

/**
 * 浏览图标动画控件
 * 
 * @author yangyp
 */
public class FileBrowserIconView extends FrameLayout {

	static int TranslationXY = 5;
	private FrameLayout iconLayout;
	private ImageView imgBg2;
	private ImageView imgBg1;
	private ImageView imgIcon;
	private TextView txtName;
	private TextView txtCount;

	public FileBrowserIconView(Context context) {
		this(context, null);
	}

	public FileBrowserIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		setClipToPadding(false);
		View.inflate(context, R.layout.file_browser_icon, this);

		TranslationXY = getResources().getDimensionPixelSize(R.dimen.local_player_icon_border);
		iconLayout = (FrameLayout) findViewById(R.id.iconLayout);
		imgBg2 = (ImageView) findViewById(R.id.imgBg2);
		imgBg1 = (ImageView) findViewById(R.id.imgBg1);
		imgIcon = (ImageView) findViewById(R.id.imgIcon);
		txtName = (TextView) findViewById(R.id.txtName);
		txtCount = (TextView) findViewById(R.id.txtCount);

		startNotSelectedAmim();
	}

	@Override
	public void getHitRect(Rect outRect) {
		super.getHitRect(outRect);
		outRect.left = iconLayout.getLeft();
		outRect.top = iconLayout.getTop();
		outRect.right = iconLayout.getRight();
		outRect.bottom = iconLayout.getBottom();
	}

	public void startNotSelectedAmim() {
		imgIcon.setBackgroundResource(R.drawable.local_image_border);
		ViewPropertyAnimator animator = imgIcon.animate();
		animator.scaleX(1f);
		animator.scaleY(1f);
		animator.setDuration(50);
		animator.start();

		animator = imgBg1.animate();
		animator.scaleX(1f);
		animator.scaleY(1f);
		animator.translationXBy(-TranslationXY);
		// animator.translationYBy(translationXY);
		animator.setDuration(50);
		animator.start();

		animator = imgBg2.animate();
		animator.scaleX(1f);
		animator.scaleY(1f);
		animator.translationXBy(-TranslationXY * 2);
		// animator.translationYBy(translationXY * 2);
		animator.setDuration(50);
		animator.start();
		setMargin(txtCount,40,40);
		
	}

	public void startSelectedAmim() {
		imgIcon.setBackgroundResource(R.drawable.local_image_border_sel);
		ViewPropertyAnimator animator = imgIcon.animate();
		animator.scaleX(1.2f);
		animator.scaleY(1.2f);
		animator.setDuration(100);
		animator.start();

		animator = imgBg1.animate();
		animator.scaleX(1.1f);
		animator.scaleY(1.2f);
		animator.translationX(0);
		animator.setDuration(100);
		animator.start();

		animator = imgBg2.animate();
		animator.scaleX(1f);
		animator.scaleY(1.2f);
		animator.translationX(0);
		animator.setDuration(100);
		animator.start();
		
		setMargin(txtCount,20,12);
	}

	public void setIconBound(int width, int height) {
		imgIcon.getLayoutParams().width = width;
		imgIcon.getLayoutParams().height = height;
		imgBg1.getLayoutParams().width = width;
		imgBg1.getLayoutParams().height = height;
		imgBg2.getLayoutParams().width = width;
		imgBg2.getLayoutParams().height = height;
	}

	public void setIconText(String name, int count) {
		txtName.setText(name);
		txtCount.setText(String.valueOf(count));
	}

	public void setIconImageBitmap(Bitmap bm) {
		imgIcon.setImageBitmap(bm);
	}

	public void setIconImageBitmapRes(int resId) {
		Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
		setIconImageBitmap(bm);
	}

	public void setMargin(TextView tv,int top,int right){
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, 1); 
		lp.setMargins(0, top, right, 0); 
		lp.gravity = Gravity.RIGHT;
		tv.setLayoutParams(lp);
	}
}
