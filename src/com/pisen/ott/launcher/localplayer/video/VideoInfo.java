package com.pisen.ott.launcher.localplayer.video;


/**
 * 视频对象
 */
public class VideoInfo {
	
	String name;
	String url;
	long   duration;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
}
