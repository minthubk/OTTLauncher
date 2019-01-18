package com.pisen.ott.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;

import com.pisen.ott.launcher.base.OttBaseActivity;

public class ContentLayout extends FrameLayout {

	private OttBaseActivity baseActivity;
	private View lastAwayView; // 最后切换到菜单的View

	public ContentLayout(Context context) {
		super(context);
	}

	public ContentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContentLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setActivity(OttBaseActivity baseActivity) {
		this.baseActivity = baseActivity;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_MENU:
				lastAwayView = findFocus();
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if (!hasNextFocused(this, View.FOCUS_UP)) {
					lastAwayView = findFocus();
					if (baseActivity.showNotificationBar()) {
						//lastAwayView.clearFocus();
						return true;
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (!hasNextFocused(this, View.FOCUS_LEFT)) {
					lastAwayView = findFocus();
					if (baseActivity.showLeftMenu()) {
						//lastAwayView.clearFocus();
						return true;
					}
				}
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	public void requestChildFocus() {
		if (lastAwayView != null) {
			//lastAwayView.setFocusable(true);
			lastAwayView.requestFocus();
		}
	}

	/**
	 * 判断是否有下个焦点
	 * 
	 * @return
	 */
	public static boolean hasNextFocused(ViewGroup root, int direction) {
		View currentFocused = root.findFocus();
		if (currentFocused == root) {
			currentFocused = null;
		}

		boolean result = false;

		// 处理ListView GridView焦点
		if (currentFocused instanceof AbsListView) {
			if (currentFocused instanceof ListView) {
				AbsListView listView = (AbsListView) currentFocused;
				if (direction == View.FOCUS_UP) {
					result = listView.getSelectedItemPosition() - 1 >= 0;
				}
			} else if (currentFocused instanceof GridView) {
				GridView gridView = (GridView) currentFocused;
				if (direction == View.FOCUS_UP) {
					int selectedItemPos = gridView.getSelectedItemPosition();
					int numCol = gridView.getNumColumns();
					int rowNum = (selectedItemPos + 1) / numCol + (selectedItemPos + 1) % numCol > 0 ? 1 : 0;
					result = rowNum - 1 >= 1;
				}
			}
		}

		if (!result) {
			View nextFocused = FocusFinder.getInstance().findNextFocus(root, currentFocused, direction);
			result = nextFocused != null;
		}

		return result;
	}

}