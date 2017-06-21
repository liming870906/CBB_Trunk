package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MessageInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.response.MessageDeleteResponse;
import com.tingtingfm.cbb.response.MessageDetailResponse;
import com.tingtingfm.cbb.ui.view.AdaptableTextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2017/1/3.
 * 信息界面
 */

public class MessageDetailActivity extends BaseActivity {

    @BindView(R.id.msg_detail_time)
    TextView timeTextView;
    @BindView(R.id.msg_detail_info)
    AdaptableTextView msgInfoTextView;
    @BindView(R.id.message_detail_ok)
    Button okButton;
    @BindView(R.id.message_detail_cancel)
    Button cancelButton;
    @BindView(R.id.message_detail_des)
    Button desButton;
    private MessageInfo msgObj; //信息对象
    private int okRefuse = 0;
    public static final int RESULT = 1;
    /**
     * 网络返回已读id集合
     */
    private ArrayList<String> ids = new ArrayList<String>();

    @Override
    protected View initContentView() {
        return getContentView(R.layout.message_detail_activity);
    }

    @Override
    protected void handleCreate() {
        Intent intent = getIntent();
        //获取对象
        msgObj = (MessageInfo) intent.getSerializableExtra("msgInfo");
        //设置title
        setCenterViewContent(msgObj.getMessage_title());
        //设置时间
        timeTextView.setText(msgObj.getMessage_time());
        //设置详细
        msgInfoTextView.setText(msgObj.getMessage_content().getContent_detail().getTips());

        //用户中心(2)，群主(owner)，action为in(加群)
        if (msgObj.getMessage_type() == 2 && msgObj.getMessage_content().getContent_type().equals("owner")
                && msgObj.getMessage_content().getContent_detail().getAction().equals("in")) {
            //当为加群时，0未操作 1同意 2拒绝
            int state = msgObj.getMessage_content().getContent_detail().getOperrate_status();
            if (state == 0) {
                //显示同意，拒绝
                showOkCancel();
            } else if (state == 1) {
                showAgreed(R.string.message_detail_agreed);
            } else if (state == 2) {
                showAgreed(R.string.message_detail_refuse);
            }
        }

        if (NetUtils.isNetConnected()) {
            readToNet(msgObj.getMessage_id());
        } else {
            showToast(R.string.login_not_net);
        }
    }

    /**
     * 显示 已同意,已拒绝
     * @param stringId 资源id
     */
    private void showAgreed(int stringId) {
        okButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        desButton.setVisibility(View.VISIBLE);
        desButton.setText(getString(stringId));
    }

    /**
     * 显示同意，拒绝
     */
    private void showOkCancel() {
        okButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        desButton.setVisibility(View.GONE);
    }

    @Override
    protected void processMessage(Message msg) {

    }

    /**
     * 返回事件
     */
    @Override
    protected void onLeftView1Click() {
        callBack();
        super.onLeftView1Click();
    }

    /**
     * 系统返回事件
     */
    @Override
    public void onBackPressed() {
        callBack();
        super.onBackPressed();
    }

    /**
     * 返回数据处理
     */
    private void callBack() {
        Intent intent = new Intent();
        intent.putExtra("objId",msgObj.getMessage_id());
        intent.putExtra("okRefuse", okRefuse);
        intent.putStringArrayListExtra("ids",ids);
        setResult(RESULT, intent);
    }

    @OnClick({R.id.message_detail_ok, R.id.message_detail_cancel})
    public void clickOkCancel(View view) {
        switch (view.getId()) {
            case R.id.message_detail_ok:
                if (NetUtils.isNetConnected()) {
                    okCancelToNet(1);
                } else {
                    showToast(R.string.login_not_net);
                }
                break;
            case R.id.message_detail_cancel:
                if (NetUtils.isNetConnected()) {
                    okCancelToNet(0);
                } else {
                    showToast(R.string.login_not_net);
                }
                break;
        }
    }

    /**
     * 群组用户加入消息同意、拒绝
     * @param val
     */
    private void okCancelToNet(final int val) {
        if (null != msgObj) {
            RequestEntity entity = new RequestEntity(UrlManager.MESSAGE_GROUP_ACT);

            entity.addParams("admin_id", PreferencesConfiguration.getSValues(Constants.USER_ID));
            entity.addParams("message_id", msgObj.getMessage_id() + ""); //消息id
            entity.addParams("gid", msgObj.getMessage_content().getContent_detail().getGroup_id() + ""); //群组id
            entity.addParams("fid", msgObj.getMessage_content().getContent_detail().getMember_id() + ""); //申请人id
            entity.addParams("status", val + ""); //同意或拒绝 1同意，0拒绝
            HttpRequestHelper.post(entity, new BaseRequestCallback<MessageDetailResponse>() {
                @Override
                public void onStart() {
                    TTLog.i("lqsir ---onStart");
                }

                @Override
                public void onSuccess(MessageDetailResponse response) {
                    TTLog.i("lqsir --- " + response.getData().toString());
                    if (response.getErrno() == 0 && null != response.getData()) {
                        int flag = response.getData().getSucc();
                        if (flag == 1) {
                            if (val == 1) {
                                showAgreed(R.string.message_detail_agreed);
                                showToast(R.string.message_detail_agreed);
                                okRefuse = 1;
                            } else if (val == 0) {
                                showAgreed(R.string.message_detail_refuse);
                                showToast(R.string.message_detail_refuse);
                                okRefuse = 2;
                            }
                        } else if (flag == 2) {//已同意
                            showAgreed(R.string.message_detail_agreed);
                            showToast(R.string.message_detail_agreed);
                            okRefuse = 1;
                        } else if (flag == 4) {//已拒绝
                            showAgreed(R.string.message_detail_refuse);
                            showToast(R.string.message_detail_refuse);
                            okRefuse = 2;
                        } else if (flag == 3) {//抱歉，此请求已失效，无法进行操作
                            showToast(getString(R.string.message_detail_err));
                            if (val == 1) {
                                okRefuse = 2;
                                showAgreed(R.string.message_detail_refuse);
                            } else if (val == 0) {
                                showAgreed(R.string.message_detail_agreed);
                                okRefuse = 1;
                            }
                        } else if (flag == -1) {//操作失败
                            showToast(R.string.message_detail_group_no);
                        } else if (flag == 0) {
                            showToast(R.string.message_detail_fail);
                        }
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
    }

    /**
     * 把消息变成已读
     * @param msgId
     */
    private void readToNet(int msgId) {
        RequestEntity entity = new RequestEntity(UrlManager.MESSAGE_READ);
        entity.addParams("admin_id", PreferencesConfiguration.getSValues(Constants.USER_ID));
        entity.addParams("message_id", msgId + "");
        HttpRequestHelper.post(entity, new BaseRequestCallback<MessageDeleteResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(MessageDeleteResponse response) {
                TTLog.i("lqsir --- " + response.getData().toString());
                if (response.getErrno() == 0 && null != response.getData()) {
                    ids = response.getData().getSucc_id();
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
}
