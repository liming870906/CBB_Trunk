package com.tingtingfm.cbb.ui.play;

import android.content.Context;

import com.tingtingfm.cbb.TTApplication;

/**
 * Created by lqsir on 2017/2/21.
 */

public class MediaUtil {
    public final static String TAG = "VLC/Util";

    public static MediaCore getLibVlcInstance() throws LibMediaException {
        MediaCore instance = MediaCore.getExistingInstance();
        if (instance == null) {
            // Thread.setDefaultUncaughtExceptionHandler(new VlcCrashHandler());

            instance = MediaCore.getInstance();
            Context context = TTApplication.getAppContext();
            instance.init(context);
        }
        return instance;
    }


    public static void destoryMediaCore() {
        MediaCore instance = MediaCore.getExistingInstance();

        if (instance != null) {
            instance.destroy();
            instance = null;
        }
    }

}
