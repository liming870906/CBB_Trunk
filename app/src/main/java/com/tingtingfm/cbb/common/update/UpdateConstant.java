package com.tingtingfm.cbb.common.update;


import com.tingtingfm.cbb.common.configuration.UrlManager;

/**
 * 下载模块 常量
 *
 * @author lqsir
 */
public class UpdateConstant {
    // -----------MSGID------------------------------
    /**
     * 退出下载消息
     */
    public static final int DOWNLOAD_REMOVE_DOWNLOAD_MSGID = 0x100;
    public static final int DOWNLOAD_UPDATE_MSGID = DOWNLOAD_REMOVE_DOWNLOAD_MSGID + 1;
    /**
     * 显示toast
     */
    public static final int DOWNLOAD_SHOW_TOAST_MSGID = DOWNLOAD_REMOVE_DOWNLOAD_MSGID + 2;

    // -----------MSGID end------------------------------

    /**
     * apk存放目录
     */
    public static final String DOWNLOAD_FILE_PATH = "tingting/apktemp";

    /**
     * 升级地址
     */
    public static final String CHECKUPDATE_URL = UrlManager.VERSION_UPDATE;
}