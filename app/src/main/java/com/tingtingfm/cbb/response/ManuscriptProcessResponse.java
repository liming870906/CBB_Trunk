package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.bean.ProcessInfo;
import com.tingtingfm.cbb.common.net.BaseResponse;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tianhu on 2017/1/16.
 */

public class ManuscriptProcessResponse extends BaseResponse implements Serializable {
    private ArrayList<ProcessInfo> data;

    public ArrayList<ProcessInfo> getData() {
        return data;
    }

    public void setData(ArrayList<ProcessInfo> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ManuscriptCompareResponse{" +
                "data=" + data.toString() +
                '}';
    }

}
