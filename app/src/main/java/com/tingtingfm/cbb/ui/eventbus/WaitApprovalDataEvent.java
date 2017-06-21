package com.tingtingfm.cbb.ui.eventbus;

import com.tingtingfm.cbb.bean.ApprovalInfo;

import java.util.ArrayList;

/**
 * Created by liming on 2017/6/12.
 */

public class WaitApprovalDataEvent {
    private ArrayList<ApprovalInfo> data;
    private int mMessageTag;

    public WaitApprovalDataEvent(ArrayList<ApprovalInfo> data, int mMessageTag) {
        this.data = data;
        this.mMessageTag = mMessageTag;
    }

    /**
     * 获得审批数据
     * @return
     */
    public ArrayList<ApprovalInfo> getData() {
        return data;
    }

    /**
     * 判断数据是否为null
     * @return
     */
    public boolean isHaveData(){
        if(data == null || data.size() == 0){
            return false;
        }
        return true;
    }

    /**
     * 消息标记
     * @return
     */
    public int getmMessageTag() {
        return mMessageTag;
    }
}
