package com.tingtingfm.cbb;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.StrictMode;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.crash.CrashManager;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.LoadLocationAudioUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.ui.activity.ActivityStack;


public class TTApplication extends Application {
    private static final String TAG = "TTFM/TTApplication";

    private static TTApplication instance = null;

    private boolean isUpdating = false;

    /**
     * 获得TTApplication对象
     * @return
     */
    public static Context getAppContext() {
        return instance;
    }

    /**
     * 获得资源对象
     * @return
     */
    public static Resources getAppResources() {
        if (instance == null) {
            return null;
        }
        return instance.getResources();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TTLog.i(TAG + "onCreate");
        instance = this;
        Stetho.initializeWithDefaults(this);

        CrashManager crashManager = CrashManager.getInstance();
        crashManager.init(instance);

        StorageUtils.createPicDirectory(this);

        isUpdating = false;

        if (!TTLog.getAbleLogging()) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeathOnNetwork()
                    .build());
        }
        initImageLoader(this);
        initSetData();
        LoadLocationAudioUtils.loadLocationAudioInfo(getAppContext());
    }

    //初始化设置-默认值
    private void initSetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.isEmpty(PreferencesConfiguration.getSValues(Constants.APP_IS_RUN))){
                    //应用是否运行过。运行过就不在进行初始化数据
                    PreferencesConfiguration.setSValues(Constants.APP_IS_RUN,"run");
                    //初始化设置-录音时屏幕倒转标记
                    PreferencesConfiguration.setBValues(Constants.SETTING_REVERSAL,true);
                    //初始化设置-录音时屏幕长亮标记
                    PreferencesConfiguration.setBValues(Constants.SETTING_BRIGHT,true);
                }
            }
        }).start();

    }

    private void initImageLoader(final Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                        .threadPriority(Thread.NORM_PRIORITY - 2)
                        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                        .tasksProcessingOrder(QueueProcessingType.LIFO)
                        .diskCacheSize(50 * 1024 * 1024)
                        .writeDebugLogs()
                        .build();
                // Initialize ImageLoader with configuration.
                ImageLoader.getInstance().init(config);
            }
        }).start();
    }

    /**
     * 低内存
     */
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * 关闭Activity，并结束进程
     */
    public void finishActivity() {
        ActivityStack.getInstance().popAllActivityExcept();
		android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 获取升级标记
     * @return
     */
    public boolean isUpdating() {
        return isUpdating;
    }

    /**
     * 设置升级标记
     * @param isUpdating
     */
    public void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }
}
