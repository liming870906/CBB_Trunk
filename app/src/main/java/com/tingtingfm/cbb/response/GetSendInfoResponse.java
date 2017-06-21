package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.ContactsInfo;
import com.tingtingfm.cbb.bean.DepartmentInfo;
import com.tingtingfm.cbb.bean.GroupInfo;
import com.tingtingfm.cbb.bean.SendInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

import java.util.List;

/**
 * Created by liming on 17/1/6.
 */

public class GetSendInfoResponse extends BaseResponse {


    private SendInfo data;

    public SendInfo getData() {
        return data;
    }

    public void setData(SendInfo data) {
        this.data = data;
    }
}
