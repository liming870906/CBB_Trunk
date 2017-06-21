package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.tingtingfm.cbb.TTApplication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DeviceUtils {
    private static DisplayMetrics dm;
    private static TelephonyManager telephonyManager = null;

    /**
     * 获取当前手机的密度
     *
     * @return
     */
    public static float getDensity() {
        if (dm == null) {
            dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) TTApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
        }

        return dm.density;
    }

    /**
     * 获取系统版本 DeviceUtils.getSDKVersion()<BR>
     *
     * @return string
     */
    public static String getAndroidSDKVersion() {
        return android.os.Build.VERSION.SDK;
    }

    /**
     * 获取操作系统的版本号
     *
     * @return String 系统版本号
     */
    public static String getSysRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return String 手机型号
     */
    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机品牌
     *
     * @return String 手机品牌
     */
    public static String getBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 是否为开发者 调试模式
     * return boolean true为调试模式
     */
    public static boolean enableAdb() {
        int adb_enabled = Settings.Secure.getInt(TTApplication.getAppContext().getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        return adb_enabled > 0;
    }

    /**
     * 读取手机串号 IMEI
     *
     * @return String 手机串号
     */
    public static String getTelephoneSerialNum() {
//        int permission = ContextCompat.checkSelfPermission(TTApplication.getAppContext(), Manifest.permission.READ_PHONE_STATE);
//        if (permission == PackageManager.PERMISSION_GRANTED) {
            if (telephonyManager == null) {
                telephonyManager = (TelephonyManager) TTApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            }

            if (telephonyManager != null) {
                return telephonyManager.getDeviceId();
            }
//        }
        return "";
    }

    public static String getDeviceId() {
        try {
            String temp = getTelephoneSerialNum();
            if (TextUtils.isEmpty(temp)) {
                temp = getLocalMacAddress();
                if (!TextUtils.isEmpty(temp)) {
                    temp = encryption(temp);
                }
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    static String encryption(String plainText) {
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset] & 0xff;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }

    /**
     * 获取手机MAC地址
     *
     * @return
     */
    static String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) TTApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
}
