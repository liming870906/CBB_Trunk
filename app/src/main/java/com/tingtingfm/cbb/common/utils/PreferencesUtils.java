package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;
import java.util.Map.Entry;

public class PreferencesUtils {
    public static void putString(SharedPreferences sp, String key, String value) {
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(SharedPreferences sp, String key) {
        return sp.getString(key, "");
    }

    public static void putArrays(SharedPreferences sp, Map<String, String> values) {
        Editor editor = sp.edit();
        for (Entry<String, String> entry : values.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.commit();
    }

    public static void putInt(SharedPreferences sp, String key, int value) {
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(SharedPreferences sp, String key, int defaultvalue) {
        return sp.getInt(key, defaultvalue);
    }

    public static void putLong(SharedPreferences sp, String key, long value) {
        Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(SharedPreferences sp, String key, long defaultvalue) {
        return sp.getLong(key, defaultvalue);
    }

    public static void putBoolean(SharedPreferences sp, String key, boolean value) {
        Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(SharedPreferences sp, String key, boolean defaultvalue) {
        return sp.getBoolean(key, defaultvalue);
    }

    public static void deletePreferrencesForSetting(Context context, String xmlName, int albumId) {
        SharedPreferences settings = context.getSharedPreferences(xmlName, 0);
        SharedPreferences.Editor setEditor = settings.edit();
        setEditor.remove(albumId + "");
        setEditor.commit();
    }

    public static void clearPreferrencesForSetting(Context context, String xmlName) {
        SharedPreferences settings = context.getSharedPreferences(xmlName, 0);
        settings.edit().clear().commit();
    }
}
