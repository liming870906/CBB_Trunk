package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by think on 2016/12/20.
 */

public class LoginResponse extends BaseResponse {
    private LoginInfo data;

    public LoginResponse() {
    }

    public LoginInfo getData() {
        return data;
    }

    public void setData(LoginInfo data) {
        this.data = data;
    }
}
