package com.tingtingfm.cbb.bean;

/**
 * Created by think on 2016/12/20.
 */

public class LoginInfo {
    /**
     * userid : 168
     * realname : 孟瑜
     * department :
     * role : 高级编辑
     * face_url : http://kuaiting-dev-cdn.ting-ting.cn/caibianbo_user/2016/1108/ec/8a/ec8a32bcda468c47b7781185f18657fc.jpg
     * session_key : 90930705_168_166066c49e0729266eed3b10839a710c
     * ap_username:
     * email:
     * mobile:
     */

    private int userid;  //用户编号，唯一标识
    private String realname;  //真实姓名
    private String department; //部门
    private String role;  //职务
    private String face_url;  //头像地址
    private String session_key; //登录验证串
    private String ap_username; //制作网帐号
    private String email;//邮箱
    private String mobile;//手机号
    private int is_disabled;//1：表示禁用，0：表示正常

    public int getIs_disabled() {
        return is_disabled;
    }

    public void setIs_disabled(int is_disabled) {
        this.is_disabled = is_disabled;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFace_url() {
        return face_url;
    }

    public void setFace_url(String face_url) {
        this.face_url = face_url;
    }

    public String getAp_username() {
        return ap_username;
    }

    public void setAp_username(String ap_username) {
        this.ap_username = ap_username;
    }


    public String getSession_key() {
        return session_key;
    }

    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "userid=" + userid +
                ", realname='" + realname + '\'' +
                ", department='" + department + '\'' +
                ", role='" + role + '\'' +
                ", face_url='" + face_url + '\'' +
                ", session_key='" + session_key + '\'' +
                ", ap_username='" + ap_username + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}
