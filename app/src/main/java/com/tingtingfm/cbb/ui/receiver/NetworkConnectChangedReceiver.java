package com.tingtingfm.cbb.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.upload.ScanTaskThread;

import static android.content.ContentValues.TAG;

/**
 * 网络连接监听
 */
public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            int netWorkType = intent.getExtras().getInt(ConnectivityManager.EXTRA_NETWORK_TYPE);
            boolean isWiFi = netWorkType == ConnectivityManager.TYPE_WIFI;
            boolean isMobile = netWorkType == ConnectivityManager.TYPE_MOBILE;
            NetworkInfo info = manager.getNetworkInfo(netWorkType);
            boolean connected = info.isConnected();

            if (isWiFi) {
                if (connected) {
                    TTLog.i(TAG + " WIFI NetworkInfo.State.CONNECTED");
                    new ScanTaskThread().start();
                } else {
                    TTLog.i(TAG + " WIFI NetworkInfo.State.DISCONNECTED");
                }
            } else if (isMobile) {
                if (connected) {
                    TTLog.i(TAG + " mobile NetworkInfo.State.CONNECTED");
                    new ScanTaskThread().start();
                } else {
                    TTLog.i(TAG + " mobile NetworkInfo.State.DISCONNECTED");
                }
            }
        }
    }

}