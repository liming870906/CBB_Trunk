package com.tingtingfm.cbb.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tingtingfm.cbb.R;

import java.util.Random;


/**
 * 波形动画控件
 *
 * @author liming
 */
public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    //绘制线程
    private DrawThread drawThread;
    //画笔
    private Paint paint;
    //X轴其实位置
    int mX = 0;
    //第一条线波形变量
    float mFrist = 0.0f;
    //Y轴高度
    float mFristY = 0.0f;
    //增长标记
    boolean isFirstLine = true;
    float mSecond = 50.0f;
    float mSecondY = 0.0f;
    boolean isSecondLine = true;
    float mThird = 50.0f;
    float mThirdY = 0.0f;
    boolean isThirdLine = true;
    //暂停标记
    boolean isPauseFlag = true;
    //
    int random = 1;
    //背景资源
    int mBackground;
    Random mRandom;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawView);
        mBackground = a.getResourceId(R.styleable.DrawView_draw_bg_color, -1);
        mRandom = new Random();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        drawBackground(canvas);
        drawThirdLine(canvas, true);
        drawSecondLine(canvas, true);
        drawFirstLine(canvas, true);
        canvas.restore();
    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    public void drawBackground(Canvas canvas) {
        canvas.drawColor(getResources().getColor(mBackground));
    }

    /**
     * 绘制第一条线
     *
     * @param canvas
     */
    public void drawFirstLine(Canvas canvas, boolean isChange) {
        int centerY = getHeight() / 2;
        int centerX = getWidth();
        while (mX < centerX) {
            //第一条线
            paint.setColor(Color.parseColor("#FFFFFFFF"));
            paint.setStrokeWidth(5);
            double rad = degreeToRad(mX + mFrist);//角度转换成弧度
            int y = (int) (centerY - Math.sin(rad) * mFristY);
            canvas.drawPoint(mX, y, paint);
            mX++;
        }
        if (mFristY <= 0) {
            isFirstLine = true;
            random = mRandom.nextInt(3) + 1;
        } else if (mFristY >= 195 / random) {
            isFirstLine = false;
        }
        if (isChange) {
            if (isFirstLine) {
                mFristY += (2.5f * random);
            } else {
                mFristY -= (2.5f * random);
            }

            mFrist -= 21;
        }
        mX = 0;
    }

    /**
     * 绘制第二条线
     *
     * @param canvas
     */
    public void drawSecondLine(Canvas canvas, boolean isChange) {
        int centerY = getHeight() / 2;
        int centerX = getWidth();
        while (mX < centerX) {
            //第一条线
            paint.setColor(Color.parseColor("#60FFFFFF"));
            paint.setStrokeWidth(5);
            double rad = degreeToRad(mX + mSecond);//角度转换成弧度
            int y = (int) (centerY - Math.sin(rad) * mSecondY);
            canvas.drawPoint(mX, y, paint);
            mX++;
        }
        if (mSecondY <= 0) {
            isSecondLine = true;
        } else if (mSecondY >= 156 / random) {
            isSecondLine = false;
        }
        if (isChange) {
            if (isSecondLine) {
                mSecondY += (2.0f * random);
            } else {
                mSecondY -= (2.0f * random);
            }

            mSecond -= 12;
        }
        mX = 0;
    }

    /**
     * 绘制第三条线
     *
     * @param canvas
     */
    public void drawThirdLine(Canvas canvas, boolean isChange) {
        int centerY = getHeight() / 2;
        int centerX = getWidth();
        while (mX < centerX) {
            //第一条线
            paint.setColor(Color.parseColor("#2BFFFFFF"));
            paint.setStrokeWidth(5);
            double rad = degreeToRad(mX + mThird);//角度转换成弧度
            int y = (int) (centerY - Math.sin(rad * 3) * mThirdY);
            canvas.drawPoint(mX, y, paint);
            mX++;
        }
        if (mThirdY <= 0) {
            isThirdLine = true;
        } else if (mThirdY >= 39 / random) {
            isThirdLine = false;
        }
        if (isChange) {
            if (isThirdLine) {
                mThirdY += (0.5f * random);
            } else {
                mThirdY -= (0.5f * random);
            }
            mThird -= 3;
        }
        mX = 0;
    }

    /**
     * 角度转换成弧度
     *
     * @param degree
     * @return
     */
    private double degreeToRad(double degree) {
        return degree * Math.PI / 180;
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawDefaultWave();
        drawThread = new DrawThread(this, getHolder());
        drawThread.isPause = isPauseFlag;
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean isStop = true;
        drawThread.isFlag = false;
        while (isStop) {
            try {
                drawThread.join();
                isStop = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public void startAnim() {
        // TODO: 17/4/11 开启动画
        drawThread.isPause = false;
        isPauseFlag = false;
    }

    public void stopAnim() {
        // TODO: 17/4/11 结束动画
        drawThread.isPause = true;
        isPauseFlag = true;
    }

    public void drawDefaultWave() {
        new Thread() {
            @Override
            public void run() {
                Canvas canvas = null;
                try {
                    canvas = getHolder().lockCanvas(null);
                    synchronized (getHolder()) {
                        canvas.save();
                        drawBackground(canvas);
                        drawThirdLine(canvas, false);
                        drawSecondLine(canvas, false);
                        drawFirstLine(canvas, false);
                        canvas.restore();
                    }
                } catch (Exception e) {
                    Log.i("info", "DrawThread-->ExceptionMessage:" + e.getMessage());
                } finally {
                    if (canvas != null) {
                        getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }
        }.start();
    }

    public void relsetView() {
        //X轴其实位置
        this.mX = 0;
        //第一条线波形变量
        this.mFrist = 0.0f;
        //Y轴高度
        this.mFristY = 0.0f;
        //增长标记
        this.isFirstLine = true;
        this.mSecond = 50.0f;
        this.mSecondY = 0.0f;
        this.isSecondLine = true;
        this.mThird = 50.0f;
        this.mThirdY = 0.0f;
        this.isThirdLine = true;
        //暂停标记
        this.isPauseFlag = true;
    }
}
