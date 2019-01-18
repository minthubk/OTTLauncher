package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.pisen.ott.launcher.R;

/**
 * 倒影抽象类
 * 
 * @author yangyp
 */
public abstract class IconReflectView extends FrameLayout implements IReflect {

	private int reflectHeight;
	private int reflectGap = 4; // 原始图片和反射图片中间的间距

	private View contentView;
	private boolean reflectEnabled = true; // 是否启用倒影

	public IconReflectView(Context context) {
		super(context);
		initViews(context);
	}

	public IconReflectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IconReflectView);
		final int N = a.getIndexCount();
		for (int i = 0; i < N; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.IconReflectView_iconBackground:
				Drawable iconBackground = a.getDrawable(R.styleable.IconReflectView_iconBackground);
				if (iconBackground != null) {
					setIconBackground(iconBackground);
				}
				break;
			case R.styleable.IconReflectView_iconWidth:
				int iconWidth = a.getDimensionPixelSize(R.styleable.IconReflectView_iconWidth, LayoutParams.MATCH_PARENT);
				if (iconWidth > 0) {
					contentView.getLayoutParams().width = iconWidth;
				}
				break;
			case R.styleable.IconReflectView_iconHeight:
				int iconHeight = a.getDimensionPixelSize(R.styleable.IconReflectView_iconHeight, LayoutParams.MATCH_PARENT);
				if (iconHeight > 0) {
					contentView.getLayoutParams().height = iconHeight;
				}
				break;
			case R.styleable.IconReflectView_iconText:
				String iconText = a.getString(R.styleable.IconReflectView_iconText);
				if (iconText != null) {
					setIconText(iconText);
				}
				break;
			case R.styleable.IconReflectView_iconImage:
				Drawable iconImage = a.getDrawable(R.styleable.IconReflectView_iconImage);
				if (iconImage != null) {
					BitmapDrawable bmDrawable = (BitmapDrawable) iconImage;
					setIconImageBitmap(bmDrawable.getBitmap());
				}
				break;
			default:
				initStyledAttributes(a, attr);
				break;
			}
		}
		a.recycle();
	}

	/**
	 * 初始化相关属性
	 * 
	 * @param a
	 * @param attr
	 */
	protected void initStyledAttributes(TypedArray a, int attr) {
	}

	private void initViews(Context context) {
		setFocusable(true);
		setWillNotDraw(false);
		contentView = onInflateView(context);
		addView(contentView);
		reflectHeight = getPaddingBottom();
		onViewCreated(context);
	}

	/**
	 * 初始化View布局文件
	 * 
	 * @param context
	 * @return
	 */
	public abstract View onInflateView(Context context);

	/**
	 * 初始化UI
	 */
	public void onViewCreated(Context context) {
	}

	/**
	 * 设置标题
	 * 
	 * @param name
	 */
	public void setIconText(String name) {
	}

	/**
	 * 设置图标背景
	 */
	public void setIconBackground(Drawable background) {
		contentView.setBackground(background);
	}

	/**
	 * 设置图标
	 * 
	 * @param background
	 */
	public void setIconImageBitmap(Bitmap bm) {
	}

	/**
	 * 是否启动倒影
	 * 
	 * @param enabled
	 */
	public void setReflectEnabled(boolean enabled) {
		this.reflectEnabled = enabled;
	}

	@Override
	public void getHitRect(Rect outRect) {
		super.getHitRect(outRect);
		outRect.top += getPaddingTop();
		outRect.bottom -= reflectHeight;
		outRect.left += getPaddingLeft();
		outRect.right -= getPaddingRight();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (reflectEnabled && contentView != null && reflectHeight > 0) {
			contentView.destroyDrawingCache();
			contentView.setDrawingCacheEnabled(true);
			contentView.buildDrawingCache(true);
			Bitmap originalImage = contentView.getDrawingCache(true);
			if (originalImage != null) {
				Bitmap refBitmap = createReflectImage(originalImage, reflectHeight);
				canvas.drawBitmap(refBitmap, getPaddingLeft(), getHeight() - reflectHeight + reflectGap, null);
			}
		}
	}

	/**
	 * 创建一个倒影图，不包含原图
	 * 
	 * @param originalImage
	 *            原图
	 * @param reflectHeight
	 *            倒影高度
	 * @return
	 */
	private static Bitmap createReflectImage(Bitmap srcBitmap, int reflectionHeight) {
		int srcWidth = srcBitmap.getWidth();
		int srcHeight = srcBitmap.getHeight();

		// The matrix
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		// The reflection bitmap, width is same with original's
		Bitmap reflectionBitmap = Bitmap.createBitmap(srcBitmap, 0, srcHeight - reflectionHeight, srcWidth, reflectionHeight, matrix, false);
		Canvas canvas = new Canvas(reflectionBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		LinearGradient shader = new LinearGradient(0, 0, 0, reflectionBitmap.getHeight(), 0x60000000, Color.TRANSPARENT, TileMode.CLAMP); // TileMode.CLAMP
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		// Draw the linear shader.
		canvas.drawRect(0, 0, reflectionBitmap.getWidth(), reflectionBitmap.getHeight(), paint);
		return reflectionBitmap;
	}

}
