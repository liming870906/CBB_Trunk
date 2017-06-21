package com.tingtingfm.cbb.response;

import com.tingtingfm.cbb.common.net.BaseResponse;

import java.util.ArrayList;


/**
 * Created by liming on 17/1/5.
 */

public class CheckMaterialResponse extends BaseResponse {

    /**
     * errno : 0
     * data : [1,2]
     * page_cost_time : 0.0547
     */

    private ArrayList<Integer> data;

    public ArrayList<Integer> getData() {
        return data;
    }

    public void setData(ArrayList<Integer> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CheckMaterialResponse{" +
                "data=" + data +
                '}';
    }
}
