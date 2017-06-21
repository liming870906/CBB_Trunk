package com.tingtingfm.cbb.ui.serve;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.upload.DBOperationUtils;
import com.tingtingfm.cbb.common.upload.config.UploadConfiguration;
import com.tingtingfm.cbb.common.upload.operatioin.UploadHttpsUtils;
import com.tingtingfm.cbb.response.UploadFaceResponse;


/**
 * Created by lqsir on 2017/4/25.
 */

public class ManuscriptAudioUploadThread extends Thread {
    /**
     * 上传配置信息
     */
    private UploadConfiguration mConfiguration;
    /**
     * handler
     */
    private Handler mHandler;
    /**
     * 音频信息
     */
    private MediaInfo mMediaInfo;

    public ManuscriptAudioUploadThread(Handler handler, MediaInfo mediaInfo) {
        mHandler = handler;
        mMediaInfo = mediaInfo;
        mConfiguration = new UploadConfiguration.Builder().build();
    }

    @Override
    public void run() {
        super.run();
        //音频上传处理
        String body = UploadHttpsUtils.getSliceUploadInfo(mMediaInfo, mConfiguration);
        //返回结果进行处理
        if (!TextUtils.isEmpty(body)) {
            UploadFaceResponse response = JSON.parseObject(body, UploadFaceResponse.class);
            if (response.getErrno() == 0) {
                Message mMes = mHandler.obtainMessage();
                mMes.what = Constants.UPLOAD_SUCCESS;
                Bundle bundle = new Bundle();
                bundle.putString("id", response.data.getId()+"");
                bundle.putString("url", response.data.getUrl());
                bundle.putString("locaType", response.data.getLocation_type());
                mMes.setData(bundle);
                mHandler.sendMessage(mMes);
                TTLog.i("Comment_test", "----upload voice success" + response.data.getUrl());
            } else if (response.getErrno() == -2) {
                mMediaInfo.setSliceId(0);
                mMediaInfo.setSliceCount(0);
                mMediaInfo.setSuccessIds("");
                DBOperationUtils.updateMaterialDb(mMediaInfo);
                mHandler.sendEmptyMessage(Constants.UPLOAD_FAIL);
            } else  {
                mHandler.sendEmptyMessage(Constants.UPLOAD_FAIL);
            }
        } else {
            mHandler.sendEmptyMessage(Constants.UPLOAD_FAIL);
        }
    }
}
