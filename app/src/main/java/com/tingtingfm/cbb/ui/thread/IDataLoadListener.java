package com.tingtingfm.cbb.ui.thread;

/**
 * Created by liming on 17/2/28.
 */

public interface IDataLoadListener {
    //开始加载数据
    public void startLoad();
    //加载数据
    public void loadMaterialData();
    //技术数据
    public void stopLoad();
}
