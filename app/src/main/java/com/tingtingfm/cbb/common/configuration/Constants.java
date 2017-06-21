package com.tingtingfm.cbb.common.configuration;

/**
 * Created by think on 2016/12/21.
 * preferences缓存常量
 */

public class Constants {
    public static final String APP_IS_RUN = "appRun"; //应用运行标识
    public static final String AUDIO_CACHE_DIR = "manuscript_audio"; //稿件管理-音频缓存目录
    //文件恢复标记
    public static final String RECOVERY_AUDIO_IS_FINISH = "recovery_audio_is_finish";
    /**
     * 跟帐户相关
     */
    public static final String USER_ID = "login_userid"; //用户编号，唯一标识
    public static final String REAL_NAME = "login_realname"; //真实姓名
    public static final String DEPARTMENT = "login_department"; //部门
    public static final String FACE_URL = "login_face_url";  //头像地址
    public static final String ROLE = "login_role";    //职务
    public static final String SESSION_KEY = "session_key";  //登录验证串
    public static final String AP_USERNAME = "login_ap_username";  //制作网帐号
    public static final String ACCOUT_MOBILE = "mobile";
    public static final String ACCOUT_EAMIL = "email";

    //拍照常量标记
    public static final int TAKE_PHOTO = 0X001;
    //摄像常量标记
    public static final int CAMERA_TUBE = 0X002;
    //录音状态_未录音
    public static final int RECORD_UNSTART = 0X003;
    //录音状态——暂停录音
    public static final int RECORD_PAUSE = 0X004;
    //录音状态——录音中
    public static final int RECORD_RECORDING = 0X005;

    //设置-录音时屏幕倒转
    public static final String SETTING_REVERSAL = "setting_reversal";
    //设置-录音时屏幕长亮
	public static final String SETTING_BRIGHT = "setting_bright";
    //系统日期标记
    public static final String SYSTEM_DATA = "system_time";
    //默认录音文件后缀
    public static final String RECORD_FILE_DEFAULT_NAME = "001";
    //默认录音文件存储标签
    public static final String RECORD_FILE_DEFAULT_KEY = "record_file_name_key";
    //更新文件及剩余空间标记
    public static final int UPDATE_FILE_SIZE_TAG = 0X1001;
    //更新录音音频时间标记
    public static final int UPDATE_RECORD_AUDIO_TIME_TAG = 0X1002;
    //录音界面检验剩余空间不足标记
    public static final int RECORD_RAIM_SIZE_TAG = 0X1003;
    //素材管理界面加载完成标记
    public static final int MATERIAL_LOAD_DATA_TAG = 0X1004;
    //加载数据选择标记
    public static final int MATERIAL_LOAD_CHOOSE_DATA_TAG = 0X1005;
    //选择状态下 发送标记
    public static final int MATERIAL_CHOOSE_MEDIA_DATA_TAG = 0X1006;
    //重新加载数据
    public static final int MATERIAL_RESTART_ALL_DATA_TAG = 0X1007;
    //设置素材标记
    public static final String MATERIAL_NAVIGATION_KEY = "material_navigation";
    //文件大小标记
    public static final String FILE_SIZE_KEY = "file_size_key";
    //剩余空间大小标记
    public static final String RAIM_SIZE_KEY = "raim_size_key";
    //录音时间标记
    public static final String RECORD_AUDIO_TIME_KEY = "record_audio_time_key";
    //应用第一次请求接口标记
    public static final String FIRST_START_TIME = "app_first_start_time";
    //多媒体文件MIME_TYPE
    public static final String MIME_TYPE_AUDIO_MP3 = "audio/mp3";
    public static final String MIME_TYPE_VIDEO = "video";
    public static final String MIME_TYPE_AUDIO = "audio";
    public static final String MIME_TYPE_IMAGE = "image";
    //上传状态
    public static final int UPLOAD_STATUS_DEFAULT = 1;
    public static final int UPLOAD_STATUS_SUCCESS = 2;
    public static final int UPLOAD_STATUS_FAILURE = 3;
    public static final int UPLOAD_STATUS_LOADING = 4;
    //实体对象标记
    public static final String MEDIA_AUDIO_KEY = "media_audio_key";
    //设置音频预览播放倍速
    public static final int MEDIA_AUDIO_SEEK_1X = 0X1008;
    public static final int MEDIA_AUDIO_SEEK_1_5X = 0X1009;
    //音频预览播放状态
    public static final int AUDIO_PREVIEW_PLAY = 0X1010;
    public static final int AUDIO_PREVIEW_PAUSE = 0X1011;
    //更新音频预览界面进度标记
    public static final int AUDIO_PREVIEW_UPLOAD_PROGRESS_TAG = 0X1012;
    //设置发送集界面导航点击标记
    public static final int SEND_CONTACT_NAVIGATION_TAG = 0X1013;
    public static final int SEND_GROUP_NAVIGATION_TAG = 0X1014;
    public static final int SEND_DEPARTMENT_NAVIGATION_TAG = 0X1015;
    //更新发送数据
    public static final int UPDATE_SEND_DATA_TAG = 0X1016;
    //发送界面数据传递KEY
    public static final String SEND_GOTO_TYPE_KEY = "type";
    public static final String SEND_GOTO_TYPE_VALUE_GROUP = "group";
    public static final String SEND_GOTO_TYPE_VALUE_DEPARTMENT = "department";
    public static final String SEND_GOTO_KEY_OTHER_INFO = "other_info";
    public static final String SEND_GOTO_KEY_CONTACTS_INFO = "contacts_info";
    public static final String SEND_ID_LIST = "id_list";
    //更新联系人
    public  static final int UPDATE_CONTACTS_DATA_TAG = 0X1017;

