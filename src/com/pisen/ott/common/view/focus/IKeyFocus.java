package com.pisen.ott.common.view.focus;

import android.graphics.Canvas;
import android.view.View;

public interface IKeyFocus {

	/**
	 * 
	 * @param focus 
	 */
	void layout(View focus);

	void draw(Canvas canvas);
}
