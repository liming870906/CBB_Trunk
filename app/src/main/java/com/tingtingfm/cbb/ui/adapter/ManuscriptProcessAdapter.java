package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.bean.ProcessInfo;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.ui.activity.CallInterface;
import com.tingtingfm.cbb.ui.activity.ManuscriptAddActivity;

import java.util.ArrayList;

/**
 * Created by tianhu on 2017/1/17.
 * 稿件流程 gridView adapter
 */

public class ManuscriptProcessAdapter extends CommonAdapter<ProcessInfo> {
    private int selectId = -1;

    public ManuscriptProcessAdapter(Context context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    public void covert(ViewHolder holder, final ProcessInfo processInfo) {
        TextView title = holder.getView(R.id.process_textview);
        title.setText(processInfo.getProcess_name());
        if(processInfo.getProcess_id() == selectId){
            title.setBackgroundResource(R.drawable.manuscript_process_background);
            title.setTextColor(context.getResources().getColor(R.color.color_697FB4));
        }else{
            title.setTextColor(context.getResources().getColor(R.color.color_767b8e));
            title.setBackgroundResource(R.drawable.manuscript_process_background1);
        }
    }

    public void setSelectId(int selectId) {
        this.selectId = selectId;
        notifyDataSetChanged();
    }
}
