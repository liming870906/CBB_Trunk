package com.tingtingfm.cbb.bean;

/**
 * Created by admin on 2016/12/26.
 */

public class PersonInfo {

    private String realname;//真实姓名
    private String department;// 部门
    private String role;//职务
    private String face_url;//头像
    private int userid;//用户id
    private String email;//邮箱
    private String mobile;//手机号
    private String ap_username;//制作网帐号
    private int is_disabled;//1：表示禁用，0：表示正常

    public int getIs_disabled() {
        return is_disabled;
    }

    public void setIs_disabled(int is_disabled) {
        this.is_disabled = is_disabled;
    }

    public String getAp_username() {
        return ap_username;
    }

    public void setAp_username(String ap_username) {
        this.ap_username = ap_username;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFace_url() {
        return face_url;
    }

    public void setFace_url(String face_url) {
        this.face_url = face_url;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

}
