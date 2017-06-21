package com.tingtingfm.cbb.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.tingtingfm.cbb.R;

/**
 * Created by lqsir on 2017/4/18.
 */

public class CommonProgress extends Dialog {
    private Context mContext;
    private CommonProgress(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
        this.mContext = context;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        ImageView imageView = (ImageView) findViewById(R.id.common_progress_imageview);

        Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        imageView.setAnimation(operatingAnim);
    }

    public static CommonProgress show(Context context, String message) {
        CommonProgress dialog = new CommonProgress(context, R.style.new_circle_progress);
        dialog.setContentView(R.layout.tt_common_progress_dialog);

        TextView txt = (TextView) dialog.findViewById(R.id.common_progress_message);
        txt.setText(message);

        // 按返回键是否取消
        dialog.setCancelable(true);
        // 设置居中
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        // 设置背景层透明度
        lp.dimAmount = 0.5f;
        dialog.getWindow().setAttributes(lp);
        // dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.show();
        return dialog;
    }
}
