package com.tingtingfm.cbb.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MessageInfo;
import com.tingtingfm.cbb.ui.activity.CallInterface;
import com.tingtingfm.cbb.ui.activity.MessageDetailActivity;

import java.util.ArrayList;

/**
 * Created by tianhu on 2017/1/3.
 * 消息adapter
 */

public class MessageAdapter extends CommonAdapter<MessageInfo> {

    private final Activity activity;
    private ArrayList<MessageInfo> selectData;
    private boolean editState;//编辑状态
    private CallInterface anInterface;

    public MessageAdapter(Activity context, int layoutId) {
        super(context, layoutId);
        activity = context;
    }

    @Override
    public void covert(ViewHolder holder, final MessageInfo megInfo) {

        LinearLayout llayout = holder.getView(R.id.message_all_layout);
        llayout.setBackgroundResource(R.drawable.manuscript_list_bg_selector);
        ImageView mageView = holder.getView(R.id.message_info_imageView);
        ImageView flag = holder.getView(R.id.message_info_flag);
        TextView redTv = holder.getView(R.id.message_red);
        holder.setText(R.id.message_info_date, megInfo.getMessage_time());

        //设置标题，与内容数据显示
//        if (megInfo.getMessage_type() == 1 ||
//                megInfo.getMessage_type() == 2 ||
//                megInfo.getMessage_type() == 3
//                ) {
            holder.setText(R.id.message_info_title, megInfo.getMessage_title());
            holder.setText(R.id.message_info_title1, megInfo.getMessage_content().getContent_detail().getTips());
//        } else {
//            holder.setText(R.id.message_info_title, context.getString(R.string.message_unknown_title));
//            holder.setText(R.id.message_info_title1, context.getString(R.string.message_unknown_info));
//        }

        //设置图片显示
        if (megInfo.getMessage_type() == 1) {
            mageView.setImageResource(R.drawable.meg_text);
        } else if (megInfo.getMessage_type() == 2) {
            mageView.setImageResource(R.drawable.meg_group);
        } else if (megInfo.getMessage_type() == 3) {
            mageView.setImageResource(R.drawable.meg_material);
        } else {
            mageView.setImageResource(R.drawable.msg_new);
        }

        if(megInfo.getIs_read() == 0){
            redTv.setVisibility(View.VISIBLE);
        }else{
            redTv.setVisibility(View.GONE);
        }

        if(editState){
            flag.setVisibility(View.VISIBLE);
            if(selectData.contains(megInfo)){
                flag.setImageResource(R.drawable.meg_selected);
            }else {
                flag.setImageResource(R.drawable.meg_select);
            }
        }else{
            flag.setVisibility(View.GONE);
        }
        llayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editState){
                    if(selectData.contains(megInfo)){
                        selectData.remove(megInfo);
                    }else {
                        selectData.add(megInfo);
                    }
                    notifyDataSetChanged();
                    if(null != anInterface){
                        anInterface.clickCall(CallInterface.SELECT_VAL,0);
                    }
                }else{
                    if(megInfo.getMessage_type() == 1 ||
                            megInfo.getMessage_type() == 2||
                            megInfo.getMessage_type() == 3){
                        Intent in = new Intent(context, MessageDetailActivity.class);
                        in.putExtra("msgInfo",megInfo);
                        activity.startActivityForResult(in,MessageDetailActivity.RESULT);
                    }else{
                        if(null != anInterface){
                            anInterface.clickCall(CallInterface.UNKONWN_VAL,0);
                        }
                    }
                }
            }
        });
    }

    public void setSelectData(ArrayList<MessageInfo> selectD) {
        this.selectData = selectD;
    }

    public void setEditState(boolean editState) {
        this.editState = editState;
        notifyDataSetChanged();
    }

    public void setInterface(CallInterface ainterface) {
        this.anInterface = ainterface;
    }
}
