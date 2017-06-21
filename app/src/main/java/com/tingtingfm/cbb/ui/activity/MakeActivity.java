package com.tingtingfm.cbb.ui.activity;

import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.PersonInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.encrypt.DesECBUtil;
import com.tingtingfm.cbb.response.MakeResponse;
import com.tingtingfm.cbb.response.PersonInfoResponse;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2016/12/26.
 */

public class MakeActivity extends BaseActivity {
    //帐号显示布局
    @BindView(R.id.make_account_llayout)
    LinearLayout accountLlayout;
    //输入帐号
    @BindView(R.id.make_account)
    EditText accountEditText;
    //输入密码
    @BindView(R.id.make_password)
    EditText passwordEditText;


    //帐号输入布局
    @BindView(R.id.make_ap_username_llayout)
    LinearLayout apUserNameLlayout;
    //帐号显示View
    @BindView(R.id.make_ap_username)
    TextView userName;
    //提交按钮
    @BindView(R.id.make_button)
    Button makeButton;

    @BindView(R.id.make_account_remove)
    ImageView accountRemoveImageView;
    @BindView(R.id.make_password_remove)
    ImageView passwordRemoveImageView;

    private final int SHOW_INTO = 0x3001;//有帐号，显示重置页面
    private final int HIDE_INTO = 0x3002;//无帐号，显示提交设置页面
    private final int MEG_SHOW = 0x3003;//帐号，密码，删除图标显示
    private final int MEG_GONG = 0x3004;//帐号，密码，删除图标隐藏
    private final int ACCOUNT_FLAG = 3; //帐号输入框标记
    private final int PASS_FLAG = 2;//密码输入框标记
    private String accountStr = "";//制作网帐号
    private int numFlag = 0; //0：帐号为空，1：帐号不为空
    private String name = "GONE";

    @Override
    protected View initContentView() {
        return getContentView(R.layout.make_activity);
    }

    @Override
    protected void handleCreate() {
        setCenterViewContent(R.string.setting_make);
        changeView(name);
        if (NetUtils.isNetConnected()) {
            getAccountInfo();
        } else {
            showToast(R.string.login_not_net);
        }
    }

    private void getAccountInfo() {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.GETUSERINFO);
        HttpRequestHelper.post(entity, new BaseRequestCallback<PersonInfoResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
                showLoadDialog();
            }

            @Override
            public void onSuccess(PersonInfoResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    PersonInfo personInfo = response.getData();
                    AccoutConfiguration.updateAccoutInfo(personInfo);
                    changeView(personInfo.getAp_username());
                } else {
                    showToast(R.string.login_accPass_err);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                dismissDlg();
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case MEG_GONG://帐号，密码，删除图标隐藏
                if (msg.arg1 == ACCOUNT_FLAG) {
                    accountRemoveImageView.setVisibility(View.GONE);
                }else if(msg.arg1 == PASS_FLAG){
                    passwordRemoveImageView.setVisibility(View.GONE);
                }

                break;
            case MEG_SHOW://帐号，密码，删除图标隐藏
                if (msg.arg1 == ACCOUNT_FLAG) {
                    accountRemoveImageView.setVisibility(View.VISIBLE);
                }else if(msg.arg1 == PASS_FLAG){
                    passwordRemoveImageView.setVisibility(View.VISIBLE);
                }
                break;
            case SHOW_INTO:
                if (!TextUtils.isEmpty(accountStr)) {
                    userName.setText(accountStr);
                    accountLlayout.setVisibility(View.GONE);
                    apUserNameLlayout.setVisibility(View.VISIBLE);
                    makeButton.setText(R.string.make_reset);
                    accountEditText.setText("");
                    passwordEditText.setText("");
                    numFlag = 1;
                }
                break;
            case HIDE_INTO:
                accountLlayout.setVisibility(View.VISIBLE);
                apUserNameLlayout.setVisibility(View.GONE);
                makeButton.setText(R.string.audio_record_save);
                numFlag = 0;
                break;
        }

    }

    private void changeView(String name) {
        if ("GONE".equals(name)) {
            apUserNameLlayout.setVisibility(View.GONE);
            accountLlayout.setVisibility(View.GONE);
            makeButton.setVisibility(View.GONE);
        } else {
            this.name = name;
            makeButton.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(this.name)) {//为空，显示输入框，显示保存按钮
                apUserNameLlayout.setVisibility(View.GONE);
                accountLlayout.setVisibility(View.VISIBLE);
                makeButton.setText(R.string.audio_record_save);
                numFlag = 0;
            } else {//不为空，显示重置
                userName.setText(this.name);
                accountLlayout.setVisibility(View.GONE);
                apUserNameLlayout.setVisibility(View.VISIBLE);
                makeButton.setText(R.string.make_reset);
                numFlag = 1;
            }
            //解决：设置账号，密码后保存，退出当前页。在次进入后，输入账号或密码后后面删除图标不显示。
            addTextChangeListener(accountEditText, ACCOUNT_FLAG);
            addTextChangeListener(passwordEditText, PASS_FLAG);
        }
    }

    @OnClick({R.id.make_account_remove,R.id.make_password_remove})
    public void clickRemove(View view){
        switch (view.getId()){
            case R.id.make_account_remove:
                accountEditText.setText("");
                break;
            case R.id.make_password_remove:
                passwordEditText.setText("");
                break;
        }
    }

    //保存制作网帐号接口
    @OnClick(R.id.make_button)
    public void saveMakeAccountInfo() {
        if (!NetUtils.isNetConnected()) {
            showToast(R.string.login_not_net);
            return;
        }
        RequestEntity entity = new RequestEntity(UrlManager.SET_AP_USER_INFO);
        if (numFlag == 0) {
            //保存按钮时，可以进行保存。重置时不执行
            accountStr = accountEditText.getText().toString();
            String passwordStr = passwordEditText.getText().toString();
            if (TextUtils.isEmpty(accountStr) || TextUtils.isEmpty(passwordStr)) {
                showToast(R.string.login_accPass_null);
                return;
            }
            //保存制作网帐号
            entity.addParams("ap_username", accountStr); //制作网帐号
            entity.addParams("ap_password", DesECBUtil.encryption(passwordStr));//制作网密码
        } else if (numFlag == 1) {
            //重置，都传值为空
            entity.addParams("ap_username", ""); //制作网帐号
            entity.addParams("ap_password", "");//制作网密码
            accountStr = "";
        }

        HttpRequestHelper.post(entity, new BaseRequestCallback<MakeResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(MakeResponse response) {
                if (response.getErrno() == 0 && null != response.getData()
                        && response.getData().getSucc() == 1) {
                    if (numFlag == 0) {
                        basicHandler.sendEmptyMessage(SHOW_INTO);
                    } else if (numFlag == 1) {
                        basicHandler.sendEmptyMessage(HIDE_INTO);
                    }
                    PreferencesConfiguration.setSValues(Constants.AP_USERNAME, accountStr);
                } else {
                    showToast(R.string.make_save_fail);
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
     * 帐号，密码输入文字监听。有内容时显示删除标记，无内容隐藏删除标记
     *
     * @param editText
     * @param flag
     */
    private void addTextChangeListener(EditText editText, final int flag) {
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Message meg = basicHandler.obtainMessage();
                if (s.toString().length() > 0) {
                    meg.what = MEG_SHOW;
                    meg.arg1 = flag;
                } else {
                    meg.what = MEG_GONG;
                    meg.arg1 = flag;
                }
                basicHandler.sendMessage(meg);
            }
        });
    }

}
