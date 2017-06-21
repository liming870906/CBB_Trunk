package com.tingtingfm.cbb.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
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
import com.tingtingfm.cbb.response.LoginResponse;
import com.tingtingfm.cbb.ui.activity.CbbActivity;
import com.tingtingfm.cbb.ui.activity.FindPasswordActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2017/3/21.
 */

public class LoginFragment extends BaseFragment {

    @BindView(R.id.login_account)
    EditText accountEText;
    @BindView(R.id.login_password)
    EditText passwordEText;
    @BindView(R.id.login_account_remove)
    ImageView accRemoveImageV;
    @BindView(R.id.login_password_remove)
    ImageView passRemoveImageV;
    @BindView(R.id.login_enter)
    Button enterButton;
    @BindView(R.id.login_find_password)
    TextView findpassTextView;

    private final int MEG_SHOW = 0x1001;//显示
    private final int MEG_GONG = 0x1002;//隐藏

    private final int ACCOUNT_FLAG = 1;//帐号标记
    private final int PASS_FLAG = 2;//密码标记

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cbb_login, null);
    }

    @Override
    protected void handleCreate() {
        addTextChangeListener(accountEText, ACCOUNT_FLAG);
        addTextChangeListener(passwordEText, PASS_FLAG);
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case MEG_SHOW://帐号，密码删除按钮-显示
                if (msg.arg1 == ACCOUNT_FLAG) {
                    accRemoveImageV.setVisibility(View.VISIBLE);
                } else if (msg.arg1 == PASS_FLAG) {
                    passRemoveImageV.setVisibility(View.VISIBLE);
                }
                break;
            case MEG_GONG://帐号，密码删除按钮-隐藏
                if (msg.arg1 == ACCOUNT_FLAG) {
                    accRemoveImageV.setVisibility(View.GONE);
                } else if (msg.arg1 == PASS_FLAG) {
                    passRemoveImageV.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * 事件处理
     */
    @OnClick({R.id.login_enter, R.id.login_account_remove, R.id.login_password_remove, R.id.login_find_password})
    public void enterEvent(View view) {
        switch (view.getId()) {
            case R.id.login_find_password://找回密码
                Intent intent = new Intent(mActivity, FindPasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.login_account_remove://删除帐号内容，重新输入
                accountEText.setText("");
                break;
            case R.id.login_password_remove://删除密码内容，重新输入
                passwordEText.setText("");
                break;
            case R.id.login_enter://提交数据，进行登录
                if (!NetUtils.isNetConnected()) {
                    showToast(R.string.login_not_net);
                    return;
                }

                //帐号，密码为空，进行提示
                String accountStr = accountEText.getText().toString();
                String passwordStr = passwordEText.getText().toString();

                if (TextUtils.isEmpty(accountStr) || TextUtils.isEmpty(passwordStr)) {
                    showToast(R.string.login_accPass_null);
                    return;
                }

                if (passwordStr.length() < 6) {
                    showToast(R.string.login_accPass_err1);
                    return;
                }

                //请求接口进行登录
                RequestEntity entity = new RequestEntity(UrlManager.LOGIN_API);
                entity.addParams("name", accountStr);
                entity.addParams("password", DesECBUtil.encryptDES(passwordStr));
                HttpRequestHelper.post(entity, new BaseRequestCallback<LoginResponse>() {
                    @Override
                    public void onStart() {
                        TTLog.i("lqsir ---onStart");
                    }

                    @Override
                    public void onSuccess(LoginResponse response) {
                        TTLog.i("lqsir --- " + response.getData().toString());
                        if (response.getErrno() == 0 && null != response.getData() && !TextUtils.isEmpty(response.getData().getSession_key())) {
                            if (response.getData().getIs_disabled() == 0) {
                                //保存应用第一次请求接口时间。
                                PreferencesConfiguration.setSValues(Constants.FIRST_START_TIME, response.getServer_time() + "");
                                //保存登录信息
                                AccoutConfiguration.setLoginInfo(response.getData());
                                //进入首页界面
                                enterMainActivity();
                            } else {
                                showToast(R.string.login_disabled);
                            }
                        } else {
                            showToast(R.string.login_accPass_err);
                        }
                    }

                    @Override
                    public void onFail(int code, String errorMessage) {
                        if (code == 108006 || code == 108005) {
                            showToast(R.string.login_accPass_err);
                        } else if (code == -1) {//登录失败，与设备绑定提醒
                            showOneButtonDialog(getString(R.string.login_fail), errorMessage, null);
                        } else {
                            showToast(errorMessage);
                        }
                    }

                    @Override
                    public void onCancel() {
                        TTLog.i("lqsir --- onCancel");
                    }
                });
                break;
        }
    }

    //登录成功，进入首页
    private void enterMainActivity() {
        CbbActivity activity = (CbbActivity) mActivity;
        activity.showFragment(CbbActivity.FLAG_MAINFRAGMENT);
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
                Message meg = fragmentHandler.obtainMessage();
                if (s.toString().length() > 0) {
                    meg.what = MEG_SHOW;
                    meg.arg1 = flag;
                } else {
                    meg.what = MEG_GONG;
                    meg.arg1 = flag;
                }
                fragmentHandler.sendMessage(meg);
            }
        });
    }
}
