package com.tingtingfm.cbb.common.crash;

import android.os.Environment;
import android.os.StatFs;

import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.DeviceUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.ScreenUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils.TimeFormat;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * 收集错误日志，当收集的错误日志大于50K时压缩日志文件。
 * 
 * 在TTApplication 里面初始该类
 */
public class CrashManager implements Thread.UncaughtExceptionHandler {
	private TTApplication mContext;
	private static CrashManager crashManager = null;
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	
	private CrashManager() {
		
	}
	
	public static CrashManager getInstance() {
		if (crashManager == null) {
			crashManager = new CrashManager();
		}
		
		return crashManager;
	}
	
	public void init(TTApplication context) {
		this.mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {		
		if (!TTLog.getAbleLogging()) {
			ex.printStackTrace();
		}
		
		handleException(ex);
		
		mContext.finishActivity();
		
/*		else {
			Intent intent = new Intent(mContext, SplashActivity.class);
			PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
			mContext.finishActivity();
		}*/
	}
	
	/** 
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 
	 *  
	 * @param ex 
	 * @return true:如果处理了该异常信息;否则返回false. 
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		creatCrashReportFile(ex);

		return true;
	}

	/**
	 * 创建进程崩溃时的错误报告
	 * 
	 * @param throwable
	 * @return
	 */
	private File creatCrashReportFile(Throwable throwable) {
		try {
			/* 获取文件 */
			File file = StorageUtils.getErrorFile(mContext);

			/* 打印当前时间 */
			final FileOutputStream output = new FileOutputStream(file, false);
			final String time_now = TimeUtils.getTimeForSpecialFormat(TimeFormat.TimeFormat3) + "\n";
			output.write(time_now.getBytes());

			final PrintStream printStream = new PrintStream(output);

			/* dump StackTrace */
			dumpStackTrace(throwable, printStream);

			/* dump /proc/meminfo */
			dumpProcMemInfo(output);

			/* Android OS Build details */
			dumpBuildDetails(output);

			/* the printStream will close the output */
			printStream.close();

			return file;
		} catch (Error error) {

		} catch (Exception e) {

		}

		return null;
	}

	private void dumpStackTrace(Throwable throwable, PrintStream printStream) {

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable cause = throwable;
		while (cause != null) {
			cause.printStackTrace(printStream);
			cause = cause.getCause();
		}
	}

	private void dumpProcMemInfo(OutputStream output) throws IOException {

		final String[] command = { "cat", "/proc/meminfo" };
		final Process process = Runtime.getRuntime().exec(command);
		final InputStream is = process.getInputStream();

		ZipAndDump.dump(output, is);
	}

	private void dumpBuildDetails(OutputStream os) throws IOException {

		final StringBuilder result = new StringBuilder();
		
		//手机信息
		result.append("\nphone-resolution：" + ScreenUtils.getScreenResolution());
		result.append("\nsystem-version：" + DeviceUtils.getSysRelease());
		result.append("\nphone-model：" + DeviceUtils.getPhoneModel());
		result.append("\nphone-brand："+ DeviceUtils.getBrand());
		result.append("\nnetwork-type：" + NetUtils.getNetType());

		//应用信息
		result.append("\napp-name：" + AppUtils.getAppName());
		result.append("\napp-version：" + AppUtils.getVersionName());
		
		os.write(result.toString().getBytes());

		final File path = Environment.getDataDirectory();
		final StatFs stat = new StatFs(path.getPath());
		final long blockSize = stat.getBlockSize();
		final long availableBlocks = stat.getAvailableBlocks();
		final long totalBlocks = stat.getBlockCount();

		// Rom
		os.write("\nTotal=".getBytes());
		os.write(Long.toString(totalBlocks * blockSize / (1024 * 1024)).getBytes());
		os.write("\nAvailable=".getBytes());
		os.write(Long.toString(availableBlocks * blockSize / (1024 * 1024)).getBytes());
		os.write('\n');
	}
	
	
	private String getLogFileName() {
		return "err-tingting.log";
	}
}
