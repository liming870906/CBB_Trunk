package com.tingtingfm.cbb.common.upload;

import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;

/**
 * Created by lqsir on 2017/2/15.
 */

public class ScanTaskThread {
    private Thread thread;

    public ScanTaskThread() {
        if (thread == null) {
            thread = new Thread(new ScanTaskRunnable());
        }
    }

    public void start() {
        if (!GlobalVariableManager.isScanning) {
            thread.start();
        }
    }
}
