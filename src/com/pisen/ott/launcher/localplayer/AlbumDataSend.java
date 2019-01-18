package com.pisen.ott.launcher.localplayer;

import java.io.Serializable;

public class AlbumDataSend implements Serializable{
	public String id;// id
	public String bucketId;
	public String path;// 路径
	public String title; // 相册名字
	public String thumbnailUrl; // 相册第一张图片
	public long updated; // 更新时间
	public int count; // 相册图片数量
	public boolean isDirectory; // 是否目录
	public int fileType;// 文件类型
	public String local;
	
	public AlbumDataSend(){
	}
}

