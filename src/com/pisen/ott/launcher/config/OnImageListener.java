package com.pisen.ott.launcher.config;

import android.graphics.Bitmap;

import com.pisen.ott.launcher.config.ImageAsyncTask.TaskResult;

public abstract class OnImageListener {

	public void onComplete(TaskResult result) {
		if (result.isSuccess()) {
			onSuccess(result.bitmap, result.isCache);
		} else {
			onError(result.err);
		}
	}

	public void onSuccess(Bitmap response, boolean isCache) {
		
	}

	public void onError(Throwable err) {

	}

}
