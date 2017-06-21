package com.tingtingfm.cbb.common.upload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tingtingfm.cbb.common.log.TTLog;

/**
 * Created by lqsir on 2017/1/16.
 */

public class UploadService extends Service {
    UploadThread mUploadThread;

    @Override
    public void onCreate() {
        super.onCreate();
        TTLog.i(this.getClass().getSimpleName() + "  :: onCreate");
        mUploadThread = new UploadThread(this);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启上传文件服务
        if (!mUploadThread.isAliveing()) {
            mUploadThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TTLog.i(this.getClass().getSimpleName() + "  :: onDestroy");
    }
}
