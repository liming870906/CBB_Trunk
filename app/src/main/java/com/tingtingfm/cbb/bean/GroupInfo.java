package com.tingtingfm.cbb.bean;

import java.io.Serializable;

/**
 * Created by liming on 17/1/6.
 */

public class GroupInfo implements Serializable{
    private int group_id;
    private String group_name;
    private int contact_count;

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public int getContact_count() {
        return contact_count;
    }

    public void setContact_count(int contact_count) {
        this.contact_count = contact_count;
    }

    @Override
    public String toString() {
        return "GroupInfo{" +
                "group_id=" + group_id +
                ", group_name='" + group_name + '\'' +
                ", contact_count=" + contact_count +
                '}';
    }
}