    public static final String UPDATEDATA = "updateData";
    //发送制作网反馈问题
    public static final int SEND_MAKE_WEB_SUCCESS = 1;
    public static final int SEND_MAKE_WEB_NOT_WEB_USER = 2;
    public static final int SEND_MAKE_WEB_FAILE = 3;
    public static final int SEND_MAKE_WEB_HANDLE = 4;
    //传制作网标记
    public static final int AUDIO_PREVIEW_MAKE_WEB_TYPE = 0X1018;
    //发送标记
    public static final int AUDIO_PREVIEW_SEND_TYPE = 0X1019;
    //制作信息编辑页面记录进入页面标记
    public static final int GOTO_TYPE_RECORD = 0X1020;
    public static final int GOTO_TYPE_PREVIEW = 0X1021;
    //素材上传
    public static final int MATERIAL_UPDATE_UPLOAD_STATIC_TAG = 0X1022;
    public static final int MATERIAL_UPDATE_UPLOAD_CLICK_TAG = 0X1023;
    //更新音频预览上传标记
    public static final int AUDIO_PREVIEW_UPLOAD_UPLOAD_STATUS_TAG = 0X1024;
    public static final int UPDATE_RAIM_SIZE_TAG = 0X1025;
    //更新录音毫秒数
    public static final int UPDATE_RECORD_MILLI_SECOND = 0X1026;
    //音频编辑页面
    public static final String GOTO_TYPE_KEY = "goto_type";
    public static final String AUDIO_INFO_AUDIO_ID_KEY = "audio_id";
    public static final String AUDIO_INFO_AUDIO_NAME_KEY = "audio_name";
    public static final String AUDIO_INFO_CF_PERSON_KEY = "cf_person";
    public static final String AUDIO_INFO_CF_EVENT_KEY = "cf_event";
    public static final String AUDIO_INFO_AUDIO_KEYWORD_KEY = "audio_keyword";
    public static final String AUDIO_INFO_AUDIO_FILE_PATH_KEY = "audio_path";

    public final static int UPLOAD_SUCCESS = 0x7101;// success
    public final static int UPLOAD_FAIL = 0x7102;// fail

    public final static String MANUSCRIPT_MORE_DATA_FLAG = "delet_more_data_manuscript";//稿件列表删除冗余数据
    public final static String SETTINGS_SCREEN_INVERSION = "settings_screen_inversion";//1.2.0 版本新增，默认设置录音屏幕倒转
    //无网络标记
    public final static int NOT_NET_WORK = 0x1027;
    //加载审批标记
    public final static int LOAD_WAIT_APPROVAL_TAG = 0x1028;
    public final static int LOAD_ALREADY_APPROVAL_TAG = 0x1029;
    public final static int LOAD_WAIT_APPROVAL_NO_DATA_TAG = 0x1030;
    public final static int LOAD_ALREADY_APPROVAL_NO_DATA_TAG = 0X1031;
    //跳转稿件详情页面标记
    public final static String KEY_MANUSCRIPT_ID = "manuscript_id_key";
    public final static String KEY_MANUSCRIPT_INFO = "manuscript_info_key";
    public final static String KEY_APPROVATE_TEXT_CONTENT = "approvate_text_content_key";
    public final static String KEY_APPROVATE_AUDIO_PATH = "approvate_text_content_key";
    public final static String KEY_APPROVATE_AUDIO_TIME = "approvate_audio_time_key";
    public final static String KEY_ACTION_TYPE = "action_key";
}
