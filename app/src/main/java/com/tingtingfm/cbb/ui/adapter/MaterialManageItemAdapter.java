package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by liming on 16/12/28.
 */

public class MaterialManageItemAdapter extends BaseAdapter {
    private ArrayList<MediaInfo> mData;
    private LayoutInflater inflater;
    private Context context;
    private SDCardImageLoader loader;
    private VideoImageLoader mVideoLoader;
    private boolean isChoose;
    private OnItemClickListener listener;
    private boolean[] mCheckBoxes;
    private int mParentPosition;
    private Animation mRotateAnim;
    private boolean isNavigation;

    public MaterialManageItemAdapter(ArrayList<MediaInfo> mData, Context context, SDCardImageLoader loader,VideoImageLoader mVideoLoader, boolean isChoose, boolean[] mCheckBoxes, int i, boolean isNavigation) {
        this.mData = mData;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.loader = loader;
        this.mVideoLoader = mVideoLoader;
        this.isChoose = isChoose;
        this.mCheckBoxes = mCheckBoxes;
        this.mParentPosition = i;
        this.mRotateAnim = AnimationUtils.loadAnimation(context, R.anim.anim_record_tape_wheel_rotate);
        this.mRotateAnim.setInterpolator(new LinearInterpolator());
        this.isNavigation = isNavigation;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.item_material_manager_gv, viewGroup, false);
            holder = new ViewHolder();
            holder.ivIcon = (ImageView) view.findViewById(R.id.iv_item_material_manager_icon);
            holder.tvName = (TextView) view.findViewById(R.id.tv_item_material_manager_name);
            holder.tvVideoTime = (TextView) view.findViewById(R.id.tv_item_material_manager_video_time);
            holder.ivUpload = (ImageView) view.findViewById(R.id.iv_item_material_manager_upload);
            holder.ivUploading = (ImageView) view.findViewById(R.id.iv_item_material_manager_upload_loading);
            holder.cbChoose = (CheckBox) view.findViewById(R.id.cb_item_material_manage_choose);
            holder.flLoadingLayout = (FrameLayout) view.findViewById(R.id.fl_item_material_manage_upload_loading_layout);
            holder.flVideoTimeLayout = (FrameLayout) view.findViewById(R.id.fl_item_material_manager_video_time_layout);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final MediaInfo _info = (MediaInfo) getItem(i);
        if (_info != null) {
            holder.tvName.setText(_info.getFullName());
            //图片资源
            if (_info.getMime_type().startsWith(Constants.MIME_TYPE_IMAGE)) {
                if (this.loader != null) {
                    holder.ivIcon.setTag(_info.getAbsolutePath());
                    loader.loadImage(2, _info.getAbsolutePath(), holder.ivIcon);
                }
            } else if (_info.getMime_type().startsWith(Constants.MIME_TYPE_VIDEO)) {
                if(this.mVideoLoader != null){
                    holder.ivIcon.setTag(_info.getAbsolutePath());
                    mVideoLoader.loadImage(_info.getAbsolutePath(), holder.ivIcon);
                }
                holder.flVideoTimeLayout.setVisibility(View.VISIBLE);
                holder.tvVideoTime.setText(TimeUtils.converToHms(_info.getDuration()/1000));
//                holder.ivIcon.setImageResource(R.drawable.material_video_default);
            } else if (_info.getMime_type().startsWith(Constants.MIME_TYPE_AUDIO)) {
                holder.ivIcon.setImageResource(R.drawable.material_audio_default);
            }
            //是否显示
            holder.cbChoose.setVisibility(isChoose ? View.VISIBLE : View.GONE);
            //判断是否显示
            if (holder.cbChoose.getVisibility() == View.VISIBLE) {
                //设置选择状态
                holder.cbChoose.setChecked(mCheckBoxes[i]);
            }
            if (isNavigation) {
                //显示上传控件方法
                showUploadView(holder, _info.getUpload_status());
            } else {
                showUploadView(holder, Constants.UPLOAD_STATUS_DEFAULT);
            }
            final ViewHolder finalHolder = holder;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        //选择状态
                        if (isChoose) {
                            //修改状态
                            mCheckBoxes[i] = !mCheckBoxes[i];
                            //设置状态
                            finalHolder.cbChoose.setChecked(mCheckBoxes[i]);
                            //回调传输数据
                            listener.controlMaterial(_info, mCheckBoxes, mCheckBoxes[i], mParentPosition);
                        } else {
                            //回调传输数据
                            listener.onItemClick(_info);
                        }
                    }
                }
            });
        }
        return view;
    }

    /**
     * 显示控件
     *
     * @param holder
     * @param uploadStatus
     */
    private void showUploadView(ViewHolder holder, int uploadStatus) {

        switch (uploadStatus) {
            case Constants.UPLOAD_STATUS_DEFAULT:
                holder.flLoadingLayout.setVisibility(View.INVISIBLE);
                holder.ivUpload.setVisibility(View.VISIBLE);
                holder.ivUpload.setImageResource(R.drawable.material_upload_no_upload);
                break;
            case Constants.UPLOAD_STATUS_SUCCESS:
                holder.flLoadingLayout.setVisibility(View.INVISIBLE);
                holder.ivUpload.setVisibility(View.VISIBLE);
                holder.ivUpload.setImageResource(R.drawable.material_upload_success);
                break;
            case Constants.UPLOAD_STATUS_FAILURE:
                holder.flLoadingLayout.setVisibility(View.INVISIBLE);
                holder.ivUpload.setVisibility(View.VISIBLE);
                holder.ivUpload.setImageResource(R.drawable.material_upload_failure);
                break;
            case Constants.UPLOAD_STATUS_LOADING:
                holder.ivUpload.setVisibility(View.INVISIBLE);
                holder.flLoadingLayout.setVisibility(View.VISIBLE);
                //开启旋转动画
                holder.ivUploading.startAnimation(mRotateAnim);
                break;
        }
    }

    class ViewHolder {
        ImageView ivIcon, ivUpload, ivUploading;
        TextView tvName,tvVideoTime;
        CheckBox cbChoose;
        FrameLayout flLoadingLayout,flVideoTimeLayout;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(MediaInfo info);

        void controlMaterial(MediaInfo info, boolean[] checks, boolean isChoose, int parentPosition);
    }
}
