package com.tingtingfm.cbb.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.ui.adapter.ManuscriptAdapter;
import com.wasabeef.richeditor.AudiobackgrounUtil;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2017/01/09.
 */

public class ManuscriptActivity extends BaseActivity implements CallInterface {

    @BindView(R.id.maunscript_listView)
    PullToRefreshListView listView;
    @BindView(R.id.manuscript_add)
    LinearLayout addLayout;
    @BindView(R.id.manuscript_botton_layout)
    LinearLayout bottonLayout;
    @BindView(R.id.manuscript_all_textView)
    TextView allTextView;
    @BindView(R.id.manuscript_delete_textView)
    TextView deleteTextView;
    @BindView(R.id.manuscript_not_Layout)
    LinearLayout notManuscriptLayout;
    @BindView(R.id.manuscript_notNet)
    RelativeLayout notNetRlayout;
    @BindView(R.id.manuscript_topLine)
    View topLine;

    /**
     * 编辑变更
     */
    private boolean edit = false;
    /**
     * 适配哭
     */
    private ManuscriptAdapter adapter;
    /**
     * 稿件灵气库帮助类
     */
    private DBManuscriptManager dbManager;
    /**
     * 本地稿件数据集合
     */
    private ArrayList<ManuscriptInfo> manusInfos = new ArrayList<ManuscriptInfo>();
    /**
     * 云端稿件数据集合
     */
    private ArrayList<ManuscriptInfo> cloudMmanusInfos = new ArrayList<ManuscriptInfo>();
    /**
     * 选中稿件数据集合
     */
    private ArrayList<ManuscriptInfo> selectDatas = new ArrayList<ManuscriptInfo>();
    /**
     * 广播接收者，更新当前内容
     */
    private Receiver receiver;
    /**
     * 更新UI常量
     */
    private final int DELETE_UPDATA_UI = 0x9001;
    /**
     * 更新数据常量
     */
    private final int SHOW_DATA = 0x9002;

    /**
     * 设置布局
     * @return
     */
    @Override
    protected View initContentView() {
        return getContentView(R.layout.manuscript_activity);
    }


    /**
     * onCreate()方法，生命周期，创建界面初始数据
     */
    @Override
    protected void handleCreate() {
        setCenterViewContent(R.string.manuscript);
        setRightView3Content(R.string.message_edit);
        setRightView3Visibility(View.VISIBLE);
        adapter = new ManuscriptAdapter(this);
        adapter.setCallInterface(this);
        adapter.setSelectDatas(selectDatas);
        dbManager = DBManuscriptManager.getInstance(this);
        listView.setAdapter(adapter);
        setListViewItemClickListener();
        setDeleteText(selectDatas.size());
        //搜索监听
        registeReceiver();
        View footerView = LayoutInflater.from(this).inflate(R.layout.manuscript_botton,null);
        listView.getRefreshableView().addFooterView(footerView);
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
    }

    /**
     * 注册广播。更新当前数据
     */
    private void registeReceiver() {
        IntentFilter filter = new IntentFilter("com.caiji.manuscript");
        receiver = new Receiver();
        registerReceiver(receiver, filter);
    }

    /**
     * 获取搜索数据
     * @param title
     */
    private void getSearchData(String title) {
        if (!TextUtils.isEmpty(title)) {
            manusInfos = dbManager.findManuscriptByTitle(title);
            selectDatas.clear();
            adapter.setSelectDatas(selectDatas);
            adapter.setData(manusInfos,cloudMmanusInfos);
            adapter.notifyDataSetChanged();
            if (manusInfos.size() == 0) {
                if(notManuscriptLayout.getVisibility() ==View.VISIBLE){
                    notManuscriptLayout.setVisibility(View.GONE);
                }
            } else {
            }
        } else {
            getDataShow();
        }
    }

