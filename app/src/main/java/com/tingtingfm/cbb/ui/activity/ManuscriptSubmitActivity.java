package com.tingtingfm.cbb.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.annotation.Dimension;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.common.utils.ManuscriptInterfaceUtils;
import com.tingtingfm.cbb.common.utils.ToastUtils;
import com.tingtingfm.cbb.ui.serve.ManuscriptServiceHelper;
import com.tingtingfm.cbb.ui.view.CircleProcessView;

import butterknife.BindView;
import butterknife.OnClick;

public class ManuscriptSubmitActivity extends AbstractActivity {

    @BindView(R.id.cus_circle)
    CircleProcessView mCirclePercentView;
    @BindView(R.id.manuscript_progressNum)
    TextView progressNumTextView;
    @BindView(R.id.manuscript_submittingTextView)
    TextView submittingTextView;
    @BindView(R.id.manuscript_success)
    TextView successLayout;
    @BindView(R.id.submit_textView)
    TextView submitTextView;
    @BindView(R.id.manuscript_upload_fail_imageView)
    ImageView uploadFailImageView;
    @BindView(R.id.submit_rlayout)
    RelativeLayout submitRlayout;
    //取消提交
    @BindView(R.id.manuscript_cancel_layout)
    LinearLayout cancelLayout;
    @BindView(R.id.manuscript_submit_cancel)
    TextView cancelTextView;
    @BindView(R.id.manuscript_submit_reUpload)
    TextView reuploadTextView;

    /**
     * 稿件id
     */
    private int localId;
    /**
     * 进程id
     */
    private int processId;
    /**
     * 当前稿件对象
     */
    private ManuscriptInfo currentObj;
    /**
     * 稿件数据库管理
     */
    private DBManuscriptManager dbManager;
    /**
     * 提审成功状态
     */
    private boolean isSuccess = false;
    /**
     * 广播接收者
     */
    private Receiver receiver;
    /**
     * 稿件服务通信帮助者
     */
    private ManuscriptServiceHelper serviceHelper;
    /**
     * 提交状态
     */
    private boolean submitcancel = false;
    /**
     * 对话框显示状态
     */
    private boolean dialogShow = false;
    /**
     * 保存当前上传状态
     */
    private int uploadState = -1;

    /**
     * 设置页面布局
     * @return
     */
    @Override
    protected View initContentView() {
        return getContentView(R.layout.manuscript_submit_activity);
    }

    /**
     * 初始化数据
     */
    @Override
    protected void handleCreate() {
        registeReceiver();
        mCirclePercentView.setMax(100);
        localId = getIntent().getIntExtra("localId", 0);
        processId = getIntent().getIntExtra("processId", 0);
        dbManager = DBManuscriptManager.getInstance(this);
        currentObj = dbManager.findManuscriptInfo(localId);
        currentObj.setProcessId(processId);
        serviceHelper = ManuscriptServiceHelper.getInstance(basicHandler);
        serviceHelper.setPageFlag(1);
        serviceHelper.setManuscriptId(currentObj.getId());
        uploadState =  currentObj.getUploadState();
        currentObj.setUploadState(2);
        serviceHelper.startUpload(currentObj);
    }

    /**
     * 注册广播。更新当前数据
     */
    private void registeReceiver() {
        IntentFilter filter = new IntentFilter("com.caiji.manuscript.submit");
        receiver = new Receiver();
        registerReceiver(receiver, filter);
    }

