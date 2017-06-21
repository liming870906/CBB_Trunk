package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by tianhu on 2017/1/16.
 */

public class GetManuscriptInfoResponse extends BaseResponse {
    private CompareInfo data;


    public CompareInfo getData() {
        return data;
    }

    public void setData(CompareInfo data) {
        this.data = data;
    }

    public class CompareInfo {
        private int id; //
        private int approval; // 0-未提交，1-审批中，2-审批成功

        public int getApproval() {
            return approval;
        }

        public void setApproval(int approval) {
            this.approval = approval;
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
                    "approval=" + approval +
                    ", id=" + id +
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
