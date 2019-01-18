package com.pisen.ott.launcher.localplayer;


import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 资源浏览视图
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:11:42
 */
public abstract class BaseLocalPalyerPagerView extends LocalPalyerPagerViewBase implements OnItemSelectedListener{
	public View lastSelectedView;
	public int curPos=-1;
	
	public BaseLocalPalyerPagerView(Context context) {
		super(context);
	}
	@Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
			if (lastSelectedView != null) {
				setSelection(curPos);
			}
        } else {
        	// GridView失去焦点，清除选中项的背景并缩小,清除Selection
			View view = getSelectedView();
			if (view instanceof FileBrowserIconView) {
				FileBrowserIconView iconView = (FileBrowserIconView)view;
				iconView.startNotSelectedAmim();
				setSelection(-1);
			}		
        }
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// 前一个选中的Item,还原未选中状态的背景，触发缩小动画
		if (lastSelectedView != null&&lastSelectedView!=view) {
			if (lastSelectedView instanceof FileBrowserIconView) {
				FileBrowserIconView iconView = (FileBrowserIconView)lastSelectedView;
				iconView.startNotSelectedAmim();
			}			
		}
		
		// 选中的Item,设置选中状态的图片背景,触发放大动画
		if (view != null) {
			if (view instanceof FileBrowserIconView) {
				FileBrowserIconView iconView = (FileBrowserIconView)view;
				iconView.startSelectedAmim();
			}	
		}
		
		lastSelectedView = view;
		curPos = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}
