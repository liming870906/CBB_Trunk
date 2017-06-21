package com.tingtingfm.cbb.common.update;

public class UpdateManager {
	private UpdateHandler handler;
	private static UpdateManager manage;
	
	private UpdateManager() {
		handler = new UpdateHandler();
	}
	
	/**
	 * 检查更新
	 */
	public void checkUpdate(boolean isShow) {
		new Thread(new UpdateRunnable(isShow, handler)).start();
	}
	
	public static UpdateManager getInstance() {
		if (manage == null) {
			manage = new UpdateManager();
		}
		
		return manage;
	}
	
}
