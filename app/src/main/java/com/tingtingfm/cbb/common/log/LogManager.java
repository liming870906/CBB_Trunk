package com.tingtingfm.cbb.common.log;

import java.util.LinkedList;

/**
 * Created by think on 2016/6/16.
 */
public class LogManager {
    private volatile static LogManager instance;
    private LinkedList<String> logList = new LinkedList<String>();

    private LogManager() {

    }

    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null) {
                    instance = new LogManager();
                }
            }
        }

        return instance;
    }

    public void addLog(String log) {
        logList.offer(log);
    }

    public LinkedList<String> getLogList() {
        return logList;
    }
}
