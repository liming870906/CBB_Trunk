package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.tingtingfm.cbb.R;

/**
 * Created by tianhu on 2017/1/6.
 */

public class NotNetUtils {

    public static final int NOT_NET_RELOAD = 0x22201;
    private static View reLoadImg;
    private static Animation operatingAnim;

    /**
     * 添加无网络视图
     */
    public static View getNotNetView(Context con, final Handler handler) {
        View view = LayoutInflater.from(con).inflate(R.layout.not_net, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.not_net_layout);
        reLoadImg = view.findViewById(R.id.not_net_imageView);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(NOT_NET_RELOAD);
            }
        });
        return view;
    }

    public static void startAnim(Context con) {
        if (operatingAnim == null && null != reLoadImg) {
            //图片加动画
            operatingAnim = AnimationUtils.loadAnimation(con, R.anim.rotate);
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            reLoadImg.setAnimation(operatingAnim);
        }else{
            reLoadImg.startAnimation(operatingAnim);
        }
    }

    public static void stopAnim() {
        if (null != reLoadImg)
            reLoadImg.clearAnimation();
    }
}
