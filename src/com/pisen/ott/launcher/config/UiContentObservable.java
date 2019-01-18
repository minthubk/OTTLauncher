package com.pisen.ott.launcher.config;

import android.database.Observable;

import com.pisen.ott.launcher.config.UiContentObservable.UiContentObserver;
import com.pisen.ott.launcher.service.UiVersionInfo.UiContent;

public class UiContentObservable extends Observable<UiContentObserver> {

	public void notifyChanged(UiContent obj) {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				if (obj != null) {
					mObservers.get(i).onChangedContent(obj);
				}
			}
		}
	}

	public interface UiContentObserver {

		/**
		 * 更新内容
		 */
		void onChangedContent(UiContent obj);
	}
}
