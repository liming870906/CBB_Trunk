package com.shuyu.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.BaseRecorder;

import java.util.ArrayList;


/**
 * 声音波形的view
 * Created by shuyu on 2016/11/15.
 */

public class AudioWaveView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String MAX = "max_volume"; //map中的key
    public static final String MIN = "min_volume";//map中的key
    final protected Object mLock = new Object();
    private Context mContext;
    private SurfaceHolder holder;
    private int line_off;//上下边距距离
    private Paint linePaint, centerLinePaint;
    private Bitmap mBitmap, mBackgroundBitmap;
    private Bitmap mRuler, mProgressTag;
    private Paint mViewPaint;
    private ArrayList<Short> mRecDataList = new ArrayList<>();
    private DrawWaveThread mInnerThread;
    private BaseRecorder mBaseRecorder;
    private int mWidthSpecSize;
    private int mHeightSpecSize;
    private int mScale = 1;
    private int mBaseLine;
    private boolean isCenterDrawTag;
    private int mOffset = -11;//波形之间线与线的间隔
    private int marginRight = 10;//波形图绘制距离右边的距离
    private boolean mIsDraw = true;
    private boolean isPaseDraw = true;
    private boolean isCanvas = false;
    private int defaultColor = R.color.color_697fb4;
    private int onePxRate = 355;//48000采样率=1秒=135px；356采样率=7.4毫秒=1px
    //    private CallBackListener listener;
    //    private int mDrawOffSet = 540;
