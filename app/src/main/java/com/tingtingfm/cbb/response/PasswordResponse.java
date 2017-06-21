package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.FindPasswordInfo;
import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by think on 2016/12/20.
 */

public class PasswordResponse extends BaseResponse {
    private FindPasswordInfo data;

    public PasswordResponse() {
    }

    public FindPasswordInfo getData() {
        return data;
    }

    public void setData(FindPasswordInfo data) {
        this.data = data;
    }
}
