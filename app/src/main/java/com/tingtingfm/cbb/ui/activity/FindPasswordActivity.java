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
import com.tingtingfm.cbb.bean.PhoneEmailInfo;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.response.PasswordResponse;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2016/12/23.
 * 找回密码界面
 */

public class FindPasswordActivity extends BaseActivity {
    //输入帐号，找回界面布局
    @BindView(R.id.password_text_llayout)
    LinearLayout textLLayout;
    //找回手机，邮箱显示界面布局
    @BindView(R.id.password_showinfo_llayout)
    LinearLayout infoShowLlayout;
    //输入帐号的内容
    @BindView(R.id.password_account)
    EditText accountText;
    //显示帐号名字，发送至
    @BindView(R.id.password_accountName)
    TextView accountName;
    //找回手机号显示View
    @BindView(R.id.password_phone)
    TextView phonetTxtView;
    //找回邮箱显示View
    @BindView(R.id.password_email)
    TextView emailTxtView;
    //帐号删除按键
    @BindView(R.id.password_account_remove)
    ImageView removeImageView;
    //找回按键
    @BindView(R.id.password_button)
    Button passwordButton;

    private final int MEG_SHOWVIEW = 0x11001;
    private final int MEG_SHOWREMOVE = 0x11002;
    private final int MEG_HIDEREMOVE = 0x11003;

    private PhoneEmailInfo phoneEmailInfo;//找回信息发送到的手机号，邮箱。
    private String accountStr;//输入的帐号信息

    @Override
    protected View initContentView() {
        return getContentView(R.layout.find_passwrod_activity);
    }

    @Override
    protected void handleCreate() {
        bgColor = R.color.color_435275;
        setCenterViewContent(R.string.password_find);
        addTextChangeListener(accountText);
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case MEG_HIDEREMOVE:
                removeImageView.setVisibility(View.GONE);
                break;
            case MEG_SHOWREMOVE:
                removeImageView.setVisibility(View.VISIBLE);
                break;
            case MEG_SHOWVIEW:
                if (null != phoneEmailInfo) {
                    accountName.setText(getString(R.string.password_send,accountStr));
                    phonetTxtView.setText(getString(R.string.password_phone,phoneEmailInfo.getMobile()));
                    emailTxtView.setText(getString(R.string.password_mail,phoneEmailInfo.getEmail()));
                    textLLayout.setVisibility(View.GONE);
                    infoShowLlayout.setVisibility(View.VISIBLE);
                }else{
                    showToast(R.string.login_not_net);
                }
                break;
        }
    }

    @OnClick(R.id.password_account_remove)
    public void clickRemove(){
        accountText.setText("");
    }

    @OnClick(R.id.password_button)
    public void clickButton() {
        accountStr = accountText.getText().toString();
        if (TextUtils.isEmpty(accountStr)) {
            showToast(R.string.password_account_null);
            return;
        }

        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.FIND_PASSWORD);
        entity.addParams("name", accountStr);
        HttpRequestHelper.post(entity, new BaseRequestCallback<PasswordResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
                showLoadDialog();
            }

            @Override
            public void onSuccess(PasswordResponse response) {
                TTLog.i("lqsir --- " + response.getData().toString());
                if (null != response.getData() && (response.getData().getSucc() == 1)) {
                    //设置返回手机，邮箱信息;
                    phoneEmailInfo = response.getData().getInfo();
                    basicHandler.sendEmptyMessage(MEG_SHOWVIEW);
                } else {
                    showToast(R.string.password_account_not);
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

    /**
     * 帐号，密码输入文字监听。有内容时显示删除标记，无内容隐藏删除标记
     *
     * @param editText
     */
    private void addTextChangeListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    basicHandler.sendEmptyMessage(MEG_SHOWREMOVE);
                }else{
                    basicHandler.sendEmptyMessage(MEG_HIDEREMOVE);
                }
            }
        });
    }
}
