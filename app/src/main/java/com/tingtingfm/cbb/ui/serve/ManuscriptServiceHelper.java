package com.tingtingfm.cbb.ui.serve;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;
import com.tingtingfm.cbb.common.utils.ToastUtils;
import com.tingtingfm.cbb.ui.activity.ManuscriptAddActivity;
import com.tingtingfm.cbb.ui.activity.ManuscriptUploadInterface;

/**
 * Created by tianhu on 2017/1/17.
 */

public class ManuscriptServiceHelper {

    /**
     *稿件服务通信帮助者
     */
    private static ManuscriptServiceHelper helper;
    /**
     * 稿件上传，提交服务
     */
    private ManuscriptService msgService;
    /**
     * 上下文
     */
    private Context context;
    /**
     * handler
     */
    private static Handler handler;

    /**
     * 上传开始通知常量
     */
    public static final int SERVICE_HELPER_UPLOADING = 0x0107;
    /**
     * 上传结果常量(成功，失败)
     */
    public static final int SERVICE_HELPER_JG = 0x0108;
    /**
     * 稿件上传份数常量(上传进度，执行几个接口，分几份)
     */
    public static final int SERVICE_HELPER_TOTAL = 0x0109;
    /**
     * 显示对话框常量
     */
    public static final int SERVICE_HELPER_DIALOG =0x6110;
    /**
     * 提交审批常量
     */
    public static final int SERVICE_HELPER_SUBMIT =0x6111;
    /**
     * 获取稿件id常量
     */
    public static final int SERVICE_HELPER_UPDATENETID =0x6112;
    /**
     * 服务端已提审常量
     */
    public static final int SERVICE_HELPER_SUBMITED=0x6113;

    /**
     * 当前稿件id
     */
    private int mId = 0;
    /**
     * 区分使用者：0添加界面，1稿件上传流程界面
     */
    private int pageFlag = -1;

    /**
     * 当前数据id,在草稿页面或进度条页面都设置值。不在这二个页面，为0,为0不提示对话框提示，直接进入下一任务
     * @param manuscriptId 稿件id
     */
    public void setManuscriptId(int manuscriptId) {
        this.mId = manuscriptId;
    }

    private ManuscriptServiceHelper() {

    }

    /**
     * 区分使用者：0添加界面，1稿件上传流程界面
     * @return
     */
    public int getPageFlag() {
        return pageFlag;
    }

    /**
     * 区分 添加界面(0)与流程进度显示界面(1)。
     * @param pageFlag
     */
    public void setPageFlag(int pageFlag) {
        this.pageFlag = pageFlag;
    }

    public static ManuscriptServiceHelper getInstance(Handler handl) {
        if (null == helper) {
            helper = new ManuscriptServiceHelper();
        }
        handler = handl;
        return helper;
    }

