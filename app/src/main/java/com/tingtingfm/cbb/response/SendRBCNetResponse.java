package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by liming on 17/1/18.
 */

public class SendRBCNetResponse extends BaseResponse {

    /**
     * status : 1
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private int status;
        private String msg;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
