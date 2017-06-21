package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tingtingfm.cbb.R;


/**
 * 弹出Toast的一个工具类，这里主要是增加了对系统Toast背景的修改
 *
 * @author Administrator
 */
public class ToastUtils {

    /**
     * @param context 上下文对象
     * @param msg     要显示的信息
     */
    public static void showToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
        LinearLayout layout = (LinearLayout) toast.getView();
        layout.setBackgroundResource(R.drawable.dialog_rectangle);
        layout.setPadding(80, 30, 80, 30);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.parseColor("#ffffff"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.text_size_48));
        tv.setText(msg);
        layout.removeAllViews();
        layout.addView(tv);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    public static void showToast(Context context, int resId) {
        showToast(context, context.getResources().getString(resId));
    }
}
