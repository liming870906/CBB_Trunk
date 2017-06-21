package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ProcessInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.ManuscriptInterfaceUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.NotNetUtils;
import com.tingtingfm.cbb.response.ManuscriptProcessResponse;
import com.tingtingfm.cbb.ui.adapter.ManuscriptProcessAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class ManuscriptProcessActivity extends BaseActivity {

    @BindView(R.id.manuscript_process_layout)
    LinearLayout gridViewLayout;

    @BindView(R.id.manuscript_no_data_layout)
    LinearLayout noDataLayout;

    @BindView(R.id.submit_listView)
    ListView listView;

    @BindView(R.id.manuscript_rLayout)
    FrameLayout rootView;
    private ManuscriptProcessAdapter adapter;
    private String selectStr;
    private ArrayList<ProcessInfo> processInfos;

    private int localId;//本地稿件id
    private int processId;
    private View netView;

    @Override
    protected View initContentView() {
        return getContentView(R.layout.manuscript_process_activity);
    }

    @Override
    protected void handleCreate() {
        setCenterViewContent(R.string.manuscript_submit_process);
        adapter = new ManuscriptProcessAdapter(this, R.layout.manuscript_process_gridview_item);
        listView.setAdapter(adapter);
        localId = getIntent().getIntExtra("LocalId", 0);
        setOnItemlistener();
        netView = NotNetUtils.getNotNetView(this, basicHandler);
        rootView.addView(netView);
        getData();
    }
    
    //获取网络数据-进行显示
    private void getData() {
        noDataLayout.setVisibility(View.GONE);
        if (NetUtils.isNetConnected()) {
            if(null != netView){
                NotNetUtils.stopAnim();
                netView.setVisibility(View.GONE);
            }
            //获取事件
            ManuscriptInterfaceUtils.getprocessData(this, basicHandler);
        } else {
            gridViewLayout.setVisibility(View.GONE);
            addNoDataView();
        }
    }

    private void addNoDataView() {
        if(null != netView){
            basicHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotNetUtils.stopAnim();
                }
            }, 1000);
            netView.setVisibility(View.VISIBLE);
        }
    }

    private void setOnItemlistener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != processInfos) {
                    if (processId != processInfos.get(position).getProcess_id()) {
                        selectStr = processInfos.get(position).getProcess_name();
                        processId = processInfos.get(position).getProcess_id();
                        adapter.setSelectId(processInfos.get(position).getProcess_id());
                    } else {
                        selectStr = "";
                        adapter.setSelectId(-1);
                        processId = -1;
                    }
                }
            }
        });
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case Constants.UPLOAD_FAIL:
                noDataLayout.setVisibility(View.GONE);
                gridViewLayout.setVisibility(View.GONE);
                addNoDataView();
                break;
            case NotNetUtils.NOT_NET_RELOAD:
                NotNetUtils.startAnim(this);
                //获取信息列表
                getData();
                break;
            case ManuscriptInterfaceUtils.MANUSCRIPT_PROCESS:
                ManuscriptProcessResponse response = (ManuscriptProcessResponse) msg.obj;
                processInfos = response.getData();
                if (processInfos.size() > 0) {
                    noDataLayout.setVisibility(View.GONE);
                    gridViewLayout.setVisibility(View.VISIBLE);
                    adapter.setData(processInfos);
                    adapter.notifyDataSetChanged();
                } else {
                    gridViewLayout.setVisibility(View.GONE);
                    noDataLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @OnClick({R.id.submit_textView})
    public void clickButton(View view) {
        switch (view.getId()) {
            case R.id.submit_textView:
                if (NetUtils.isNetConnected()) {
                    if (!TextUtils.isEmpty(selectStr)) {
                        Intent intent = new Intent(this, ManuscriptSubmitActivity.class);
                        intent.putExtra("localId", localId);
                        intent.putExtra("processId", processId);
                        startActivity(intent);
                    } else {
                        showToast(R.string.manuscript_null);
                    }
                } else {
                    showToast(R.string.manuscript_net_no);
                }
                break;
        }
    }
}
