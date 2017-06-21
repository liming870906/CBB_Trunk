package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by think on 2016/12/20.
 */

public class EditUserResponse extends BaseResponse {
    private UserInfo data;

    public EditUserResponse() {
    }

    public UserInfo getData() {
        return data;
    }

    public void setData(UserInfo data) {
        this.data = data;
    }

    public class UserInfo {
        private int succ;

        public int getSucc() {
            return succ;
        }

        public void setSucc(int succ) {
            this.succ = succ;
        }

        @Override
        public String toString() {
            return "data {" +
                    "succ='" + succ + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "EditUserResponse{" +
                "data=" + data.toString() +
                '}';
    }
}
