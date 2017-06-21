package com.tingtingfm.cbb.common.configuration;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.utils.PreferencesUtils;

/**
 * Created by think on 2016/12/21.
 */

public class PreferencesConfiguration {
    static SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TTApplication.getAppContext());

    /**
     * [Integer]根据给定的Key值得到对应的Values
     *
     * @param key 给定的Key
     * @return 对应的Values
     */
    public static int getIValues(String key) {

        return PreferencesUtils.getInt(sp, key, -1);
    }

    /**
     * 设定一组key-values
     *
     * @param key   指定的key
     * @param value 指定的values
     */
    public static void setIValues(String key, int value) {
        PreferencesUtils.putInt(sp, key, value);
    }

    /**
     * [String]根据给定的Key值得到对应的Values
     *
     * @param key 给定的Key
     * @return 对应的Values
     */
    public static String getSValues(String key) {
        return PreferencesUtils.getString(sp, key);
    }

    /**
     * 设定一组key-values
     *
     * @param key   指定的key
     * @param value 指定的values
     */
    public static void setSValues(String key, String value) {
        PreferencesUtils.putString(sp, key, value);
    }

    /**
     * [Boolean]根据给定的Key值得到对应的Values
     *
     * @param key 给定的Key
     * @return 对应的Values
     */
    public static boolean getBValues(String key) {
        return PreferencesUtils.getBoolean(sp, key, false);
    }

    /**
     * 设定一组key-values
     *
     * @param key   指定的key
     * @param value 指定的values
     */
    public static void setBValues(String key, boolean value) {
        PreferencesUtils.putBoolean(sp, key, value);
    }

    /**
     * [Long]根据给定的Key值得到对应的Values
     *
     * @param key 给定的Key
     * @return 对应的Values
     */
    public static long getLValues(String key) {
        return PreferencesUtils.getLong(sp, key, -1);
    }

    /**
     * 设定一组key-values
     *
     * @param key   指定的key
     * @param value 指定的values
     */
    public static void setLValues(String key, long value) {
        PreferencesUtils.putLong(sp, key, value);
    }
}
