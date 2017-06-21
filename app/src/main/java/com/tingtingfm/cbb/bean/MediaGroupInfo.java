package com.tingtingfm.cbb.bean;

import java.util.ArrayList;

/**
 * 多媒体分组信息
 * Created by liming on 17/2/16.
 */

public class MediaGroupInfo {
    //日期
    private String date;
    //多媒体集合
    private ArrayList<MediaInfo> mediaInfos;

    /**
     * 构造方法
     */
    public MediaGroupInfo() {
        this.mediaInfos = new ArrayList<>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<MediaInfo> getMediaInfos() {
        return mediaInfos;
    }

    /**
     * 添加多媒体信息
     * @param info
     */
    public void addMediaInfo(MediaInfo info){
        //添加多媒体信息到集合中
        mediaInfos.add(info);
    }

    public void setMediaInfos(ArrayList<MediaInfo> mediaInfos) {
        this.mediaInfos = mediaInfos;
    }
}
