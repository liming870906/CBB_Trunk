package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.ui.activity.CallInterface;
import com.tingtingfm.cbb.ui.activity.ManuscriptAddActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tianhu on 2017/6/15.
 * 稿件管理 listView adapter
 */

public class ManuscriptAdapter extends BaseAdapter {

    private final Context context;
    private boolean editState = false;
    private CallInterface callInterface;
    private ArrayList<ManuscriptInfo> selectDatas = new ArrayList<ManuscriptInfo>();
    private ArrayList<ManuscriptInfo> manuscriptInfos = new ArrayList<ManuscriptInfo>();
    private int locPos,cloudPos;//本地数据标签位置

    public ManuscriptAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (null != manuscriptInfos)
            return manuscriptInfos.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != manuscriptInfos && position < manuscriptInfos.size()) {
            return manuscriptInfos.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.manuscript_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ManuscriptInfo messageInfo = (ManuscriptInfo) getItem(position);
        if (null != holder) {
            //本地、云端标题显示
            if(locPos == position){
                holder.flagText.setVisibility(View.VISIBLE);
                holder.flagText.setText(context.getString(R.string.manuscript_loc));
            }else if(cloudPos == position){
                holder.flagText.setVisibility(View.VISIBLE);
                holder.flagText.setText(context.getString(R.string.manuscript_cloud));
            }else{
                holder.flagText.setVisibility(View.GONE);
            }

            holder.titleTxtView.setText(messageInfo.getTitle());
            TextView submitedTextView = holder.submitTxtView;
            holder.timeTxtView.setText(messageInfo.getCreateTime());
            ImageView flag = holder.selectImg;
            if (messageInfo.getIsSubmit() == 1) {//已提审，只显示提审
                submitedTextView.setBackgroundResource(R.drawable.manuscript_submited_background);
                submitedTextView.setText(context.getString(R.string.manuscript_submit_ed));
                submitedTextView.setTextColor(context.getResources().getColor(R.color.color_4a89dc));
            } else if (messageInfo.getUploadState() == 1) {//已上传
                submitedTextView.setBackgroundResource(R.drawable.tv_radius_bg_48cfad);
                submitedTextView.setText(context.getString(R.string.material_manage_audio_upload_success));
                submitedTextView.setTextColor(context.getResources().getColor(R.color.color_48cfad));
            } else if (messageInfo.getUploadState() == 2) {//上传中...
                submitedTextView.setBackgroundResource(R.drawable.tv_radius_bg_b0b5bd);
                submitedTextView.setText(context.getString(R.string.material_manage_audio_upload_loading));
                submitedTextView.setTextColor(context.getResources().getColor(R.color.color_8c949f));
            } else if (messageInfo.getUploadState() == 3) {//上传失败
                submitedTextView.setBackgroundResource(R.drawable.tv_radius_bg_ed5565);
                submitedTextView.setText(context.getString(R.string.material_manage_audio_upload_failure));
                submitedTextView.setTextColor(context.getResources().getColor(R.color.color_da4453));
            } else {//未上传
                submitedTextView.setBackgroundResource(R.drawable.tv_radius_bg_b0b5bd);
                submitedTextView.setText(context.getString(R.string.material_manage_audio_upload_default));
                submitedTextView.setTextColor(context.getResources().getColor(R.color.color_8c949f));
            }

            if (editState) {
                flag.setVisibility(View.VISIBLE);
                if (selectDatas.contains(messageInfo)) {
                    flag.setImageResource(R.drawable.meg_selected);
                } else {
                    flag.setImageResource(R.drawable.meg_select);
                }
            } else {
                flag.setVisibility(View.GONE);
            }

            holder.rootRlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editState) {
                        if (selectDatas.contains(messageInfo)) {
                            selectDatas.remove(messageInfo);
                        } else {
                            selectDatas.add(messageInfo);
                        }
                        notifyDataSetChanged();
                        if (null != callInterface) {
                            callInterface.clickCall(CallInterface.SELECT_VAL, 0);
                        }
                    } else {
                        Intent in = new Intent(context, ManuscriptAddActivity.class);
                        in.putExtra("manuInfo", messageInfo);
                        context.startActivity(in);
                    }
                }
            });
        }
        return convertView;
    }

    public void setData(ArrayList<ManuscriptInfo> data,ArrayList<ManuscriptInfo> cloudData) {
        this.manuscriptInfos.clear();
        if(null != data && data.size()>0){
            locPos = 0;
            cloudPos = data.size();
            this.manuscriptInfos.addAll(data);
        }else{
            locPos = -1;
            cloudPos = 0;
        }
        if(null != cloudData && cloudData.size()>0){
            this.manuscriptInfos.addAll(cloudData);
        }else{
            cloudPos = -1;
        }
    }

    class ViewHolder {
        @BindView(R.id.manuscript_flagtext)
        TextView flagText;
        @BindView(R.id.manuscript_info_flag)
        ImageView selectImg;
        @BindView(R.id.manuscript_item_title)
        TextView titleTxtView;
        @BindView(R.id.manuscript_item_time)
        TextView timeTxtView;
        @BindView(R.id.manuscript_submited_textView)
        TextView submitTxtView;
        @BindView(R.id.manuscript_rLayout)
        RelativeLayout rootRlayout;
        @BindView(R.id.manuscript_phoneFlag)
        ImageView phoneFlag;
        @BindView(R.id.manuscript_uploadState)
        ImageView uploadState;
        public ViewHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    /**
     * 设置编辑状态
     *
     * @param editState
     */
    public void setEditState(boolean editState) {
        this.editState = editState;
        notifyDataSetChanged();
    }

    /**
     * 设置回听接口
     *
     * @param callInterface
     */
    public void setCallInterface(CallInterface callInterface) {
        this.callInterface = callInterface;
    }

    /**
     * 设置选中数据集合
     *
     * @param selectDatas
     */
    public void setSelectDatas(ArrayList<ManuscriptInfo> selectDatas) {
        this.selectDatas = selectDatas;
    }
}
