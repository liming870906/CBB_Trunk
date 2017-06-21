package com.tingtingfm.cbb.bean;

import java.io.Serializable;

/**
 * Created by tianhu on 2017/1/10.
 */

public class ManuscriptInfo implements Serializable {
    private int id; //稿件本地id
    private int serverId;//稿件云端id
    private String title;//标题
    private int aliasTitle;//别名
    private String auther;//记者
    private String htmlText;//html稿件内容
    private String manuscriptText;//纯文本内容
    private String createTime;//创建时间
    private int uploadState;//0未上传，1已上传, 2上传中， 3上传失败
    private int isSubmit;//是否提交
    private int charCount;//纯文本字总数
    private int textEdit = 0;//标记文本上传过程中，是否在次编辑。
    private int processId = 0;//0不提审，非0进行提审

    //-----------------1.3增加字段
    private int approveState;//审核状态

    public int getApproveState() {
        return approveState;
    }

    public void setApproveState(int approveState) {
        this.approveState = approveState;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ManuscriptInfo) {
            ManuscriptInfo that = (ManuscriptInfo) o;
            return this.id == that.id && this.getIsSubmit() == that.getIsSubmit();
        }
        return false;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getTextEdit() {
        return textEdit;
    }

    public void setTextEdit(int textEdit) {
        this.textEdit = textEdit;
    }

    public int getCharCount() {
        return charCount;
    }

    public void setCharCount(int charCount) {
        this.charCount = charCount;
    }

    public int getIsSubmit() {
        return isSubmit;
    }

    public void setIsSubmit(int isSubmit) {
        this.isSubmit = isSubmit;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getManuscriptText() {
        return manuscriptText;
    }

    public void setManuscriptText(String manuscriptText) {
        this.manuscriptText = manuscriptText;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAliasTitle() {
        return aliasTitle;
    }

    public void setAliasTitle(int aliasTitle) {
        this.aliasTitle = aliasTitle;
    }

    public String getAuther() {
        return auther;
    }

    public void setAuther(String auther) {
        this.auther = auther;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }
}
