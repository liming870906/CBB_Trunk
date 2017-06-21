package com.tingtingfm.cbb.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.tingtingfm.cbb.R;

/**
 * 自定义圆形进度环以及内圆填充进度。
 *
 * @author zhudejiu
 */
public class CircleProcessView extends View{
	private Context mContext;
	private int defaultColor = 0xFFFFFFFF;
	// 控件默认长、宽
	private float circleWidth;    //圆环的宽度
	private Paint paint, mPaint, nPaint;
	private int circleColor;
	private int roundProgressColor;
	private int dotColor;
	private float progress = 100; //当前进度
	private float maxProgress;  //最大进度
	private float circleMargin;  //与控件四边的间距
	private float circleArc;  //当前进度与初始进度的夹角
	private float curPoint_x;  //当前进度图标的x,y值
	private float curPoint_y;
	private Drawable roundbackground;


	@Override
	public void buildDrawingCache() {
		super.buildDrawingCache();
	}

	private float dx = 0.0f;
	private Bitmap mbmp;


	public CircleProcessView(Context context) {
		super(context);
		mContext = context;
	}

	public CircleProcessView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setCustomAttributes(attrs);
	}

	public CircleProcessView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setCustomAttributes(attrs);
	}

	private void setCustomAttributes(AttributeSet attrs) {
		paint = new Paint();
		mPaint = new Paint();
		TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
		circleMargin = a.getColor(R.styleable.CircleProgressView_circleMargin, 0);
		maxProgress = a.getColor(R.styleable.CircleProgressView_circleMaxProcess, 100);
		circleColor = a.getColor(R.styleable.CircleProgressView_circleColor, Color.RED);
        dotColor = a.getColor(R.styleable.CircleProgressView_dotColor, Color.GREEN);
		roundProgressColor = a.getColor(R.styleable.CircleProgressView_roundProgressColor, Color.BLUE);
		roundbackground = a.getDrawable(R.styleable.CircleProgressView_roundBackground);
		circleWidth = 15;
		mbmp = BitmapFactory.decodeResource(getResources(), R.drawable.manuscript_process);
	}

	@SuppressLint({ "ResourceAsColor", "NewApi", "DrawAllocation" })
	@Override
	protected void onDraw(Canvas canvas) {

		//设置进度是实心还是空心
		mPaint.setStrokeWidth(circleWidth); //设置圆环的宽度
		mPaint.setColor(roundProgressColor);  //设置进度的颜色

		mPaint.setAntiAlias(true);  //消除锯齿
		mPaint.setStyle(Paint.Style.STROKE);

		nPaint = new Paint();
		nPaint.setStrokeWidth(circleWidth);
		nPaint.setColor(roundProgressColor);
		nPaint.setAntiAlias(true);  //消除锯齿
		nPaint.setStyle(Paint.Style.FILL);

		/**
		 * 画最外层的大圆环
		 */
		float centre = getWidth()/2; //获取圆心的x坐标
		float cRadius = (centre - circleMargin - circleWidth/2); //圆环的半径
		paint.setColor(circleColor); //设置圆环的颜色
		paint.setStrokeWidth(1); //设置圆环的宽度
		paint.setAntiAlias(true);  //消除锯齿
//		canvas.drawCircle(centre, centre, cRadius, paint); //画出圆环


		/**
		 * 画圆弧 ，画圆环的进度
		 */

		circleArc = 360 * progress / maxProgress;
		float x0 = getWidth() / 2;
		float y0 = getHeight() / 2 - (getWidth() / 2 - circleMargin - circleWidth + mbmp.getHeight() / 2);
		float r0 = getHeight() / 2 - y0;
		curPoint_x = (float)(x0 + r0 * Math.sin(circleArc * Math.PI / 180));
		curPoint_y = (float)(y0 + r0 * (1- Math.cos(circleArc * Math.PI / 180)));
		RectF oval = new RectF(centre - cRadius, centre - cRadius, centre + cRadius, centre + cRadius);  //用于定义的圆弧的形状和大小的界限
//		//mPaint.setStrokeCap(Paint.Cap.ROUND);//设置圆角
		canvas.drawArc(oval, 270, -circleArc, false, mPaint);  //根据进度画圆弧

        //画线头半圆
        paint.setColor(dotColor);
        RectF arcRect = new RectF(getWidth()/2 - circleWidth/2 ,
                                 circleMargin + 1,
                                 (getWidth()/2)+ circleWidth - circleWidth/2,
                                 circleMargin + circleWidth -1);

        canvas.drawArc(arcRect,0,360,false,paint);

		/**
		 * 进度图标
		 */
		canvas.save();
		RectF recf = new RectF((getWidth() * 0.5f - circleWidth * 0.5f + circleWidth / 10.0f)-7,
				(circleWidth + circleMargin - circleWidth - circleWidth / 10.0f)-5,
				(getWidth() * 0.5f + circleWidth * 0.5f - circleWidth / 10.0f)+7,
				(circleWidth + circleMargin)+5);
		canvas.rotate(-circleArc, getWidth() / 2, getHeight() / 2);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		mPaint.setColor(roundProgressColor);
		canvas.drawBitmap(mbmp, null, recf, mPaint);
		canvas.restore();

	}



	public synchronized float getMax() {
		return maxProgress;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.maxProgress = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized float getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(float progress) {
        progress = 100 - progress;
		if(progress < 0){
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > maxProgress){
			progress = maxProgress;
		}
		if(progress <= maxProgress){
			this.progress = progress;
			postInvalidate();
		}

	}


	public int getCircleColor() {
		return circleColor;
	}

	public void setCircleColor(int cricleColor) {
		this.circleColor = cricleColor;
	}

	public int getRoundProgressColor() {
		return roundProgressColor;
	}

	public void setRoundProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public float getCircleWidth() {
		return circleWidth;
	}

	public void setCircleWidth(float roundWidth) {
		this.circleWidth = roundWidth;
	}

	public float getCircleMargin() {
		return circleMargin;
	}

	public void setCircleMargin(float circleMargin) {
		this.circleMargin = circleMargin;
	}

}
