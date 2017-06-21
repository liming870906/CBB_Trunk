package com.tingtingfm.cbb.ui.view;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

@SuppressLint("WrongCall")
public class DrawThread extends Thread {

	private int sleepTime = 10;
	private DrawView gv;
	private SurfaceHolder holder;
	public boolean isFlag = true;
	public boolean isPause = true;
	public DrawThread(DrawView gv, SurfaceHolder holder){
		this.gv = gv ;
		this.holder = holder ;
	}
	@Override
	public void run() {
		while(isFlag){
			Canvas canvas = null;
			try {
				if(!isPause){
					canvas = holder.lockCanvas(null);
					synchronized (holder) {
						gv.onDraw(canvas);
					}
				}
			} catch (Exception e) {
				Log.i("info", "DrawThread-->ExceptionMessage:"+e.getMessage());
			}finally{
				if(canvas != null){
					holder.unlockCanvasAndPost(canvas);
				}
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