//    private int mSpeed = 1;
//    private boolean isCallBack = true;
    private int mPosition;

    public AudioWaveView(Context context) {
        super(context);
        init(context, null);
    }

    public AudioWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AudioWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 设置波形图标尺
     * @param bitmap
     */
    public void setBitmapRuler(Bitmap bitmap) {
        mRuler = bitmap;
    }

    /**
     * 设置波形图进图图片
     * @param bitmap
     */
    public void setBitmapProgressTag(Bitmap bitmap) {
        mProgressTag = bitmap;
    }

    /**
     * 绘制直线偏移量
     * @return 直线偏移量
     */
    public int getLine_off() {
        return line_off;
    }

    /**
     * 设置直线偏移量
     * @param line_off
     */
    public void setLine_off(int line_off) {
        this.line_off = line_off;
    }

    /**
     * 设置居中标记
     * @param isDraw
     */
    public void setCenter(boolean isDraw) {
        isCenterDrawTag = isDraw;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isPaseDraw = true;
        mIsDraw = false;
        if (mRuler != null && !mRuler.isRecycled()) {
            mRuler.recycle();
        }
        if (mProgressTag != null && !mProgressTag.isRecycled()) {
            mProgressTag.recycle();
        }
    }

    /**
     * 初始化方法
     * @param context
     * @param attrs
     */
    public void init(Context context, AttributeSet attrs) {
        mPosition = 0;
        mContext = context;
        setBackgroundResource(defaultColor);
        setZOrderOnTop(true);
        this.holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);

        if (isInEditMode())
            return;

        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.waveView);
            mOffset = ta.getInt(R.styleable.waveView_waveOffset, dip2px(context, -11));
            ta.recycle();
        }

        if (mOffset == dip2px(context, -11)) {
            mOffset = dip2px(context, 1);
        }

        centerLinePaint = new Paint();
        centerLinePaint.setColor(Color.parseColor("#7c95d1"));// 画笔为color
        centerLinePaint.setStrokeWidth(1);// 设置画笔粗细
        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setFilterBitmap(true);
        centerLinePaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(1);// 设置画笔粗细
        linePaint.setAntiAlias(true);
        linePaint.setFilterBitmap(true);
        linePaint.setStyle(Paint.Style.FILL);

        mViewPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE && mBackgroundBitmap == null) {
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (getWidth() > 0 && getHeight() > 0) {
                        mWidthSpecSize = getWidth();
                        mHeightSpecSize = getHeight();
                        mBaseLine = mHeightSpecSize / 2;
                        ViewTreeObserver vto = getViewTreeObserver();
                        vto.removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initSurfaceView(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 初始化视图绘制方法
     * @param sfv
     */
    public void initSurfaceView(final SurfaceView sfv) {
        if (!isCanvas) {
            isCanvas = true;

            drawDefaultWave();
        } else {
            drawWaveFrame(cloneData());
        }
    }

    /**
     * 初始化波形图样式
     */
    public void drawDefaultWave() {
        new Thread() {
            public void run() {
                Canvas canvas = getHolder().lockCanvas(
                        new Rect(0, 0, getWidth(), getHeight()));// 关键:获取画布
                if (canvas == null) {
                    return;
                }

                if (getWidth() > 0 && getHeight() > 0) {
                    mWidthSpecSize = getWidth();
                    mHeightSpecSize = getHeight();
                    mBaseLine = mHeightSpecSize / 2;
                }

                System.out.println("AudioWaveView.run "
                        + " mWidthSpecSize:  " + mWidthSpecSize
                        + " mHeightSpecSize: " + mHeightSpecSize
                        + " mBaseLine: " + mBaseLine);

                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                int height = getHeight() - line_off;
                if (mProgressTag != null) {
                    int left = 0;
                    if (isCenterDrawTag) {
                        left = mWidthSpecSize / 2 - 20;
                    }
                    canvas.drawBitmap(mProgressTag, left, 0, null);//进度标记
                }

                canvas.drawLine(0, height * 0.5f + line_off / 2, getWidth(), height * 0.5f + line_off / 2, centerLinePaint);//中心线

                if (mRuler != null) {
                    canvas.drawBitmap(mRuler, 0, getHeight() - mRuler.getHeight(), null);
                }
                getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
            }
        }.start();
    }

    /**
     * 开始绘制线程
     */
    public void startView() {
        if (mInnerThread != null && mInnerThread.isAlive()) {
            mIsDraw = false;
            while (mInnerThread.isAlive()) ;
        }
        mIsDraw = true;
        mInnerThread = new DrawWaveThread();
        mInnerThread.start();
    }

    /**
     * 停止绘制线程
     */
    public void stopView() {
        isPaseDraw = true;
        mIsDraw = false;
        mRecDataList.clear();
        if (mInnerThread != null) {
            while (mInnerThread.isAlive()) ;
        }
    }

    /**
     * 开始绘制波形
     */
    public void startDraw() {
        isPaseDraw = false;
    }

    /**
     * 暂停绘制
     */
    public void pauseDraw() {
        isPaseDraw = true;
    }

//    /**
//     * 设置好偶波形会变色
//     */
//    public void setBaseRecorder(BaseRecorder baseRecorder) {
//        mBaseRecorder = baseRecorder;
//    }

    /**
     * 将这个list传到Record线程里，对其不断的填充
     * <p>
     * Map存有两个key，一个对应AudioWaveView的MAX这个key,一个对应AudioWaveView的MIN这个key
     *
     * @return 返回的是一个map的list
     */
    public ArrayList<Short> getRecList() {
        return mRecDataList;
    }

    /**
     * 设置线与线之间的偏移
     *
     * @param offset 偏移值 pix
     */
    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    /**
     * dip转为PX
     */
    private int dip2px(Context context, float dipValue) {
        float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * fontScale + 0.5f);
    }

    /**
     * 毫秒转换像素
     *
     * @param msecs
     * @return 毫秒对应的像素
     */
    public int millisecsToPixels(int msecs) {
        return (int) ((msecs * 1.0 * 48000) / (1000.0 * 1152 / 3) + 0.5);
    }

    /**
     * 像素转换毫秒
     *
     * @param pixels
     * @return 像素对应的毫秒
     */
    public int pixelsToMillisecs(int pixels) {
        return (int) (pixels * (1000.0 * 1152 / 3) / 48000 + 0.5);
    }

    /**
     * 绘制帧数
     *
     * @param position
     */
    public void drawPosition(int position) {
        this.mPosition = position;
    }

    //内部类的线程
    class DrawWaveThread extends Thread {
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (mIsDraw) {
                if (!isPaseDraw) {
                    ArrayList<Short> dataList = cloneData();
                    drawWaveFrame(dataList);
//                    if (isCenterDrawTag) {
//                        if (mDrawOffSet > 0) {
//                            Log.i("info", "DrawOffSet=======<<<<" + mDrawOffSet);
//                            mDrawOffSet -= mSpeed;
//                        } else {
//                            removeData();
//                        }
//                    }
                    //休眠暂停资源
//                    DrawWaveSleep(4);
                } else {
                    DrawWaveSleep(500);
                }
            }
        }
    }

    private void DrawWaveSleep(int time) {
        //休眠暂停资源
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void removeData() {
//        synchronized (mRecDataList) {
//            if (mRecDataList.size() > 540) {
//                Log.i("info", "Size====>>>>" + mRecDataList.size());
//                for (int i = mSpeed; i > 0; i--) {
//                    mRecDataList.remove(i);
//                }
//                if (mRecDataList.size() < getWidth() * 2) {
//                    if (listener != null && isCallBack) {
//                        listener.loadData();
//                        isCallBack = false;
//                    }
//                }
//            }
//        }
//    }
//
//    public void removeData(long count) {
//        synchronized (mRecDataList) {
//            if (mRecDataList == null || mRecDataList.size() == 0) {
//                return;
//            }
//            long max = mRecDataList.size() >= count ? count : mRecDataList.size();
//            for (long i = max; i >= 540; i--) {
//                mRecDataList.remove(i);
//            }
//        }
//    }

    private ArrayList<Short> cloneData() {
        ArrayList<Short> dataList = new ArrayList<>();
        synchronized (mRecDataList) {
            if (mRecDataList.size() != 0) {
                dataList = (ArrayList<Short>) mRecDataList.clone();// 保存  接收数据
            }
        }
        return dataList;
    }

    public boolean drawWaveFrame(ArrayList<Short> dataList) {


        float rateY = (float) (getHeight() - line_off) / (65535 / 2);

        System.out.println("drawThread.run "
                + " dataList.size : " + dataList.size()
                + " rateY : " + rateY);

        synchronized (mLock) {
            Canvas canvas = null;// 关键:获取画布
            try {
                canvas = getHolder().lockCanvas(
                        new Rect(0, 0, getWidth(), getHeight()));// 关键:获取画布
                if (canvas == null)
                    return true;

                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                int start = (int) ((dataList.size()));
                float py = getHeight() / 2;
                float y;

                if (getWidth() / 2 - start <= marginRight) {//如果超过预留的右边距距离
                    start = getWidth() / 2 - marginRight;//画的位置x坐标
                }

                int height = getHeight() - line_off;
                canvas.drawLine(0, height * 0.5f + line_off / 2, getWidth(), height * 0.5f + line_off / 2, centerLinePaint);//中心线
                int size = dataList.size();
                if (isCenterDrawTag) {
                    int offset = mPosition > (getWidth() / 2) ? (mPosition - getWidth() / 2) : 0;
                    for (int i = offset; i < size; i++) {
                        y = Math.abs(dataList.get(i)) * rateY;
                        System.out.println("-------------------- " + mPosition);
                        float x = (offset == 0)
                                ? (getWidth() / 2 - mPosition + (i))
                                : i - offset;
                        //画线
                        canvas.drawLine(x, py - y, x, py + y, linePaint);//中间出波形
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        y = Math.abs(dataList.get(i)) * rateY;
                        float x = (i);
                        if (getWidth() / 2 - (i - 1) <= marginRight) {
                            x = getWidth() / 2 - marginRight;
                        }
                        //画线
                        canvas.drawLine(x, py - y, x, py + y, linePaint);//中间出波形
                    }
                }

                if (mProgressTag != null) {
                    int left = 0;
                    if (isCenterDrawTag) {
                        left = mWidthSpecSize / 2 - 20;
                    } else {
                        left = start;
                        if (start <= 20) {
                            left = 0;
                        } else if (start > 20 && start <= getWidth() / 2) {
                            left = start - 20;
                        }
                    }
                    canvas.drawBitmap(mProgressTag, left, 0, null);
                }


                if (mRuler != null) {
                    canvas.drawBitmap(mRuler, 0, getHeight() - mRuler.getHeight(), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
                }
            }
        }
        return false;
    }

//    /**
//     * 设置回调接口对象
//     */
//    public void setCallBackListener(CallBackListener listener) {
//        this.listener = listener;
//    }
//
//    public interface CallBackListener {
//        void loadData();
//    }

//    public void setCallBack(boolean callBack) {
//        isCallBack = callBack;
//    }

//    public void setmDrawOffSet(int mDrawOffSet) {
//        this.mDrawOffSet = mDrawOffSet;
//    }
//
//    public void setMultiple(float multiple) {
//        this.mSpeed = (int) (multiple * 8);
//    }
}
