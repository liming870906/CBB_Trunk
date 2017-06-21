package com.tingtingfm.cbb.common.upload.operatioin;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.upload.DBOperationUtils;
import com.tingtingfm.cbb.common.upload.UploadManager;
import com.tingtingfm.cbb.common.upload.config.UploadConfiguration;
import com.tingtingfm.cbb.response.UpdateAudioInfoResponse;
import com.tingtingfm.cbb.response.UploadFaceResponse;

/**
 * Created by lqsir on 2017/4/17.
 */

public class UploadOperationProc {
    public static void toUploadAudioInfo(final MediaInfo info, UploadConfiguration configuration) {
        String body = UploadHttpsUtils.getUploadAudioInfo(info, configuration);

        if (!TextUtils.isEmpty(body)) {
            UpdateAudioInfoResponse response = JSON.parseObject(body, UpdateAudioInfoResponse.class);

            if (response.data != null) {
                TTLog.i("----upload-----" + response.data.toString());
                info.setIsUpdateAudioInfo(0);
                DBOperationUtils.updateMaterialDb(info);
                UploadManager.getInstance().uploadSuccess(info.getId(), info.getMedia_id());
                return;
            } else {
                TTLog.i("----upload----- response.data is null or file does't exist");
            }
        } else {
            // TODO: 2017/4/18 请求上传音频信息失败
            info.setIsUpdateAudioInfo(1);
        }
        try {
            DBOperationUtils.updateMaterialDb(info);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UploadManager.getInstance().uploadFail(info.getId());
    }

    public static void toSliceUploadFile(MediaInfo info, UploadConfiguration configuration) {
        String body = UploadHttpsUtils.getSliceUploadInfo(info, configuration);
        if (!TextUtils.isEmpty(body)) {
            UploadFaceResponse response = JSON.parseObject(body, UploadFaceResponse.class);

            if (response.getErrno() == 0) {
                TTLog.i("----upload-----" + response.data.toString());
                info.setUpload_status(Constants.UPLOAD_STATUS_SUCCESS);
                info.setMedia_id(response.data.getId());

                DBOperationUtils.updateMaterialDb(info);
                UploadManager.getInstance().uploadSuccess(info.getId(), info.getMedia_id());
                return;
            }  else if (response.getErrno() == -2) {
                TTLog.i("----upload----- response.data is null");
                info.setUpload_status(Constants.UPLOAD_STATUS_FAILURE);
                info.setSliceId(0);
                info.setSliceCount(0);
                info.setSuccessIds("");
            } else {
                TTLog.i("----upload----- response.data is null or error = " + response.getErrno() + "  message: " + response.getError());
                info.setUpload_status(Constants.UPLOAD_STATUS_FAILURE);
            }
        } else {
            info.setUpload_status(Constants.UPLOAD_STATUS_FAILURE);
        }

        try {
            DBOperationUtils.updateMaterialDb(info);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UploadManager.getInstance().uploadFail(info.getId());
    }
}
