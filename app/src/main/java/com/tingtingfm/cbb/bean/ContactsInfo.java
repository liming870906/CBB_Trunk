package com.tingtingfm.cbb.bean;

import java.io.Serializable;

/**
 * Created by liming on 17/1/6.
 */

public class ContactsInfo implements Serializable {
    private int admin_id;
    private String admin_name;
    private String face_url;
    private String dept;
    private String role;

    public int getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(int admin_id) {
        this.admin_id = admin_id;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public String getFace_url() {
        return face_url;
    }

    public void setFace_url(String face_url) {
        this.face_url = face_url;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ContactsInfo{" +
                "admin_id=" + admin_id +
                ", admin_name='" + admin_name + '\'' +
                ", face_url='" + face_url + '\'' +
                ", dept='" + dept + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
