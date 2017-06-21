package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.UpdateInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * 版本升级对象
 */
public class UpdateVersionResponse extends BaseResponse {

    private UpdateResult data;

    public UpdateResult getData() {
        return data;
    }

    public void setData(UpdateResult data) {
        this.data = data;
    }

    public static class UpdateResult {
        private UpdateInfo new_versions;

        public UpdateInfo getNewVersion() {
            return new_versions;
        }

        public void setNew_versions(UpdateInfo new_versions) {
            this.new_versions = new_versions;
        }
    }
}