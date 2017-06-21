package com.tingtingfm.cbb.common.db;

/**
 * Created by liming on 16/12/28.
 */

public class TableConstant {
    static final String TABLE_AUDIO_RECORD_TABLE_NAME = "audio_record_table";
    static final String TABLE_AUDIO_RECORD_ID = "_id";
    static final String TABLE_AUDIO_RECORD_MEDIA_ID = "media_id";
    static final String TABLE_AUDIO_RECORD_DATA = "_data";
    static final String TABLE_AUDIO_RECORD_SIZE = "_size";
    static final String TABLE_AUDIO_RECORD_DISPLAY_NAME = "_display_name";
    static final String TABLE_AUDIO_RECORD_MIME_TYPE = "mime_type";
    static final String TABLE_AUDIO_RECORD_TITLE = "title";
    static final String TABLE_AUDIO_RECORD_DATE_ADDED = "date_added";
    static final String TABLE_AUDIO_RECORD_DATE_MODIFIED = "date_modified";
    static final String TABLE_AUDIO_RECORD_UPLOAD_STATUS = "upload_status";
    static final String TABLE_AUDIO_RECORD_DURATION = "duration";
    static final String TABLE_AUDIO_RECORD_INTERVIEW_PERSION = "interview_persion";
    static final String TABLE_AUDIO_RECORD_INTERVIEW_EVENT = "interview_event";
    static final String TABLE_AUDIO_RECORD_INTERVIEW_KEYWORD = "interview_keyword";
    static final String TABLE_AUDIO_RECORD_LONGITUDE = "longitude";
    static final String TABLE_AUDIO_RECORD_LATITUDE = "latitude";
    static final String TABLE_AUDIO_RECORD_PLACE = "place";
    static final String TABLE_AUDIO_RECORD_IS_UPLOAD = "is_upload";
    static final String TABLE_AUDIO_RECORD_USER_ID = "user_id";
    static final String TABLE_AUDIO_RECORD_SLICE_COUNT = "slice_count";
    static final String TABLE_AUDIO_RECORD_SLICE_ID = "slice_id";
    static final String TABLE_AUDIO_RECORD_SLICE_SUCCESS = "slice_success";

    static final String TABLE_IMAGE_TABLE_NAME = "image_table";
    static final String TABLE_IMAGE_ID = "_id";
    static final String TABLE_IMAGE_MEDIA_ID = "media_id";
    static final String TABLE_IMAGE_DISPLAY_NAME = "_display_name";
    static final String TABLE_IMAGE_MIME_TYPE = "mime_type";
    static final String TABLE_IMAGE_UPLOAD_STATUS = "upload_status";
    static final String TABLE_IMAGE_USER_ID = "user_id";
    static final String TABLE_IMAGE_SLICE_COUNT = "slice_count";
    static final String TABLE_IMAGE_SLICE_ID = "slice_id";
    static final String TABLE_IMAGE_SLICE_SUCCESS = "slice_success";


    static final String TABLE_VIDEO_TABLE_NAME = "video_table";
    static final String TABLE_VIDEO_ID = "_id";
    static final String TABLE_VIDEO_MEDIA_ID = "media_id";
    static final String TABLE_VIDEO_DISPLAY_NAME = "_display_name";
    static final String TABLE_VIDEO_MIME_TYPE = "mime_type";
    static final String TABLE_VIDEO_UPLOAD_STATUS = "upload_status";
    static final String TABLE_VIDEO_USER_ID = "user_id";
    static final String TABLE_VIDEO_SLICE_COUNT = "slice_count";
    static final String TABLE_VIDEO_SLICE_ID = "slice_id";
    static final String TABLE_VIDEO_SLICE_SUCCESS = "slice_success";

    /**
     * 稿件管理-表字段
     **/
    static final String TABLE_MANUSCRIPT_TABLE_NAME = "manuscript_table";
    static final String TABLE_MANUSCRIPT_NET_ID = "manuscript_net_id";
    static final String TABLE_MANUSCRIPT_TITLE = "title";
    static final String TABLE_MANUSCRIPT_ALIAS_TITLE = "aliasTitle";
    static final String TABLE_MANUSCRIPT_AUTHER = "auther";
    static final String TABLE_MANUSCRIPT_UPLOAD_STATE = "uploadState";
    static final String TABLE_MANUSCRIPT_MANUS_TEXT = "manuscriptText";
    static final String TABLE_MANUSCRIPT_HTML_TEXT = "htmlText";
    static final String TABLE_MANUSCRIPT_CREATE_TIME = "createTime";
    static final String TABLE_MANUSCRIPT_ISSUBMIT = "isSubmit";
    static final String TABLE_MANUSCRIPT_CHARCOUNT = "char_count";
    static final String TABLE_MANUSCRIPT_USER_ID = "userId"; //用户标识
    static final String TABLE_MANUSCRIPT_TEXTEDIT = "textEdit";//搞件内容是否编辑过，1编辑过，0未编辑过。
    static final String TABLE_MANUSCRIPT_POSTILNUM = "postilNum";//批注数
    static final String TABLE_MANUSCRIPT_CLAIMSTATE = "claimState";//认领状态
    static final String TABLE_MANUSCRIPT_APPROVESTATE = "approveState";//审批状态
    static final String TABLE_MANUSCRIPT_PROCESSID = "processId";//审批流程
    static final String TABLE_MANUSCRIPT_ISMYCLAIM = "isMyClaim"; //是否被我认领
    static final String TABLE_MANUSCRIPT_ISOKCLAIM = "isOkClaim";//是否可以认领
    static final String TABLE_MANUSCRIPT_MODIFYTIME = "modifyTime";//内容修改保存时间


    /**
     * 稿件管理-草稿音频-表字段
     **/
    static final String TABLE_MANUSCRIPT_AUDIO_TABLE_NAME = "manuscript_audio_table";
    static final String TABLE_MANUSCRIPT_ID = "manuscript_id"; //草稿id
    static final String TABLE_MANUSCRIPT_AUDIO_NET_PATH = "audioNetPath"; //音频网络路径
    static final String TABLE_MANUSCRIPT_SLICE_COUNT = "slice_count";
    static final String TABLE_MANUSCRIPT_SLICE_ID = "slice_id";
    static final String TABLE_MANUSCRIPT_SLICE_SUCCESS = "slice_success";
}
