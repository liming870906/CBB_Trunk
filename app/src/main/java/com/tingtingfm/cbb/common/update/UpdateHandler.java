package com.tingtingfm.cbb.common.update;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.UpdateInfo;
import com.tingtingfm.cbb.common.dialog.TTAlertDialog;
import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.ToastUtils;
import com.tingtingfm.cbb.common.utils.UpdateUtils;
import com.tingtingfm.cbb.ui.activity.ActivityStack;

import java.io.File;
import java.lang.reflect.Field;

/**
 * 更新Handler，根据相关消息在UI线程下处理相应结果
 *
 * @author liqiang
 * @da2014年3月23日
 */
class UpdateHandler extends Handler {

    public UpdateHandler() {
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UpdateConstant.DOWNLOAD_REMOVE_DOWNLOAD_MSGID:
                showDialogForRemoveDownload();
                break;
            case UpdateConstant.DOWNLOAD_UPDATE_MSGID:
                showDialogForDownload(msg.getData());
                break;
            case UpdateConstant.DOWNLOAD_SHOW_TOAST_MSGID:
                ToastUtils.showToast(TTApplication.getAppContext(), msg.getData().getString("value"));
                break;
            default:
                break;
        }
    }

    private void showDialogForRemoveDownload() {
        final Context context = getCurrentContext();

        if (context == null)
            return;

        new TTAlertDialog.Builder(context)
                .setMessage(R.string.exitappwhendownloading)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//				L.d("remove download id = " + ConfigurationCache.getVersionDownloadId());
//				int temp = manager.remove(ConfigurationCache.getVersionDownloadId());
//				L.d("success remove item : " + temp);
//				ConfigurationCache.setVersionInterrupt(true);
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    /**
     * 显示下载对话框
     *
     * @param bundle 更新数据
     */
    private void showDialogForDownload(Bundle bundle) {
        final Context context = getCurrentContext();

        if (context == null || null == bundle)
            return;

        final UpdateInfo item = (UpdateInfo) bundle.getParcelable("object");

        String fileName = AppUtils.getAppName() + item.getVersion();
        final File file = new File(UpdateService.DIR, fileName + ".apk");

        View view = LayoutInflater.from(context).inflate(R.layout.push_dialog, null);
//		TextView tvTitle = (TextView) view.findViewById(R.id.p_title);
        TextView tvMessage = (TextView) view.findViewById(R.id.p_message);
        String downLoadTitle;
        final boolean isForce = item.getForce() == 1;
        if (isForce) {
            downLoadTitle = context.getString(R.string.force_download);
        } else {
            downLoadTitle = context.getString(R.string.findnewversion, item.getVersion());
        }
//		tvTitle.setText(downLoadTitle);
        tvMessage.setText(item.getIntro());

        new TTAlertDialog.Builder(context)
                .setTitle(downLoadTitle)
                .setView(view)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (isForce) {
                            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(R.drawable.icon);
                            UpdateUtils.exitApplication();
                        }
                    }
                }).setNegativeButton(R.string.updating, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (isForce) {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        // 设置mShowing值，欺骗android系统
                        field.set(dialog, false); // 需要关闭的时候 将这个参数设置为true 他就会自动关闭了
                    }

                    if (file.exists()) {
                        UpdateUtils.Instanll(file, TTApplication.getAppContext());
                    } else {
                        Intent intent = new Intent(UpdateService.UPDATE_ACTION);
                        intent.setPackage("com.tingtingfm.cbb");
                        intent.putExtra("url", item.getUrl());
                        intent.putExtra("version", item.getVersion());
                        context.startService(intent);
                    }
                } catch (Exception e) {
                }
            }
        }).setCancelable(false).show();
    }

    /**
     * 得到一个有效的Context，因为对话框需要绑定
     *
     * @return
     */
    private Context getCurrentContext() {
        Context context = ActivityStack.getInstance().getStackTopActivity();

        if (context instanceof Activity) {
            if (!((Activity) context).isFinishing()) {
                return context;
            }
        }

        return null;
    }
}