    private void setNoDataLayout() {
        if (manusInfos.size() == 0 && cloudMmanusInfos.size() == 0) {
            notManuscriptLayout.setVisibility(View.VISIBLE);
        } else {
            notManuscriptLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeEditState();
        getDataShow();
        setNotNet();
    }

    /**
     * 设置无网络时，提示
     */
    private void setNotNet() {
       if(!NetUtils.isNetConnected()){
           notNetRlayout.setVisibility(View.VISIBLE);
           topLine.setVisibility(View.GONE);
       }else{
           notNetRlayout.setVisibility(View.GONE);
           topLine.setVisibility(View.VISIBLE);
       }
    }

    /**
     * 恢复编辑状态
     */
    private void changeEditState() {
        if(edit){//搜索后，返回页面，还原
            edit = !edit;
            setRight3();
            adapter.setEditState(edit);
        }
    }

    /**
     * 获取数据，进行显示
     */
    private void getDataShow() {
        showLoadDialog(R.string.nowisloading);
        new Thread(){
            @Override
            public void run() {
                manusInfos = dbManager.getManuscriptInfos();
                getCloudData();
                deleteMoreData();
                selectDatas.clear();
                basicHandler.sendEmptyMessage(SHOW_DATA);
            }
        }.start();
    }

    /**
     * 删除冗余稿件音频数据
     */
    private void deleteMoreData() {
        //每天消除一次
        String moreDataFlaG =  PreferencesConfiguration.getSValues(Constants.MANUSCRIPT_MORE_DATA_FLAG);
        if(TextUtils.isEmpty(moreDataFlaG) || !TimeUtils.isTodayForDate(moreDataFlaG)){
            PreferencesConfiguration.setSValues(Constants.MANUSCRIPT_MORE_DATA_FLAG,
                    TimeUtils.getTimeForSpecialFormat(TimeUtils.TimeFormat.TimeFormat10));
            String audioFilePath= StorageUtils.getSDCardStorageDirectory(ManuscriptActivity.this).getPath() + Constants.AUDIO_CACHE_DIR;
            //删除冗余稿件音频数据
            deleteData(audioFilePath);
            //删除冗余稿件图片数据
            String audioImagePath = AudiobackgrounUtil.getMascriptImgPath(ManuscriptActivity.this);
            deleteData(audioImagePath);
        }
    }

    /**
     * 给定目录路径。冗余稿件数据
     * @param manuscriptFilePath
     */
    private void deleteData(String manuscriptFilePath) {
        File file = new File(manuscriptFilePath);
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f:files ) {
                boolean isHave = false;
                for (ManuscriptInfo m:manusInfos) {
                    String filepath = f.toString();
                    int pos = filepath.lastIndexOf(File.separator);
                    if(TimeUtils.getYMDHMS(m.getCreateTime()).equals(filepath.substring(pos+1))){
                        isHave = true;
                        break;
                    }
                }
                if(!isHave){
                    deleteFileData(f);
                }
            }
        }
    }

    /**
     * 设置listView item 单击事件
     */
    private void setListViewItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ManuscriptInfo info = manusInfos.get(position);
                openMaAddActivity(info.getId());
            }
        });
    }

    /**
     * 进入写稿界面
     * @param id
     */
    private void openMaAddActivity(int id) {
        Intent intent = new Intent(ManuscriptActivity.this, ManuscriptAddActivity.class);
        intent.putExtra("manuId", id);
        startActivity(intent);
    }

    /**
     * 编辑功能按钮
     */
    @Override
    protected void onRightView3Click() {
        if (manusInfos.size() > 0 || cloudMmanusInfos.size() >0) {
            edit = !edit;
            setRight3();
            adapter.setEditState(edit);
        }
    }

    /**
     * 设置右上角，显示编辑，完成
     */
    private void setRight3() {
        if (edit) {
            setRightView3Content(R.string.finish);
            //单击完成，清空之前选中数据，恢复状态
            selectDatas.clear();
            setAllSelectState();
            setDeleteText(selectDatas.size());
            setCenterViewContent(R.string.manuscript_select);
        } else {
            setRightView3Content(R.string.message_edit);
            setCenterViewContent(R.string.manuscript);
        }
        //页面底部全部，删除布局 显示，与隐藏
        if (edit) {
            addLayout.setVisibility(View.GONE);
            bottonLayout.setVisibility(View.VISIBLE);
        } else {
            addLayout.setVisibility(View.VISIBLE);
            bottonLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case SHOW_DATA:
                adapter.setSelectDatas(selectDatas);
                adapter.setData(manusInfos,cloudMmanusInfos);
                adapter.notifyDataSetChanged();
                setNoDataLayout();
                dismissDlg();
                break;
            case DELETE_UPDATA_UI:
                adapter.notifyDataSetChanged();
                edit = !edit;
                setRight3();
                adapter.setData(manusInfos,cloudMmanusInfos);
                adapter.setEditState(edit);
                setDeleteText(selectDatas.size());
                setNoDataLayout();
                dismissDlg();
                break;
        }
    }

    @OnClick({R.id.manuscript_add, R.id.manuscript_all_textView,
            R.id.manuscript_delete_textView,R.id.manuscript_search_rlayout})
    public void clickButton(View view) {
        switch (view.getId()) {
            case R.id.manuscript_all_textView://全选事件
                //进行单击全部逻辑处理。
                if (selectDatas.size() == manusInfos.size()) {//相等，全部删除。
                    selectDatas.clear();
                } else {
                    selectDatas.clear();//不相等，全部选中
                    selectDatas.addAll(manusInfos);
                }
                adapter.notifyDataSetChanged();
                setAllSelectState();
                setDeleteText(selectDatas.size());
                break;
            case R.id.manuscript_delete_textView://删除事件
                deleteData();
                break;
            case R.id.manuscript_add://进入写稿界面
                openMaAddActivity(0);
                break;
            case R.id.manuscript_search_rlayout://进入搜索页
                startActivity(new Intent(this,SearchActivity.class));
                break;
        }
    }

    /**
     * 删除选中数据信息
     */
    private void deleteData() {
        if (selectDatas.size() > 0) {
            showLoadDialog(R.string.manuscript_execute);
        }
        new Thread(){
            @Override
            public void run() {
                if (selectDatas.size() > 0) {
                    //删除本地数据
                    for (int i = 0; i < selectDatas.size(); i++) {
                        ManuscriptInfo info = selectDatas.get(i);
                        //删除稿件内容信息
                        dbManager.deleteManuscriptInfo(info.getId());
                        //删除稿件内音频信息。
                        dbManager.deleteManuAllAudioInfo(info.getId());
                        //删除稿件内本地音频文件
                        String audioSavePath = StorageUtils.getSDCardStorageDirectory(ManuscriptActivity.this).getPath() +
                                Constants.AUDIO_CACHE_DIR + File.separator + TimeUtils.getYMDHMS(info.getCreateTime());
                        File files = new File(audioSavePath);
                        if (files.exists()) {
                            deleteFileData(files);
                        }
                        //删除稿件图片资源
                        AudiobackgrounUtil.deleteAllImg(ManuscriptActivity.this, TimeUtils.getYMDHMS(info.getCreateTime()));
                    }
                    manusInfos.removeAll(selectDatas);
                    //删除云端数据
                    cloudMmanusInfos.removeAll(selectDatas);
                    selectDatas.clear();
                    basicHandler.sendEmptyMessage(DELETE_UPDATA_UI);
                }
            }
        }.start();
    }

    /**
     * 删除稿件内所有关连音频信息
     */
    private void deleteFileData(File files) {
        if (files.isDirectory()) {
            File[] file = files.listFiles();
            for (int i = 0; i < file.length; i++) {
                File fil = file[i];
                if (fil.exists()) {
                    fil.delete();
                }
            }
            files.delete();
        }
    }

    /**
     * 设置全部，底部图标
     */
    private void setAllSelectState() {
        Drawable leftDra;
        if (selectDatas.size() == manusInfos.size()) {
            leftDra = getResources().getDrawable(R.drawable.meg_selected);
        } else {
            leftDra = getResources().getDrawable(R.drawable.meg_select);
        }
        allTextView.setCompoundDrawablePadding(DensityUtils.dp2px(this, 16));
        leftDra.setBounds(0, 0, leftDra.getMinimumWidth(), leftDra.getMinimumHeight());
        allTextView.setCompoundDrawables(leftDra, null, null, null);
    }

    /**
     * 设置底部删除显示选中的文本
     * @param num
     */
    private void setDeleteText(int num) {
        deleteTextView.setText(getString(R.string.message_delete, String.valueOf(num)));
        if (num == 0) {
            deleteTextView.setBackgroundColor(getResources().getColor(R.color.color_dadbde));
        } else {
            deleteTextView.setBackgroundResource(R.drawable.bf26c72_d35e63_bg_selector);
        }
    }

    /**
     * 设置删除(0)数量。
     * @param flag
     * @param msgId
     */
    @Override
    public void clickCall(int flag, int msgId) {
        if (flag == CallInterface.SELECT_VAL) {
            setAllSelectState();
            setDeleteText(selectDatas.size());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != receiver)
            unregisterReceiver(receiver);
    }

    public ArrayList<ManuscriptInfo> getCloudData() {
        cloudMmanusInfos.clear();
        cloudMmanusInfos.add(getInfo(1,1001,"云端数据1","2017-04-05 12:14:41",2,0,"lkjlkjlkj",1));
        cloudMmanusInfos.add(getInfo(2,1002,"云端数据2","2017-04-05 12:14:41",2,1,"lkjlkjlkj",0));
        cloudMmanusInfos.add(getInfo(3,1003,"云端数据3","2017-04-05 12:14:41",2,2,"lkjlkjlkj",1));
        cloudMmanusInfos.add(getInfo(4,1004,"云端数据4","2017-04-05 12:14:41",2,3,"lkjlkjlkj",1));
        cloudMmanusInfos.add(getInfo(5,1005,"云端数据5","2017-04-05 12:14:41",2,4,"lkjlkjlkj",1));
        cloudMmanusInfos.add(getInfo(5,1005,"云端数据5","2017-04-05 12:14:41",2,5,"lkjlkjlkj",1));
        cloudMmanusInfos.add(getInfo(5,1005,"云端数据5","2017-04-05 12:14:41",2,6,"lkjlkjlkj",1));//待审
        return cloudMmanusInfos;
    }

    private ManuscriptInfo getInfo(int id,int netId,String title,String time,
                                   int uploadState,int approveState,String htmlText,int isSubmit){
        ManuscriptInfo info = new ManuscriptInfo();
        info.setId(id);
        info.setServerId(netId);
        info.setTitle(title);
        info.setCreateTime(time);
        info.setUploadState(uploadState);
        info.setApproveState(approveState);
        info.setHtmlText(htmlText);
        info.setIsSubmit(isSubmit);
        return info;
    }

    /**
     * 得到广播，进行更新列表
     */
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            manusInfos = dbManager.getManuscriptInfos();
            adapter.setData(manusInfos, cloudMmanusInfos);
            adapter.notifyDataSetChanged();
        }
    }
}
