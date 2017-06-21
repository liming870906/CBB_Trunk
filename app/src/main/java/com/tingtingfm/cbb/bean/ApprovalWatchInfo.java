package com.tingtingfm.cbb.bean;

/**
 * Created by admin on 2017/4/20.
 */

public class ApprovalWatchInfo {
    private String error; //（空字符串认为数据正常，非空认为异常数据）
    private WatchInfo watchInfo;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public WatchInfo getWatchInfo() {
        return watchInfo;
    }

    public void setWatchInfo(WatchInfo watchInfo) {
        this.watchInfo = watchInfo;
    }
}
