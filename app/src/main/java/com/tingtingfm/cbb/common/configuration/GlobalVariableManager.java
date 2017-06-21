package com.tingtingfm.cbb.common.configuration;

/**
 * 全局变量管理
 * Created by lqsir on 2017/2/15.
 */

public class GlobalVariableManager {
    //扫描音频任务的标志，true 扫描中 false 扫描已完成（默认值）
    public static boolean isScanning = false;
    //素材标记 false加载素材，true素材正在加载中
    public static boolean isLoadMaterial = false;
    //设置非WIFI下100M上传开关
    public static boolean isOpen100 = true;

    public static final int MAXWIFIFILESIZE = 500 * 1024 * 1024;
    public static final int MAX4GFILESIZE = 100 * 1024 * 1024;
}
