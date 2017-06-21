package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.ContactsInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

import java.util.List;

/**
 * Created by liming on 17/1/10.
 */

public class GetContactsInfoResponse extends BaseResponse {
    /**
     * admin_id : 166
     * admin_name : jiangyu
     * face_url :
     * dept : 开发部
     * role : 管理员
     */

    private List<ContactsInfo> data;

    public List<ContactsInfo> getData() {
        return data;
    }

    public void setData(List<ContactsInfo> data) {
        this.data = data;
    }
}
