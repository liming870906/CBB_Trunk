package com.tingtingfm.cbb.ui.serve;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.ManuscriptInterfaceUtils;
import com.tingtingfm.cbb.common.utils.ManuscriptUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.ui.activity.ManuscriptUploadInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by tianhu on 2017/1/17.
 */

public class ManuscriptService extends Service {

    /**
     * 稿件临时对象
     */
    private ManuscriptInfo manuscriptInfo,manuscriptSubmit;
    /**
     * 稿件数据管帮助类
     */
    private DBManuscriptManager dbManager;
    /**
     * 音频信息
     */
    private MediaInfo mediaInfo;
    /**
     * 音频上传下标
     */
    private int filePos = 0;
    /**
     * 音频列表
     */
    private List<ManuscriptInfo> manuscriptInfos = new ArrayList<ManuscriptInfo>();
    /**
     * 上传状态，同一时间，只能上传一个。
     */
    private static boolean uploading = false;
    /**
     * false获取网络id.true上传稿件内容
     */
    private boolean uploadManuscriptFlag = false;
    /**
     * 上传接口总数
     */
    private int totalTime = 0;
    /**
     * 上传接口下标
     */
    private int timeSize;
    /**
     * 当前稿件音频集合
     */
    private ArrayList<MediaInfo> mediaInfos;
    /**
     * handler消息处理
     */
    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case ManuscriptServiceHelper.SERVICE_HELPER_SUBMIT:
                    if (msg.arg1 == 1) {
                        TTLog.e("manuscript 提交成功");
                    } else {
                        TTLog.e("manuscript 提交失败");
                    }
                    manuscriptSubmit.setIsSubmit(msg.arg1 == 1 ? 1 : 0);
                    dbManager.updataManuscriptSubmitState(manuscriptSubmit);
                    submitSendBroadcast();
                    manuscriptSubmit = null;
                    //删除当前，执行一下数据
                    break;
                case ManuscriptInterfaceUtils.MANUSCRIPT_INFO:
                    if(msg.arg1 == 0 ){
                        TTLog.e("manuscript 提审id返回值为:0,重新获取id");
                        uploadManuscriptFlag = false;
                        manuscriptInfo.setServerId(0);
                        ManuscriptInterfaceUtils.getManuscriptNetId(manuscriptInfo, handler);
                    }else{
                        if (msg.arg2 == 0) {
                            TTLog.e("manuscript 未提审，进行对比-");
                            //进行比对
                            ManuscriptInterfaceUtils.getCompareState(ManuscriptService.this, manuscriptInfo, handler);
                        }else {
                            TTLog.e("manuscript 提审返回值为:"+msg.arg2+" 已提审");
                            int mInfoid = manuscriptInfo.getId();//保存提交id,
                            String name = manuscriptInfo.getTitle();
                            //删除当前，执行一下数据
                            deleteNextData(1);
                            if(null != onProgressListener){
                                onProgressListener.callSubmited(mInfoid,name);
                            }
                        }
                    }
                    break;
                case Constants.UPLOAD_SUCCESS://音频上传成功
                    Bundle bundle = msg.getData();
                    String url = bundle.getString("url");
                    //将数据源换成网络地址
                    manuscriptInfo.setHtmlText(ManuscriptUtils.convertAudioFlag(manuscriptInfo.getHtmlText(), mediaInfo.getAbsolutePath(), url, mediaInfo.getFullName()));
                    //音频上传完，修改音频上传状态
                    mediaInfo.setIsUpdateAudioInfo(1);
                    mediaInfo.setAudioNetPath(url);
                    updataSameData(url);
                    dbManager.MediaInfoUploadState(mediaInfo);
                    ++filePos;
                    if(null != onProgressListener) {
                        onProgressListener.setUploadDataNum(timeSize * filePos);
                    }
                    uploadAudioDatas();
                    break;
                case Constants.UPLOAD_FAIL://音频上传失败。
                    if (null != onProgressListener) {
                        onProgressListener.setUploadState(false, manuscriptInfo.getId(),manuscriptInfo.getTitle());
                    }
                    //删除当前，执行一下数据
                    deleteNextData(0);
                    //通知更新列表
                    sendBroadcast();
                    break;
                case ManuscriptInterfaceUtils.MANUSCRIPT_SUBMIT_SUCCESS:
                    if (uploadManuscriptFlag) {
                        TTLog.e("manuscript 上传稿件成功，百分比回调，上传状态更新，回调页面更新页面图标改变");
                        ++filePos;
                        if(null != onProgressListener){
                            onProgressListener.setUploadDataNum(timeSize * filePos);
                        }
                        if (null != onProgressListener) {
                            onProgressListener.setUploadState(true, manuscriptInfo.getId(),manuscriptInfo.getTitle());
                        }
                        //删除当前，执行一下数据
                        deleteNextData(0);
                        //通知更新列表
                        sendBroadcast();
                    } else {
                        manuscriptInfo.setServerId(msg.arg1);
                        if (null != onProgressListener) {
                            onProgressListener.callSetNetId(msg.arg1,manuscriptInfo.getId());
                        }
                        dbManager.updataManuscriptNetId(manuscriptInfo);
                        TTLog.e("manuscript 缓存id成功，更新草稿页面id:"+msg.arg1);
                        //调对比接口。
                        ManuscriptInterfaceUtils.getCompareState(ManuscriptService.this, manuscriptInfo, handler);
                    }
                    break;
                case ManuscriptInterfaceUtils.MANUSCRIPT_SUBMIT_FAIL:
                    if (uploadManuscriptFlag) {
                        TTLog.e("manuscript 上传稿件失败上传状态更新，回调页面更新页面图标改变");
                        if (null != onProgressListener) {
                            onProgressListener.setUploadState(false, manuscriptInfo.getId(),manuscriptInfo.getTitle());
                        }
                    } else {
                        TTLog.e("manuscript 上传失败，请重新上传");
                        manuscriptInfo.setServerId(0);
                        dbManager.updataManuscriptNetId(manuscriptInfo);
                        if (null != onProgressListener) {
                            onProgressListener.setUploadState(false, manuscriptInfo.getId(),manuscriptInfo.getTitle());
                        }
                    }
                    //删除当前，执行一下数据
                    deleteNextData(0);
                    //通知更新列表
                    sendBroadcast();
                    break;
                case ManuscriptInterfaceUtils.MANUSCRIPT_COMPARE_UPLOAD:
                    if(msg.arg1 == 0){
                        TTLog.e("manuscript 对比，稿件id返回为:0 重新获取id");
                        uploadManuscriptFlag = false;
                        ManuscriptInterfaceUtils.getManuscriptNetId(manuscriptInfo, handler);
                    }else{
                        TTLog.e("manuscript 对比成功，未修改，进行上传");
                        upload();
                    }
                    break;
                case ManuscriptInterfaceUtils.MANUSCRIPT_COMPARE_DIALOG://对比后，提示显示对话框
                    if(msg.arg1 == 0){
                        TTLog.e("manuscript 对比，稿件id返回为:0 重新获取id");
                        uploadManuscriptFlag = false;
                        ManuscriptInterfaceUtils.getManuscriptNetId(manuscriptInfo, handler);
                    }else{
                        if(manuscriptInfo.getProcessId() != -2){
                            TTLog.e("manuscript 对比成功修改后，进行对话框提示");
                            if (null != onProgressListener) {
                                onProgressListener.callShowDialog(manuscriptInfo.getId(),manuscriptInfo.getTitle());
                            }
                        }else{
                            //删除当前，执行一下数据
                            deleteNextData(1);
                        }
                    }
                    break;
            }
        }
    };

    /**
     * 更新相同数据状态
     * @param url 网络音频路径
     */
    private void updataSameData(String url) {
        for (MediaInfo m:mediaInfos ) {
             if(m.getAbsolutePath().equals(mediaInfo.getAbsolutePath())){
                 m.setIsUpdateAudioInfo(1);
                 m.setAudioNetPath(url);
                 dbManager.MediaInfoUploadState(m);
             }
        }
    }

    /**
     * 发送广播，更新稿件列表界面
     */
    private void submitSendBroadcast() {
        Intent intent = new Intent();
        intent.setAction("com.caiji.manuscript.submit");
        intent.putExtra("manuscriptId",manuscriptSubmit.getId());
        intent.putExtra("submit",manuscriptSubmit.getIsSubmit());
        sendBroadcast(intent);
    }

    /**
     * 发送广播，更新稿件列表界面
     */
    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction("com.caiji.manuscript");
        ManuscriptService.this.sendBroadcast(intent);
    }

    /**
     * 进行上传
     */
    public void upload() {
        //获取当前稿件音频数据进行上传
        String saveAudioDir = StorageUtils.getSDCardStorageDirectory(this).getPath() + Constants.AUDIO_CACHE_DIR + File.separator +
                TimeUtils.getYMDHMS(manuscriptInfo.getCreateTime());
        File fileDir = new File(saveAudioDir);
        if (fileDir.exists()) {//内容保存过。路径必定存在
            filePos = 0;
            mediaInfos = dbManager.getMediaInfoInfos(manuscriptInfo.getId());
            setUploadDataNum(mediaInfos.size());
            uploadAudioDatas();
        } else {
            setUploadDataNum(0);
            TTLog.e("manuscriptTH ---------------not audio Path");
            TTLog.e("manuscript 进行搞件内容上传");
            uploadManuscriptFlag = true;
            //进行上传
            ManuscriptInterfaceUtils.getManuscriptNetId(manuscriptInfo, handler);
        }
    }

    /**
     * 上传音频总数量，用于上传进度使用
     *
     * @param size 音频数
     */
    private void setUploadDataNum(int size) {
        if (null != onProgressListener) {
            //长度，音频数+ 稿件接口+提交接口
            totalTime = size+2;
            timeSize = 100 / totalTime;//每次大小数
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开始上传数
     * @param manu 稿件对象
     */
    public void startUploadData(ManuscriptInfo manu) {
        ManuscriptInfo mInfp = setManuscriptInfo(manu);
        if(mInfp.getProcessId() != 0){
            manuscriptInfos.add(0,mInfp);
        }else{
            manuscriptInfos.add(mInfp);
        }
        updateUIAndData(mInfp);
        uploadData();

    }

    /**
     * 上传数据
     */
    private void uploadData() {
        if (manuscriptInfos.size() > 0 && !uploading) {
            uploading = true;
            manuscriptInfo = manuscriptInfos.get(0);
            if (manuscriptInfo.getServerId() == 0) {
                uploadManuscriptFlag = false;
                TTLog.e("manuscript 获取id");
                ManuscriptInterfaceUtils.getManuscriptNetId(manuscriptInfo, handler);
            } else {
                //是否提审过
                ManuscriptInterfaceUtils.getManuscriptSubmitInfo(this, manuscriptInfo.getServerId(), handler);
            }
        }
    }

    /**
     * 更新开始上传UI,与更新数据库状态
     * @param manu 稿件对象
     */
    private void updateUIAndData(ManuscriptInfo manu) {
        if (null != onProgressListener) {
            onProgressListener.setUploadIng(true);
        }
        //更新数据上传中
        dbManager.updataManuscriptUploadState(manu);
    }


    /**
     * 复制要上传的对象
     *
     * @param manu 稿件对象
     */
    private ManuscriptInfo setManuscriptInfo(ManuscriptInfo manu) {
        ManuscriptInfo manuscriptInfo = new ManuscriptInfo();
        manuscriptInfo.setId(manu.getId());
        manuscriptInfo.setHtmlText(manu.getHtmlText());
        manuscriptInfo.setAuther(manu.getAuther());
        manuscriptInfo.setUploadState(manu.getUploadState());
        manuscriptInfo.setAliasTitle(manu.getAliasTitle());
        manuscriptInfo.setCreateTime(manu.getCreateTime());
        manuscriptInfo.setIsSubmit(manu.getIsSubmit());
        manuscriptInfo.setManuscriptText(manu.getManuscriptText());
        manuscriptInfo.setServerId(manu.getServerId());
        manuscriptInfo.setTitle(manu.getTitle());
        manuscriptInfo.setCharCount(manu.getCharCount());
        manuscriptInfo.setProcessId(manu.getProcessId());
        return manuscriptInfo;
    }

    /**
     * 获取当前稿件音频数据进行上传
     */
    private void uploadAudioDatas() {
        //有音频，则先上传音频。
        if (null != mediaInfos && mediaInfos.size() > 0 && filePos < mediaInfos.size()) {
            mediaInfo = mediaInfos.get(filePos);
            if (mediaInfo.getIsUpdateAudioInfo() != 1) {
                TTLog.e("manuscript 上传音频：" + filePos);
                mediaInfo.setExtraInteger(manuscriptInfo.getServerId());
                new ManuscriptAudioUploadThread(handler, mediaInfo).start();
            } else {
                TTLog.e("manuscript " + filePos + "该音频已传");
                ++filePos;
                if (null != onProgressListener) {
                    onProgressListener.setUploadDataNum(timeSize * filePos);
                }
                manuscriptInfo.setHtmlText(ManuscriptUtils.convertAudioFlag(manuscriptInfo.getHtmlText(), mediaInfo.getAbsolutePath(), mediaInfo.getAudioNetPath(), mediaInfo.getFullName()));
                uploadAudioDatas();
            }
        } else {//没有音频上传后。直接直接进行提交搞件
            TTLog.e("manuscript 进行搞件内容上传");
            uploadManuscriptFlag = true;
            //进行上传
            ManuscriptInterfaceUtils.getManuscriptNetId(manuscriptInfo, handler);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbManager = DBManuscriptManager.getInstance(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 取消提审
     * @param manuscriptId  搞件id
     * @param flag -1 草稿界面取消删除数据 当前数据不上传，-2,提审界面，不删除数据可以上传成功
     */
    public void cancelSubmit(int manuscriptId,int flag) {
        for (int i = 0; i < manuscriptInfos.size() ; i++) {
            ManuscriptInfo mInfo = manuscriptInfos.get(i);
            if(mInfo.getId() == manuscriptId){
                if(flag == -1){
                    TTLog.e("manuscript 草稿页面取消，删除当前数");
                    deleteNextData(1);

                }
                break;
            }
        }
    }

    /**
     * 删除当前，执行一下数据
     * @param flog 1 的时候需要进行处理，上传中状态修改为未上传。0值时，此处不进行处理。ManuscriptServicehelper_>setUploadState()进行处理。
     */
    public void deleteNextData(int flog) {
        TTLog.e("manuscript 进行下一下稿件上传");
        manuscriptInfos.remove(manuscriptInfo);
        manuscriptInfo = null;
        uploading = false;
        uploadData();
    }


    public class MyBinder extends Binder {
        public ManuscriptService getService() {
            return ManuscriptService.this;
        }
    }

    /**
     * 更新进度的回调接口
     */
    private ManuscriptUploadInterface onProgressListener;

    /**
     * 注册回调接口的方法，供外部调用
     * @param onProgressListener
     */
    public void setOnProgressListener(ManuscriptUploadInterface onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    /**
     * 进行提交
     * @param manu 稿件对象
     */
    public void startSubmit(ManuscriptInfo manu){
        manuscriptSubmit = manu;
        TTLog.e("manuscript 提交搞件");
        ManuscriptInterfaceUtils.getManuscriptSubmit(ManuscriptService.this, manuscriptSubmit.getServerId(), manuscriptSubmit.getProcessId(), handler);
    }
}
