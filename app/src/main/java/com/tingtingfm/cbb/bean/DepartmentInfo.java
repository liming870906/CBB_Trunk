package com.tingtingfm.cbb.bean;

import java.io.Serializable;

/**
 * Created by liming on 17/1/6.
 */

public class DepartmentInfo implements Serializable{
    private int department_id;
    private String department_name;
    private int contact_count;

    public int getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(int department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public int getContact_count() {
        return contact_count;
    }

    public void setContact_count(int contact_count) {
        this.contact_count = contact_count;
    }

    @Override
    public String toString() {
        return "DepartmentInfo{" +
                "department_id=" + department_id +
                ", department_name='" + department_name + '\'' +
                ", contact_count=" + contact_count +
                '}';
    }
}
