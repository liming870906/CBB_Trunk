package com.tingtingfm.cbb.common.upload;

import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.bean.UploadFirstResponse;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;
import com.tingtingfm.cbb.common.db.DBMaterialImageManager;
import com.tingtingfm.cbb.common.db.DBMaterialVideoManager;

/**
 * Created by lqsir on 2017/4/18.
 */

public class DBOperationUtils {
    public static void updateMaterialDb(final MediaInfo info) {
        if (info.getManuscriptId() != 0) {
            DBManuscriptManager.getInstance(TTApplication.getAppContext()).updateAudioInfo(info);
        } else {
            String type = info.getMime_type();
            if (type.startsWith("image/")) {
                DBMaterialImageManager.getInstance(TTApplication.getAppContext()).updateImageMediaID(info);
            } else if (type.startsWith("video/")) {
                DBMaterialVideoManager.getInstance(TTApplication.getAppContext()).updateVideoMediaID(info);
            } else if (type.startsWith("audio/")) {
                DBAudioRecordManager.getInstance(TTApplication.getAppContext()).updateAudioRecord(info);
            }
        }
    }

    public static UploadFirstResponse getUploadInfoForMediaInfo(final MediaInfo info) {
        UploadFirstResponse response = null;

        if (info.getManuscriptId() != 0) {
            response = DBManuscriptManager.getInstance(TTApplication.getAppContext()).getUploadInfo(info.getId());
        } else {
            String type = info.getMime_type();
            if (type.startsWith("image/")) {
                response = DBMaterialImageManager.getInstance(TTApplication.getAppContext()).getUploadInfo(info.getId());
            } else if (type.startsWith("video/")) {
                response = DBMaterialVideoManager.getInstance(TTApplication.getAppContext()).getUploadInfo(info.getId());
            } else if (type.startsWith("audio/")) {
                response = DBAudioRecordManager.getInstance(TTApplication.getAppContext()).getUploadInfo(info.getId());
            }
        }
        return response;
    }
}
