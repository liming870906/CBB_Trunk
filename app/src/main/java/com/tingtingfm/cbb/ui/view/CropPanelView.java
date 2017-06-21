package com.tingtingfm.cbb.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.utils.ScreenUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by think on 2017/1/13.
 */

public class CropPanelView {
    private Handler mHandle;
    private Context mContext;
    private Dialog moreDialog;

    public CropPanelView(Context context, Handler handler) {
        mContext = context;
        mHandle = handler;
    }

    public void show() {
        moreDialog = new Dialog(mContext, R.style.defind_dialog);

        LinearLayout contentView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.view_crop_layout, null);
        ButterKnife.bind(this, contentView);

        int count = contentView.getChildCount();
        for (int i = 0; i < count - 2; i++) {
            LinearLayout childLayout = (LinearLayout) contentView.getChildAt(i);
            for (int j = 0; j < childLayout.getChildCount(); j++) {
                final int what = 4 * i + j;
                childLayout.getChildAt(j).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mHandle.obtainMessage(what).sendToTarget();
                        onclickCloseView();
                    }
                });
            }
        }

        Window window = moreDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        moreDialog.setContentView(contentView);// 设置布局
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ScreenUtils.getScreenWidth();
        moreDialog.getWindow().setAttributes(lp);
        moreDialog.getWindow().setWindowAnimations(R.style.mini_player_pop_anim_style);

        moreDialog.show();
    }

    @OnClick(R.id.view_crop_close)
    void onclickCloseView() {
        if (moreDialog != null && moreDialog.isShowing()) {
            moreDialog.dismiss();
        }
    }

}
