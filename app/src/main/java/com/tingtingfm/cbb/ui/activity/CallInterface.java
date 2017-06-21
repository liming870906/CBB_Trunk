package com.tingtingfm.cbb.ui.activity;

/**
 * Created by tianhu on 2017/1/4.
 */

public interface CallInterface {
    int SELECT_VAL = 0;//删除选中项
    int UNKONWN_VAL = 1;//未知类型数据
    void clickCall(int flag,int msgId);
}