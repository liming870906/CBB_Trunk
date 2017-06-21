package com.tingtingfm.cbb.bean;

import java.util.List;

/**
 * Created by liming on 17/1/6.
 */

public class SendInfo {
    /**
     * admin_id : 166
     * admin_name : jiangyu
     * face_url :
     * dept : 开发部
     * role : 管理员
     */

    private List<ContactsInfo> contacts;
    /**
     * group_id : 66
     * group_name : 3456
     * contact_count : 3
     */

    private List<GroupInfo> group;

    public SendInfo() {
    }

    public SendInfo(List<ContactsInfo> contacts, List<GroupInfo> group, List<DepartmentInfo> department) {
        this.contacts = contacts;
        this.group = group;
        this.department = department;
    }

    /**
     * department_id : 84
     * department_name : 产品部
     * contact_count : 1
     */

    private List<DepartmentInfo> department;

    public List<ContactsInfo> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactsInfo> contacts) {
        this.contacts = contacts;
    }

    public List<GroupInfo> getGroup() {
        return group;
    }

    public void setGroup(List<GroupInfo> group) {
        this.group = group;
    }

    public List<DepartmentInfo> getDepartment() {
        return department;
    }

    public void setDepartment(List<DepartmentInfo> department) {
        this.department = department;
    }


}
