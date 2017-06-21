package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.response.GetManuscriptInfoResponse;
import com.tingtingfm.cbb.response.GetManuscriptProcessResponse;
import com.tingtingfm.cbb.response.ManuscriptCompareResponse;
import com.tingtingfm.cbb.response.ManuscriptInfoResponse;
import com.tingtingfm.cbb.response.ManuscriptProcessResponse;
import com.tingtingfm.cbb.ui.serve.ManuscriptServiceHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by tianhu on 2017/1/17.
 */

public class ManuscriptInterfaceUtils {
    /**
     * 上传成功
     */
    public static final int MANUSCRIPT_SUBMIT_SUCCESS = 0x30021;

    /**
     * 上传失败
     */
    public static final int MANUSCRIPT_SUBMIT_FAIL = 0x30022;

    /**
     * 对比后，进行上传
     */

    public static final int MANUSCRIPT_COMPARE_UPLOAD = 0x30023;

    /**
     * 对比后，服务修改过，弹出对话框，提示是否进行覆盖。
     */
    public static final int MANUSCRIPT_COMPARE_DIALOG = 0x30024;

    /**
     * 获取部门稿件审批流程列表
     */
    public static final int MANUSCRIPT_PROCESS = 0x30025;

    /**
     * 获取稿件信息，
     */
    public static final int MANUSCRIPT_INFO = 0x30026;


    /**
     * 获取搞件id，与提交稿件
     * @param currentObj 当前稿件对象
     * @param handler
     */
    public static void getManuscriptNetId(final ManuscriptInfo currentObj, final Handler handler) {
        try {
            //请求接口进行登录
            RequestEntity entity = new RequestEntity(UrlManager.UPLOAD_MANUSCRIPT);
            entity.addParams("id", currentObj.getServerId() + "");
            entity.addParams("title", currentObj.getTitle());
            entity.addParams("reporter", currentObj.getAuther());
            entity.addParams("content", currentObj.getServerId() == 0 ? "" : URLEncoder.encode(currentObj.getHtmlText(), "UTF-8"));
            entity.addParams("char_count", currentObj.getServerId() == 0 ? "0" : currentObj.getCharCount()+"");
            HttpRequestHelper.post(entity, new BaseRequestCallback<ManuscriptInfoResponse>() {
                @Override
                public void onStart() {
                    TTLog.i("lqsir ---onStart");
                }

                @Override
                public void onSuccess(ManuscriptInfoResponse response) {
                    if (response.getErrno() == 0 && null != response.getData()) {
                        //1、草稿页面---获取稿件id,更新数据库
                        //2、后台上传服务---音频上传完后，提交内容成功。(删除本稿件，本稿件音频，音频文本)
                        Message msg = handler.obtainMessage();
                        msg.what = MANUSCRIPT_SUBMIT_SUCCESS;
                        msg.arg1 = response.getData().getId();
                        handler.sendMessage(msg);
                    } else {
                        handler.sendEmptyMessage(MANUSCRIPT_SUBMIT_FAIL);
                    }
                }

                @Override
                public void onFail(int code, String errorMessage) {
                    handler.sendEmptyMessage(MANUSCRIPT_SUBMIT_FAIL);
                }

                @Override
                public void onCancel() {
                    TTLog.i("lqsir --- onCancel");
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 稿件对比
     *
     * @param con 上下文本
     * @param currentObj 稿件对象
     */
    public static void getCompareState(final Context con, ManuscriptInfo currentObj, final Handler handler) {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.PRE_UPLOAD_MANUSCRIPT);
        entity.addParams("id", currentObj.getServerId() + "");
        HttpRequestHelper.post(entity, new BaseRequestCallback<ManuscriptCompareResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(ManuscriptCompareResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    Message msg = handler.obtainMessage();
                    msg.arg1 = response.getData().getId();
                    if (response.getData().getIs_same() == 1) {
                        msg.what = MANUSCRIPT_COMPARE_UPLOAD;
                    } else {
                        msg.what = MANUSCRIPT_COMPARE_DIALOG;
                    }
                    handler.sendMessage(msg);
                } else {
                    handler.sendEmptyMessage(Constants.UPLOAD_FAIL);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                handler.sendEmptyMessage(Constants.UPLOAD_FAIL);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    /**
     * 获取稿件流程
     * @param con
     * @param handler
     */
    public static void getprocessData(final Context con, final Handler handler) {

        //获取部门稿件审批流程列表——张强
        final RequestEntity entity = new RequestEntity(UrlManager.GET_APPROVE_PROCESS);
        HttpRequestHelper.post(entity, new BaseRequestCallback<ManuscriptProcessResponse>() {

            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(ManuscriptProcessResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    Message msg = handler.obtainMessage();
                    msg.what = MANUSCRIPT_PROCESS;
                    msg.obj = response;
                    handler.sendMessage(msg);
                }else{
                    handler.sendEmptyMessage(Constants.UPLOAD_FAIL);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                handler.sendEmptyMessage(Constants.UPLOAD_FAIL);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    /**
     * 获取稿件信息  ------0未提审，1审批中，2审批成功
     *
     * @param con
     * @param handler
     */
    public static void getManuscriptSubmitInfo(final Context con, int manusId, final Handler handler) {

        //获取部门稿件审批流程列表——张强
        final RequestEntity entity = new RequestEntity(UrlManager.GET_MANUSCRIPT_INFO);
        entity.addParams("id", manusId + "");
        HttpRequestHelper.post(entity, new BaseRequestCallback<GetManuscriptInfoResponse>() {

            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(GetManuscriptInfoResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    Message msg = handler.obtainMessage();
                    msg.what = MANUSCRIPT_INFO;
                    msg.arg1 = response.getData().getId();
                    msg.arg2 = response.getData().getApproval();
                    handler.sendMessage(msg);
                }else{
                    handler.sendEmptyMessage(Constants.UPLOAD_FAIL);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                handler.sendEmptyMessage(Constants.UPLOAD_FAIL);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    /**
     * 获取稿件信息  ------ 0 - 失败, 1 - 成功, 2 - 已提审
     *
     * @param con
     * @param handler
     */
    public static void getManuscriptSubmit(final Context con, int manusId, int processId, final Handler handler) {
        final RequestEntity entity = new RequestEntity(UrlManager.SUBMIT_APPROVAL);
        entity.addParams("id", manusId + "");
        entity.addParams("tpl_id", processId + "");
        HttpRequestHelper.post(entity, new BaseRequestCallback<GetManuscriptProcessResponse>() {

            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(GetManuscriptProcessResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    Message msg = handler.obtainMessage();
                    msg.what = ManuscriptServiceHelper.SERVICE_HELPER_SUBMIT;
                    msg.arg1 = response.getData().getStatus();
                    handler.sendMessage(msg);
                }else{
                    Message msg = handler.obtainMessage();
                    msg.what = ManuscriptServiceHelper.SERVICE_HELPER_SUBMIT;
                    msg.arg1 = 0;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                Message msg = handler.obtainMessage();
                msg.what = ManuscriptServiceHelper.SERVICE_HELPER_SUBMIT;
                msg.arg1 = 0;
                handler.sendMessage(msg);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }


}
