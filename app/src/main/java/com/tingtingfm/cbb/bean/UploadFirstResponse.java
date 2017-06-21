package com.tingtingfm.cbb.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class UploadFirstResponse {
    public int identity;//切片Id
    public int count;//切片总数
    public String successSlice;//成功上传切片

    public UploadFirstResponse(int identity, int count, String successSlice) {
        this.identity = identity;
        this.count = count;
        this.successSlice = successSlice;
    }

    public List<Integer> getSuccessUploadIds() {
        List<Integer> ids = new ArrayList<>();
        if (TextUtils.isEmpty(successSlice)) {
            return ids;
        }

        if (successSlice.lastIndexOf(",") == -1) {
            ids.add(Integer.parseInt(successSlice));
        } else {
            String[] strings = successSlice.split(",");
            for (String str : strings) {
                ids.add(Integer.parseInt(str));
            }
        }

        return ids;
    }

    public List<Integer> getNotUploadIds() {
        List<Integer> allIds = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            allIds.add(i);
        }

        allIds.removeAll(getSuccessUploadIds());

        return allIds;
    }

    @Override
    public String toString() {
        return "UploadFirstResponse{" +
                "identity=" + identity +
                ", count=" + count +
                ", successSlice='" + successSlice + '\'' +
                '}';
    }
}