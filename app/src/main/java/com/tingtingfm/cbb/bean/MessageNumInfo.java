package com.tingtingfm.cbb.bean;

/**
 * Created by admin on 2016/12/27.
 */

public class MessageNumInfo {
    private int num; //未读数目

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "MessageNumInto -----  num:"+getNum();
    }
}
