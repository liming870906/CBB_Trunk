package com.tingtingfm.cbb.common.upload;

import java.util.List;

/**
 * Created by lqsir on 2017/1/16.
 *
 * 上传回调接口
 */

public interface UploadListener {
    void start(List<Integer> ids);

    void fail(int id);

    void success(int id, int mediaId);
}
