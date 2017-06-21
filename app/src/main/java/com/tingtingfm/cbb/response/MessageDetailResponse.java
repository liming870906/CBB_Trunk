package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

import java.util.ArrayList;

/**
 * Created by think on 2016/12/20.
 */

public class MessageDetailResponse extends BaseResponse {
    private detailInfo data;

    public detailInfo getData() {
        return data;
    }

    public void setData(detailInfo data) {
        this.data = data;
    }

    public class detailInfo{
        private int succ;

        public int getSucc() {
            return succ;
        }

        public void setSucc(int succ) {
            this.succ = succ;
        }

        @Override
        public String toString() {
            return "detailInfo{" +
                    "succ=" + succ +
                    '}';
        }
    }
}
