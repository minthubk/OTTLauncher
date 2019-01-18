package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.izy.util.StringUtils;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.appmanage.AppInfo;

/**
 * 我的应用
 * @author Liuhc
 * @version 1.0 2015年4月17日 下午2:37:55
 */
public class AppManageItemView extends FrameLayout implements OnClickListener {

	private TextView txtName;
	private LinearLayout controlLayout;
	private Button btnInstall;
	private ImageView imgIcon;
	private AppInfo gridViewItem;
	private Context context;

	private final RectF roundRect = new RectF();
    private float rect_adius = 5;
    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();
    
	public AppManageItemView(Context context) {
		this(context, null);
		this.context = context;
		init();
	}

	public AppManageItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
		View.inflate(context, R.layout.icon_reflect_download_search, this);
		txtName = (TextView) findViewById(R.id.txtName);
		controlLayout = (LinearLayout) findViewById(R.id.controlLayout);
		btnInstall = (Button) findViewById(R.id.btnInstall);
		imgIcon = (ImageView) findViewById(R.id.imgIcon);
		btnInstall.setOnClickListener(this);
		setDrawbleLeft(btnInstall);
		((LinearLayout) findViewById(R.id.controlLayoutItem)).setBackgroundResource(R.drawable.uninstall_red);
	}

	private void init() {
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //
        zonePaint.setAntiAlias(true);
        zonePaint.setColor(Color.WHITE);
        //
        float density = getResources().getDisplayMetrics().density;
        rect_adius = rect_adius * density;
    }
	
	public void setAppIcon(Drawable drawable){
		imgIcon.setImageDrawable(drawable);
	}
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = getWidth();
        int h = getHeight();
        roundRect.set(0, 0, w, h);
    }
 
    @Override
    public void draw(Canvas canvas) {
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
        super.draw(canvas);
        canvas.restore();
    }
    
	public AppInfo getItem(){
		return gridViewItem;
	}
	
	public void setName(String name) {
		txtName.setText(name);
		if (StringUtils.isEmpty(name)) {
			txtName.setVisibility(View.GONE);
		}else{
			txtName.setVisibility(View.VISIBLE);
		}
	}

	public void nextClick(AppInfo uiContent) {
		this.gridViewItem = uiContent;
		if (!isShowControl()) {
			showControlLayout();
		} else {
			// Install Click
			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
		}
	}

	public boolean isShowControl() {
		return controlLayout.getVisibility() == View.VISIBLE;
	}

	public void showControlLayout() {
		if (!isShowControl()) {
			int id = getId();
			setNextFocusUpId(id);
			setNextFocusDownId(id);
			setNextFocusLeftId(id);
			setNextFocusRightId(id);
			controlLayout.setVisibility(View.VISIBLE);
			btnInstall.setVisibility(View.VISIBLE);
			btnInstall.setText("卸载");
			txtName.setVisibility(View.INVISIBLE);
		}
	}

	public void hideControlLayout() {
		if (isShowControl()) {
			setNextFocusUpId(View.NO_ID);
			setNextFocusDownId(View.NO_ID);
			setNextFocusLeftId(View.NO_ID);
			setNextFocusRightId(View.NO_ID);
			controlLayout.setVisibility(View.GONE);
			txtName.setVisibility(View.VISIBLE);
		}
	}

	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (isShowControl()) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				hideControlLayout();
				return true;
			}

			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return true;
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					if (btnInstall.getVisibility() == View.VISIBLE) {
						btnInstall.performClick();
					}
					return true;
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View v) {
		if (gridViewItem != null) {
			deleteApk(gridViewItem.getPkgName());
		}
	}
	
	/**
	 * 启动apk
	 */
	public void openApk(String packgename) {
		PackageManager pm = context.getPackageManager();
		Intent i = pm.getLaunchIntentForPackage(packgename);// 获取启动的包名
		if (i != null) {
			context.startActivity(i);
		}
	}

	/**
	 * 删除app
	 */
	public void deleteApk(String packgename) {
		Intent i = new Intent();
		Uri uri = Uri.parse("package:" + packgename);// 获取删除包名的URI
		i.setAction(Intent.ACTION_DELETE);// 设置我们要执行的卸载动作
		i.setData(uri);// 设置获取到的URI
		context.startActivity(i);
	}
	
	private void setDrawbleLeft(Button v){
		Resources res = getResources();
		Drawable d = res.getDrawable(R.drawable.app_statu_delete);
		d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
		v.setCompoundDrawables(d, null, null, null); //设置左图标
	}
}
