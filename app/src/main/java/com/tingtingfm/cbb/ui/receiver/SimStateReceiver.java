package com.tingtingfm.cbb.ui.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tingtingfm.cbb.common.cache.MediaDataManager;

/**
 * Created by liming on 2017/5/23.
 */

public class SimStateReceiver extends BroadcastReceiver {
    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static int SIM_VALID = 0;
    private final static int SIM_INVALID = 1;
    private static OnSimStateCallBackListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("info","==>sim state changed");
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            int state = tm.getSimState();
            switch (state) {
                case TelephonyManager.SIM_STATE_READY :
                    Log.i("info","==>Sim is ready");
                    callbackSimState(SIM_VALID);
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN :
                case TelephonyManager.SIM_STATE_ABSENT :
                case TelephonyManager.SIM_STATE_PIN_REQUIRED :
                case TelephonyManager.SIM_STATE_PUK_REQUIRED :
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
                default:
                    Log.i("info","==>Sim invalid");
                    callbackSimState(SIM_INVALID);
                    break;
            }
        }
    }

    private void callbackSimState(int states){
        if(this.mListener != null){
            this.mListener.callBack(states);
        }
    }

    public static boolean isListener(){
        if(mListener != null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 添加Sim卡状态监听方法
     * @param listener Sim卡状态回调接口
     */
    public static void addOnSimStateCallBackListener(OnSimStateCallBackListener listener){
        mListener = listener;
    }

    /**
     * 移除Sim卡状态监听方法
     */
    public static void removeaddOnSimStateCallBackListener(){
        mListener = null;
    }

    /**
     * Sim卡状态回调接口
     */
    public interface OnSimStateCallBackListener{
        void callBack(int simState);
    }
}
