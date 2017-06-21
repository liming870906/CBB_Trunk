package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.bean.MessageNumInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by think on 2016/12/20.
 */

public class MessageResponse extends BaseResponse {
    private MessageNumInfo data;

    public MessageResponse() {
    }

    public MessageNumInfo getData() {
        return data;
    }

    public void setData(MessageNumInfo data) {
        this.data = data;
    }
}
