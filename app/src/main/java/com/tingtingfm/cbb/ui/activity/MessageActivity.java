package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MessageInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.NotNetUtils;
import com.tingtingfm.cbb.common.utils.ScreenUtils;
import com.tingtingfm.cbb.response.MessageDeleteResponse;
import com.tingtingfm.cbb.response.MessageListResponse;
import com.tingtingfm.cbb.ui.adapter.MessageAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2017/1/3.
 * 信息界面
 */

public class MessageActivity extends BaseActivity implements CallInterface {

    @BindView(R.id.message_act_fLayout)
    FrameLayout rootView;
    @BindView(R.id.message_listview)
    PullToRefreshListView rListview;
    @BindView(R.id.message_botton_layout)
    LinearLayout bottonLayout;

    @BindView(R.id.message_all_textView)
    TextView allTextView;
    @BindView(R.id.message_delete_textView)
    TextView deleteTextView;
    @BindView(R.id.message_detail_data_null)
    LinearLayout dataNullLayout;

    /**
     * 显示数据常量
     */
    private final int SHOW_DATA = 0x0221;

    /**
     * 适配器
     */
    private MessageAdapter adapter;

    /**
     * 消息集合
     */
    private ArrayList<MessageInfo> datas = new ArrayList<MessageInfo>();

    /**
     * 编辑状态
     */
    private boolean edit = false;
    /**
     * 默认为0，返回20条数据。非0，返回指定数的条数信息数据。
     */
    private int megSize = 0;
    /**
     * 默认每页数据20条。
     */
    private int pageNum = 20;
    /**
     * 是否有下一页数据
     */
    private boolean isHaveNextPage = false;
    //存放选择数据id
    private ArrayList<MessageInfo> selectData = new ArrayList<MessageInfo>();
    private View view;
    private View netView;

    /**
     * 上拉，下拉状态变量
     */
    private boolean isUp = false;
    /**
     * 屏幕高度
     */
    private int screentHeight;
    /**
     * 行高度
     */
    private int itemHeight;
    /**
     * 屏幕
     */
    private int lines;

    //getMessageDatas()获取信息列表。deleteData()删除选中的数据。readToNet() 设置为已读。

    @Override
    protected View initContentView() {
        return getContentView(R.layout.message_activity);
    }

    @Override
    protected void handleCreate() {
        adapter = new MessageAdapter(this, R.layout.message_info);
        adapter.setSelectData(selectData);
        adapter.setEditState(edit);
        adapter.setInterface(this);
        rListview.setAdapter(adapter);
        setRight3();
        setRightView3Visibility(View.VISIBLE);
        setDeleteText(selectData.size());
        setListViewRefresh();

        //行高
        itemHeight = getResources().getDimensionPixelSize(R.dimen.dp_82_7);
        //屏高
        screentHeight = ScreenUtils.getScreenHeight();
        //屏显示行数
        lines = (screentHeight / itemHeight);
        //获取信息列表
        getData();
    }

    /**
     * 获取信息列表
     */
    private void getData() {
        if (NetUtils.isNetConnected()) {
            if (null != netView) {
                NotNetUtils.stopAnim();
                netView.setVisibility(View.GONE);
            }
            datas.clear();
            getMessageDatas(0);
        } else {
            if (datas.size() == 0) {
                adddNoNetView();
            } else {
                showToast(R.string.login_not_nets);
            }
        }
    }

