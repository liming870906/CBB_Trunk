package com.tingtingfm.cbb.common.log;

import android.util.Log;

public final class TTLog {
	private static final String TAG = "CBB";
	private static final String LOG_FORMAT = "%1$s\n%2$s";
	private static volatile boolean DISABLED = false;

	private TTLog() {
	}

	public static void enableLogging() {
		DISABLED = false;
	}

	public static void disableLogging() {
		DISABLED = true;
	}
	
	public static boolean getAbleLogging() {
		return DISABLED;
	}

	public static void d(String message, Object... args) {
		log(Log.DEBUG, null, message, args);
	}

	public static void i(String message, Object... args) {
		log(Log.INFO, null, message, args);
	}

	public static void w(String message, Object... args) {
		log(Log.WARN, null, message, args);
	}

	public static void e(Throwable ex) {
		log(Log.ERROR, ex, null);
	}

	public static void e(String message, Object... args) {
		log(Log.ERROR, null, message, args);
	}

	public static void e(Throwable ex, String message, Object... args) {
		log(Log.ERROR, ex, message, args);
	}

	private static void log(int priority, Throwable ex, String message, Object... args) {
		if (DISABLED) return;
		if (args.length > 0) {
			message = String.format(message, args);
		}

		String log;
		if (ex == null) {
			log = message;
		} else {
			String logMessage = message == null ? ex.getMessage() : message;
			String logBody = Log.getStackTraceString(ex);
			log = String.format(LOG_FORMAT, logMessage, logBody);
		}
		Log.println(priority, TAG, log);

//        LogManager.getInstance().addLog(log);
	}
}