    /**
     * 广播接收者
     */
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int manuscriptId = intent.getIntExtra("manuscriptId", 0);
            int submit = intent.getIntExtra("submit", 0);
            if (manuscriptId == currentObj.getId()) {
                if (submit == 0) {
                    //显示失败界面
                    showFailView();
                } else {
                    //提审确定接口
                    isSuccess = true;
                    setProgressValue(100);
                }
            }
        }
    }

    /**
     * 控件单击事件
     * @param view
     */
    @OnClick({R.id.submit_textView, R.id.manuscript_submit_cancel, R.id.manuscript_submit_reUpload})
    public void clickButton(View view) {
        switch (view.getId()) {
            case R.id.submit_textView://取消提交关闭当前页
                if (isSuccess) {
                    ActivityStack.getInstance().popSomeActivity(ManuscriptAddActivity.class);
                    ActivityStack.getInstance().popSomeActivity(ManuscriptProcessActivity.class);
                    finish();
                } else {
                    if(!dialogShow){
                        submitCancel();
                    }
                }
                break;
            case R.id.manuscript_submit_cancel://取消提交关闭当前页
                submitCancel();
                break;
            case R.id.manuscript_submit_reUpload://重新提交
                //重置进度值
                mCirclePercentView.setProgress(0);
                progressNumTextView.setText(String.valueOf(0));
                //显示进度，隐藏失败图票
                uploadFailImageView.setVisibility(View.GONE);
                cancelLayout.setVisibility(View.GONE);
                submitRlayout.setVisibility(View.VISIBLE);
                submittingTextView.setVisibility(View.VISIBLE);
                submittingTextView.setText(getString(R.string.manuscript_submiting));
                submittingTextView.setTextColor(getResources().getColor(R.color.color_697FB4));
                submittingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.text_size_48));
                submitTextView.setVisibility(View.VISIBLE);
                serviceHelper.setManuscriptId(currentObj.getId());
                currentObj.setUploadState(2);
                serviceHelper.startUpload(currentObj);
                break;
        }
    }

    /**
     * 取消当前提交
     */
    private void submitCancel() {
        submitcancel = true;
        TTLog.e("submitcancel:"+submitcancel);
        ActivityStack.getInstance().popSomeActivity(ManuscriptProcessActivity.class);
        finish();
    }

    /**
     * handler消息处理
     * @param msg
     */
    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case ManuscriptServiceHelper.SERVICE_HELPER_SUBMITED:
                if(currentObj.getId() == msg.arg1){
                    showOneButtonDialog(getString(R.string.manuscript_submited),"" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recoverValue();
                            //显示失败界面
                            showFailView();
                        }
                    });
                }
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_UPDATENETID:
                if(null != currentObj && currentObj.getId() == msg.arg2)
                   currentObj.setServerId(msg.arg1);
                break;
            case Constants.UPLOAD_FAIL:
                showFailView();
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_JG:
                if (msg.arg1 == 1) {
                    submitTextView.setVisibility(View.GONE);
                    TTLog.e("submitcancel update success ");
                    basicHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TTLog.e("submitcancel:"+submitcancel);
                            if(!submitcancel){
                                serviceHelper.submit(currentObj);
                            }
                        }
                    },100);
                } else if (msg.arg1 == 0) {
                    //显示失败界面
                    showFailView();
                }
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_TOTAL://上传次数总数
                setProgressValue(msg.arg1);
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_DIALOG:
                if(currentObj.getId() == msg.arg1){
                    if(!submitcancel){
                        dialogShow = true;
                        //进行提示  -- 取消，确定后进行上传
                        showTwoButtonDialog(getString(R.string.manuscript_reset), "" ,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
									dialogShow = false;
                                        recoverValue();
                                        serviceHelper.cancelSubmit(currentObj.getId(),-1);
                                        //显示失败界面
                                        showFailView();
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
									dialogShow = false;
                                        if (null != currentObj) {//判断当前上传，进行取消上传。未上传情况下进行上传。
                                            serviceHelper.startUpload();
                                        }
                                    }
                                });
                    }else if(null != serviceHelper){
                        serviceHelper.cancelSubmit(currentObj.getId(),-1);
                    }
                }
                break;
        }
    }

    /**
     * 恢复上传前的上传状态
     */
    private void recoverValue() {
        if(uploadState != -1){
            currentObj.setUploadState(uploadState);
            dbManager.updataManuscriptUploadState(currentObj);
        }
    }

    /**
     * 显示失败界面
     */
    private void showFailView() {
        submitRlayout.setVisibility(View.GONE);
        uploadFailImageView.setVisibility(View.VISIBLE);
        submitTextView.setVisibility(View.GONE);
        cancelLayout.setVisibility(View.VISIBLE);
        submittingTextView.setText(getString(R.string.manuscript_submit_fail));
        submittingTextView.setTextColor(getResources().getColor(R.color.color_e4696e));
        submittingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.text_size_60));
//        currentObj.setUploadState(0);
//        dbManager.updataManuscriptInfo(currentObj);
    }

    /**
     * 提交成功
     */
    private void submitSuccess() {
        //完成描述
        submittingTextView.setVisibility(View.GONE);
        successLayout.setVisibility(View.VISIBLE);

        uploadFailImageView.setVisibility(View.GONE);
        submitRlayout.setVisibility(View.VISIBLE);
        cancelLayout.setVisibility(View.GONE);

        //成功按钮
        submitTextView.setVisibility(View.VISIBLE);
        submitTextView.setText(getString(R.string.finish));
        submitTextView.setBackground(getResources().getDrawable(R.drawable.manuscript_submit_background));
        submitTextView.setTextColor(getResources().getColor(R.color.color_697FB4));
    }

    /**
     * 生命周期页面未显示
     */
    @Override
    protected void onPause() {
        super.onPause();
        serviceHelper.setManuscriptId(0);
    }

    /**
     * 销毁页面
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != receiver)
            unregisterReceiver(receiver);
    }

    /**
     * 设置进度值
     */
    private void setProgressValue(int value) {
        mCirclePercentView.setProgress(value);
        progressNumTextView.setText(String.valueOf(value));
        if (value == 100) {
            submitSuccess();
        }
    }

    /**
     * 返回事件，(此页面不处理返回事件)
     */
    @Override
    public void onBackPressed() {
//        if(submitTextView.getVisibility() == View.VISIBLE){
//            if (isSuccess) {
//                ActivityStack.getInstance().popSomeActivity(ManuscriptAddActivity.class);
//                ActivityStack.getInstance().popSomeActivity(ManuscriptProcessActivity.class);
//            }
//            super.onBackPressed();
//        }
    }
}
