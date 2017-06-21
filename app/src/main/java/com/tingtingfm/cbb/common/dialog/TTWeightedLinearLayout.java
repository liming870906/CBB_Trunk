package com.tingtingfm.cbb.common.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.tingtingfm.cbb.R;


/**
 * A special layout when measured in AT_MOST will take up a given percentage of
 * the available space.
 */
public class TTWeightedLinearLayout extends LinearLayout {

    private float mMajorWeight;

    private float mMinorWeight;

    public TTWeightedLinearLayout(Context context) {
        super(context);
    }

    public TTWeightedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray type = context.obtainStyledAttributes(attrs,
                R.styleable.TTWeightedLinearLayout);
        mMajorWeight = type.getFloat(
                R.styleable.TTWeightedLinearLayout_majorWeight, 0);
        mMinorWeight = type.getFloat(
                R.styleable.TTWeightedLinearLayout_minorWeight, 0);
        Log.i(".........", mMajorWeight + ">>>>>>>>>>>>>>>" + mMinorWeight);
        type.recycle();
    }

    /**
     * 衡量自定义的对话框
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		/*final DisplayMetrics metrics = getContext().getResources()
                .getDisplayMetrics();
		final int screenWidth = metrics.widthPixels; 
		final boolean isPortrait = screenWidth < metrics.heightPixels;
		
		final int widthMode = getMode(widthMeasureSpec);
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = getMeasuredWidth(); 
		int height = getMeasuredHeight(); 
		boolean measure = false;
		
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, EXACTLY); //获取到精确的高度
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, EXACTLY); 
		final float widthWeight = isPortrait ? mMinorWeight : mMajorWeight;
		if (widthMode == AT_MOST && widthWeight > 0.0f) {
//			if (width < (screenWidth * widthWeight)) {
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(
						(int) (screenWidth * widthWeight), EXACTLY);
				measure = true;
//			}
		}

		// TODO: Support height?

		if (measure) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}*/
    }
}

