package com.shuyu.waveview;

/**
 * Created by shuyu on 2016/12/19.
 */

public class Manager {

    private static Manager mInstance;

    public static synchronized Manager newInstance() {
        if (mInstance == null) {
            mInstance = new Manager();
        }
        return mInstance;
    }
}
