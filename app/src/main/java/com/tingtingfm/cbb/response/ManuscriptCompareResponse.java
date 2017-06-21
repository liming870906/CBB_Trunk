package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by tianhu on 2017/1/16.
 */

public class ManuscriptCompareResponse extends BaseResponse {
    private CompareInfo data;


    public CompareInfo getData() {
        return data;
    }

    public void setData(CompareInfo data) {
        this.data = data;
    }

    public class CompareInfo {
//        id  int  稿件ID
//        is_same  int  1 - 一致, 0 - 不一致

        private int id; //
        private int is_same;

        public int getIs_same() {
            return is_same;
        }

        public void setIs_same(int is_same) {
            this.is_same = is_same;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "CompareInfo{" +
                    "id=" + id +
                    ", is_same=" + is_same +
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
