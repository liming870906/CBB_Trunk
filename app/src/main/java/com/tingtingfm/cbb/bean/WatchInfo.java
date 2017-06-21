package com.tingtingfm.cbb.bean;

/**
 * Created by admin on 2017/4/20.
 */

public class WatchInfo {
    private String url; //头像 url
    private String approvalName; //审批人名称
    private int approvalLayer; //审批层级 （1 一审2二审3三审）
    private int ApprovalOperation; //审批操作 （0退回1通过）
    private String operationTime; //审批操作时间
    private String approvalText;//批注文本内容
    private String audioUrl; //批注音频URL
    private int audioTime; //批注音频时长

    public int getApprovalLayer() {
        return approvalLayer;
    }

    public void setApprovalLayer(int approvalLayer) {
        this.approvalLayer = approvalLayer;
    }

    public String getApprovalName() {
        return approvalName;
    }

    public void setApprovalName(String approvalName) {
        this.approvalName = approvalName;
    }

    public int getApprovalOperation() {
        return ApprovalOperation;
    }

    public void setApprovalOperation(int approvalOperation) {
        ApprovalOperation = approvalOperation;
    }

    public int getAudioTime() {
        return audioTime;
    }

    public void setAudioTime(int audioTime) {
        this.audioTime = audioTime;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(String operationTime) {
        this.operationTime = operationTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApprovalText() {
        return approvalText;
    }

    public void setApprovalText(String approvalText) {
        this.approvalText = approvalText;
    }
}
