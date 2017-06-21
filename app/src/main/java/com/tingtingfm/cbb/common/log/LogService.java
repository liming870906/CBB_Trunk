package com.tingtingfm.cbb.common.log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by think on 2016/6/16.
 * 打印日志到文件的服务，日志文件存储路径为/sdcard/tingting_xxx.txt
 */
public class LogService extends Service {
    private File logFile;
    private boolean isOk;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isOk = true;
        logFile = new File("/sdcard/tingting_" + System.currentTimeMillis() + ".txt");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("LogService.onCreate " + logFile.getPath());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!TTLog.getAbleLogging()) {
            new WriteLogThread().start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOk = false;
        System.out.println("LogService.onDestroy");
    }


    class WriteLogThread extends Thread {
        @Override
        public void run() {
            super.run();
            LogManager instance = LogManager.getInstance();
            String log;
            StringBuffer stringBuffer = new StringBuffer();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(logFile.getPath(), true);
                while (isOk) {
                    try {
                        if (instance == null) {
                            instance = LogManager.getInstance();
                        }

                        if (instance.getLogList().size() > 0 && (log = instance.getLogList().pollFirst()) != null) {
                            stringBuffer.append(log);
                            stringBuffer.append("\n");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    if (stringBuffer.length() > 1024) {
                        //写入文件
                        fileWriter.write(stringBuffer.toString());
                        fileWriter.flush();
                        stringBuffer.delete(0, stringBuffer.length());
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (stringBuffer.length() > 0) {
                    fileWriter.write(stringBuffer.toString());
                    fileWriter.flush();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
