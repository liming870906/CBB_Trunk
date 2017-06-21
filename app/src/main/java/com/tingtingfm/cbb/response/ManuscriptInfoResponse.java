package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by tianhu on 2017/1/16.
 */

public class ManuscriptInfoResponse extends BaseResponse {
    private manuInfo data;

    public ManuscriptInfoResponse() {
    }

    public manuInfo getData() {
        return data;
    }

    public void setData(manuInfo data) {
        this.data = data;
    }

    public class manuInfo {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "manuInfo{" +
                    "id=" + id +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ManuscriptInfoResponse{" +
                "data=" + data +
                '}';
    }
}
