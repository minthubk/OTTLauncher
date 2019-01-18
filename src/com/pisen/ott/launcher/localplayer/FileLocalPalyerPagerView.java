package com.pisen.ott.launcher.localplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.izy.util.URLUtils;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.pisen.ott.launcher.R;

/**
 * 资源浏览视图
 * 
 * @author yangyp
 * @version 1.0, 2015年1月20日 上午11:11:42
 */
public class FileLocalPalyerPagerView extends LocalPalyerPagerViewBase implements OnItemSelectedListener {

	private View lastSelectedView;
	private int curPos = -1;
	ViewPropertyAnimator animatorZoomOut;

	public FileLocalPalyerPagerView(Context context) {
		super(context);
		setNumColumns(4);
		setAdapter(new FileLocalPalyerPagerAdapter(context));
		setOnItemSelectedListener(this);		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (lastSelectedView == null) {
			setSelection(0);
			lastSelectedView = getChildAt(0);
			curPos = 0;
		}
	}

	@Override
	public List<AlbumData> findAlbums(Context context) {
		ArrayList<AlbumData> results = new ArrayList<AlbumData>();
		String[] externalSd = /*EnvironmentUtils.*/getExternalStorageDirectoryAll();
		for (String sd : externalSd) {
			AlbumData data = new AlbumData(getContext());
			data.id = sd;
			data.path = sd;
			data.fileType = AlbumData.File;
			if(sd.equals(externalSd[0])){//第一个盘为本地Rom
				data.local="本地";
			}
			data.title = URLUtils.getNameURI(sd);
			data.isDirectory = true;
			results.add(data);
		}
		return results;
	}
	
	public static String[] getExternalStorageDirectoryAll() {
		List<String> results = new ArrayList<String>();
		if (hasSDCard()) {
			results.add(Environment.getExternalStorageDirectory().getPath());
			File mountFile = new File("/proc/mounts");
			if (mountFile.exists()) {
				try {
					Scanner scanner = new Scanner(mountFile);
					while (scanner.hasNext()) {
						String line = scanner.nextLine().trim();
						if (line.startsWith("/dev/block/vold/")) {
							String[] lineElements = line.split(" ");
							String element = lineElements[1];
							if (!(element.equals("/mnt/sdcard"))) {
								File root = new File(element);
								if ((root.exists()) && (root.isDirectory()) && (root.canWrite())) {
									if(!results.contains(root.getPath()))
										results.add(root.getPath());
								}
							}
						}
					}
					scanner.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return ((String[]) results.toArray(new String[0]));
	}
	
	public static boolean hasSDCard() {
		return "mounted".equals(Environment.getExternalStorageState());
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			if (lastSelectedView != null) {
				if(((FileLocalPalyerPagerAdapter.ViewHolder)lastSelectedView.getTag()).isUsb){
					lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_usb_selected);
				}else{
					lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_dir_selected);
				}
				
				itemZoomOut(lastSelectedView.findViewById(R.id.imgPhoto));
			}
		}else{
			View view = getSelectedView();
			if (view != null) {
				if(((FileLocalPalyerPagerAdapter.ViewHolder)view.getTag()).isUsb){
					view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_usb_normal);
				}else{
					view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_dir_normal);
				}
				ViewPropertyAnimator animator1 = view.findViewById(R.id.imgPhoto).animate();
				animator1.scaleX(1f);
				animator1.scaleY(1f);
				animator1.setDuration(50);
				animator1.start();
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		
		// 前一个选中的Item,还原未选中状态的背景，触发缩小动画
		if (lastSelectedView != null && lastSelectedView != view) {
			itemZoomIn(lastSelectedView.findViewById(R.id.imgPhoto));
			//lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_usb_normal);
			if(((FileLocalPalyerPagerAdapter.ViewHolder)lastSelectedView.getTag()).isUsb){
				lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_usb_normal);
			}else{
				lastSelectedView.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_dir_normal);
			}
			Log.i("testMsg", "onItemSelected Zoomin ");
		}
		// 选中的Item,设置选中状态的图片背景,触发放大动画
		if (view != null) {
			//view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_usb_selected);
			if(((FileLocalPalyerPagerAdapter.ViewHolder)view.getTag()).isUsb){
				view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_usb_selected);
			}else{
				view.findViewById(R.id.imgPhoto).setBackgroundResource(R.drawable.local_dir_selected);
			}
			itemZoomOut(view.findViewById(R.id.imgPhoto));
			lastSelectedView = view;
			curPos = position;
			Log.i("testMsg", "onItemSelected ZoomOut ");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	/**
	 * 放大
	 * 
	 * @param selectedView
	 */
	private void itemZoomOut(View selectedView) {
		ViewPropertyAnimator animator = selectedView.animate();

		animator  = selectedView.animate();
		animator.scaleX(1.1f);
		animator.scaleY(1.1f);
		animator.setDuration(250);
		animator.start();
		
//		if(animatorZoomOut!=null){
//			animatorZoomOut.cancel();
//		}
//		animatorZoomOut  = selectedView.animate();
//		animatorZoomOut.scaleX(1.1f);
//		animatorZoomOut.scaleY(1.1f);
//		animatorZoomOut.setDuration(250);
//		animatorZoomOut.start();
	}

	/**
	 * 选中项缩小
	 * 
	 * @param selectedView
	 */
	private void itemZoomIn(View selectedView) {
		Log.i("testMsg", "Zoom in");
		ViewPropertyAnimator animator = selectedView.animate();
		animator.scaleX(1f);
		animator.scaleY(1f);
		animator.setDuration(50);
		animator.start();
	}

}
