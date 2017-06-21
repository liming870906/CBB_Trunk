package com.tingtingfm.cbb.common.upload;

import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描音频文件任务
 * 将未上传、上传失败的音频文件添加到上传队列中
 * 任务启动时机：应用启动、网络状态改变
 * Created by lqsir on 2017/2/14.
 */

public class ScanTaskRunnable implements Runnable {
    @Override
    public void run() {
        GlobalVariableManager.isScanning = true;
        List<MediaInfo> uploadInfos = new ArrayList<MediaInfo>();
        ArrayList<MediaInfo> infos = DBAudioRecordManager.getInstance(TTApplication.getAppContext()).queryAllAudioRecord();

        int netStatus = NetUtils.getNetConnectType();
        for (MediaInfo info : infos) {
            if (info.getUpload_status() != 2 || info.getIsUpdateAudioInfo() == 1) {
                if (GlobalVariableManager.isOpen100 && netStatus == 2) {
                    if (info.getSize() <= GlobalVariableManager.MAX4GFILESIZE) {
                        uploadInfos.add(info);
                    }
                } else if (info.getSize() < GlobalVariableManager.MAXWIFIFILESIZE) {
                    uploadInfos.add(info);
                }
            }
        }

        if (uploadInfos.size() != 0) {
            UploadManager.getInstance().addUpload(uploadInfos);
        }
        GlobalVariableManager.isScanning = false;
    }
}
