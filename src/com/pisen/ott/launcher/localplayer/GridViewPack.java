package com.pisen.ott.launcher.localplayer;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

public class GridViewPack extends FrameLayout {
	
	public static int FRM_FILE_ID =32;
	public static int FRM_VIDEO_ID =33;
	public static int FRM_IMAGE_ID =34;
	public static int FRM_MUSIC_ID =35;

	private FileCategoryLayout menuLayout;
	private OTTWiatProgress animViewImage;
	private LocalPalyerPagerViewBase pageView;
	private TextView emptyText;

	public GridViewPack(Context context,FileCategoryLayout menuLayout, LocalPalyerPagerViewBase pageView,int id) {
		super(context);
		this.menuLayout= menuLayout;
		this.pageView = pageView;
		this.setId(id);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;

		LinearLayout emptyImage = (LinearLayout) inflate(context, R.layout.empty, null);
		emptyText = (TextView) emptyImage.findViewById(R.id.emptyText);
		pageView.setEmptyView(emptyImage);
		addView(pageView);

		addView(emptyImage);

		animViewImage = new OTTWiatProgress(context);
		animViewImage.setVisibility(View.GONE);
		animViewImage.setLayoutParams(lp);
		addView(animViewImage);

		// frmImage.setId(GridViewPack.FRM_IMAGE_ID);

	}
	
	public LocalPalyerPagerViewBase getPagerView(){
		return pageView;
	}

	public void setEmptyText(TextView emptyText) {
		this.emptyText = emptyText;
	}

	public TextView getEmptyText() {
		return emptyText;
	}

	void showAnim() {
		getEmptyText().setText("");
		animViewImage.show();
	}

	void cancelAnim() {
		getEmptyText().setText("内容为空");
		animViewImage.cancel();
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Rect r = new Rect();
		this.getHitRect(r);
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:
				
				FocusFinder.getInstance().findNextFocusFromRect((ViewGroup) this.getParent(), r, FOCUS_UP);
				if (FocusFinder.getInstance().findNextFocusFromRect((ViewGroup) this.getParent(), r, FOCUS_UP) == null && this.hasFocus()) {
					// 一级界面，根据browserType,设置向上切换到相应的一级导航
					Log.i("testMsg", this.getId()+"");
					View v = findFocus();
//					if (!(v instanceof LocalPalyerPagerViewBase)) {
//						return super.dispatchKeyEvent(event);
//					}

					if (v instanceof MusicLocalPalyerPagerView || (this.getId()==FRM_MUSIC_ID)) {
						v.setNextFocusUpId(menuLayout.getChildAt(3).getId());
					}

					if ((v instanceof VideoLocalPalyerPagerView) || (this.getId()==FRM_VIDEO_ID) ) {
						v.setNextFocusUpId(menuLayout.getChildAt(1).getId());
					}
					if (v instanceof ImageLocalPalyerPagerView || (this.getId()==FRM_IMAGE_ID)) {
						v.setNextFocusUpId(menuLayout.getChildAt(2).getId());
					}
					if (v instanceof FileLocalPalyerPagerView || (this.getId()==FRM_FILE_ID)) {
						v.setNextFocusUpId(menuLayout.getChildAt(0).getId());
					}

				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (FocusFinder.getInstance().findNextFocusFromRect((ViewGroup) this.getParent(), r, FOCUS_RIGHT) == null && this.hasFocus()) {
					// 一级界面,阻止音乐向右滚动
					View v = findFocus();
					if (!(v instanceof LocalPalyerPagerViewBase)) {
						return super.dispatchKeyEvent(event);
					}

					if (v instanceof MusicLocalPalyerPagerView) { //
						// v.setNextFocusRightId(menuLocalPlayer.getChildAt(3).getId());
						v.setNextFocusRightId(v.getId());
					}
				}
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

}
