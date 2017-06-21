package com.tingtingfm.cbb.common.helper;

/**
 * Created by lqsir on 2016/4/20.
 */
public class ErrorCode {
    public static final int errorcode_author_failed = 100001;    //无权访问
    public static final int errorcode_sign_error = 100002;    //签名错误
    public static final int errorcode_argument_error = 100003;    //参数错误
    public static final int errorcode_album_noexist = 200001;    //专辑不存在或已删除
    public static final int errorcode_internal_error = 300001;  //内部错误
    public static final int errorcode_net_error = 800000; //网络异常
}
