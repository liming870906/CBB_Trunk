package com.tingtingfm.cbb.ui.thread;

import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;

/**
 * Created by liming on 17/2/28.
 */

public class DataRunnable implements Runnable {
    private IDataLoadListener mListener;

    public DataRunnable(IDataLoadListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void run() {
        GlobalVariableManager.isLoadMaterial = true;
        mListener.loadMaterialData();
        //加载数据
        GlobalVariableManager.isLoadMaterial = false;
        mListener.stopLoad();
    }
}