    /**
     * 绑定服务
     */
    public void bindService(){
        if(null == msgService){
            context = TTApplication.getAppContext();
            context.bindService(new Intent(context, ManuscriptService.class),
                    mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 开始上传数据
     *
     * @param currentOb
     */
    public void startUpload(ManuscriptInfo currentOb) {
        if(null!= msgService){
            msgService.startUploadData(currentOb);
        }
    }

    /**
     * 关闭服务
     *
     * @param context
     */
    public void stopService(Context context) {
        try {
            if (msgService != null) {
                context.unbindService(mServiceConnection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            msgService = ((ManuscriptService.MyBinder) iBinder).getService();
            msgService.setOnProgressListener(new ManuscriptUploadInterface() {

                @Override
                public void setUploadIng(boolean uploadIng) {//开始进行上传事件
                    handler.sendEmptyMessage(SERVICE_HELPER_UPLOADING);
                }

                @Override
                public void setUploadState(boolean b,int manuscriptId,String manuscriptName) {//上传稿件内容返回结果事件
                    Message msg = handler.obtainMessage();
                    msg.what = SERVICE_HELPER_JG;
                    ManuscriptInfo manuscriptInfo = DBManuscriptManager.getInstance(context).findManuscriptInfo(manuscriptId);
                    if(null != manuscriptInfo){
                        if (manuscriptInfo.getTextEdit() == 1) {//编辑过。显示未上传图标
                            manuscriptInfo.setUploadState(0);
                            manuscriptInfo.setTextEdit(0);
                            DBManuscriptManager.getInstance(context).updataManuscriptTextEdit(manuscriptInfo);
                        } else {
                            if (!b) {//上传失败
                                if (pageFlag == 0) {
                                    showToast(manuscriptName);
                                }
                            }
                            msg.arg1 = b ? 1 : 0;
                            manuscriptInfo.setUploadState(b ? 1 : 3);
                        }
                        DBManuscriptManager.getInstance(context).updataManuscriptUploadState(manuscriptInfo);
                        msg.arg2 = manuscriptId;//当前操作完成Id
                        handler.sendMessage(msg);
                    }else{
                        msg.arg1 = b ? 1 : 0;
                        msg.arg2 = manuscriptId;//当前操作完成Id
                        handler.sendMessage(msg);
                    }
                }

                @Override
                public void setUploadDataNum(int length) {//返回上上传数量回调事件
                    Message msg = handler.obtainMessage();
                    msg.what = SERVICE_HELPER_TOTAL;
                    msg.arg1 = length;
                    handler.sendMessage(msg);
                }

                @Override
                public void callShowDialog(int manuscriptId,String manuscriptName) {
                    if(mId == manuscriptId){
                        Message msg =  handler.obtainMessage();
                        msg.what = SERVICE_HELPER_DIALOG;
                        msg.arg1 = manuscriptId;
                        handler.sendMessage(msg);
                    }else{
                        //删除当前，执行一下数据
                        msgService.deleteNextData(1);
                        showToast(manuscriptName);
                    }
                }

                @Override
                public void callSetNetId(int netId,int manuscriptId) {
                    Message msg =  handler.obtainMessage();
                    msg.what = SERVICE_HELPER_UPDATENETID;
                    msg.arg1 = netId;
                    msg.arg2 = manuscriptId;
                    handler.sendMessage(msg);
                }

                @Override
                public void callSubmited(int manuscriptId,String manuscriptName) {
                    if (mId == manuscriptId) {
                        Message msg = handler.obtainMessage();
                        msg.what = SERVICE_HELPER_SUBMITED;
                        msg.arg1 = manuscriptId;
                        handler.sendMessage(msg);
                    } else {
                        //删除当前，执行一下数据
                        msgService.deleteNextData(1);
                        String name = manuscriptName;
                        if (name.length() > 7) {
                            name = name.substring(0, 6) + "...";
                        }
                        ToastUtils.showToast(context, context.getString(R.string.manuscript_submit_xx_fail, name));
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /**
     * 消息提示
     * @param manuscriptName 稿件名称
     */
    private void showToast(String manuscriptName) {
        String name = manuscriptName;
        if(name.length()>7){
            name = name.substring(0,6)+"...";
        }
        ToastUtils.showToast(context, context.getString(R.string.manuscript_upload_XX_fail,name));
    }


    /**
     * 对话框，可以进行上传
     */
    public void startUpload() {
        if(null!=msgService){
            msgService.upload();
        }
    }

    /**
     * 取消提审
     * @param manuscriptId  搞件id
     * @param flag -1 草稿界面取消删除数据 当前数据不上传，0 提审界面，不删除数据可以上传成功
     */
    public void cancelSubmit(int manuscriptId,int flag) {
        if(null!=msgService){
            msgService.cancelSubmit(manuscriptId , flag);
        }
    }

    //提交审批
    public void submit(ManuscriptInfo mInfo) {
        if(null!=msgService){
            msgService.startSubmit(mInfo);
        }
    }
}
