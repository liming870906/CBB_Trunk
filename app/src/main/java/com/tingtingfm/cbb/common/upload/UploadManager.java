package com.tingtingfm.cbb.common.upload;

import android.content.Intent;

import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.DataConvertUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by lqsir on 2017/1/16.
 */

public class UploadManager {
    private static UploadManager manager;
    private Queue<MediaInfo> queue = new ArrayDeque<MediaInfo>();
    private List<UploadListener> mListeners = new ArrayList<UploadListener>();

    private UploadManager() {
        queue.clear();
        mListeners.clear();
    }

    public static UploadManager getInstance() {
        if (manager == null) {
            synchronized (UploadManager.class) {
                if (manager == null) {
                    manager = new UploadManager();
                }
            }
        }

        return manager;
    }

    public void addUpload(MediaInfo info) {
        addUpload(DataConvertUtils.getMediaInfos(info));
    }

    public void addUpload(List<MediaInfo> infos) {
        List<MediaInfo> uploadList = new ArrayList<>();
        for (MediaInfo info : infos) {
            if (!info.checkUploadInfo()) {
                info.setUpload_status(Constants.UPLOAD_STATUS_LOADING);
            }

            if (!queue.contains(info)) {
                uploadList.add(info);
                queue.add(info);
            }
        }

        uploadStart(uploadList);
        updateLocaldb(uploadList);
        startService();
    }

    /**
     * TODO 考虑异步处理
     * @param infos
     */
    private void updateLocaldb(List<MediaInfo> infos) {
        for (MediaInfo info : infos) {
            DBOperationUtils.updateMaterialDb(info);
        }
    }

    MediaInfo poll() {
        return queue.poll();
    }

    private void startService() {
        Intent intent = new Intent();
        intent.setClass(TTApplication.getAppContext(), UploadService.class);
        intent.setPackage(TTApplication.getAppContext().getPackageName());
        TTApplication.getAppContext().startService(intent);
    }

    public void addUploadListener(UploadListener listener) {
        mListeners.add(listener);
    }

    public void removeUploadListener(UploadListener listener) {
        mListeners.remove(listener);
    }

    private void uploadStart(List<MediaInfo> infos) {
        for (UploadListener listener : mListeners) {
            List<Integer> ids = new ArrayList<>();
            for (MediaInfo info : infos) {
                ids.add(info.getId());
            }
            listener.start(ids);
        }
    }

    public void uploadFail(int id) {
        for (UploadListener listener : mListeners) {
            listener.fail(id);
        }
    }

    public void uploadSuccess(int id, int mediaId) {
        for (UploadListener listener : mListeners) {
            listener.success(id, mediaId);
        }
    }


}
