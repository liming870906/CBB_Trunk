package com.tingtingfm.cbb.common.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.utils.AppUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 升级服务
 *
 * @author lqsir
 *
 */
public class UpdateService extends Service {
	public static final String UPDATE_ACTION = "com.tingtingfm.cbb.update.action";
	public static final String UPDATE_CANCEL_ACTION = "com.tingting.cbb.cancel.action";
	private static final int UPDATE_CANCEL = 0x100;
	private static final int UPDATE_FINISH = 0x101;
	private static final int UPDATE_ING = 0x102;

	public static final String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +UpdateConstant.DOWNLOAD_FILE_PATH;
	private static final int icon = R.mipmap.ic_launcher;

	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private Thread downLoadThread;
	private Context mContext = this;
	private TTApplication app;

	private int progress;
	// 取消下载或下载已完成,默认为true
	private boolean canceled = true;
	private int lastRate = 0;
	private String apkUrl;
	private String version;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_FINISH:
				app.setUpdating(false);
				// 下载完毕
				// 取消通知
				mNotificationManager.cancel(icon);
				installApk();
				break;
			case UPDATE_CANCEL:
				app.setUpdating(false);
				// 这里是用户界面手动取消，所以会经过activity的onDestroy();方法
				// 取消通知
				mNotificationManager.cancel(icon);
				break;
			case UPDATE_ING:
				int rate = msg.arg1;
				app.setUpdating(true);
				if (rate < 100) {
					mBuilder.setProgress(100, rate, false);
				} else {
					System.out.println("下载完毕!!!!!!!!!!!");
//					mNotification.setLatestEventInfo(mContext, "下载完成", "文件已下载完毕", null);
//					stopSelf();// 停掉服务自身
				}
				mNotificationManager.notify(icon, mBuilder.build());
				break;
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("downloadservice ondestroy");
		// 假如被销毁了，无论如何都默认取消了。
		app.setUpdating(false);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		app = (TTApplication) getApplication();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null  ) {
			if (intent.getAction().equals(UPDATE_ACTION)) {
				if (!app.isUpdating()) {// 当前是否正在下载
					apkUrl = intent.getStringExtra("url");
					version = intent.getStringExtra("version");
					start();
				}
			} else if (intent.getAction().equals(UPDATE_CANCEL_ACTION)) {
				if (app.isUpdating()) {
					cancelNotification();
				}
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	public void start() {
		if (downLoadThread == null || !downLoadThread.isAlive()) {
			app.setUpdating(true);
			progress = 0;
			setNotification();
			startDownload();
		}
	}

	public void cancel() {
		canceled = true;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void cancelNotification() {
		mHandler.sendEmptyMessage(UPDATE_CANCEL);
	}

	private void startDownload() {
		canceled = false;
		downLoadThread = new Thread(updateRunable);
		downLoadThread.start();
	}

	/**
	 * 创建通知
	 */
	private void setNotification() {
		CharSequence tickerText = "开始下载";
		mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setOngoing(false);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
		mBuilder.setContentTitle(tickerText);
		mBuilder.setContentText(AppUtils.getAppName());
		mBuilder.setProgress(100, 0, false);

		// 指定个性化视图
		mNotificationManager.notify(icon, mBuilder.build());
	}

	/**
	 * 安装apk
	 * 
	 */
	private void installApk() {
		String fileName = AppUtils.getAppName() + version + ".apk";
		File apkfile = new File(DIR, fileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(i);
	}

	private Runnable updateRunable = new Runnable() {
		InputStream is = null;
		HttpURLConnection conn = null;
		FileOutputStream fos = null;

		@Override
		public void run() {
			try {
				URL url = new URL(apkUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				is = conn.getInputStream();

				File file = new File(DIR);
				if (!file.exists()) {
					file.mkdirs();
				}

				File tempFile = new File(file, AppUtils.getAppName() + version + ".temp");
				if (tempFile.exists()) {
					tempFile.delete();
				}
				fos = new FileOutputStream(tempFile);

				int count = 0;
				byte buf[] = new byte[1024 * 50];

				if (conn.getResponseCode() == 200) {
					do {
						int numread = is.read(buf);
						count += numread;
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						Message msg = mHandler.obtainMessage();
						msg.what = UPDATE_ING;
						msg.arg1 = progress;

						if (progress >= lastRate + 1) {
							mHandler.sendMessage(msg);
							lastRate = progress;
						}

						if (numread <= 0) {
							tempFile.renameTo(new File(DIR, AppUtils.getAppName() + version + ".apk"));
							// 下载完成通知安装
							mHandler.sendEmptyMessage(UPDATE_FINISH);
							// 下载完了，cancelled也要设置
							canceled = true;
							break;
						}
						fos.write(buf, 0, numread);
					} while (!canceled);// 点击取消就停止下载.
				}
			} catch (Exception e) {
				e.printStackTrace();
				canceled = false;
				app.setUpdating(false);
				cancelNotification();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}

					if (is != null) {
						is.close();
					}
				} catch (Exception e2) {
				}

				if (conn != null) {
					conn.disconnect();
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
