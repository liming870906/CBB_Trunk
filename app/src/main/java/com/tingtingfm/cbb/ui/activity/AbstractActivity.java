package com.tingtingfm.cbb.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.dialog.CommonProgress;
import com.tingtingfm.cbb.common.dialog.TTAlertDialog;
import com.tingtingfm.cbb.common.utils.ToastUtils;

import butterknife.ButterKnife;


/**
 * Created by think on 2016/12/21.
 */

public abstract class AbstractActivity extends FragmentActivity {
    protected static TTAlertDialog mAlertDialog = null;

    protected CommonProgress mProgressDialog = null;

    protected Handler basicHandler = new BasicHandle();

    protected boolean isFullScreen = false;


    //对话框显示取消，确实按钮
    public void showTwoButtonDialog(String title, String message,
                                    DialogInterface.OnClickListener listener1,
                                    DialogInterface.OnClickListener listener2) {
        showTwoButtonDialog(false, false, title, message, listener1, listener2);
    }

    public void showTwoButtonDialog(boolean isCenter,
                                    String title, String message,
                                    DialogInterface.OnClickListener listener1,
                                    DialogInterface.OnClickListener listener2) {
        showTwoButtonDialog(isCenter, false, title, message, listener1, listener2);
    }

    public void showTwoButtonDialog(boolean isCenter, boolean isSingleLine,
                                    String title, String message,
                                    DialogInterface.OnClickListener listener1,
                                    DialogInterface.OnClickListener listener2) {
        TTAlertDialog.Builder builder = new TTAlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setMessageGravity(isCenter);
        builder.setMessageLine(isSingleLine);
        builder.setPositiveButton(getString(R.string.cancel), listener1);
        builder.setNegativeButton(getString(R.string.ok), listener2);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    public void showOneButtonDialog(String title, String message,
                                    DialogInterface.OnClickListener listener1) {
        showOneButtonDialog(false, title, message, listener1);
    }

    //对话框显示中间一个按钮
    public void showOneButtonDialog(boolean isCenter, String title, String message,
                                    DialogInterface.OnClickListener listener1) {
        TTAlertDialog.Builder builder = new TTAlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setMessageGravity(isCenter);
        builder.setNeutralButton(getString(R.string.ok), listener1);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    /**
     * 初始化一些View操作.
     */
    protected abstract View initContentView();

    /**
     * 逻辑操作，如：请求数据，加载界面...
     */
    protected abstract void handleCreate();

    /**
     * Handler消息处理
     *
     * @param msg
     */
    protected abstract void processMessage(Message msg);

    boolean onCreateTTActivity(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!onCreateTTActivity(savedInstanceState)) {
            View view = initContentView();
            if (view != null) {
                setContentView(view);
            }
            ButterKnife.bind(this);
            handleCreate();
        }

        ActivityStack.getInstance().pushActivity(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);
        }

        if (isFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.getInstance().popActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityStack.getInstance().addFrontPage();
        ActivityStack.getInstance().pushToStack(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityStack.getInstance().decrementFrontPage();
        ActivityStack.getInstance().popForStack(this);
    }

    public void sendMessage(int what) {
        sendMessage(what, 0, 0, null);
    }

    public void sendMessage(int what, Object data) {
        sendMessage(what, 0, 0, data);
    }

    /**
     * {@link Handler#obtainMessage(int, int, int)}
     *
     * @param what
     * @param arg1
     * @param arg2
     * @param data
     */
    public void sendMessage(int what, int arg1, int arg2, Object data) {
        if (basicHandler != null) {
            Message message = basicHandler.obtainMessage(what, arg1, arg2);
            message.obj = data;
            basicHandler.sendMessage(message);
        }
    }

    public final void showLoadDialog() {
        showLoadDialog(R.string.nowisloading);
    }

    public final void showLoadDialog(final int messageId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AbstractActivity.this.isFinishing()
                        || (mProgressDialog != null && mProgressDialog.isShowing())) {
                    return;
                }
                if (mProgressDialog != null) {
                    mProgressDialog.show();
                } else {
                    mProgressDialog = CommonProgress.show(AbstractActivity.this, getString(messageId));
                }
            }
        });
    }

    public void dismissDlg() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (AbstractActivity.this.isFinishing()) {
                    return;
                }

                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });

    }

    public final void showToast(int res) {
        showToast(getString(res));
    }

    public final void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showToast(AbstractActivity.this, msg);
            }
        });
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }

    public void showSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }


    protected View getContentView(int layoutId) {
        return LayoutInflater.from(this).inflate(layoutId, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //需重写此方法，让Fragment 中的onActivityResult也能执行，同时对应Activity不可设置为 android:launchMode="singleTask"
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final class BasicHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean isDispatch = false;
            switch (msg.what) {
                default:
                    break;
            }

            if (!isDispatch) {
                processMessage(msg);
            }
        }
    }
}

