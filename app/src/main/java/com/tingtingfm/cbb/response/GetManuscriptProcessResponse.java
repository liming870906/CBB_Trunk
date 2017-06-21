package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by tianhu on 2017/1/16.
 */

public class GetManuscriptProcessResponse extends BaseResponse {
    private CompareInfo data;


    public CompareInfo getData() {
        return data;
    }

    public void setData(CompareInfo data) {
        this.data = data;
    }

    public class CompareInfo {
        private int status ; //

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "CompareInfo{" +
                    "status=" + status +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ManuscriptCompareResponse{" +
                "data=" + data.toString() +
                '}';
    }

}
