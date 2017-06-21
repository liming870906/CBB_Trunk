package com.tingtingfm.cbb.ui.thread;

import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;

/**
 * Created by liming on 17/2/28.
 */

public class DataTaskThread {
    private Thread mThread = null;
    private IDataLoadListener listener;


    /**
     * 添加监听器方法
     * @param listener
     * @return
     */
    public DataTaskThread setLoadListener(IDataLoadListener listener){
        this.listener = listener;
        return this;
    }

    /**
     * 生成数据对象
     * @return
     */
    public DataTaskThread crateNewMaterialRunnable(){
        if (mThread == null && listener != null)
            mThread = new Thread(new DataRunnable(listener));
        return this;
    }
    /**
     * 开启线程
     */
    public void start() {
        if (!GlobalVariableManager.isLoadMaterial) {
            listener.startLoad();
            mThread.start();
        }
    }
}
