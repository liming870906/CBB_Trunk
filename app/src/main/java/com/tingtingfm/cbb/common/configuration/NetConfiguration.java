package com.tingtingfm.cbb.common.configuration;

import com.tingtingfm.cbb.common.log.TTLog;

/**
 * Created by think on 2016/12/19.
 */

class NetConfiguration {
    private enum EnumDevPlatform {
        PF_DEV,
        PF_TEST,
        PF_RELEASE
    }

    //配置当前应用环境地址
    private final static EnumDevPlatform epf = EnumDevPlatform.PF_TEST;

    static String SERVERURL;

    static String H5_URL;

    // API地址
    private final static String SERVER_URL_DEV = "https://api-cloud-dev.tingtingfm.com";// 开发环境
    private final static String SERVER_URL_TEST = "https://api-cloud-test.tingtingfm.com";// 内网测试环境
    private final static String SERVER_URL_RELEASE = "https://api-cloud.xuntingyun.com";// 外网正式环境

    //h5地址
    private final static String H5_URL_DEV = "https://mobile-cloud-dev.tingtingfm.com";// 开发环境
    private final static String H5_URL_TEST = "https://mobile-cloud-test.tingtingfm.com";// 内网测试环境
    private final static String H5_URL_RELEASE = "https://mobile-cloud.xuntingyun.com";// 外网正式环境

    //初始化平台对应请求接口
    static {
        switch (epf) {
            case PF_DEV:
                SERVERURL = SERVER_URL_DEV;
                H5_URL = H5_URL_DEV;
                TTLog.enableLogging();
                break;
            case PF_TEST:
                SERVERURL = SERVER_URL_TEST;
                H5_URL = H5_URL_TEST;
                TTLog.enableLogging();
                break;
            case PF_RELEASE:
            default:
                SERVERURL = SERVER_URL_RELEASE;
                H5_URL = H5_URL_RELEASE;
                TTLog.disableLogging();
                break;
        }
    }
}
