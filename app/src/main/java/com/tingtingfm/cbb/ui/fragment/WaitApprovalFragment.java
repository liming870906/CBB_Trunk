package com.tingtingfm.cbb.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ApprovalInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.NotNetUtils;
import com.tingtingfm.cbb.ui.activity.ApprovalManageActivity;
import com.tingtingfm.cbb.ui.activity.TestApprovalActivity;
import com.tingtingfm.cbb.ui.adapter.ApprovalAdapter;
import com.tingtingfm.cbb.ui.eventbus.MessageEvent;
import com.tingtingfm.cbb.ui.eventbus.WaitApprovalDataEvent;

import java.util.ArrayList;

import butterknife.BindView;
import de.greenrobot.event.EventBus;

/**
 * Created by tianhu on 2017/4/14.
 * Update by liming on 2017/6/9.
 * 待审碎片页面
 */

public class WaitApprovalFragment extends BaseFragment {

    @BindView(R.id.approval_listView)
    PullToRefreshListView listView;
    @BindView(R.id.approval_root)
    FrameLayout flView;
    @BindView(R.id.approval_no_data_layout)
    LinearLayout llNoDataLayout;
    @BindView(R.id.approval_no_data_des)
    TextView tvNoDataView;
    private ApprovalAdapter adapter;
    private View netView;
    private ArrayList<ApprovalInfo> datas;
    private boolean isPullDown = false;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approval, container, false);
    }

    @Override
    protected void handleCreate() {
        datas = ((ApprovalManageActivity) mActivity).getmWaitData();
        adapter = new ApprovalAdapter(mActivity, R.layout.approval_item);
        adapter.setApprovalFlag(0);
        adapter.setData(datas);
        listView.setAdapter(adapter);
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        setListViewRefreshListener();
        setOnItemClickListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销EventBus
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(WaitApprovalDataEvent dataEvent) {

        if(isPullDown){
            isPullDown = false;
            refreshComplete();
        }
        switch (dataEvent.getmMessageTag()) {
            case Constants.NOT_NET_WORK: //无网络
                adddNoNetView();
                break;
            case Constants.LOAD_WAIT_APPROVAL_TAG://加载待审标记数据
                if (dataEvent.isHaveData()) {
                    if (listView.getVisibility() == View.GONE) {
                        listView.setVisibility(View.VISIBLE);
                        llNoDataLayout.setVisibility(View.GONE);
                    }
                    datas = dataEvent.getData();
                    //加载数据
                    if (null != netView) {
                        netView.setVisibility(View.GONE);
                        NotNetUtils.stopAnim();
                    }
                    adapter.setData(datas);
                    adapter.notifyDataSetChanged();
                } else {
                    //显示没有数据页面
                    if (netView != null) netView.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    llNoDataLayout.setVisibility(View.VISIBLE);
                    tvNoDataView.setText(R.string.manuscript_approval_no_wait_data);
                }
                break;
            case Constants.LOAD_WAIT_APPROVAL_NO_DATA_TAG://没有待审数据标记
                //显示没有数据页面
                if (netView != null) netView.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                llNoDataLayout.setVisibility(View.VISIBLE);
                tvNoDataView.setText(R.string.manuscript_approval_no_wait_data);
                break;
        }
    }

    /**
     * listView 下拉刷新
     */
    private void setListViewRefreshListener() {
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if(NetUtils.isNetConnected()){
                    isPullDown = true;
                    //发送加载数据方法
                    EventBus.getDefault().post(new MessageEvent().Obtion(Constants.LOAD_WAIT_APPROVAL_TAG));
                }else{
                    showToast(R.string.net_error_reload);
                    refreshComplete();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            }
        });
    }

    private void refreshComplete(){
        fragmentHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.onRefreshComplete();
            }
        }, 1000);
    }

    /**
     * listView 单击条目监听
     */
    private void setOnItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0)position -= 1;
                ApprovalInfo _info = adapter.getItem(position);
                if(_info != null){
                    int _Id = _info.getManuscriptId();
                    Intent _intent = new Intent(mActivity, TestApprovalActivity.class);
                    _intent.putExtra(Constants.KEY_MANUSCRIPT_ID,_Id);
                    startActivity(_intent);
                }
            }
        });
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case NotNetUtils.NOT_NET_RELOAD:
                NotNetUtils.startAnim(mActivity);
                //发送加载数据方法
                EventBus.getDefault().post(new MessageEvent().Obtion(Constants.LOAD_WAIT_APPROVAL_TAG));
                break;
        }
    }

    //添加网络异常页
    private void adddNoNetView() {
        if (null == netView) {
            netView = NotNetUtils.getNotNetView(mActivity, fragmentHandler);
            flView.addView(netView);
        } else {
            netView.setVisibility(View.VISIBLE);
            fragmentHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotNetUtils.stopAnim();
                }
            }, 1000);
        }
    }
}
