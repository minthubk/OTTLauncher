package com.pisen.ott.launcher;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.izy.content.IntentUtils;
import android.izy.util.StringUtils;
import android.izy.widget.BaseListAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemFocusChangeListener;
import com.pisen.ott.launcher.base.DefaultActivity;
import com.pisen.ott.launcher.base.OttBaseActivity;
import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.config.OnImageListener;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.widget.CategoryMenuLayout;
import com.pisen.ott.launcher.widget.DownloadItemView;
import com.pisen.ott.launcher.widget.GridScaleView;
import com.pisen.ott.launcher.widget.OTTWiatProgress;

/**
 * 三级界面模板
 * @author Liuhc
 * @version 1.0 2015年4月15日 下午3:34:24
 */
public class CategoryDetailActivity extends DefaultActivity implements OnItemClickListener {

	private static List<UiContent> categoryList;
	private static String selectedViewCode;
	private static String contentViewCode;
	
	private TextView txtCategoryTitle;
	private TextView txtAppNum;
	private CategoryMenuLayout menuLayout;
	private GridScaleView grdContent;
	private AppsAdapter itemAdapter;
	private OTTWiatProgress progressLoading;

	public static void start(OttBaseActivity context, List<UiContent> categoryList
			, String selectedViewCode, String viewCode) {
		CategoryDetailActivity.categoryList = categoryList;
		CategoryDetailActivity.selectedViewCode = selectedViewCode;
		CategoryDetailActivity.contentViewCode = viewCode;
		context.startActivity(new Intent(context, CategoryDetailActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_category_detail);
		txtCategoryTitle = (TextView) findViewById(R.id.txtCategoryTitle);
		txtAppNum = (TextView) findViewById(R.id.txtAppNum);
		menuLayout = (CategoryMenuLayout) findViewById(R.id.menuLayout);
		progressLoading = (OTTWiatProgress) findViewById(R.id.progressLoading);
		grdContent = (GridScaleView) findViewById(R.id.grdContent);
		grdContent.setAdapter(itemAdapter = new AppsAdapter(this));
		grdContent.setMasterTitle(menuLayout);
		grdContent.setOnItemClickListener(this);
		menuLayout.setOnItemFocusChangeListener(new OnItemFocusChangeListener() {
			@Override
			public void onItemFocusChanged(View v, boolean hasFocus) {
				if (hasFocus && menuLayout.hasNewChildFocus()) {
					itemAdapter.clear();
					progressLoading.show();
					grdContent.setVisibility(View.GONE);
					
					UiContent uiContent = (UiContent) v.getTag();
					if (uiContent.ChildContent != null) {
						txtAppNum.setText(uiContent.Name + "  |  " + uiContent.ChildContent.size());
						grdContent.setSelection(-1);
						itemAdapter.setData(uiContent.ChildContent);
						progressLoading.cancel();
						grdContent.setVisibility(View.VISIBLE);
						if (uiContent.ChildContent.isEmpty()) {
							menuLayout.requestChildFocus();
						}
					}
				}
			}
		});

		this.initMenuLayout();
		this.initGridData();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.initMenuLayout();
		this.initGridData();
	}

	@Override
	protected void onDestroy() {
		categoryList = null;
		selectedViewCode = null;
		contentViewCode = null;
		super.onDestroy();
	}
	
	private void initMenuLayout() {
		if (menuLayout != null) {
			menuLayout.removeAllViews();
		}
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int margin = getResources().getDimensionPixelSize(R.dimen.banner_category_layout_margin);
		lp.setMargins(margin, margin, margin, margin);
		ColorStateList whiteColor = getResources().getColorStateList(R.color.item_category_text);
		int padding_h = getResources().getDimensionPixelSize(R.dimen.banner_category_item_padding_horizontal);
		int padding_v = getResources().getDimensionPixelSize(R.dimen.banner_category_item_padding_vertical);
		for (final UiContent uiContent : categoryList) {
			Button newButton = new Button(this);
			newButton.setPadding(padding_h, padding_v, padding_h, padding_v);
			newButton.setTag(uiContent);
			newButton.setText(uiContent.Name);
			newButton.setTextSize(24);
			newButton.setTextColor(whiteColor);
			newButton.setBackground(null);
			menuLayout.addView(newButton, lp);
			if (selectedViewCode.equals(uiContent.DisplayCode)) {
				menuLayout.setChildFocusedView(newButton);
				txtAppNum.setText(uiContent.Name + "  |  " + uiContent.ChildContent.size());
			}
		}
	}

	private void initGridData() {
		progressLoading.show();
		grdContent.setVisibility(View.GONE);
		itemAdapter.clear();
		if (CategoryDetailActivity.contentViewCode.equals("BLOCK_APP")) {
			txtCategoryTitle.setText("应用");
		}else{
			txtCategoryTitle.setText("游戏");
		}
		
		if (selectedViewCode != null) {
			for (UiContent uiContent : categoryList) {
				if (selectedViewCode.equals(uiContent.DisplayCode)) {
					// 设置内容列表
					if (uiContent.ChildContent != null) {
						itemAdapter.setData(uiContent.ChildContent);
						progressLoading.cancel();
						grdContent.clearFocus();
						grdContent.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	private class AppsAdapter extends BaseListAdapter<UiContent> {

		Context context;

		public AppsAdapter(Context context) {
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				if (CategoryDetailActivity.contentViewCode.equals("BLOCK_APP")) {
					convertView = View.inflate(context, R.layout.launcher_category_detail_app_item, null);
				}else{
					convertView = View.inflate(context, R.layout.launcher_category_detail_grid_item, null);
				}
			}

			final DownloadItemView itemDownload = (DownloadItemView) convertView.findViewById(R.id.itemDownload);
			UiContent item = getItem(position);
			itemDownload.setName(item.Name);
			String imgUrl = item.Image;
			if (!StringUtils.isEmpty(imgUrl)) {
				ImageLoader.loader(imgUrl, new OnImageListener() {
					@Override
					public void onSuccess(Bitmap response, boolean isCache) {
						itemDownload.setBackground(new BitmapDrawable(getResources(), response));
					}
					@Override
					public void onError(Throwable err) {
						super.onError(err);
						itemDownload.setBackgroundResource(R.drawable.home_bg_default);
					}
				});
			}

			return convertView;
		}
	}

	private DownloadItemView itemDownload;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		UiContent item = itemAdapter.getItem(position);
		if (IntentUtils.isInstalledApk(this, item.ApkFile)) {
			IntentUtils.startApk(this, item.ApkFile);
		} else {
			grdContent.lockItem();
			itemDownload = (DownloadItemView) view.findViewById(R.id.itemDownload);
			itemDownload.nextClick(item,grdContent);
		}
	}

	@Override
	public boolean onBackKeyEvent() {
		grdContent.unlockItem();
		if (itemDownload != null && itemDownload.isShowControl()) {
			itemDownload.cancelDownload();
			itemDownload.hideControlLayout();
			itemDownload = null;
			return true;
		}
		return super.onBackKeyEvent();
	}
	
	@Override
	protected void onResume() {
		if (grdContent.isLockItem()) {
			if (itemDownload != null) {
				if (itemDownload.checkInstalled()) {
					grdContent.unlockItem();
				}
			}
		}
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		if (itemDownload != null && itemDownload.isShowControl()) {
			itemDownload.cancelDownload();
			itemDownload.hideControlLayout();
			itemDownload = null;
		}
		super.onStop();
	}
}
