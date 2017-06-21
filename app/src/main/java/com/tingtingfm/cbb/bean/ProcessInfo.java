package com.tingtingfm.cbb.bean;

/**
 * Created by tianhu on 2017/1/17.
 */

public class ProcessInfo {
    
    private int process_id;
    private String process_name;

    public int getProcess_id() {
        return process_id;
    }

    public void setProcess_id(int process_id) {
        this.process_id = process_id;
    }

    public String getProcess_name() {
        return process_name;
    }

    public void setProcess_name(String process_name) {
        this.process_name = process_name;
    }

    @Override
    public String toString() {
        return "Process{" +
                "process_id=" + process_id +
                ", process_name=" + process_name +
                '}';
    }
}
