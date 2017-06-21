package com.tingtingfm.cbb.ui.activity.webview;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.DeviceUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.RootUtils;
import com.tingtingfm.cbb.common.utils.ToastUtils;
import com.tingtingfm.cbb.common.utils.encrypt.DesECBUtil;
import com.tingtingfm.cbb.ui.activity.AbstractActivity;
import com.tingtingfm.cbb.ui.view.MenuOnCancelListener;
import com.tingtingfm.cbb.ui.view.SelectPicPopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2016/12/27.
 */

public class HelpActivity extends AbstractActivity
        implements MyWebChomeClient.OpenFileChooserCallBack,
        View.OnClickListener,
        MenuOnCancelListener{
    final String TAG = "HelpActivity";

    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    public WebView webView;
    public ValueCallback<Uri[]> mUploadMsgForAndroid5;
    private Intent mSourceIntent;
    private ValueCallback<Uri> mUploadMsg;
    private SelectPicPopupWindow menuWindow;

    @BindView(R.id.title_back)
    TextView mBackView;
    @BindView(R.id.title_close)
    TextView mCloseView;
    @BindView(R.id.title_content)
    TextView mCenterTxt;

    @Override
    protected View initContentView() {
        View view = getContentView(R.layout.help_activity);

        webView = (WebView) view.findViewById(R.id.help_webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
        webSettings.setJavaScriptEnabled(true);//是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setNeedInitialFocus(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setSupportZoom(true);//是否可以缩放，默认true
        webSettings.setBuiltInZoomControls(false);//是否显示缩放按钮，默认false
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        webSettings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
//        webSettings.setAppCacheEnabled(true);//设置是否打开。默认关闭，即，H5的缓存无法使用。
        webSettings.setDomStorageEnabled(true);//DOM Storage
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setBlockNetworkLoads(false);
        webSettings.setDomStorageEnabled(true);
//        webSettings.setUserAgentString("User-Agent:Android");//设置用户代理，一般不用
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebChromeClient(new MyWebChomeClient(HelpActivity.this));
        webView.setWebViewClient(new BaseViewClient());

        if(NetUtils.isNetConnected()){
            webView.loadUrl(UrlManager.SET_HELP, getRequestHead());
        }else{
            showToast(R.string.login_not_net);
        }
//        webView.loadUrl("http://m.bjaudio.com/help/question");

        return view;
    }

    @Override
    protected void handleCreate() {
        mCenterTxt.setText(R.string.setting_help);
    }

    @OnClick(R.id.title_back)
    public void onBackViewClick() {
        boolean res = webView.canGoBack();
        if (res) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @OnClick(R.id.title_close)
    public void onCloseViewClick() {
        finish();
    }

    @Override
    protected void processMessage(Message msg) {

    }

    public Map<String, String> getRequestHead() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", getParamsValue());

        return map;
    }

    public String getParamsValue() {
//		s_k    客户端的session_key数据，如果客户端是已经登录的，那么转到H5活动页后，默认为登录状态。
//		c_id   渠道编号(channel_id)
//		i_ct    记录的第一次启动客户端的时间（第一次接口返回的时间为准）(install_ctime)
//		c_os  客户端系统，client_os，ios或android
//		ce    客户端标识(client)
//		vs     客户端版本(version)
//		ie    手机IMEI(imei)，如果拿不到，可以用其他能够标识唯一设备的除client以外的值。
//		a_r    Android是否是root(android_root)    安卓设备传
//		a_d    Android是否是开发者模式(android_dev)    安卓设备传
//		a_o_v    Android系统版本号(android_os_version)     安卓设备传
//		a_dv    Android设备型号(android_device)    安卓设备传

        String result = "";
        try {
            Map<String, String> posts = new HashMap<String, String>();
            posts.put("s_k", PreferencesConfiguration.getSValues(Constants.SESSION_KEY));
//            posts.put("c_id", TTConstantManager.getInstance().getScid() + "");
            posts.put("i_ct", PreferencesConfiguration.getSValues(Constants.FIRST_START_TIME));
            posts.put("c_os", "android");
            posts.put("ce", DeviceUtils.getDeviceId());
            posts.put("vs", AppUtils.getVersionName());
            posts.put("ie", DeviceUtils.getTelephoneSerialNum());
            posts.put("a_r", RootUtils.isRootSystem() ? String.valueOf(1) : String.valueOf(0));
            posts.put("a_d", DeviceUtils.enableAdb() ? String.valueOf(1) : String.valueOf(0));
            posts.put("a_o_v", DeviceUtils.getSysRelease());
            posts.put("a_dv", DeviceUtils.getPhoneModel());

            List<String> keys = new ArrayList<String>();
            for (String str : posts.keySet()) {
                keys.add(str);
            }
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                sb.append(Uri.encode(keys.get(i)));
                sb.append("=");
                sb.append(Uri.encode(posts.get(keys.get(i))));
                sb.append("&");
            }

            if (sb.toString().endsWith("&")) {
                result = sb.toString().substring(0, sb.toString().length() - 1);
            }
            result = DesECBUtil.encryptDES(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
        mUploadMsg = uploadMsg;
        showOptions();
    }

    @Override
    public boolean openFileChooserCallBackAndroid5(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mUploadMsgForAndroid5 = filePathCallback;
        showOptions();

        return true;
    }

    @Override
    public void onClick(View v) {
        menuWindow.dismiss();
        switch (v.getId()) {
            case R.id.btn_take_photo:
                mSourceIntent = ImageUtil.takeBigPicture();
                startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                break;
            case R.id.btn_pick_photo:
                mSourceIntent = ImageUtil.choosePicture();
                startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(null);
            }

            if (mUploadMsgForAndroid5 != null) {         // for android 5.0+
                mUploadMsgForAndroid5.onReceiveValue(null);
            }
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CAPTURE:
            case REQUEST_CODE_PICK_IMAGE: {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        if (mUploadMsg == null) {
                            return;
                        }

                        String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);

                        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                            Log.e(TAG, "sourcePath empty or not exists.");
                            break;
                        }
                        Uri uri = Uri.fromFile(new File(sourcePath));
                        mUploadMsg.onReceiveValue(uri);

                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (mUploadMsgForAndroid5 == null) {        // for android 5.0+
                            return;
                        }

                        String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);

                        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                            Log.e(TAG, "sourcePath empty or not exists.");
                            break;
                        }
                        Uri uri = Uri.fromFile(new File(sourcePath));
                        mUploadMsgForAndroid5.onReceiveValue(new Uri[]{uri});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void showOptions() {
        //显示窗口
        menuWindow = new SelectPicPopupWindow(HelpActivity.this, this);
        menuWindow.setListener(this);
        menuWindow.showAtLocation(webView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    private void restoreUploadMsg() {
        if (mUploadMsg != null) {
            mUploadMsg.onReceiveValue(null);
            mUploadMsg = null;

        } else if (mUploadMsgForAndroid5 != null) {
            mUploadMsgForAndroid5.onReceiveValue(null);
            mUploadMsgForAndroid5 = null;
        }
    }

    @Override
    public void onCancel() {
        restoreUploadMsg();
    }

    class BaseViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mCenterTxt.setText(view.getTitle());
            mCloseView.setVisibility(view.canGoBack() ? View.VISIBLE : View.GONE);
        }
    }
}
