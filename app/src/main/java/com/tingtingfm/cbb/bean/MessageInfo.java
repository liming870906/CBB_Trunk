package com.tingtingfm.cbb.bean;

import java.io.Serializable;

/**
 * Created by admin on 2016/12/27.
 */

public class MessageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    //    "message_id": 1014,
//            "message_type": 3,
//            "message_title": "资源中心",
//            "message_time": "2017-01-04 14:59:43",
//            "is_read": 0,
//            "message_content": {
//              "content_type": "resource_content",
//                "content_detail": {
//                "tips": "杨宾发送了“IMG_0035.PNG”给您，请在素材管理-个人素材-我接收的素材查收。"
//        }
//    }

    private int message_id; //消息id
    private int message_type; //消息类型 （1 审批消息 ，2 用户中心消息，3资源中心消息）
    private String message_title;  //title
    private String message_time;//时间
    private int is_read; //0未读，1读
    private MegContent message_content;

    public int getIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    public MegContent getMessage_content() {
        return message_content;
    }

    public void setMessage_content(MegContent message_content) {
        this.message_content = message_content;
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public String getMessage_title() {
        return message_title;
    }

    public void setMessage_title(String message_title) {
        this.message_title = message_title;
    }

    public int getMessage_type() {
        return message_type;
    }

    public void setMessage_type(int message_type) {
        this.message_type = message_type;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "is_read=" + is_read +
                ", message_id=" + message_id +
                ", message_type=" + message_type +
                ", message_title='" + message_title + '\'' +
                ", message_time='" + message_time + '\'' +
                ", message_content=" + message_content.toString() +
                '}';
    }

    public class MegContent implements Serializable {
        private String content_type;//审批：approve(由我审批)、 submit（由我提审）用户中心：owner(群主)、member（非群主）资源中心：resource_content
        private MegDetail content_detail;

        public MegDetail getContent_detail() {
            return content_detail;
        }

        public void setContent_detail(MegDetail content_detail) {
            this.content_detail = content_detail;
        }

        public String getContent_type() {
            return content_type;
        }

        public void setContent_type(String content_type) {
            this.content_type = content_type;
        }

        @Override
        public String toString() {
            return "message_content{" +
                    "content_detail=" + content_detail.toString() +
                    ", content_type='" + content_type + '\'' +
                    '}';
        }
        public class MegDetail implements Serializable {
            //资源信息内容，使用成员(tips);
            //审批信息内容，使用成员(tips,approve_id)
            //群组信息内容，所有以下成员都使用

            private String tips;  //信息内容
            private int  group_id; // 群组id
            private String action; //in加群 out退群 dissolve群解散 add加成员 kick 踢成员
            private int member_id; //申请人id
            private int operrate_status; //当为加群时，0未操作 1同意 2拒绝

            public String getAction() {
                return action;
            }

            public void setAction(String action) {
                this.action = action;
            }

            public int getGroup_id() {
                return group_id;
            }

            public void setGroup_id(int group_id) {
                this.group_id = group_id;
            }

            public int getMember_id() {
                return member_id;
            }

            public void setMember_id(int member_id) {
                this.member_id = member_id;
            }

            public int getOperrate_status() {
                return operrate_status;
            }

            public void setOperrate_status(int operrate_status) {
                this.operrate_status = operrate_status;
            }

            public String getTips() {
                return tips;
            }

            public void setTips(String tips) {
                this.tips = tips;
            }

            @Override
            public String toString() {
                return "MegDetail{" +
                        "action='" + action + '\'' +
                        ", tips='" + tips + '\'' +
                        ", group_id=" + group_id +
                        ", member_id=" + member_id +
                        ", operrate_status=" + operrate_status +
                        '}';
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MessageInfo){
            MessageInfo info = (MessageInfo) o;
            return this.message_id == info.getMessage_id();
        }
        return false;
    }
}
