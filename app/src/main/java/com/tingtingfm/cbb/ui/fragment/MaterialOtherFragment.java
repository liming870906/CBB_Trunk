package com.tingtingfm.cbb.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaGroupInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.ui.activity.AudioPreviewActivity;
import com.tingtingfm.cbb.ui.activity.PreviewPhotoActivity;
import com.tingtingfm.cbb.ui.adapter.MaterialManageAdapter;
import com.tingtingfm.cbb.ui.eventbus.MediaDataEvent;
import com.tingtingfm.cbb.ui.eventbus.MessageEvent;


import java.util.ArrayList;

import butterknife.BindView;
import de.greenrobot.event.EventBus;

/**
 * 视频及图片界面
 * Created by liming on 17/4/17.
 */

public class MaterialOtherFragment extends BaseFragment implements MaterialManageAdapter.OnMaterialItemClickListener {
    @BindView(R.id.lv_material_manager_list)
    ListView lvMaterialList;
    @BindView(R.id.ll_material_manager_no_data_layout)
    LinearLayout llMaterialNoDataLayout;
    //声明适配器对象
    private MaterialManageAdapter mAdapter;
    //显示数据
    private ArrayList<MediaGroupInfo> mData;
    //设置选择标记
    private boolean isChoose = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_material_other, container, false);
    }

    @Override
    protected void handleCreate() {
        mData = MediaDataManager.getInstance().getmMediaGroupInfos();
        //初始化对象
        mAdapter = new MaterialManageAdapter(mData, mActivity, isChoose);
        //设置
        lvMaterialList.setAdapter(mAdapter);
        //添加监听器方法
        addListener();
        if(mData.size() <= 0){
            showNoMaterialLayout();
        }
    }

    /**
     * 添加监听器方法
     */
    private void addListener() {
        mAdapter.setOnMaterialItemClickListener(this);
    }

    @Override
    protected void processMessage(Message msg) {

    }

    public void onEventMainThread(MessageEvent msg){
        mData = MediaDataManager.getInstance().getmMediaGroupInfos();
        switch (msg.what){
            case Constants.MATERIAL_LOAD_DATA_TAG:
                if (mData != null && mData.size() > 0) {
                    //设置数据
                    mAdapter.setData(mData, isChoose);
                } else {
                    //引用显示没有数据布局
                    showNoMaterialLayout();
                }
                break;
            case Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG:
                isChoose = (boolean) msg.obj;
                //取消选择清除缓存
                if(!isChoose){
                    mAdapter.clearMapChecks();
                }
                //设置适配器标记
                mAdapter.setIsChoose(isChoose);
                //刷新页面
                mAdapter.setData(mData);
                if(mData != null && mData.size() <=0){
                    showNoMaterialLayout();
                }
                break;
        }
    }
    @Override
    public void onMaterialItemClick(MediaInfo info) {
        //页面分发
        controlGotoActivity(info);
    }

    @Override
    public void controlMaterial(MediaInfo info, boolean isCheck) {
        //设置数据
        mAdapter.setData(mData, isChoose);
        EventBus.getDefault().post(new MediaDataEvent(info,isCheck));
    }

    /**
     * 控制界面分发
     *
     * @param info
     */
    private void controlGotoActivity(MediaInfo info) {
        String _mimeType = info.getMime_type();
        if (!TextUtils.isEmpty(_mimeType)) {
            if (_mimeType.startsWith(Constants.MIME_TYPE_AUDIO)) {
                //跳转音频预览界面
                Intent intent = new Intent(mActivity, AudioPreviewActivity.class);
                intent.putExtra(Constants.MEDIA_AUDIO_KEY, info);
                startActivity(intent);
            } else if (_mimeType.startsWith(Constants.MIME_TYPE_IMAGE)) {
                Intent intent = new Intent(mActivity, PreviewPhotoActivity.class);
                intent.putExtra("mediaInfo", info);
                startActivity(intent);
            } else if (_mimeType.startsWith(Constants.MIME_TYPE_VIDEO)) {
                //跳转至系统视频预览界面
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + info.getAbsolutePath()), "video/*");
                startActivity(intent);
            }
        }
    }

    /**
     * 显示没有素材控件
     */
    private void showNoMaterialLayout() {
        //隐藏ListView列表
        lvMaterialList.setVisibility(View.GONE);
        //显示没有数据布局
        llMaterialNoDataLayout.setVisibility(View.VISIBLE);
    }
}
