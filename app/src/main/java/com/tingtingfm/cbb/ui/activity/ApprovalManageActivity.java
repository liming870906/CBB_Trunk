package com.tingtingfm.cbb.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ApprovalInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.ui.eventbus.AlreadyApprovalDataEvent;
import com.tingtingfm.cbb.ui.eventbus.MessageEvent;
import com.tingtingfm.cbb.ui.eventbus.WaitApprovalDataEvent;
import com.tingtingfm.cbb.ui.fragment.WaitApprovalFragment;
import com.tingtingfm.cbb.ui.fragment.AlreadyApprovalFragment;
import com.tingtingfm.cbb.ui.thread.DataTaskThread;
import com.tingtingfm.cbb.ui.thread.IDataLoadListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by tianhu on 2017/4/14.
 * Modify by liming on 2017/6/7
 * 已审/待审管理页面
 */

public class ApprovalManageActivity extends BaseActivity implements IDataLoadListener {
    public static final String FLAG_WAIT_FRAGMENT = "wait_approval_fragment";//待审
    public static final String FLAG_ALREADY_FRAGMENT = "already_approval_fragment"; //已审
    @BindView(R.id.activity_approval)
    Button btnApproval;
    @BindView(R.id.activity_approvaled)
    Button btnApprovaled;
    //待审标记
    private String mFlag = FLAG_ALREADY_FRAGMENT;
    //数据
    private ArrayList<ApprovalInfo> mWaitData, mAlreadyData;
    //声明返回标记
    private boolean isRestart = false;

    @Override
    protected View initContentView() {
        return LayoutInflater.from(this).inflate(R.layout.approval_activity, null);
    }

    @Override
    protected void handleCreate() {
        //注册EventBus
        EventBus.getDefault().register(this);
//        ActivityStack.getInstance().pushActivity(this);
        //设置标题
        setCenterViewContent(R.string.manuscript_approval_manage);
        //初始化方法
        init();
        //初始化碎片管理器方法
        initFragmentManage();
        //显示碎片
        showFragment(FLAG_WAIT_FRAGMENT);
        //加载数据
        new DataTaskThread().setLoadListener(this).crateNewMaterialRunnable().start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册
        EventBus.getDefault().unregister(this);
    }

