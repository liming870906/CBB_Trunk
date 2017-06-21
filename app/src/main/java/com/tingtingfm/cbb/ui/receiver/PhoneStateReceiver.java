package com.tingtingfm.cbb.ui.receiver;

/*****************************************************************************
 * PhoneStateReceiver.java
 * ****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.ArrayList;


/**
 * 电话广播
 * 拨打电话广播/Intent.ACTION_NEW_OUTGOING_CALL
 * 接电话广播/
 *
 * @author lqsir
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "TTFM/PhoneStateReceiver";
    private static ArrayList<PhoneStateChangeListener> listeners = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            pauseAudio();
        } else {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                telManager.listen(new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        // 注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
                        super.onCallStateChanged(state, incomingNumber);
                        switch (state) {
                            case TelephonyManager.CALL_STATE_IDLE://挂断(非人为暂停，需要恢复播放)
                                playAudio();
                                break;
                            case TelephonyManager.CALL_STATE_OFFHOOK://接听
                            case TelephonyManager.CALL_STATE_RINGING://响铃
                                pauseAudio();
                                break;
                        }
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    public static void setOnPhoneStateListener(PhoneStateChangeListener listener) {
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public static void removeOnPhoneStateListener(PhoneStateChangeListener listener) {
        if(listeners.contains(listener)){
            listeners.remove(listener);
        }
    }

    private void pauseAudio(){
        for (PhoneStateChangeListener listener : listeners){
            listener.pausePhoneStateChange();
        }
    }

    private void playAudio(){
        for (PhoneStateChangeListener listener : listeners){
            listener.playPhoneStateChange();
        }
    }

    public interface PhoneStateChangeListener {
        void pausePhoneStateChange();
        void playPhoneStateChange();
    }
}

