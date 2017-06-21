package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaGroupInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.ui.view.MaterialGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liming on 16/12/28.
 */

public class MaterialManageAdapter extends BaseAdapter implements MaterialManageItemAdapter.OnItemClickListener {
//    private ArrayList<Map<String, ArrayList<MediaInfo>>> mData;
    private ArrayList<MediaGroupInfo> mData;
    private LayoutInflater inflater;
    private Context context;
    private SDCardImageLoader loader;
    private VideoImageLoader mVideoLoader;
    private boolean isChoose;
    private boolean isNavigation = true;
    private OnMaterialItemClickListener listener;
    private Map<Integer, boolean[]> mMapCheckes;

    public MaterialManageAdapter(ArrayList<MediaGroupInfo> mData, Context context, boolean isChoose) {
        this.mData = mData;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.loader = SDCardImageLoader.getInstance();
        this.mVideoLoader = VideoImageLoader.getInstance();
        this.isChoose = isChoose;
        this.mMapCheckes = new HashMap<>();
    }

    public void setData(ArrayList<MediaGroupInfo> mData) {
        if (mData != null) {
            this.mData = mData;
        } else {
            this.mData = new ArrayList<>();
        }
        this.notifyDataSetChanged();
    }

    public void setData(ArrayList<MediaGroupInfo> mData, boolean isChoose) {
        setIsChoose(isChoose);
        if (mData != null) {
            this.mData = mData;
        } else {
            this.mData = new ArrayList<>();
        }
        this.notifyDataSetChanged();
    }

    public void setIsChoose(boolean isChoose) {
        this.isChoose = isChoose;
    }

    public void clearMapChecks(){
        mMapCheckes.clear();
    }

    public void setNavigation(boolean navigation) {
        isNavigation = navigation;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.item_material_manager_lv, viewGroup, false);
            holder = new ViewHolder();
            holder.tvTime = (TextView) view.findViewById(R.id.tv_item_material_manager_time);
            holder.gvList = (MaterialGridView) view.findViewById(R.id.gv_item_material_manager_list);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        MediaGroupInfo _mediaGroupInfo = (MediaGroupInfo) getItem(i);
        if(_mediaGroupInfo != null){
            holder.tvTime.setText(TimeUtils.isTodayForDate(_mediaGroupInfo.getDate()) ? context.getString(R.string.material_manage_date_title_today) : _mediaGroupInfo.getDate());
            ArrayList<MediaInfo> _infos = _mediaGroupInfo.getMediaInfos();
            //创建数组
            boolean[] _checkes = mMapCheckes.get(i);
            //判断数组是否为null
            if (_checkes == null) {
                //创建新数组
                _checkes = new boolean[_infos != null ? _infos.size() : 0];
            }
            //初始化适配器对象
            final MaterialManageItemAdapter _itemAdapter = new MaterialManageItemAdapter(_infos, context, loader,mVideoLoader, isChoose, _checkes, i,isNavigation);
            //注册信息
            _itemAdapter.setOnItemClickListener(this);
            //设置数据
            holder.gvList.setAdapter(_itemAdapter);
            //保存数组
            mMapCheckes.put(i, _checkes);
        }

        return view;
    }

    public void setOnMaterialItemClickListener(OnMaterialItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(MediaInfo info) {
        if (listener != null) {
            listener.onMaterialItemClick(info);
        }
    }

    @Override
    public void controlMaterial(MediaInfo info, boolean[] checks, boolean isChoose, int parentPostion) {
        if (mMapCheckes != null && mMapCheckes.containsKey(parentPostion)) {
            mMapCheckes.put(parentPostion, checks);
        }
        //存储当前位置
        if (listener != null) {
            listener.controlMaterial(info, isChoose);
        }
    }

    class ViewHolder {
        TextView tvTime;
        MaterialGridView gvList;
    }

    public interface OnMaterialItemClickListener {
        void onMaterialItemClick(MediaInfo info);

        void controlMaterial(MediaInfo info, boolean isChoose);
    }
}