    /**
     * 进程消息
     *
     * @param msg
     */
    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case Constants.LOAD_ALREADY_APPROVAL_TAG:
                //发送加载数据
                EventBus.getDefault().post(new AlreadyApprovalDataEvent(mAlreadyData, Constants.LOAD_ALREADY_APPROVAL_TAG));
                break;
            case Constants.LOAD_WAIT_APPROVAL_TAG:
                //发送加载数据
                EventBus.getDefault().post(new WaitApprovalDataEvent(mWaitData, Constants.LOAD_WAIT_APPROVAL_TAG));
                break;
        }
    }

    public void onEventMainThread(MessageEvent messageEvent) {
        switch (messageEvent.what) {
            case Constants.LOAD_WAIT_APPROVAL_TAG://加载待审数据
                loadWaitApprovalData();
                break;
            case Constants.LOAD_ALREADY_APPROVAL_TAG://加载已审数据
                loadAlreadyApprovalData();
                break;
        }
    }

    @OnClick({R.id.activity_approval, R.id.activity_approvaled})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.activity_approval://待审按钮
                showFragment(FLAG_WAIT_FRAGMENT);
                break;
            case R.id.activity_approvaled://已审按钮
                showFragment(FLAG_ALREADY_FRAGMENT);
                break;
        }
    }

    private void init() {
        mWaitData = new ArrayList<>();
        mAlreadyData = new ArrayList<>();
    }

    private void initFragmentManage() {
        try {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.approval_frameLayout, WaitApprovalFragment.class.newInstance(), FLAG_WAIT_FRAGMENT);
            transaction.add(R.id.approval_frameLayout, AlreadyApprovalFragment.class.newInstance(), FLAG_ALREADY_FRAGMENT);
            transaction.commitAllowingStateLoss();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 改变标记控件样式
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void changeFragmentTagView() {
        if (mFlag.equals(FLAG_WAIT_FRAGMENT)) {
            btnApproval.setBackground(getDrawable(R.drawable.approlval_left));
            btnApproval.setTextColor(getResources().getColor(R.color.white));
            btnApprovaled.setBackgroundColor(getResources().getColor(R.color.transparent));
            btnApprovaled.setTextColor(getResources().getColor(R.color.color_697fb4));
        } else if (mFlag.equals(FLAG_ALREADY_FRAGMENT)) {
            btnApprovaled.setBackground(getDrawable(R.drawable.approlval_right));
            btnApprovaled.setTextColor(getResources().getColor(R.color.white));
            btnApproval.setBackgroundColor(getResources().getColor(R.color.transparent));
            btnApproval.setTextColor(getResources().getColor(R.color.color_697fb4));
        }
    }

    /**
     * 显示界面fragment
     */
    public void showFragment(String flag) {
        if (flag.equals(FLAG_WAIT_FRAGMENT) && !mFlag.equals(FLAG_WAIT_FRAGMENT)) {
            mFlag = FLAG_WAIT_FRAGMENT;
            showFragment(WaitApprovalFragment.class, FLAG_WAIT_FRAGMENT);//待审-页面
        } else if (flag.equals(FLAG_ALREADY_FRAGMENT) && !mFlag.equals(FLAG_ALREADY_FRAGMENT)) {
            mFlag = FLAG_ALREADY_FRAGMENT;
            showFragment(AlreadyApprovalFragment.class, FLAG_ALREADY_FRAGMENT);//已审-页面
        }
        //改变按钮标记
        changeFragmentTagView();
    }

    @Override
    public void startLoad() {
        showLoadDialog();
    }

    @Override
    public void loadMaterialData() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadWaitApprovalData();
        loadAlreadyApprovalData();
    }

    @Override
    public void stopLoad() {
        dismissDlg();
    }

    private void loadWaitApprovalData() {
        //判断网络是否连接
        if (NetUtils.isNetConnected()) {
            if (mWaitData != null && mWaitData.size() > 0) {
                mWaitData.clear();
            }
            //网络连接并加载数据
            getTestData(mWaitData);
            //发送消息
            basicHandler.sendEmptyMessage(Constants.LOAD_WAIT_APPROVAL_TAG);
        } else {
            //判断是否返回标记
            if (!isRestart) {
                EventBus.getDefault().post(new WaitApprovalDataEvent(null, Constants.NOT_NET_WORK));
            } else {
                showToast(R.string.net_error_reload);
            }
        }
    }

    private void loadAlreadyApprovalData() {
        //判断网络是否连接
        if (NetUtils.isNetConnected()) {
            if (mAlreadyData != null && mAlreadyData.size() > 0) {
                mAlreadyData.clear();
            }
            //网络连接并加载数据
            getTestData(mAlreadyData);
            basicHandler.sendEmptyMessage(Constants.LOAD_ALREADY_APPROVAL_TAG);
        } else {
            //判断是否返回标记
            if (!isRestart) {
                EventBus.getDefault().post(new AlreadyApprovalDataEvent(null, Constants.NOT_NET_WORK));
            } else {
                showToast(R.string.net_error_reload);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //设置返回标记
        isRestart = true;
        //加载数据
        new DataTaskThread().setLoadListener(this).crateNewMaterialRunnable().start();
    }

    /**
     * 模拟数据
     */
    private void getTestData(ArrayList<ApprovalInfo> data) {
        ApprovalInfo approvalInfo = new ApprovalInfo();
        approvalInfo.setManuscriptId((int) (Math.random() * 20));
        approvalInfo.setManuscriptName("中看中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中中中中中中中中中中中中");
        approvalInfo.setProposer("测试人");
        approvalInfo.setState(0);
        approvalInfo.setTime("2017-04-17 12:10:12");
        data.add(approvalInfo);

        ApprovalInfo approvalInfo1 = new ApprovalInfo();
        approvalInfo1.setManuscriptId(1);
        approvalInfo1.setManuscriptName("有有爱你的中销欠情味中中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中销欠情味中中中中销欠情味中中中佣兵顶替顶替");
        approvalInfo1.setProposer("测试人");
        approvalInfo1.setState(1);
        approvalInfo1.setTime("2017-04-17 12:10:12");
        data.add(approvalInfo1);

        ApprovalInfo approvalInfo2 = new ApprovalInfo();
        approvalInfo2.setManuscriptId(2);
        approvalInfo2.setManuscriptName("苦阿斯蒂芬 中中妥人情味中销欠情中销欠情味中中中中销欠情味中中中味中中中中销欠情味中中中中销欠情味中中中销欠妥人情味爱你工需要需要需");
        approvalInfo2.setProposer("测试人");
        approvalInfo2.setState(2);
        approvalInfo2.setTime("2017-04-17 12:10:12");
        data.add(approvalInfo2);

        ApprovalInfo approvalInfo3 = new ApprovalInfo();
        approvalInfo3.setManuscriptId(3);
        approvalInfo3.setManuscriptName("苦苦苦苦中中加极乐中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中空间回归");
        approvalInfo3.setProposer("测试人");
        approvalInfo3.setState(3);
        approvalInfo3.setTime("2017-04-17 12:10:12");
        data.add(approvalInfo3);

        ApprovalInfo approvalInfo4 = new ApprovalInfo();
        approvalInfo4.setManuscriptId(4);
        approvalInfo4.setManuscriptName("苦苦苦苦中中加中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中极乐空间回归");
        approvalInfo4.setProposer("测试人");
        approvalInfo4.setState(4);
        approvalInfo4.setTime("2017-04-17 12:10:12");
        data.add(approvalInfo4);

        ApprovalInfo approvalInfo5 = new ApprovalInfo();
        approvalInfo5.setManuscriptId(5);
        approvalInfo5.setManuscriptName("苦苦苦苦中中加中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中中销欠情味中中中极乐空间回归");
        approvalInfo5.setProposer("测试人");
        approvalInfo5.setState(5);
        approvalInfo5.setTime("2017-04-17 12:10:12");
        data.add(approvalInfo5);
    }

    public ArrayList<ApprovalInfo> getmAlreadyData() {
        return mAlreadyData;
    }

    public ArrayList<ApprovalInfo> getmWaitData() {
        return mWaitData;
    }

    private void showFragment(Class<? extends Fragment> clazz, String tag) {
        hideAllFragments();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Fragment fragment = manager.findFragmentByTag(tag);
        try {
            if (fragment == null) {
                fragment = clazz.newInstance();
//                fragment.setArguments(NULL);
                transaction.add(R.id.approval_frameLayout, fragment, tag);
            }
            transaction.show(fragment);

            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAllFragments() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        List<Fragment> fragments = manager.getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f != null) {
                    transaction.hide(f);
                }
            }
        }
        transaction.commitAllowingStateLoss();
    }
}
