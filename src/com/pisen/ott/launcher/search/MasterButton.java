package com.pisen.ott.launcher.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Button;

import com.pisen.ott.launcher.widget.IDetailContent;
import com.pisen.ott.launcher.widget.IMasterTitle;

public class MasterButton extends Button implements IMasterTitle {

	private IDetailContent gridView;
	
	public MasterButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestChildFocus() {
		this.requestFocus();
	}

	@Override
	public void setDetailContent(IDetailContent detailContent) {
		this.gridView = detailContent;

	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean result = super.dispatchKeyEvent(event);
//		lasFocusedView = getFocusedChild();

		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() != KeyEvent.ACTION_UP) {
			if (gridView != null && gridView.hasData()) {
				gridView.requestChildFocus();
				return true;
			}
		}

		return result;
	}

}
