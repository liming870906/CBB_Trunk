package com.tingtingfm.cbb.common.configuration;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.bean.PersonInfo;
import com.tingtingfm.cbb.common.utils.PreferencesUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 讯听云账号管理
 * Created by lqsir on 2017/3/3.
 */

public class AccoutConfiguration {

    /**
     * 设置登录帐号信息
     * @param loginInfo 登录帐号信息
     */
    public static void setLoginInfo(LoginInfo loginInfo) {
        putLoginInfo(loginInfo);
    }

    /**
     * 获取登录帐号信息
     * @return 登陆实体类对象
     */
    public static LoginInfo getLoginInfo() {
        LoginInfo loginInfo = new LoginInfo();
        SharedPreferences sp = PreferencesConfiguration.sp;
        String userId = PreferencesUtils.getString(sp, Constants.USER_ID);
        loginInfo.setUserid(TextUtils.isEmpty(userId) ? 0 :Integer.valueOf(userId));
        loginInfo.setRealname(PreferencesUtils.getString(sp, Constants.REAL_NAME));
        loginInfo.setFace_url(PreferencesUtils.getString(sp, Constants.FACE_URL));
        loginInfo.setRole(PreferencesUtils.getString(sp, Constants.ROLE));
        loginInfo.setDepartment(PreferencesUtils.getString(sp, Constants.DEPARTMENT));
        loginInfo.setSession_key(PreferencesUtils.getString(sp, Constants.SESSION_KEY));
        loginInfo.setAp_username(PreferencesUtils.getString(sp, Constants.AP_USERNAME));
        loginInfo.setEmail(PreferencesUtils.getString(sp, Constants.ACCOUT_EAMIL));
        loginInfo.setMobile(PreferencesUtils.getString(sp, Constants.ACCOUT_MOBILE));

        return loginInfo;
    }

    /**
     * 删除登录帐号信息
     */
    public static void removeLoginInfo(){
        RequestParamsConfiguration.clearSessionKey();
        putLoginInfo(null);
    }

    /**
     * 设置登录帐号信息
     * @param loginInfo
     */
    private static void putLoginInfo(LoginInfo loginInfo) {
        Map<String, String> values = new HashMap<String, String>();
        values.put(Constants.USER_ID,loginInfo == null ? "" : String.valueOf(loginInfo.getUserid()));
        values.put(Constants.REAL_NAME,loginInfo == null ? "" : loginInfo.getRealname());
        values.put(Constants.DEPARTMENT,loginInfo == null ? "" : loginInfo.getDepartment());
        values.put(Constants.FACE_URL,loginInfo == null ? "" : loginInfo.getFace_url());
        values.put(Constants.ROLE,loginInfo == null ? "" : loginInfo.getRole());
        values.put(Constants.AP_USERNAME,loginInfo == null ? "" : loginInfo.getAp_username());
        values.put(Constants.SESSION_KEY,loginInfo == null ? "" : loginInfo.getSession_key());
        PreferencesUtils.putArrays(PreferencesConfiguration.sp,values);
    }

    public static void updateAccoutInfo(PersonInfo info) {
        if (info == null) {
            return;
        }

        Map<String, String> values = new HashMap<String, String>();
        values.put(Constants.USER_ID, String.valueOf(info.getUserid()));
        values.put(Constants.REAL_NAME, info.getRealname());
        values.put(Constants.DEPARTMENT, info.getDepartment());
        values.put(Constants.FACE_URL, info.getFace_url());
        values.put(Constants.ROLE, info.getRole());
        values.put(Constants.AP_USERNAME, info.getAp_username());
        values.put(Constants.ACCOUT_MOBILE, info.getMobile());
        values.put(Constants.ACCOUT_EAMIL, info.getEmail());
        PreferencesUtils.putArrays(PreferencesConfiguration.sp, values);
    }

}
