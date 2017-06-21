package com.tingtingfm.cbb.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.tingtingfm.cbb.R;

/**
 * Created by lqsir on 2017/4/5.
 */

public class WaveView extends View {
    private float waveFirstFrequency2;
    private int waveLineFirstWidth2;
    private int DEF_HEIGHT = 60;
    private Paint waveFirstPaint;
    private Paint waveSecondPaint;
    private float defaultCrestValue;
    private float waveFirstAmplifier;
    private float waveFirstAmplifier2;
    private float waveSecondAmplifier;
    private float waveFirstFrequency;
    private float waveSecondFrequency;
    private float waveFirstPhase;
    private float waveSecondPhase;
    private int waveLineFirstWidth;
    private int waveLineSecondWidth;
    private int viewWidth;
    private float viewCenterY;
    private int waveFirstColor;
    private int waveSecondColor;

    ValueAnimator valueAnimator;
    private Paint waveFirstPaint2;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DEF_HEIGHT = context.getResources().getDimensionPixelOffset(R.dimen.dp_264);
        waveFirstColor = Color.parseColor("#fefefe");//第一条线颜色
        waveSecondColor = Color.parseColor("#eeeeee");//第二条线颜色

        waveLineFirstWidth = 9;//第一条线的宽度，即粗细
        waveLineFirstWidth2 = 4;//第一条线的宽度，即粗细
        waveLineSecondWidth = 4;//第二条线的宽度，即粗细

        defaultCrestValue = waveFirstAmplifier = DEF_HEIGHT / 3;//第一条线的振幅
        waveFirstAmplifier2 = DEF_HEIGHT / 6;//第一条线的振幅
        waveSecondAmplifier = DEF_HEIGHT / 6;//第二条线的振幅

        waveFirstPhase = 45.0f;//第一条线的相位，初始X轴偏移
        waveSecondPhase = 45.0f;//第二条线的相位，初始X轴偏移

        waveFirstFrequency = 2.0f;//第一条线的频率，可改变波长
        waveFirstFrequency2 = 3.0f;//第一条线的频率，可改变波长
        waveSecondFrequency = 3.0f;//第二条线的频率，可改变波长

        initTools();
    }

    private void initTools() {
        waveFirstPaint = new Paint();
        waveFirstPaint.setColor(waveFirstColor);
        waveFirstPaint.setAntiAlias(true);
        waveFirstPaint.setStyle(Paint.Style.FILL);
        waveFirstPaint.setStrokeJoin(Paint.Join.ROUND);
        waveFirstPaint.setStrokeCap(Paint.Cap.ROUND);
        waveFirstPaint.setStrokeWidth(waveLineFirstWidth);

        waveSecondPaint = new Paint();
        waveSecondPaint.setColor(waveSecondColor);
        waveSecondPaint.setAntiAlias(true);
        waveSecondPaint.setStyle(Paint.Style.FILL);
        waveSecondPaint.setStrokeJoin(Paint.Join.ROUND);
        waveSecondPaint.setStrokeCap(Paint.Cap.ROUND);
        waveSecondPaint.setStrokeWidth(waveLineSecondWidth);

        waveFirstPaint2 = new Paint();
        waveFirstPaint2.setColor(waveFirstColor);
        waveFirstPaint2.setAntiAlias(true);
        waveFirstPaint2.setStyle(Paint.Style.FILL);
        waveFirstPaint2.setStrokeJoin(Paint.Join.ROUND);
        waveFirstPaint2.setStrokeCap(Paint.Cap.ROUND);
        waveFirstPaint2.setStrokeWidth(waveLineFirstWidth2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < viewWidth - 1; i++) {
            canvas.drawLine((float) i,
                    viewCenterY - waveFirstAmplifier2 * (float) (Math.sin(waveFirstPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveFirstFrequency2 * i / viewWidth)),
                    (float) (i + 1),
                    viewCenterY - waveFirstAmplifier2 * (float) (Math.sin(waveFirstPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveFirstFrequency2 * (i + 1) / viewWidth)),
                    waveFirstPaint2);


            canvas.drawLine((float) i,
                    viewCenterY - waveSecondAmplifier * (float) (Math.sin(-waveSecondPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveSecondFrequency * i / viewWidth)),
                    (float) (i + 1),
                    viewCenterY - waveSecondAmplifier * (float) (Math.sin(-waveSecondPhase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveSecondFrequency * (i + 1) / viewWidth)),
                    waveSecondPaint);

            canvas.drawLine((float) i,
                    viewCenterY - waveFirstAmplifier * (float) (Math.sin(DEF_HEIGHT * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveFirstFrequency * i / viewWidth)),
                    (float) (i + 1),
                    viewCenterY - waveFirstAmplifier * (float) (Math.sin(DEF_HEIGHT * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * waveFirstFrequency * (i + 1) / viewWidth)),
                    waveFirstPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewCenterY = h / 2;
        waveFirstAmplifier = (waveFirstAmplifier * 2 > h) ? (h / 2) : waveFirstAmplifier;
        waveFirstAmplifier2 = (waveFirstAmplifier2 * 2 > h) ? (h / 2) : waveFirstAmplifier2;
        waveSecondAmplifier = (waveSecondAmplifier * 2 > h) ? (h / 2) : waveSecondAmplifier;
        waveAnim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightMeaure;

        if (heightMeasureMode == MeasureSpec.AT_MOST || heightMeasureMode == MeasureSpec.UNSPECIFIED) {
            heightMeaure = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_HEIGHT, getResources().getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeaure, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void waveAnim() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0F, 1.F);
            valueAnimator.setDuration(2000);//控制移动快慢，值越小越快
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);//重新启动
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限重复
            valueAnimator.setInterpolator(new LinearInterpolator());//速率变化
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float aFloat = Float.valueOf(animation.getAnimatedValue().toString());
                    waveFirstPhase = 360.F * aFloat;
                    waveSecondPhase = 360.F * aFloat;

                    if (aFloat < 0.5f) {
                        if (aFloat < 0.25f) {
                            waveFirstAmplifier = (1 - aFloat * 4) * defaultCrestValue;
                        } else {
                            waveFirstAmplifier = (float) (-defaultCrestValue + (4 * (0.5 - aFloat) * defaultCrestValue));
                            if (waveFirstAmplifier < -defaultCrestValue) {
                                waveFirstAmplifier = -defaultCrestValue;
                            } else if (waveFirstAmplifier > 0) {
                                waveFirstAmplifier = 0;
                            }
                        }
                    } else {
                        if (aFloat < 0.75f) {
                            waveFirstAmplifier = (float) (-defaultCrestValue + (4 * (aFloat - 0.5) * defaultCrestValue));
                            if (waveFirstAmplifier < -defaultCrestValue) {
                                waveFirstAmplifier = -defaultCrestValue;
                            } else if (waveFirstAmplifier > 0) {
                                waveFirstAmplifier = 0;
                            }
                        } else {
                            waveFirstAmplifier = (float) (4 * (aFloat - 0.75f) * defaultCrestValue);
                            if (waveFirstAmplifier < 0) {
                                waveFirstAmplifier = 0;
                            } else if (waveFirstAmplifier > defaultCrestValue) {
                                waveFirstAmplifier = defaultCrestValue;
                            }
                        }
                    }
                    invalidate();
                }
            });
        }

        valueAnimator.start();
        isRunning = true;
    }

    public void wavePause() {
        if (valueAnimator != null) {
            isRunning = false;
            valueAnimator.cancel();
        }
    }

    private boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }
}
