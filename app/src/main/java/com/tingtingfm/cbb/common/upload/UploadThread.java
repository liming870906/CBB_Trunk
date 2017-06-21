package com.tingtingfm.cbb.common.upload;

import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.upload.config.UploadConfiguration;
import com.tingtingfm.cbb.common.upload.operatioin.UploadOperationProc;

/**
 * Created by lqsir on 2017/1/16.
 */

public class UploadThread extends Thread {
    private UploadConfiguration mConfiguration;
    private UploadService mUploadService;
    private MediaInfo mInfo;
    private boolean isAliveing = false;

    public UploadThread(UploadService service) {
        this.mConfiguration = new UploadConfiguration.Builder().build();
        this.mUploadService = service;
    }

    public boolean isAliveing() {
        return isAliveing;
    }

    @Override
    public void run() {
        super.run();
        isAliveing = true;
        mInfo = UploadManager.getInstance().poll();
        while (mInfo != null) {
            if (mInfo.checkUploadInfo()) {
                //只上传音频内容（不包含文件）
                UploadOperationProc.toUploadAudioInfo(mInfo, mConfiguration);
            } else {
                UploadOperationProc.toSliceUploadFile(mInfo, mConfiguration);
            }

            mInfo = UploadManager.getInstance().poll();
        }

        isAliveing = false;
        mUploadService.stopSelf();
    }
}
