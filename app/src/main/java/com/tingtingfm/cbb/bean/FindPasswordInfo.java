package com.tingtingfm.cbb.bean;

/**
 * Created by think on 2016/12/20.
 */

public class FindPasswordInfo {
//    "succ": 1,
//    "info": {
//                "mobile": "181****1111",
//                "email": "mengyu@tingtingfm.com"
//    }
    private int succ; //1成功0失败
    private PhoneEmailInfo info; //手机，邮件对象


    public PhoneEmailInfo getInfo() {
        return info;
    }

    public void setInfo(PhoneEmailInfo info) {
        this.info = info;
    }

    public int getSucc() {
        return succ;
    }

    public void setSucc(int succ) {
        this.succ = succ;
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "succ=" + succ +
                ", mobile='" + getInfo().getMobile() + '\'' +
                ", email='" + getInfo().getEmail() + '\'' +
                '}';
    }
}
