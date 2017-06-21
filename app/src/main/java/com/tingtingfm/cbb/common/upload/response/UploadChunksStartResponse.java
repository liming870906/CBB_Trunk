package com.tingtingfm.cbb.common.upload.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by lqsir on 2017/4/17.
 */

public class UploadChunksStartResponse extends BaseResponse {
    private Identity data;

    public Identity getData() {
        return data;
    }

    public void setData(Identity data) {
        this.data = data;
    }

    public static class Identity {
        private int identity;
        private int serial_number;

        public int getIdentity() {
            return identity;
        }

        public void setIdentity(int identity) {
            this.identity = identity;
        }

        public int getSerial_number() {
            return serial_number;
        }

        public void setSerial_number(int serial_number) {
            this.serial_number = serial_number;
        }
    }
}
