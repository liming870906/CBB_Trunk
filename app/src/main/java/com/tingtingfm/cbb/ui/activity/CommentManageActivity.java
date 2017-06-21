package com.tingtingfm.cbb.ui.activity;

import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ApprovalWatchInfo;
import com.tingtingfm.cbb.bean.WatchInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.NotNetUtils;
import com.tingtingfm.cbb.ui.adapter.ApprovalWatchAdapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 审批页面（文本批注，语音批注）
 * @author liming
 */
public class CommentManageActivity extends BaseActivity {

    ApprovalWatchAdapter adapter;
    @BindView(R.id.approval_listView)
    PullToRefreshListView listView;
    @BindView(R.id.approval_no_data_layout)
    LinearLayout noDataLayout;
    @BindView(R.id.approval_no_data_img)
    ImageView noDataImg;
    @BindView(R.id.approval_no_data_des)
    TextView noDataDes;
    @BindView(R.id.approval_root)
    FrameLayout rootView;
    private int mManuscriptID;


    private List<ApprovalWatchInfo> approvalWatchInfos = new ArrayList<ApprovalWatchInfo>();
    private View netView;

    @Override
    protected View initContentView() {
        return getContentView(R.layout.fragment_approval);
    }

    @Override
    protected void handleCreate() {
        setCenterViewContent(R.string.manuscript_look);
        adapter = new ApprovalWatchAdapter(this, R.layout.approval_watch,listView);
        listView.setAdapter(adapter);
        mManuscriptID = getIntent().getIntExtra(Constants.KEY_MANUSCRIPT_ID,-1);
        getDatas();
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what){
            case NotNetUtils.NOT_NET_RELOAD:
                NotNetUtils.startAnim(this);
                //获取信息列表
                getDatas();
                break;
        }
    }

    private void getDatas() {
        if(NetUtils.isNetConnected()){
            if (null != netView) {
                netView.setVisibility(View.GONE);
                NotNetUtils.stopAnim();
            }
            ApprovalWatchInfo approvalWatchInfo = new ApprovalWatchInfo();
            approvalWatchInfo.setError("");
            WatchInfo watchInfo = new WatchInfo();
            watchInfo.setApprovalLayer(3);
            watchInfo.setApprovalName("张帅");
            watchInfo.setApprovalOperation(1);
            watchInfo.setApprovalText("写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好写的很好好");
            watchInfo.setOperationTime("2017-4-20 12:33:30");
            watchInfo.setUrl("");
            watchInfo.setAudioUrl("http://7o51i4.com1.z0.glb.clouddn.com/%E6%9D%8E%E7%8E%89%E5%88%9A-%E5%88%9A%E5%A5%BD%E9%81%87%E8%A7%81%E4%BD%A0.mp3");
            watchInfo.setAudioTime(16);
            approvalWatchInfo.setWatchInfo(watchInfo);
            approvalWatchInfos.add(approvalWatchInfo);

            ApprovalWatchInfo approvalWatchInfo1 = new ApprovalWatchInfo();
            approvalWatchInfo1.setError("");
            WatchInfo watchInfo1 = new WatchInfo();
            watchInfo1.setApprovalLayer(2);
            watchInfo1.setApprovalName("张帅");
            watchInfo1.setApprovalOperation(0);
            watchInfo1.setApprovalText("");
            watchInfo1.setOperationTime("2017-4-21 12:33:30");
            watchInfo1.setUrl("http://7o51i4.com1.z0.glb.clouddn.com/a.jpg");
            watchInfo1.setAudioUrl("http://7o51i4.com1.z0.glb.clouddn.com/%E8%B5%B5%E9%9B%B7-%E6%88%90%E9%83%BD.mp3");
            watchInfo1.setAudioTime(11);
            approvalWatchInfo1.setWatchInfo(watchInfo1);
            approvalWatchInfos.add(approvalWatchInfo1);
            adapter.setData(approvalWatchInfos);
            adapter.notifyDataSetChanged();

            ApprovalWatchInfo approvalWatchInfo2 = new ApprovalWatchInfo();
            approvalWatchInfo2.setError("");
            WatchInfo watchInfo2 = new WatchInfo();
            watchInfo2.setApprovalLayer(1);
            watchInfo2.setApprovalName("张帅");
            watchInfo2.setApprovalOperation(1);
            watchInfo2.setApprovalText("写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错写的的不错");
            watchInfo2.setOperationTime("2017-4-21 12:33:30");
            watchInfo2.setUrl("");
            watchInfo2.setAudioUrl("sdafasd");
            watchInfo2.setAudioTime(56);
            approvalWatchInfo2.setWatchInfo(watchInfo2);
            approvalWatchInfos.add(approvalWatchInfo2);
            adapter.setData(approvalWatchInfos);
            adapter.notifyDataSetChanged();
        }else{
            adddNoNetView();
        }
    }

    //添加网络异常页
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

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        onLeftView1Click();
    }

    @Override
    protected void onLeftView1Click() {
        super.onLeftView1Click();
        //关闭音频
        adapter.stopPlayer();
    }
}
