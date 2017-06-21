package com.tingtingfm.cbb.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by liming on 16/12/29.
 */

public class MaterialGridView extends GridView {
    public MaterialGridView(Context context) {
        super(context);
    }

    public MaterialGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置不滚动
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
