package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.MakeInfo;
import com.tingtingfm.cbb.bean.PersonInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by think on 2016/12/20.
 */

public class MakeResponse extends BaseResponse {
    private MakeInfo data;

    public MakeResponse() {
    }

    public MakeInfo getData() {
        return data;
    }

    public void setData(MakeInfo data) {
        this.data = data;
    }
}
