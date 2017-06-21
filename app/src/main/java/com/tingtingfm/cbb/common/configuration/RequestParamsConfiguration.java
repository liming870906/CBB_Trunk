package com.tingtingfm.cbb.common.configuration;

import android.text.TextUtils;

import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.DeviceUtils;

/**
 * Created by think on 2016/12/19.
 * 服务端接口请求参数配置
 * session_key
 * client
 * version
 */

public class RequestParamsConfiguration {
    static String session_key;
    static String client;
    static String version;


    public static void clearSessionKey() {
        session_key = "";
    }

    public static String getSession_key() {
        if (!TextUtils.isEmpty(session_key)) {
            return session_key;
        }

        session_key = PreferencesConfiguration.getSValues(Constants.SESSION_KEY);

        return session_key;
    }

    public static String getClient() {
        if (!TextUtils.isEmpty(client)) {
            return client;
        }

        client = "android_" + DeviceUtils.getDeviceId();

        return client;
    }

    public static String getVersion() {
        if (!TextUtils.isEmpty(version)) {
            return version;
        }

        version = "android_" + AppUtils.getVersionName();

        return version;
    }
}
