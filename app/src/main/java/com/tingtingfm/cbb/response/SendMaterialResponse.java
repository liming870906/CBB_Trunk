package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by liming on 17/1/10.
 */

public class SendMaterialResponse extends BaseResponse {


    /**
     * succ : 1
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private int succ;

        public int getSucc() {
            return succ;
        }

        public void setSucc(int succ) {
            this.succ = succ;
        }
    }
}
