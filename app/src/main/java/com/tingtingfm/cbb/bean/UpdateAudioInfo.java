package com.tingtingfm.cbb.bean;

/**
 * Created by lqsir on 2017/3/9.
 */

public class UpdateAudioInfo {
    private int succ;
    private int id;

    public int getSucc() {
        return succ;
    }

    public void setSucc(int succ) {
        this.succ = succ;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UpdateAudioInfo{" +
                "succ=" + succ +
                ", id=" + id +
                '}';
    }
}
