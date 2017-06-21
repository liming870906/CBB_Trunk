package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.MessageInfo;
import com.tingtingfm.cbb.bean.MessageNumInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

import java.util.ArrayList;

/**
 * Created by think on 2016/12/20.
 */

public class MessageListResponse extends BaseResponse {
    private ArrayList<MessageInfo> data;

    public ArrayList<MessageInfo> getData() {
        return data;
    }
    public void setData(ArrayList<MessageInfo> data) {
        this.data = data;
    }
}
