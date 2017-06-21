package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ApprovalInfo;

/**
 * Created by tianhu on 2017/4/14.
 * 待审列表，已审列表共用Adapter,
 */

public class ApprovalAdapter extends CommonAdapter<ApprovalInfo> {

    private int ApprovalFlag;//区分待审(0)，已审(1)

    public ApprovalAdapter(Context context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    public void covert(ViewHolder holder, ApprovalInfo messageInfo) {
        //判断返回数据不为空
        if(messageInfo != null){
            TextView tvState = (TextView) holder.getContentView().findViewById(R.id.approval_item_state);
            holder.setText(R.id.approval_item_title, messageInfo.getManuscriptName());
            holder.setText(R.id.approval_item_time, messageInfo.getTime());
            if (ApprovalFlag == 0) {
                holder.setText(R.id.approval_item_des, context.getString(R.string.manuscript_approval_proposer));
            } else if (ApprovalFlag == 1) {
                holder.setText(R.id.approval_item_des, context.getString(R.string.manuscript_approval_approver));
            }
            holder.setText(R.id.approval_item_proposer, messageInfo.getProposer());
            setStateView(messageInfo.getState(),tvState);
        }
    }

    private void setStateView(int state, TextView tvState) {
        //待审状态，“等待一审，等待二审”
//        String str = null;
//        int drawableId = R.drawable.tv_radius_bg_b0b5bd;
//        int color = R.color.color_8c949f;
        String str;
        int drawableId;
        int color;
        switch (state) {
            default:
                drawableId = R.drawable.tv_radius_bg_b0b5bd;
                color = context.getResources().getColor(R.color.color_8c949f);
                str = context.getString(R.string.manuscript_approval_drafts);
                break;
            case 1:
                drawableId = R.drawable.manuscript_submited_background;
                color = context.getResources().getColor(R.color.color_4a89dc);
                str = context.getString(R.string.manuscript_approval_wait_one);
                break;
            case 2:
                drawableId = R.drawable.manuscript_submited_background;
                color = context.getResources().getColor(R.color.color_4a89dc);
                str = context.getString(R.string.manuscript_approval_wait_two);
                break;
            case 3:
                drawableId = R.drawable.manuscript_submited_background;
                color = context.getResources().getColor(R.color.color_4a89dc);
                str = context.getString(R.string.manuscript_approval_wait_three);
                break;
            case 4:
                drawableId = R.drawable.tv_radius_bg_48cfad;
                color = context.getResources().getColor(R.color.color_37bc9b);
                str = context.getString(R.string.manuscript_approval_success);
                break;
            case 5:
                drawableId = R.drawable.tv_radius_bg_ed5565;
                color = context.getResources().getColor(R.color.color_da4453);
                str = context.getString(R.string.manuscript_approval_back);
                break;
        }
        tvState.setText(str);
        tvState.setTextColor(color);
        tvState.setBackgroundResource(drawableId);
    }

    /**
     * 区分待审(0)，已审(1)
     *
     * @param flag
     */
    public void setApprovalFlag(int flag) {
        this.ApprovalFlag = flag;
    }

}