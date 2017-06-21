package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liming on 17/4/17.
 */

public class MaterialAudioAdapter extends BaseAdapter {
    //设置数据
    private ArrayList<MediaInfo> mMediaInfos;
    private LayoutInflater mInflater;
    //设置缓存
    private Map<Integer, Boolean> mMapCheckes;
    private OnItemClickListener mListener;
    private boolean isChoose, isShowUploadStatus;
    private Context context;

    public MaterialAudioAdapter(ArrayList<MediaInfo> mediainfos, Context context) {
        this.context = context;
        this.mMediaInfos = mediainfos;
        this.mInflater = LayoutInflater.from(context);
        mMapCheckes = new HashMap<>();
        isShowUploadStatus = true;
    }

    public void setChoose(boolean isChoose) {
        this.isChoose = isChoose;
        if (!this.isChoose) {
            clearMap();
        }
    }

    public void setShowUploadStatus(boolean status) {
        this.isShowUploadStatus = status;
    }

    public void clearMap() {
        mMapCheckes.clear();
    }

    /**
     * 设置数据
     *
     * @param mediainfos
     */
    public void setData(ArrayList<MediaInfo> mediainfos) {
        this.mMediaInfos = mediainfos;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mMediaInfos != null && mMediaInfos.size() > 0) {
            return mMediaInfos.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return this.mMediaInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.mMediaInfos.get(position).getUser_id();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder _holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_material_audio_content_lv, parent, false);
            _holder = new ViewHolder();
            _holder.tvTitleAudioDate = (TextView) convertView.findViewById(R.id.tv_item_material_audio_time);
            _holder.tvAudioName = (TextView) convertView.findViewById(R.id.tv_item_material_audio_name);
            _holder.tvAudioDate = (TextView) convertView.findViewById(R.id.tv_item_material_audio_date);
            _holder.tvAudioTime = (TextView) convertView.findViewById(R.id.tv_item_material_audio_time_length);
            _holder.tvUpload = (TextView) convertView.findViewById(R.id.tv_item_material_upload_status);
            _holder.cbChoose = (CheckBox) convertView.findViewById(R.id.cb_item_material_audio_choose);
            _holder.llLayout = (LinearLayout) convertView.findViewById(R.id.ll_item_material_audio_layout);
            convertView.setTag(_holder);
        } else {
            _holder = (ViewHolder) convertView.getTag();
        }
        final MediaInfo _info = (MediaInfo) getItem(position);
        //判断数据不为空
        if (_info != null) {
            //当前时间
            String _time = TimeUtils.getYearMonthDayHMS(_info.getDate_added());
            String _now = TimeUtils.getYearMonthDayHMS(System.currentTimeMillis() / 1000);
            if (_now.equals(_time)) {
                //设置数据
                _holder.tvTitleAudioDate.setText(R.string.material_manage_date_title_today);
            } else {
                //设置数据
                _holder.tvTitleAudioDate.setText(_time);
            }
            _holder.tvAudioName.setText(_info.getFullName());
            _holder.tvAudioDate.setText(TimeUtils.getYearMonthDayHMS1(_info.getDate_added()*1000));
            _holder.tvAudioTime.setText(TimeUtils.converToHms((long) (_info.getDuration() / 1000)));
            _holder.cbChoose.setChecked(false);
            //是否显示
            _holder.cbChoose.setVisibility(isChoose ? View.VISIBLE : View.GONE);
            _holder.llLayout.setBackgroundResource(R.drawable.lv_item_material_layout_selector);
            //判断是否第一个数据
            if (position > 0) {
                MediaInfo _previous = (MediaInfo) getItem(position - 1);
                //当前时间
                String _previous_time = TimeUtils.getYearMonthDayHMS(_previous.getDate_added());
                if (_previous_time.equals(_time)) {
                    _holder.tvTitleAudioDate.setVisibility(View.GONE);
                } else {
                    _holder.tvTitleAudioDate.setVisibility(View.VISIBLE);
                }
            }else{
                _holder.tvTitleAudioDate.setVisibility(View.VISIBLE);
            }
            //获得缓存数据
            Boolean _isCheck = mMapCheckes.get(position);
            if (_isCheck == null) {
                _isCheck = false;
            }
            _holder.cbChoose.setChecked(_isCheck);
            mMapCheckes.put(position, _isCheck);
            if (isShowUploadStatus) {
                showUploadView(_holder, _info.getUpload_status());
            } else {
                _holder.tvUpload.setVisibility(View.INVISIBLE);
            }
            _holder.llLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        if (isChoose) {
                            mMapCheckes.put(position, !mMapCheckes.get(position));
                            mListener.controlMaterial(_info, mMapCheckes.get(position));
                        } else {
                            mListener.onItemClick(_info);
                        }
                    }
                }
            });
        }
        return convertView;
    }

    /**
     * 显示控件
     *
     * @param holder
     * @param uploadStatus
     */
    private void showUploadView(ViewHolder holder, int uploadStatus) {
        holder.tvUpload.setVisibility(View.VISIBLE);
        switch (uploadStatus) {
            case Constants.UPLOAD_STATUS_DEFAULT:
                holder.tvUpload.setBackgroundResource(R.drawable.tv_radius_bg_b0b5bd);
                holder.tvUpload.setTextColor(context.getResources().getColor(R.color.color_8c949f));
                holder.tvUpload.setText(R.string.material_manage_audio_upload_default);
                break;
            case Constants.UPLOAD_STATUS_SUCCESS:
                holder.tvUpload.setBackgroundResource(R.drawable.tv_radius_bg_48cfad);
                holder.tvUpload.setTextColor(context.getResources().getColor(R.color.color_37bc9b));
                holder.tvUpload.setText(R.string.material_manage_audio_upload_success);
                break;
            case Constants.UPLOAD_STATUS_FAILURE:
                holder.tvUpload.setBackgroundResource(R.drawable.tv_radius_bg_ed5565);
                holder.tvUpload.setTextColor(context.getResources().getColor(R.color.color_da4453));
                holder.tvUpload.setText(R.string.material_manage_audio_upload_failure);
                break;
            case Constants.UPLOAD_STATUS_LOADING:
                holder.tvUpload.setBackgroundResource(R.drawable.tv_radius_bg_b0b5bd);
                holder.tvUpload.setTextColor(context.getResources().getColor(R.color.color_8c949f));
                holder.tvUpload.setText(R.string.material_manage_audio_upload_loading);
                break;
        }
    }

    class ViewHolder {
        TextView tvAudioName, tvAudioDate, tvAudioTime, tvTitleAudioDate, tvUpload;
        CheckBox cbChoose;
        LinearLayout llLayout;
    }

    public interface OnItemClickListener {
        void onItemClick(MediaInfo info);

        void controlMaterial(MediaInfo info, boolean checks);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
}
