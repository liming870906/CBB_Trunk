package com.tingtingfm.cbb.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.log.TTLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Provides application storage paths
 */
public final class StorageUtils {

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "photo";
    private static final String INDIVIDUAL_DIR_DOWNLOAD = "download";
    private static final String INDIVIDUAL_DIR_ERROR = "error";
    private static final String AUDIO_RECORD_DIR = "audio";
    private static final String CAMERA_PHOTO_DIR = "Pictures";

    private StorageUtils() {
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    public static File getCacheDirectory(Context context) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            TTLog.w("Can't define system cache directory! The app should be restarted.");
        }
        return appCacheDir;
    }

    /**
     * 创建不同的目录
     */
    public static File createDirectory(String path) {
        File file = null;
        if (sdCardExists()) {
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static File getSDCardStorageDirectory(Context context) {
        //判断是否有SD卡
        File file = createDirectory(Environment.getExternalStorageDirectory() + "/CBB/"+AUDIO_RECORD_DIR);
        if(file == null){
            file = context.getFilesDir();
        }
        return file;
    }

    public static File createPicDirectory(Context context) {
        File picDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            picDir = new File(Environment.getExternalStorageDirectory(), CAMERA_PHOTO_DIR);
        }

        if (picDir != null && !picDir.exists()) {
            if (!picDir.mkdirs()) {
                TTLog.w("Unable to create Pictures cache directory");
                return null;
            }

            try {
                new File(picDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                TTLog.i("Can't create \".nomedia\" file in application Pictures cache directory");
            }
        }

        return picDir;
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    public static File getPhotoDirectory(Context context) {
        File cacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_NAME);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = cacheDir;
            }
        }
        return individualCacheDir;
    }

    public static File getIndividualDownLoadDirectory(Context context) {
        File cacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_DOWNLOAD);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = cacheDir;
            }
        }
        return individualCacheDir;
    }

    public static File getIndividualErrorLogDirectory(Context context) {
        File cacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_ERROR);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = cacheDir;
            }
        }
        return individualCacheDir;
    }

    public static File getOwnAudioRecordDirectory(Context context) {
        File recordDir = getSDCardStorageDirectory(context);
        File individualCacheDir = new File(recordDir, String.valueOf(AccoutConfiguration.getLoginInfo().getUserid()));
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = recordDir;
            }
        }
        return individualCacheDir;
    }

    public static File getOwnPicDirectory(Context context) {
        File picDir = null;
        File ownDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            picDir = new File(new File(Environment.getExternalStorageDirectory(), "tingting"), CAMERA_PHOTO_DIR);
        }

        ownDir = getCacheDirectory(context);
        if (picDir == null || (!picDir.exists() && !picDir.mkdirs())) {
            picDir = new File(ownDir, CAMERA_PHOTO_DIR);
        }

        if (picDir != null && !picDir.exists()) {
            if (!picDir.mkdir()) {
                picDir = ownDir;
            }
        } else if (picDir == null) {
            picDir = ownDir;
        }

        return picDir;
    }

    /**
     * Returns specified application cache directory. Cache directory will be created on SD card by defined path if card
     * is mounted and app has appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                TTLog.w("Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                TTLog.i("Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /*
     * 已使用空间
     * fileDir   缓存文件名字
     * flag 标识是空间大小       2是图片,  1是下载目录
     */
    public static String getUseSpace(Context context, String fileDir, int flag) {
        float maxSpace = 0;
        File mFile;
        if (flag == 2) {
            mFile = new File(getCacheDirectory(context), fileDir);
        } else {
            mFile = new File(fileDir);
        }
        mFile.mkdirs();
        if (mFile.exists()) {
            File[] mfiles = mFile.listFiles();
            for (File file : mfiles) {
                maxSpace += file.length();
            }
            if (maxSpace < 1 * 1024) {
                return flag == 1 ? "0M" : "0k";
            }
            if (maxSpace < 1 * 1024 * 1024) {
                String avail = maxSpace / (float) 1024 + "";
                return flag == 1 ? "0." + avail.substring(0, avail.lastIndexOf('.')) + "M" : avail.substring(0, avail.lastIndexOf('.')) + "k";
            }
            if (maxSpace < 1073741824) {
                String avail = ((float) maxSpace / (float) 1024 / (float) 1024)
                        + "";
                return avail.substring(0, avail.lastIndexOf('.') + 2) + "M";
            } else {
                float val = ((float) maxSpace / (float) 1024 / (float) 1024)
                        / (float) 1024;
                if (val == (float) 1) {
                    return val + ".0G";
                } else {
                    String avail = val + "";
                    return avail.substring(0, avail.lastIndexOf('.') + 2) + "G";
                }
            }
        }
        return "0G";
    }

    /**
     * 目标文件大小
     *
     * @param fileDir
     * @return
     */
    public static String getTagetFileSize(String fileDir) {
        float maxSpace = 0;
        File mFile = new File(fileDir);
        mFile.mkdirs();
        if (mFile.exists()) {
            maxSpace += mFile.length();
            if (maxSpace < 1 * 1024) {
                return "0.00M";
            }
            if (maxSpace < 1073741824) {
                float number = ((float) maxSpace / (float) 1024 / (float) 1024);
                String avail = number
                        + "000";
                if (number < 1) {
                    return avail.substring(0, avail.lastIndexOf('.') + 3) + "M";
                } else {
                    return avail.substring(0, avail.lastIndexOf('.') + 2) + "M";
                }
            } else {
                float val = ((float) maxSpace / (float) 1024 / (float) 1024)
                        / (float) 1024;
                if (val == (float) 1) {
                    return val + ".0G";
                } else {
                    String avail = val + "000";
                    if (val < 1) {
                        return avail.substring(0, avail.lastIndexOf('.') + 3) + "G";
                    } else {
                        return avail.substring(0, avail.lastIndexOf('.') + 2) + "G";
                    }
                }
            }
        }
        return "0.00M";
    }

    private static float calculateFileSize(float maxSpace, File mFile) {
        File[] mfiles = mFile.listFiles();
        for (int i = 0; null != mfiles && i < mfiles.length; i++) {
            maxSpace += mfiles[i].length();
        }
        return maxSpace;
    }

    /*
     * 已使用空间大小
     */
    public static int getUseSpaceNum(Context context, String fileDir) {
        float maxSpace = 0;
        File mFile;
        mFile = new File(fileDir);
        mFile.mkdirs();
        if (mFile.exists()) {
            maxSpace = calculateFileSize(maxSpace, mFile);
            if (maxSpace < 1 * 1024 * 1024) {
                return 1;
            }
            return (int) ((float) maxSpace / (float) 1024 / (float) 1024);
        }
        return 0;
    }

    public static String value2M(int value) {
        if (value < 1024) {
            return "0k";
        }

        String avail = ((float) value / (float) 1024 / (float) 1024) + "";
        return avail.substring(0, avail.lastIndexOf('.') + 2) + "M";
    }

    /*
     * 剩余可用空间
     */
    public static String getSurplusSapce(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isDirectory() && file.canWrite()) {
            StatFs sf = new StatFs(fileName);
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            float sapce = availCount * blockSize;
            if (sapce < 1024 * 1024 * 512) {
                return "空间不足";
            } else if (sapce > 1024 * 1024 * 512 && sapce < 1073741824) {
                String avail = ((float) sapce / (float) 1024 / (float) 1024 / (float) 1024) + "000";
                return avail.substring(0, avail.lastIndexOf('.') + 3) + "G";
            } else {
                float val = ((float) sapce / (float) 1024 / (float) 1024) / (float) 1024;
                if (val == (float) 1) {
                    return val + ".0G";
                } else {
                    String avail = val + "000";
                    return avail.substring(0, avail.lastIndexOf('.') + 2) + "G";
                }
            }
        }
        return "0G";
    }

    /*
    * 剩余可用空间Size
    */
    public static float getSurplusSapce1(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isDirectory() && file.canWrite()) {
            StatFs sf = new StatFs(fileName);
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            float sapce = availCount * blockSize;
            return sapce;
        }
        return 0f;
    }

    /*
     * 剩余可用空间
     */
    public static boolean isSurplusSapce(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isDirectory() && file.canWrite()) {
            StatFs sf = new StatFs(fileName);
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            float sapce = availCount * blockSize;
            if (sapce < 1073741824) {
                float avail = ((float) sapce / (float) 1024 / (float) 1024 / (float) 1024);
                return avail < 0.5f ? true : false;
            }
        }
        return false;
    }

    /*
     * 根据给定的路径计算剩下的可用空间(数字)
     */
    public static float getFreeSapceForPath(String fileName) {
        float sapce = 0;
        try {
            File file = new File(fileName);
            if (file.exists() && file.isDirectory() && file.canWrite()) {
                StatFs sf = new StatFs(fileName);
                long blockSize = sf.getBlockSize();
                long availCount = sf.getAvailableBlocks();
                sapce = availCount * blockSize;
            }
        } catch (Exception e) {

        }
        return sapce;
    }

    /**
     * 剩余可用空间大小
     */
    public static int getSapceNum(String filePath) {
        float sapce = 0;
        try {
            File file = new File(filePath);
            if (file.exists() && file.isDirectory() && file.canWrite()) {
                StatFs sf = new StatFs(filePath);
                long blockSize = sf.getBlockSize();
                long availCount = sf.getAvailableBlocks();
                sapce = availCount * blockSize;
                return (int) ((float) sapce / (float) 1024 / (float) 1024);
            }
        } catch (Exception e) {

        }

        return 0;
    }

    /**
     * 获取给定目录的总空间大小
     *
     * @param fileName
     * @return
     */
    public static String getSapceCount(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isDirectory() && file.canWrite()) {
            StatFs sf = new StatFs(fileName);
            long nTotalBlocks = sf.getBlockCount();
            long blockSize = sf.getBlockSize();
            float sapce = nTotalBlocks * blockSize;
            if (sapce < 1073741824) {
                String avail = ((float) sapce / (float) 1024 / (float) 1024) + "";
                return avail.substring(0, avail.lastIndexOf('.') + 2) + "M";
            } else {
                float val = ((float) sapce / (float) 1024 / (float) 1024) / (float) 1024;
                if (val == (float) 1) {
                    return val + ".0G";
                } else {
                    String avail = val + "";
                    return avail.substring(0, avail.lastIndexOf('.') + 2) + "G";
                }
            }
        }
        return "0G";
    }

    /*
     * 根据路径删除相应文件
     */
    public static void deleteFile(String filepath) {
        if (!TextUtils.isEmpty(filepath)) {
            File mFile = new File(filepath);
            if (mFile.exists()) {
                FileUtils.deleteFile(mFile);
            }
            try {
                mFile.getParentFile().mkdirs();
                mFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getErrorFile(Context context) {
        File file = new File(StorageUtils.getIndividualErrorLogDirectory(context), "err-tingting.log");
        return file;
    }

    /**
     * 得到Sdcard最大剩余空间的存储路径
     *
     * @param con
     * @param paths 手机有效存储路径
     * @return
     */
    public static String getSdcardMaxFreeSpacePath(Context con, List<String> paths) {
        if (null == paths || paths.size() == 0) {
            return Environment.getExternalStorageDirectory().getPath();
        }

        float spaceNum = 0;
        int position = 0;
        for (int i = 0; i < paths.size(); i++) {
            float fileNum = getFreeSapceForPath(paths.get(i));
            if (spaceNum < fileNum) {
                spaceNum = fileNum;
                position = i;
            }
        }

        return paths.get(position);
    }

    // 获得可用的内存
    public static long getmem_UNUSED(Context mContext) {
        long MEM_UNUSED;
        // 得到ActivityManager
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        // 创建ActivityManager.MemoryInfo对象

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        // 取得剩余的内存空间

        MEM_UNUSED = mi.availMem / 1024;
        return MEM_UNUSED;
    }

    /**
     * 清空下载目录下所有文件
     *
     * @param filePath
     * @param flagType 1下载目录    2 缓存图片目录
     */
    public static void deleteDownloadFile(String filePath, int flagType) {
        String downloadFiel = null;
        if (flagType == 1) {
            downloadFiel = filePath;
        } else if (flagType == 2) {
            downloadFiel = getCacheDirectory(TTApplication.getAppContext()) + File.separator + filePath;
        }
        File file = new File(downloadFiel);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    /**
     * 判断SD卡
     *
     * @return
     */
    public static boolean sdCardExists() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    //将文件复制到目标文件夹下
    public static boolean copyFile(File fromFile, File toFile) {
        boolean valu = false;
        try {
            FileInputStream fosfrom = new FileInputStream(fromFile);
            FileOutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024 * 1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            valu = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.toString();
        }
        return valu;
    }
}