    /**
     * 添加网络异常页
     */
    private void adddNoNetView() {
        if (null == netView) {
            netView = NotNetUtils.getNotNetView(this, basicHandler);
            rootView.addView(netView);
        } else {
            netView.setVisibility(View.VISIBLE);
            basicHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotNetUtils.stopAnim();
                }
            }, 1000);
        }
    }

    /**
     * 监听上拉，下拉事件
     */
    private void setListViewRefresh() {
        rListview.setMode(PullToRefreshBase.Mode.BOTH);
        rListview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (NetUtils.isNetConnected()) {
                    isUp = false;
                    getNewData();
                } else {
                    if (null == netView)
                        showToast(R.string.login_not_nets);
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                isUp = true;
                if (NetUtils.isNetConnected()) {
                    if (isHaveNextPage) {
                        getMessageDatas(datas.get(datas.size() - 1).getMessage_id());
                    } else {
                        rListview.onRefreshComplete();
                    }
                } else {
                    if (null == netView)
                        showToast(R.string.login_not_nets);
                }
            }
        });
    }

    /**
     * 获取最新数据
     */
    private void getNewData() {
        rListview.setMode(PullToRefreshBase.Mode.BOTH);
        addFoot(false);
        getMessageDatas(0);
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case NotNetUtils.NOT_NET_RELOAD:
                NotNetUtils.startAnim(this);
                //获取信息列表
                getData();
                break;
            case SHOW_DATA:
                if (datas.size() > 0) {

                    adapter.setData(datas);
                    adapter.notifyDataSetChanged();
                    dataNullLayout.setVisibility(View.GONE);
                } else {
                    dataNullLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * 编辑功能
     */
    @Override
    protected void onRightView3Click() {
        if (datas.size() > 0) {
            edit = !edit;
            setRight3();
            adapter.setEditState(edit);
        }
    }

    @OnClick({R.id.message_all_textView, R.id.message_delete_textView})
    public void clickTextView(View view) {
        switch (view.getId()) {
            case R.id.message_all_textView:
                //进行单击全部逻辑处理。
                if (selectData.size() == datas.size()) {//相等，全部删除。
                    selectData.clear();
                } else {
                    selectData.clear();//不相等，全部选中
                    selectData.addAll(datas);
                }
                adapter.notifyDataSetChanged();
                setAllSelectState();
                setDeleteText(selectData.size());
                break;
            case R.id.message_delete_textView:
                if (selectData.size() > 0) {
                    if (NetUtils.isNetConnected()) {
                        deleteData();
                    } else {
                        showToast(R.string.login_not_nets);
                    }
                }
                break;
        }
    }

    /**
     * 设置编辑时，底部图标
     */
    private void setAllSelectState() {
        Drawable leftDra;
        if (selectData.size() == datas.size()) {
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
     * 设置右上角，显示编辑，完成
     */
    private void setRight3() {
        if (edit) {
            setCenterViewContent(R.string.main_edit_message);
            setRightView3Content(R.string.finish);
            //单击完成，清空之前选中数据，恢复状态
            selectData.clear();
            setAllSelectState();
            setDeleteText(selectData.size());
            setLeftView1Visibility(View.GONE);
            rListview.setMode(PullToRefreshBase.Mode.DISABLED);
        } else {
            setCenterViewContent(R.string.main_message);
            setLeftView1Visibility(View.VISIBLE);
            setRightView3Content(R.string.message_edit);
            if(null != view && view.getVisibility() == View.VISIBLE){
                rListview.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            }else {
                rListview.setMode(PullToRefreshBase.Mode.BOTH);
            }
        }

        //页面底部全部，删除布局 显示，与隐藏
        if (edit) {
            bottonLayout.setVisibility(View.VISIBLE);
            rListview.getRefreshableView().setPadding(0, 0, 0, DensityUtils.dp2px(this, 47));
        } else {
            bottonLayout.setVisibility(View.GONE);
            rListview.getRefreshableView().setPadding(0, 0, 0, 0);
        }
    }

    /**
     * listView 条目选中回调
     * @param flag
     * @param msgId
     */
    @Override
    public void clickCall(int flag, int msgId) {
        if (flag == CallInterface.SELECT_VAL) {
            setAllSelectState();
            setDeleteText(selectData.size());
        }else if(flag == CallInterface.UNKONWN_VAL){
             showOneButtonDialog("",getString(R.string.message_unknown_dialog_info),null);
        }
    }

    /**
     * 删除选择的数据接口
     */
    private void deleteData() {
        if (selectData.size() == 0)
            return;
        //将删除数据变以逗点分隔的字符串。
        StringBuffer sbStr = new StringBuffer();
        for (int i = 0; i < selectData.size(); i++) {
            int id = selectData.get(i).getMessage_id();
            sbStr.append(id);
            if (i != selectData.size() - 1) {
                sbStr.append(",");
            }
        }
        RequestEntity entity = new RequestEntity(UrlManager.MESSAGE_DEL);
        entity.addParams("admin_id", PreferencesConfiguration.getSValues(Constants.USER_ID));
        entity.addParams("message_id", sbStr.toString());
        HttpRequestHelper.post(entity, new BaseRequestCallback<MessageDeleteResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(MessageDeleteResponse response) {
                TTLog.i("lqsir --- " + response.getData().toString());
                if (response.getErrno() == 0 && null != response.getData()) {
                    ArrayList<String> dis = response.getData().getSucc_id();
                    if (dis.size() == selectData.size()) {
                        datas.removeAll(selectData); //删除选择的数据
                        selectData.clear();
                        adapter.notifyDataSetChanged();
                        setAllSelectState();
                        setDeleteText(selectData.size());
                    } else {
                        try {
                            //将返回删除的数据，从内存中删除。
                            ArrayList<MessageInfo> listTemp = new ArrayList<MessageInfo>();
                            for (int i = 0; i < dis.size(); i++) {
                                int id = Integer.valueOf(dis.get(i));
                                for (int j = 0; j < selectData.size(); j++) {
                                    MessageInfo info = selectData.get(i);
                                    if (info.getMessage_id() == id) {
                                        listTemp.add(info);
                                        break;
                                    }
                                }
                            }
                            //将删除成功的数据，从本地缓存删除。
                            deleteLocData(listTemp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        showToast(R.string.message_serv_err);
                    }
                    if (datas.size() == 0) {
                        //数据为空时，显示“暂无数据哦”，去除底部文字 “没有了哦”
                        dataNullLayout.setVisibility(View.VISIBLE);
                        addFoot(false);
                    }
                    if (isHaveNextPage) {
                        if (datas.size() == 0) {
                            getNewData();
                        } else if (datas.size() <= (lines + 1)) {
                            isUp = true;
                            getMessageDatas(datas.get(datas.size() - 1).getMessage_id());
                        }
                    }
                    edit = !edit;
                    setRight3();
                    adapter.setEditState(edit);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    /**
     * 将删除成功的数据，从本地缓存删除。
     *
     * @param listTemp
     */
    private void deleteLocData(ArrayList<MessageInfo> listTemp) {
        datas.removeAll(listTemp);
        selectData.removeAll(listTemp);
        adapter.notifyDataSetChanged();
        setAllSelectState();
        setDeleteText(selectData.size());
    }

    /**
     * 获取消息信息  第一次与下拉刷新获取最新数据，值都为0。
     *
     * @param megID
     */
    private void getMessageDatas(int megID) {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.MESSAGE_LIST);
        entity.addParams("admin_id", PreferencesConfiguration.getSValues(Constants.USER_ID));
        entity.addParams("message_id", megID + "");
        entity.addParams("size", megSize + "");
        HttpRequestHelper.post(entity, new BaseRequestCallback<MessageListResponse>() {

            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(MessageListResponse response) {
                TTLog.i("lqsir --- " + response.getData().toString());
                if (response.getErrno() == 0 && null != response.getData()) {
                    rListview.onRefreshComplete();

                    if (isUp) {
                        datas.addAll(response.getData());
                    } else {
                        datas.clear();
                        datas.addAll(response.getData());
                    }

                    basicHandler.sendEmptyMessage(SHOW_DATA);

                    //判断是否有下一页
                    if (pageNum == response.getData().size()) {
                        isHaveNextPage = true;
                        //获取最后一条数据的id,用于下次请使用。
                        addFoot(false);
                    } else {
                        isHaveNextPage = false;
                        rListview.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        addFoot(true);
                    }

                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                if(datas.size()>0){
                    showToast(errorMessage);
                }else{
                    adddNoNetView();
                }
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    /**
     * 添加底部"没有了哦"布局
     * @param bool
     */
    private void addFoot(boolean bool) {
        if (null == view)
            view = LayoutInflater.from(this).inflate(R.layout.no_data, null);
        if (bool) {
            if (rListview.getRefreshableView().getFooterViewsCount() == 1) {
                rListview.getRefreshableView().addFooterView(view);
            }
        } else {
            if (rListview.getRefreshableView().getFooterViewsCount() == 2) {
                rListview.getRefreshableView().removeFooterView(view);
                view = null;
            }
        }
    }

    /**
     * 消息详情页返回，更新已读状态。
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            int objId = data.getIntExtra("objId", 0);
            int okRefuse = data.getIntExtra("okRefuse", 0);
            if (okRefuse != 0) {
                for (int i = 0; i < datas.size(); i++) {
                    MessageInfo mInfo = datas.get(i);
                    if (mInfo.getMessage_id() == objId) {
                        if (mInfo.getMessage_type() == 2 && mInfo.getMessage_content().getContent_type().equals("owner")
                                && mInfo.getMessage_content().getContent_detail().getAction().equals("in")) {
                            //当为加群时，0未操作 1同意 2拒绝
                            mInfo.getMessage_content().getContent_detail().setOperrate_status(okRefuse);
                        }
                        break;
                    }
                }
            }
            ArrayList<String> ids = data.getStringArrayListExtra("ids");
            if (ids.size() > 0) {
                //将已读数据改为已读。
                for (int i = 0; i < ids.size(); i++) {
                    int id = Integer.valueOf(ids.get(i));
                    for (int j = 0; j < datas.size(); j++) {
                        if (datas.get(j).getMessage_id() == id) {
                            datas.get(j).setIs_read(1);
                            break;
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
