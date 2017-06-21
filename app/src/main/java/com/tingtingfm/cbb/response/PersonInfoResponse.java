package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.bean.PersonInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by think on 2016/12/20.
 */

public class PersonInfoResponse extends BaseResponse {
    private PersonInfo data;

    public PersonInfoResponse() {
    }

    public PersonInfo getData() {
        return data;
    }

    public void setData(PersonInfo data) {
        this.data = data;
    }
}
