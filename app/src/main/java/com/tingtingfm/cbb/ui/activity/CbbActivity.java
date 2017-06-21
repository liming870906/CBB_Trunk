package com.tingtingfm.cbb.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.update.UpdateManager;
import com.tingtingfm.cbb.common.utils.ManuscriptUtils;
import com.tingtingfm.cbb.ui.fragment.LoginFragment;
import com.tingtingfm.cbb.ui.fragment.MainFragment;
import com.tingtingfm.cbb.ui.play.LibMediaException;
import com.tingtingfm.cbb.ui.play.MediaUtil;

import java.util.List;

/**
 * Created by tianhu on 2017/3/21.
 */

public class CbbActivity extends FragmentActivity {
    public static final String FLAG_LOAGINFRAGMENT = "login";//登录页
    public static final String FLAG_MAINFRAGMENT = "main"; //首页

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cbb_activity);

        //第一次安装启动后显示首页，进入二级页面。按home键显示桌面，在次单击icon重新开启此页面问题(解决)。
        if (ActivityStack.getInstance().isHaveActivity(CbbActivity.class)) {
            finish();
            return;
        }
        /**
         * 此处代码保留，不能做更改，作为自动升级使用
         */
        UpdateManager.getInstance().checkUpdate(false);

        ActivityStack.getInstance().pushActivity(this);

        ManuscriptUtils.updateManuscriptState(this);
        try {
            // Start LibVLC
            MediaUtil.getLibVlcInstance();
        } catch (LibMediaException e) {
            e.printStackTrace();
            TTLog.e("message---LibVLC failed to initialize (LibVlcException)");
        }

        PreferencesConfiguration.setSValues(Constants.FIRST_START_TIME, "");
        if (TextUtils.isEmpty(PreferencesConfiguration.getSValues(Constants.SESSION_KEY))) {
            showFragment(FLAG_LOAGINFRAGMENT);
        } else {
            showFragment(FLAG_MAINFRAGMENT);
        }
    }

    public void showFragment(String flag) {
        if (flag.equals(FLAG_LOAGINFRAGMENT)) {
            fullScreen(true);
            showFragment(LoginFragment.class, FLAG_LOAGINFRAGMENT);//登录页
        } else {
            fullScreen(false);
            showFragment(MainFragment.class, FLAG_MAINFRAGMENT);//首页
        }
    }

    @Override
    public void onBackPressed() {
        ((TTApplication) TTApplication.getAppContext()).finishActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.getInstance().popActivity(this);
    }

    //控制显示与隐藏状态栏
    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Window window = getWindow();
            window.setAttributes(attr);
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.color_6489b4));
            }
        }
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
                transaction.add(R.id.cbb_frameLayout, fragment, tag);
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
