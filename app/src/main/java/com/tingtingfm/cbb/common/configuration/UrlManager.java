package com.tingtingfm.cbb.common.configuration;

/**
 * Created by think on 2016/12/19.
 * 接口地址管理
 */

public class UrlManager {
    public static final String LOGIN_API = NetConfiguration.SERVERURL + "/user/login";

    public static final String VERSION_UPDATE = NetConfiguration.SERVERURL + "/config/check_version";

    //找回密码
    public static final String FIND_PASSWORD = NetConfiguration.SERVERURL + "/user/find_password";

    //获取个人信息
    public static final String GETUSERINFO = NetConfiguration.SERVERURL + "/user/get_user_info";

    //编辑制作网帐号
    public static final String SET_AP_USER_INFO = NetConfiguration.SERVERURL + "/user/edit_ap_user_info";

    //设置-帮助与反馈
    public static final String SET_HELP = NetConfiguration.H5_URL + "/help/enter";

    //获取未读消息数——姜宇
    public static final String GET_NOREAD_NUM = NetConfiguration.SERVERURL + "/message/get_noread_num";

    //获取信息接口——姜宇
    public static final String MESSAGE_LIST = NetConfiguration.SERVERURL + "/message/message_list";

    //消息删除（批量）——姜宇
    public static final String MESSAGE_DEL = NetConfiguration.SERVERURL + "/message/message_del";

    //把消息变成已读——姜宇
    public static final String MESSAGE_READ = NetConfiguration.SERVERURL + "/message/message_read";

    //群组用户加入消息同意、拒绝——姜宇
    public static final String MESSAGE_GROUP_ACT = NetConfiguration.SERVERURL + "/message/message_group_act";

    //上传图片、音频、视频、稿件
    public static final String UPLOAD = NetConfiguration.SERVERURL + "/upload/upload";

    //编辑音频信息
    public static final String UPLOAD_AUDIO_INFO = NetConfiguration.SERVERURL + "/material/edit_audio_info";

    public static final String UPLOAD_SLICE_START = NetConfiguration.SERVERURL + "/upload/chunks_upload_start";
    public static final String UPLOAD_SLICE_UPLOAD = NetConfiguration.SERVERURL + "/upload/chunks_upload";
    public static final String UPLOAD_SLICE_END = NetConfiguration.SERVERURL + "/upload/chunks_upload_end";

    //编辑个人信息(将上传的图片与当前帐号绑定)
    public static final String EDIT_USER_INFO = NetConfiguration.SERVERURL + "/user/edit_user_info";

    //检查素材是否被删除
    public static final String CHECK_MATERIALS = NetConfiguration.SERVERURL + "/material/check_materials";

    //发送素材获取联系人、群组、部门数据
    public static final String GET_SEND_INFO = NetConfiguration.SERVERURL + "/material/get_send_info";

    //通过群组、部门获取联系人
    public static final String GET_CONTACTS_INFO = NetConfiguration.SERVERURL + "/material/get_contacts_info";

    //发送
    public static final String MATERIAL_SEND = NetConfiguration.SERVERURL + "/material/material_send";

    //稿件上传——张强
    public static final String UPLOAD_MANUSCRIPT = NetConfiguration.SERVERURL + "/manuscript/upload_manuscript";

    //稿件对比——张强
    public static final String PRE_UPLOAD_MANUSCRIPT = NetConfiguration.SERVERURL + "/manuscript/pre_upload_manuscript";

    //获取部门稿件审批流程列表——张强
    public static final String GET_APPROVE_PROCESS = NetConfiguration.SERVERURL + "/manuscript/get_approve_process";

    //发送制作网——孟瑜
    public static final String SEND_RBC_NET = NetConfiguration.SERVERURL + "/material/send_rbc_net";

    //获取稿件信息
    public static final String GET_MANUSCRIPT_INFO = NetConfiguration.SERVERURL + "/manuscript/get_manuscript_info";

    //提审——张强
    public static final String SUBMIT_APPROVAL = NetConfiguration.SERVERURL + "/manuscript/submit_approval";
}
