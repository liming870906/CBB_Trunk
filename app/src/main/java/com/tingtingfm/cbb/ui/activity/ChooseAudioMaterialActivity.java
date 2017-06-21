package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.ui.adapter.MaterialAudioAdapter;
import com.tingtingfm.cbb.ui.thread.IDataLoadListener;
import com.tingtingfm.cbb.ui.thread.DataTaskThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;

/**
 * 选择音频页面
 */
public class ChooseAudioMaterialActivity extends BaseActivity
    implements IDataLoadListener,MaterialAudioAdapter.OnItemClickListener {
    //声明控件
    @BindView(R.id.lv_choose_audio_material_list)
    ListView lvChooseAudios;
    @BindView(R.id.ll_choose_audio_material_no_data_layout)
    LinearLayout llChooseNoDataLayout;
    //显示音频数据
    private ArrayList<MediaInfo> mData;
    private MaterialAudioAdapter mAdapter;
    private ArrayList<MediaInfo> mUploadMedias;


    @Override
    protected View initContentView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_choose_audio_material,null);
    }

    @Override
    protected void handleCreate() {
        mData = new ArrayList<>();
        mUploadMedias = new ArrayList<>();
        setCenterViewContent(R.string.choose_audio_material_title_content);
        setRightView3Visibility(View.VISIBLE);
        setRightView3Content(R.string.audio_info_title_right_sure);
        setRightView3TextColor(R.color.color_80ffffff);
        mAdapter = new MaterialAudioAdapter(mData,this);
        mAdapter.setShowUploadStatus(false);
        lvChooseAudios.setAdapter(mAdapter);
        //开启加载数据线程
        new DataTaskThread().setLoadListener(this).crateNewMaterialRunnable().start();
        //
        addListener();
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what){
            case Constants.MATERIAL_LOAD_DATA_TAG:
                if(mData != null && mData.size() > 0 ){
                    mAdapter.setChoose(true);
                    mAdapter.setData(mData);
                }else{
                    showNoMaterialLayout();
                }
                break;
        }
    }

    private void addListener(){
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void startLoad() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadDialog();
            }
        });
    }

    @Override
    public void loadMaterialData() {
        mData = DBAudioRecordManager.getInstance(this).queryAllAudioRecord();
        //根据时间排序
        Collections.sort(mData, new MediaComparator());
        basicHandler.sendEmptyMessage(Constants.MATERIAL_LOAD_DATA_TAG);
    }

    @Override
    public void stopLoad() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDlg();
            }
        });
    }

    @Override
    public void onItemClick(MediaInfo info) {

    }

    @Override
    public void controlMaterial(MediaInfo info, boolean checks) {
        mAdapter.setData(mData);
        //判断是否选中数据
        if (checks) {
            //添加数据
            mUploadMedias.add(info);
        } else {
            //移除数据
            mUploadMedias.remove(info);
        }
        updateTitleView();
    }

    @Override
    protected void onRightView3Click() {
        super.onRightView3Click();
        resultData();
    }

    //将选中数据返回--tianhu
    private void resultData() {
        if (null != mUploadMedias && mUploadMedias.size() > 0) {
            //超过500文件个数
            ArrayList<MediaInfo> _More500MFiles = getListForSize(GlobalVariableManager.MAXWIFIFILESIZE, mUploadMedias);
            //判断是否提示对话框
            if (_More500MFiles.size() > 0) {
                //显示提示框
                showMore500MDialog(_More500MFiles.size(), R.string.material_manage_more_count_text2);
            } else {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("ObjList", mUploadMedias);
                setResult(0, intent);
                finish();
            }
        }
    }
    /**
     * 显示大于500MB对话框
     */
    private void showMore500MDialog(int count, int messageResId) {
//        new TTAlertDialog.Builder(this)
//                .setMessage(getString(messageResId, count))
//                .setNegativeButton(R.string.audio_record_dialog_sure, null)
//                .create()
//                .show();
        showOneButtonDialog(true, "", getString(messageResId, count), null);
    }

    /**
     * 过滤给定的集合中文件大小>500M的数据
     *
     * @return
     */
    private ArrayList<MediaInfo> getListForSize(int fileSize, ArrayList<MediaInfo> pMedias) {
        ArrayList<MediaInfo> _infos = new ArrayList<>();
        for (MediaInfo _info : pMedias) {
            long _size = Math.abs(_info.getSize());
            //大于500M
            if (_size > fileSize) {
                _infos.add(_info);
            }
        }
        return _infos;
    }

    /**
     * 更新标题控件
     */
    private void updateTitleView() {
        if (mUploadMedias.size() > 0) {
            //设置标题
            mContentView.setText(getString(R.string.material_manage_choose_title, mUploadMedias.size()));
            setRightView3TextColor(R.color.tv_color_fff_seletor);
        } else {
            //设置标题
            setCenterViewContent(R.string.choose_audio_material_title_content);
            setRightView3TextColor(R.color.color_80ffffff);
        }
    }

    private void showNoMaterialLayout(){
        lvChooseAudios.setVisibility(View.GONE);
        llChooseNoDataLayout.setVisibility(View.VISIBLE);
    }
    class MediaComparator implements Comparator<MediaInfo> {

        @Override
        public int compare(MediaInfo o1, MediaInfo o2) {
            long time1 = o1.getDate_added();
            long time2 = o2.getDate_added();
            if (time2 < time1) {
                return -1;
            } else if (time2 == time1) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
