package com.pisen.ott.launcher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.izy.util.LogCat;
import android.izy.util.StringUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pisen.ott.common.view.GridFocusLayout;
import com.pisen.ott.common.view.focus.DefaultKeyFocus.OnItemClickListener;
import com.pisen.ott.launcher.base.NavigationActivity;
import com.pisen.ott.launcher.config.ImageLoader;
import com.pisen.ott.launcher.config.LauncherConfig;
import com.pisen.ott.launcher.config.OnImageListener;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;
import com.pisen.ott.launcher.widget.IDownloadItem;
import com.pisen.ott.launcher.widget.IconReflectView;

/**
 * 默认二级界面模板
 * 
 * @author yangyp
 * @version 1.0, 2015年1月7日 下午2:51:21
 */
public class AppRecommendActivity extends NavigationActivity implements OnItemClickListener {

	public static UiContent uiContent = null;
	private static List<UiContent> categoryList = new ArrayList<UiContent>();

	private RelativeLayout bannerLayout;
	private GridFocusLayout categoryLayout;
	private TextView txtBannerTitle;
	private int mParentID;
	
	public static void start(Context context, UiContent uiContent) {
		AppRecommendActivity.uiContent = uiContent;
		AppRecommendActivity.categoryList = LauncherApplication.getConfig().getBannerCategory(uiContent.ContentViewCode);
		context.startActivity(new Intent(context, AppRecommendActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppRecommendActivity.uiContent = null;
		AppRecommendActivity.categoryList = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLeftMenuEnable(false);
		setContentView(R.layout.launcher_app_recommend);
		txtBannerTitle = (TextView) findViewById(R.id.txtBannerTitle);
		bannerLayout = (RelativeLayout) findViewById(R.id.bannerLayout);
		categoryLayout = (GridFocusLayout) findViewById(R.id.categoryLayout);
		categoryLayout.setOnItemClickListener(this);

		this.setTitle();
		//默认显示倒影
		this.bindAppImage(/*!hasCategory()*/true,false);
		this.bindAppCategory();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getBottomMenuLayout().hideMenu();
		this.setTitle();
		this.bindAppImage(/*!hasCategory()*/true,true);
		this.bindAppCategory();
	}
	
	@Override
	protected void onResume() {
		if (downItem != null && downItem.isShowControl()) {
			if (downItem != null) {
				downItem.checkInstalled();
			}
		}
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		if (downItem != null && downItem.isShowControl()) {
			downItem.hideControlLayout();
			downItem = null;
		}
		super.onStop();
	}

	private void setTitle(){
		if (uiContent != null) {
			if (uiContent.ContentViewCode.equals("BLOCK_APP")) {
				txtBannerTitle.setText("应用市场");
			}else if (uiContent.ContentViewCode.equals("BLOCK_GAME")) {
				txtBannerTitle.setText("游戏大厅");
			}else if (uiContent.ContentViewCode.equals("BLOCK_SHOP")) {
				txtBannerTitle.setText("在线购物");
			}else if (uiContent.ContentViewCode.equals("BLOCK_EDU")) {
				txtBannerTitle.setText("在线教育");
			}else if (uiContent.ContentViewCode.equals("BLOCK_SOCIAL")) {
				txtBannerTitle.setText("聊吧");
			}else{
				txtBannerTitle.setText(uiContent.Name);
			}
		}
	}
	/**
	 * 是否有分类
	 * 
	 * @return
	 */
	public boolean hasCategory() {
		return !categoryList.isEmpty();
	}

	private void bindAppImage(boolean reflectEnabled,boolean isNeedInvalidate) {
		int count = bannerLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = bannerLayout.getChildAt(i);
			// 根据当前模版查询子级内容
			String viewCode = String.valueOf(child.getTag());
			LauncherConfig config = LauncherApplication.getConfig();
			final UiContent itemCount = config.getUiVersion().getBanner(uiContent.ContentViewCode, viewCode);

			if (itemCount == null) {
				return;
			}
			
			if (child instanceof IconReflectView) {
				IconReflectView iconView = (IconReflectView) child;
				if (viewCode.equals(getString(R.string.banner_0012)) 
						|| viewCode.equals(getString(R.string.banner_0111))
						|| viewCode.equals(getString(R.string.banner_1111))) {
					iconView.setIconText("");
				}else{
					if (!TextUtils.isEmpty(itemCount.Name)) {
//						iconView.setIconText(itemCount.Name);
						iconView.setIconText("");//图片上已有名字
					}
				}
				
				// 如果是分类就没有倒影
				if (reflectEnabled) {
					iconView.setReflectEnabled(true);
				}else{
					iconView.setReflectEnabled(false);
				}
				
				if (isNeedInvalidate) {
					iconView.invalidate();
				}
			}

			loadBannerImage(child, itemCount);
			child.setOnClickListener(onItemClick(itemCount));
		}
	}

	/**
	 * 根据控件tag和分类属性
	 * 
	 * @param view
	 * @param itemCount
	 */
	private void loadBannerImage(final View view, final UiContent itemCount) {
		if (itemCount != null) {
			System.out.println("使用:"+itemCount.Image);
			ImageLoader.loader(itemCount.Image, new OnImageListener() {
				@Override
				public void onSuccess(Bitmap response, boolean isCache) {
					if (view instanceof IconReflectView) {
						IconReflectView iconView = (IconReflectView) view;
						iconView.setIconBackground(new BitmapDrawable(view.getResources(), response));
					} else {
						view.setBackground(new BitmapDrawable(view.getResources(), response));
					}
				}
			});
		}
	}

	private OnClickListener onItemClick(final UiContent itemCount) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (itemCount.getStartType()) {
				case ContentView:
					if (itemCount.DisplayCode.equals("0111") && itemCount.ContentViewCode.startsWith("LIST")) {// 更多
						CategoryDetailActivity.start(AppRecommendActivity.this, categoryList, /* "c0001" */itemCount.DisplayCode, /*uiContent.ContentViewCode*/itemCount.ContentViewCode);
						return;
					}
					break;
				case App:
					if (itemCount.ApkFile != null&&v instanceof IDownloadItem) {
						downItem = (IDownloadItem) v;
						downItem.nextClick(itemCount,null);
					}
					break;
				case Bowser:
					Intent intent = new Intent(AppRecommendActivity.this, ProductInfoActivity.class);
					intent.putExtra(ProductInfoActivity.PARAM_KEY, itemCount.SelectedImage);
					startActivity(intent);
					break;
				default:
					LogCat.e("未知启动类型(%s)", uiContent.Type);
					break;
				}
			}
		};
	}

	private IDownloadItem downItem = null;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (downItem != null && downItem.isShowControl()) {
			return downItem.dispatchKeyEvent(event);
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 绑定分类
	 */
	protected void bindAppCategory() {
		if (categoryLayout != null) {
			categoryLayout.removeAllViews();
		}
		
		for (int i = 0, N = categoryList.size(); i < N; i++) {
			UiContent c = categoryList.get(i);
			if (mParentID <= 0) {
				mParentID = c.ParentID;
			}

			final TextView txtCategoryName = (TextView) addCategoryView(this, categoryLayout, i);
			txtCategoryName.setText(c.Name);
			txtCategoryName.setTag(c.DisplayCode);
			ImageLoader.loader(c.Image, new OnImageListener() {
				@Override
				public void onSuccess(Bitmap response, boolean isCache) {
					BitmapDrawable drawable = new BitmapDrawable(getResources(), response);
					drawable.setAlpha(100);
					txtCategoryName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
				}
			});
		}
	}

	/**
	 * 添加分类控件
	 * 
	 * @param context
	 * @param categoryLayout
	 * @param i
	 * @return
	 */
	private static View addCategoryView(Context context, GridFocusLayout categoryLayout, int i) {
		View view = View.inflate(context, R.layout.launcher_app_recommend_category_item, null);
		view.setId(R.layout.launcher_app_recommend_category_item + (i + Byte.MAX_VALUE));
		GridFocusLayout.LayoutParams lp = new GridFocusLayout.LayoutParams();
		int margin = context.getResources().getDimensionPixelSize(R.dimen.block_banner_margin);
		lp.setMargins(margin, margin, margin, margin);
		if ((i + 1) % categoryLayout.getColumnCount() == 0) {
			view.setNextFocusRightId(view.getId());
		}
		categoryLayout.addView(view, lp);
		return view;
	}

	public enum ContentViewCode {
		LIST_APP, LIST_DEFAULT, Unknown;

		public static ContentViewCode getContentViewCode(String contentViewCode) {
			try {
				return (ContentViewCode) Enum.valueOf(ContentViewCode.class, contentViewCode);
			} catch (IllegalArgumentException e) {
				return ContentViewCode.Unknown;
			}
		}
	}

	@Override
	public void onItemClick(View v) {
		String selectedViewCode = String.valueOf(v.getTag());
		// DetailCategoryActivity.start(this, categoryList, selectedViewCode);
		CategoryDetailActivity.start(this, categoryList, selectedViewCode,uiContent.ContentViewCode);
//		ContentViewCode viewCode = ContentViewCode.getContentViewCode(uiContent.ContentViewCode);
//		switch (viewCode) {
//		case LIST_APP:
//
//			break;
//		default:
//
//			break;
//		}
	}

	@Override
	public void onChangedContent(UiContent obj) {
		super.onChangedContent(obj);
		if (mParentID != obj.ParentID || obj.LayerLevel != 2) {
			return;
		}
		if (StringUtils.isEmpty(obj.DisplayCode) || StringUtils.isEmpty(obj.Image) || StringUtils.isEmpty(obj.SelectedImage)) {
			return;
		}

		System.out.println("obj.DisplayCode:"+obj.DisplayCode);
		if (obj.ContentViewCode == null) {
			for (int i = 0, N = bannerLayout.getChildCount(); i < N; i++) {
				final View child = bannerLayout.getChildAt(i);
				if (child.getTag() != null) {
					String tag = child.getTag().toString();
					if (obj.DisplayCode.equals(tag)) {
						System.out.println("obj.Image:"+obj.Image);
						refreshImage(child, obj.Image);
					}
				}
			}
		} else if (obj.ContentViewCode.startsWith("LIST")) {
			for (int i = 0, N = categoryLayout.getChildCount(); i < N; i++) {
				final View child = categoryLayout.getChildAt(i);
				if (child.getTag() != null) {
					String tag = child.getTag().toString();
					if (obj.DisplayCode.equals(tag)) {
						final TextView txtCategoryName = (TextView) child.findViewById(R.id.txtCategoryName);
						if (txtCategoryName != null) {
							ImageLoader.loader(obj.Image, new OnImageListener() {
								@Override
								public void onSuccess(Bitmap response, boolean isCache) {
									BitmapDrawable drawable = new BitmapDrawable(getResources(), response);
									drawable.setAlpha(100);
									txtCategoryName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
								}
							});
						}
					}
				}
			}
		}
	}
}
