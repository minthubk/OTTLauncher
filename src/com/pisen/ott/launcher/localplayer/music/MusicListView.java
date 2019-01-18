package com.pisen.ott.launcher.localplayer.music;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pisen.ott.launcher.R;
import com.pisen.ott.launcher.widget.IDetailContent;
import com.pisen.ott.launcher.widget.IMasterTitle;

/**
 * 记忆光标位置ListView
 * @author Liuhc
 * @version 1.0 2015年3月6日 下午2:58:23
 */
public class MusicListView extends ListView implements IDetailContent{

	private View lastSelectedView;
	private int lastSelectedItemPos = -1;
	private int lastSelectedY = 0;
	private IMasterTitle menuLayout;
	public boolean isInit = false;

	public MusicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		setClipToPadding(false);
		setVerticalFadingEdgeEnabled(false);
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	public void setMasterTitle(IMasterTitle menuLayout) {
		menuLayout.setDetailContent(this);
		this.menuLayout = menuLayout;
	}

	@Override
	public boolean hasData() {
		return getCount() > 0;
	}

	@Override
	public void requestChildFocus() {
		if (hasData()) {
			setFocusable(true);
			requestFocus();
			setSelectionFromTop(lastSelectedItemPos, lastSelectedY);
			((BaseAdapter) getAdapter()).notifyDataSetChanged();
		}
	}


	/**
	 * 设置选中效果
	 * @param v
	 * @param position
	 */
	public void setFocusedViewColor(View v){
		TextView txtName = (TextView) v.findViewById(R.id.txt_musicname);
		TextView txtTime = (TextView) v.findViewById(R.id.txt_musicTime);
		TextView txtsongid = (TextView) v.findViewById(R.id.txtsongid);
		if (txtsongid != null) {
			txtsongid.setTextColor(getResources().getColorStateList(R.color.white));
		}
		if (txtName != null) {
			txtName.setTextColor(getResources().getColorStateList(R.color.white));
		}
		if (txtTime != null) {
			txtTime.setTextColor(getResources().getColorStateList(R.color.white));
		}
		v.setBackground(getResources().getDrawable(R.drawable.list_focus_bg));
		lastSelectedView = v;
		lastSelectedItemPos = getSelectedItemPosition();
		lastSelectedY = v.getTop();
	}
	
	/**
	 * 还原
	 * @param v
	 * @param position
	 */
	private void resetViewColor(View v){
		ImageView headImageview = (ImageView) v.findViewById(R.id.imageHead);
		TextView txtName = (TextView) v.findViewById(R.id.txt_musicname);
		TextView txtTime = (TextView) v.findViewById(R.id.txt_musicTime);
		TextView txtsongid = (TextView) v.findViewById(R.id.txtsongid);
		if (txtsongid != null) {
			txtsongid.setTextColor(getResources().getColorStateList(R.color.lightblue));
		}
		if (txtTime != null) {
			txtTime.setTextColor(getResources().getColorStateList(R.color.gray));
		}
		if (headImageview.isShown()) {
			txtName.setTextColor(getResources().getColorStateList(R.color.lightblue));
		}else{
			txtName.setTextColor(getResources().getColorStateList(R.color.blacklight));
		}
		
		if (getPositionForView(v) % 2 == 0) {
			v.setBackgroundResource(R.color.graywhite);
		}else{
			v.setBackgroundResource(R.color.white);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int count = getChildCount();
		if ( count > 0) {
			View selectedView = getSelectedView();
			if (selectedView != null) {
				if (isFocused()) {
					setFocusedViewColor(selectedView);
				}
			}
		}
	}

//	@Override
//	public void setSelection(int position) {
//		lastSelectedItemPos = position;
//		super.setSelection(position);
//	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		this.lastSelectedItemPos = position;
		this.lastSelectedY = y;
		super.setSelectionFromTop(position, y);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
				int curItemPos = getSelectedItemPosition() + 1;
				lastSelectedItemPos = curItemPos;
				if (curItemPos == 1) {
					return true;
				}
			}
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
				int numCol = getCount();
				int curItemPos = getSelectedItemPosition() + 1;
				lastSelectedItemPos = curItemPos;
				if (numCol == curItemPos) {
					return true;
				}
			}
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && menuLayout != null) {
				lastSelectedItemPos = getSelectedItemPosition();
				menuLayout.requestChildFocus();
				setFocusable(false);
				View selectedView = getSelectedView();
				if (selectedView != null) {
					resetViewColor(selectedView);
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = super.onKeyDown(keyCode, event);
		View selectedView = getSelectedView();
		if (lastSelectedView == selectedView) {
			return result;
		}

		if (lastSelectedView != null) {
			resetViewColor(lastSelectedView);
		}
		
		if (selectedView != null) {
			lastSelectedView = selectedView;
			setFocusedViewColor(selectedView);
		}
		return result;
	}

}