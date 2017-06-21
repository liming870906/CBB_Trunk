package com.tingtingfm.cbb.ui.activity;

/**
 * Created by admin on 2017/1/17.
 */

public interface ManuscriptUploadInterface {

    /**
     * 当前是否上传中
     * @param uploadIng
     */
    void setUploadIng(boolean uploadIng);

    /**
     * 上传成功，或失败
     * @param b
     */
    void setUploadState(boolean b,int manuscriptId,String manuscriptName);

    /**
     * 数据上传次数，
     * @param length
     */
    void setUploadDataNum(int length);

    //回调提示显示对话框
    void callShowDialog(int manuscriptId,String manuscriptName);

    //回调更新网络id
    void callSetNetId(int netId,int manuscriptId);

    //其它位置已提审
    void callSubmited(int manuscriptId ,String manuscriptName);
}
