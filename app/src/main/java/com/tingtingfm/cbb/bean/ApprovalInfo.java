package com.tingtingfm.cbb.bean;

/**
 * Created by admin on 2017/4/14.
 */

public class ApprovalInfo {
    //稿件ID
    private int  manuscriptId;
    //稿件名称
    private String manuscriptName;
    //审批状态（0：未提审 1：等待一审 2:等待二审 3:等待三审 4：终审完成 5:审核退回）未提审是草稿，默认是0
    private int state;
    //审批人及提审人
    private String proposer;
    //稿件的修改时间
    private String time;

    public int getManuscriptId() {
        return manuscriptId;
    }

    public void setManuscriptId(int manuscriptId) {
        this.manuscriptId = manuscriptId;
    }

    public String getManuscriptName() {
        return manuscriptName;
    }

    public void setManuscriptName(String manuscriptName) {
        this.manuscriptName = manuscriptName;
    }

    public String getProposer() {
        return proposer;
    }

    public void setProposer(String proposer) {
        this.proposer = proposer;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ApprovalInfo{" +
                "manuscriptId=" + manuscriptId +
                ", manuscriptName='" + manuscriptName + '\'' +
                ", state=" + state +
                ", proposer='" + proposer + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
