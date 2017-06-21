package com.tingtingfm.cbb.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.ui.activity.AudioPreviewActivity;
import com.tingtingfm.cbb.ui.adapter.MaterialAudioAdapter;
import com.tingtingfm.cbb.ui.eventbus.MediaDataEvent;
import com.tingtingfm.cbb.ui.eventbus.MessageEvent;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;

import butterknife.BindView;


/**
 * 素材管理音频列表碎片
 * Created by liming on 17/4/17.
 */

public class MaterialAudioFragment extends BaseFragment implements MaterialAudioAdapter.OnItemClickListener {

    @BindView(R.id.lv_material_audio_list)
    ListView lvMaterialAudio;
    @BindView(R.id.ll_material_audio_no_data_layout)
    LinearLayout llMaterialNoDataLayout;
    //声明适配器对象
    MaterialAudioAdapter mAdapter;
    //声明数据对象
    private ArrayList<MediaInfo> mData;
    //设置选择状态
    private boolean isChoose = false;
    //
    private boolean isFirst = true;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_material_audio, container, false);
    }

    @Override
    protected void handleCreate() {
        //获得数据
        mData = MediaDataManager.getInstance().getmMediaInfos();
        //声明适配器对象
        mAdapter = new MaterialAudioAdapter(mData, mActivity);
        //设置适配器
        lvMaterialAudio.setAdapter(mAdapter);
        //添加监听器方法
        addListener();
        isFirstGo();
    }

    private void isFirstGo() {
        if (isFirst){
            isFirst = false;
        }else{
            if(mData.size() <= 0){
                showNoMaterialLayout();
            }
        }
    }

    @Override
    protected void processMessage(Message msg) {

    }

    private void addListener() {
        mAdapter.setOnItemClickListener(this);
    }

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

    public void onEventMainThread(MessageEvent msg) {
        mData = MediaDataManager.getInstance().getmMediaInfos();
        switch (msg.what) {
            case Constants.MATERIAL_LOAD_DATA_TAG://加载数据完成
                if (mData != null && mData.size() > 0) {
                    mAdapter.setData(mData);
                } else {
                    showNoMaterialLayout();
                }
                break;
            case Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG://选择按钮状态
                //获得选择状态
                this.isChoose = (boolean) msg.obj;
                //清除数据
                if (!isChoose) {
                    mAdapter.clearMap();
                }
                //设置选择状态
                mAdapter.setChoose(isChoose);
                //更新数据
                mAdapter.setData(mData);
                if (mData.size() <= 0) {
                    showNoMaterialLayout();
                }
                break;
        }
    }

    private void showNoMaterialLayout() {
        lvMaterialAudio.setVisibility(View.GONE);
        llMaterialNoDataLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(MediaInfo info) {
        //跳转音频预览界面
        Intent intent = new Intent(mActivity, AudioPreviewActivity.class);
        intent.putExtra(Constants.MEDIA_AUDIO_KEY, info);
        startActivity(intent);
    }

    @Override
    public void controlMaterial(MediaInfo info, boolean checks) {
        mAdapter.setData(mData);
        EventBus.getDefault().post(new MediaDataEvent(info, checks));
    }
}
