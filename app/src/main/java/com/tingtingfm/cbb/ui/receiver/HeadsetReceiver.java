package com.tingtingfm.cbb.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * 耳机广播接收器
 * Created by liming on 16/12/23.
 */

public class HeadsetReceiver extends BroadcastReceiver {
    //声明临时存储接口对象
    private HeadsetStatusListener listener;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            if(intent.hasExtra("state")) {
                //设置耳机状态——默认内部麦克风
                boolean _isStatus = false;
                //判断耳机状态
                if(intent.getIntExtra("state", 0) == 0) {
                    //未连接耳机
                    _isStatus = false;
                } else if(intent.getIntExtra("state", 0) == 1) {
                    //连接耳机
                    _isStatus = true;
                }
                //判断监听器
                if(this.listener != null){
                    //通知方法
                    this.listener.onHeadsetStatus(_isStatus);
                }
            }
        }
    }

    /**
     * 设置监听器方法
     * @param listener
     */
    public void setHeadsetStatusListener(HeadsetStatusListener listener){
        this.listener = listener;
    }

    /**
     * 耳机状态接口
     * @author liming
     */
    public interface HeadsetStatusListener{
        void onHeadsetStatus(boolean isStatus);
    }
}
