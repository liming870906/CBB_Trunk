package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.UpdateAudioInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

/**
 * Created by lqsir on 2017/3/9.
 */

public class UpdateAudioInfoResponse extends BaseResponse {
    public UpdateAudioInfo data;

    public UpdateAudioInfo getData() {
        return data;
    }

    public void setData(UpdateAudioInfo data) {
        this.data = data;
    }
}
