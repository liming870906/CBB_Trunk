package com.tingtingfm.cbb.ui.play;

import android.content.Context;

import com.playengine.PlayerEngine;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.log.TTLog;


/**
 * @author lqsir
 */

public class MediaCore {
	private static final String TAG = "TTFM/MediaCore >>> ";
	private static final String PlayerEngineError = "PlayerEngine object is null, please init";
	private static final String libError = "PlayerEngine load data/data/lib error(.so not exists";

	static String s_libPath = TTApplication.getAppContext().getFilesDir().getParentFile().getPath() + "/lib/";
	static {
		try {
			TTLog.i(TAG + "system loadlibrary avmtcore/vrvessel/vrcore/entry.so");
			System.loadLibrary("avmtcore");
			System.loadLibrary("vrvessel");
			System.loadLibrary("vrcore");
			System.loadLibrary("entry");
		} catch (UnsatisfiedLinkError e) {
			System.exit(1);
		} catch (SecurityException se) {
			System.exit(1);
		}
	}

	private PlayerEngine mPlayerEngine = null;
	private static MediaCore sInstance;
	private boolean mIsInitialized = false;

	private MediaCore() {
		mPlayerEngine = new PlayerEngine();
	}

	public static MediaCore getInstance() {
		synchronized (MediaCore.class) {
			if (sInstance == null) {
				/* First call */
				sInstance = new MediaCore();
			}
		}

		return sInstance;
	}

	public static MediaCore getExistingInstance() {
		synchronized (MediaCore.class) {
			return sInstance;
		}
	}

	@Override
	public void finalize() {
		if (sInstance != null) {
			TTLog.i(TAG + "finalize MediaCore is was destroyed yet before finalize()");
			destroy();
		}
	}

    public void init(Context context) throws LibMediaException {
		TTLog.i(TAG + " init Initializing MediaCore");
		if (!mIsInitialized) {

			if (null == mPlayerEngine) {
				TTLog.e(TAG, "Initializing PlayerEngine");
				throw new LibMediaException(PlayerEngineError);
			} else {
				TTLog.i(TAG + " init mPlayerEngine is not null " + mPlayerEngine.toString()
						+ " MediaCore : " + sInstance.toString());
			}

			int i_err = mPlayerEngine.init(s_libPath);
			if (i_err != 0) {
				TTLog.e(TAG + "/system/lib/.so");
				i_err = mPlayerEngine.init("/system/lib/");
			}
		        
			if (i_err != 0) {
				TTLog.e(TAG + "init PlayerEngine load lib/.so error " + s_libPath);
				throw new LibMediaException(libError);
			}

			mIsInitialized = true;
		}
	}

	public void destroy() {
		TTLog.i(TAG + "destroy release all resource");
		if (mPlayerEngine != null) {
			mPlayerEngine.release();
			mPlayerEngine = null;
		}

		if (sInstance != null) {
			sInstance = null;
		}

		mIsInitialized = false;
	}

	public PlayerEngine getPlayerEngine() {
		return mPlayerEngine;
	}

	public void release() {
		if (mPlayerEngine != null) {
			mPlayerEngine.release();
			mPlayerEngine = null;
		}
		
		mIsInitialized = false;
	}
	
	public boolean isPlaying() {
		if (mPlayerEngine != null) {
			return mPlayerEngine.isPlaying();
		}
		
		return false;
	}
	
	public void pause() {
		if (mPlayerEngine != null) {
			mPlayerEngine.pause();
		}
	}
	
	public void play() {
		if (mPlayerEngine != null ) {
			mPlayerEngine.start();
		}
	}

}
