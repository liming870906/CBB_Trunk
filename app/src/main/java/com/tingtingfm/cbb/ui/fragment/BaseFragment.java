package com.tingtingfm.cbb.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.dialog.TTAlertDialog;
import com.tingtingfm.cbb.common.utils.ToastUtils;

import butterknife.ButterKnife;

/**
 * Created by tianhu on 2017/3/21.
 */

public abstract class BaseFragment extends Fragment {
    protected Handler fragmentHandler = new FragmentHandler();
    protected Activity mActivity;
    protected Resources mResource;

    protected static TTAlertDialog mAlertDialog = null;

    public class FragmentHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            processMessage(msg);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mResource = mActivity.getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = initView(inflater,container,savedInstanceState);
        ButterKnife.bind(this, view);
        handleCreate();
        return view;
    }

    //初始View
    protected abstract View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    //初始界面
    protected abstract void handleCreate();

    //handleCreate -》 handleMessage
    protected abstract void processMessage(Message msg);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public final void showToast(int res) {
        showToast(getString(res));
    }

    public final void showToast(final String msg) {
        fragmentHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showToast(mActivity, msg);
            }
        });
    }

    public void showOneButtonDialog(String title, String message,
                                    DialogInterface.OnClickListener listener1) {
        showOneButtonDialog(false, title, message, listener1);
    }

    //对话框显示中间一个按钮
    public void showOneButtonDialog(boolean isCenter, String title, String message,
                                    DialogInterface.OnClickListener listener1) {
        TTAlertDialog.Builder builder = new TTAlertDialog.Builder(mActivity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setMessageGravity(isCenter);
        builder.setNeutralButton(getString(R.string.ok), listener1);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }
}
