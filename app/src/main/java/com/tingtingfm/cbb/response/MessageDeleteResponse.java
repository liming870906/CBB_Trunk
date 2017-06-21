package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.MessageInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

import java.util.ArrayList;

/**
 * Created by think on 2016/12/20.
 */

public class MessageDeleteResponse extends BaseResponse {
    private deleteInfo data;

    public deleteInfo getData() {
        return data;
    }

    public void setData(deleteInfo data) {
        this.data = data;
    }

    public class deleteInfo{
        private ArrayList<String> succ_id;

        public ArrayList<String> getSucc_id() {
            return succ_id;
        }

        public void setSucc_id(ArrayList<String> succ_id) {
            this.succ_id = succ_id;
        }

        @Override
        public String toString() {
            return "deleteInfo{" +
                    "succ_id=" + succ_id.toArray().toString() +
                    '}';
        }
    }
}
