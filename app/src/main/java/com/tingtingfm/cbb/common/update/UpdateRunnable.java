package com.tingtingfm.cbb.common.update;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.UpdateInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.response.UpdateVersionResponse;
import com.tingtingfm.cbb.ui.activity.ActivityStack;

import java.io.File;

/**
 * 检查更新的线程
 *
 * @author lqsir
 */
class UpdateRunnable implements Runnable {
    /**
     * 是否显示消息
     */
    private boolean isShow = false;
    private Handler handler;
    private Context context;

    public UpdateRunnable(boolean isShow, Handler handler) {
        this.context = ActivityStack.getInstance().getStackTopClassName();
        this.isShow = isShow;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            RequestEntity entity = new RequestEntity(UrlManager.VERSION_UPDATE);
            HttpRequestHelper.post(entity, new BaseRequestCallback<UpdateVersionResponse>() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(UpdateVersionResponse response) {
                    if (response != null && response.getData() != null
                            && response.getData().getNewVersion() != null
                            && !TextUtils.isEmpty(response.getData().getNewVersion().getUrl())) {
                        TTApplication application = (TTApplication) TTApplication.getAppContext();
                        if (application.isUpdating()) {
                            showToast(R.string.updateing, isShow);
                        } else {
                            dispathUpdateData(response);
                        }
                    } else {
                        deleteUpdateFile(); // 删掉之前下载文件
                        if (isShow) {
                            showToast(R.string.newestversion, isShow);
                        }
                    }
                }

                @Override
                public void onFail(int code, String errorMessage) {

                }

                @Override
                public void onCancel() {

                }
            });
            Looper.loop();
        } catch (Exception e) {

        }
    }


    /**
     * 处理更新数据
     *
     * @param updateData
     */
    private void dispathUpdateData(UpdateVersionResponse updateData) {
        // 判断data数据是否存在，存在说明有更新数据，反之，无更新数据
        UpdateInfo updateInfo = updateData.getData().getNewVersion();

        if (null != updateInfo && !TextUtils.isEmpty(updateInfo.getUrl())) {
            if (AppUtils.getVersionName().equals(updateInfo.getVersion())) {
                showToast(R.string.newestversion, isShow);
                return;
            }

            //强制安装，每次都弹提示
            if (updateInfo.getForce() == 1) {
                sendMessage(UpdateConstant.DOWNLOAD_UPDATE_MSGID, buildBundle(updateInfo));
            } else {
                // 非强制安装，一天弹提示一次
                String updateTime = TimeUtils.getTimeForSpecialFormat(TimeUtils.TimeFormat.TimeFormat2);
                if (!isShow) {// 非当天
                    if (!isEqualToday(updateTime)) {
                        PreferencesConfiguration.setSValues(Constants.UPDATEDATA, updateTime);
                        sendMessage(UpdateConstant.DOWNLOAD_UPDATE_MSGID, buildBundle(updateInfo));
                    }
                } else {
                    sendMessage(UpdateConstant.DOWNLOAD_UPDATE_MSGID, buildBundle(updateInfo));
                }
            }
        }
    }

    private Bundle buildBundle(UpdateInfo updateData) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("object", updateData);
        return bundle;
    }

    /**
     * 如果不需要更新把更新文件目录清空
     *
     * @DATE 2014年1月9日 15:46:11
     */
    private void deleteUpdateFile() {
        File file = openDownLoadFile(AppUtils.getAppName() + "_" + AppUtils.getVersionName() + ".apk");
        if (file.exists()) {
            file.delete();
        }

        file = openDownLoadFile(AppUtils.getAppName() + "_" + AppUtils.getVersionName() + ".temp");
        if (file.exists()) {
            file.delete();
        }
    }

    private final File openDownLoadFile(String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(UpdateConstant.DOWNLOAD_FILE_PATH), filename);
        return file;
    }

    public void sendMessage(int what, String data) {
        Bundle bundle = new Bundle();
        bundle.putString("value", data);
        sendMessage(what, 0, 0, bundle);
    }

    public void sendMessage(int what, Bundle bundle) {
        sendMessage(what, 0, 0, bundle);
    }

    public void sendMessage(int what, int arg1, int arg2, Bundle data) {
        if (handler != null) {
            Message message = handler.obtainMessage(what, arg1, arg2);
            message.setData(data);
            handler.sendMessage(message);
        }
    }

    private final void showToast(int resId, boolean need) {
        if (need) {
            sendMessage(UpdateConstant.DOWNLOAD_SHOW_TOAST_MSGID, context.getString(resId));
        }
    }

    /**
     * 日期是否是今天
     * @return
     */
    boolean isEqualToday(String date) {
        return PreferencesConfiguration.getSValues(Constants.UPDATEDATA).equals(date);
    }
}
