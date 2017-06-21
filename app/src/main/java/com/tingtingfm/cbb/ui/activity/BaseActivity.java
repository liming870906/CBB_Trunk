package com.tingtingfm.cbb.ui.activity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by think on 2016/12/23.
 */

public abstract class BaseActivity extends AbstractActivity {
    protected int bgColor = R.color.color_697fb4;
    RelativeLayout mTitleLayout;
    @BindView(R.id.title_left1)
    ImageView mLeftView1;
    @BindView(R.id.title_left3)
    TextView mLeftView3;
    @BindView(R.id.title_right1)
    ImageView mRightView1;
    @BindView(R.id.title_right3)
    TextView mRightView3;
    @BindView(R.id.title_content)
    TextView mContentView;
    @BindView(R.id.title_right_layout)
    RelativeLayout mTitleRightLayout;

    @BindView(R.id.title_left_layout)
    LinearLayout leftLayout;
    private View wholelay;

    @Override
    boolean onCreateTTActivity(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        wholelay = inflater.inflate(R.layout.tt_basic_titlebar_lay, null, false);
        mTitleLayout = (RelativeLayout) wholelay.findViewById(R.id.container_top_layout);
        View contentView = initContentView();
        if (contentView != null) {
            LinearLayout.LayoutParams contentLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout contentlay = (LinearLayout) wholelay.findViewById(R.id.content_lay);
            contentlay.addView(contentView, contentLayoutParams);
        }
        setContentView(wholelay);
        ButterKnife.bind(this, wholelay);
        handleCreate();

        wholelay.setBackgroundColor(getResources().getColor(bgColor));
        mTitleLayout.setBackgroundColor(getResources().getColor(bgColor));
        return true;
    }

    @OnClick(R.id.title_left1)
    protected void onLeftView1Click() {
        finish();
        hideSoftInput(mLeftView1);
    }

    @OnClick(R.id.title_left3)
    protected void onLeftView3Click() {
        onLeftView1Click();
    }

    @OnClick(R.id.title_right1)
    protected void onRightView1Click() {
    }

    @OnClick(R.id.title_right3)
    protected void onRightView3Click() {

    }

    @OnClick(R.id.title_content)
    protected void onCenterViewClick() {

    }

    protected View getRootLayout(){
        return wholelay;
    }

    protected void setLeftView1Background(int resId) {
        if (mLeftView1 != null) {
            mLeftView1.setImageResource(resId);
        }
    }

    protected void setLeftView1Visibility(int visibility) {
        if (mLeftView1 != null) {
            mLeftView1.setVisibility(visibility);
        }
    }


    //设置文字大小
    protected void setmLeftView3TextSize(int  size){
        mLeftView3.setTextSize(TypedValue.COMPLEX_UNIT_PX ,size);
    }

    /**
     * 设置跟随返回图标文字
     *
     * @param strResId 要显示的内容资源id
     */
    protected void setLeftView3Content(int strResId) {
        if (mLeftView3 != null) {
            mLeftView3.setText(strResId);
            mLeftView3.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置跟随返回图标文字
     *
     * @param str 要显示的内容资源id
     */
    protected void setLeftView3Content(String str) {
        if (mLeftView3 != null) {
            mLeftView3.setText(str);
            mLeftView3.setVisibility(View.VISIBLE);
        }
    }


    protected void setRightView1Background(int resId) {
        if (mRightView1 != null) {
            mRightView1.setImageResource(resId);
        }
    }

    protected void setRightView1Visibility(int visibility) {
        if (mRightView1 != null) {
            mRightView1.setVisibility(visibility);
        }

        if (mRightView3 != null)
            mRightView3.setVisibility(View.GONE);
    }

    /**
     * 设置右边按钮的属性
     *
     * @param strResId 要显示的内容资源id
     */
    protected void setRightView3Content(int strResId) {
        if (mRightView3 != null) {
            mRightView3.setText(strResId);
        }
    }

    /**
     * 设置右边按钮的属性
     *
     * @param size 要显示的内容Size
     */
    protected void setRightView3Size(int size) {
        if (mRightView3 != null) {
            mRightView3.setTextSize(size);
        }
    }

    /**
     * 设置右边按钮的属性
     *
     * @param strResId 要显示的内容
     */
    protected void setRightView3Content(String strResId) {
        if (mRightView3 != null) {
            mRightView3.setText(strResId);
        }
    }

    /**
     * 设置右边按钮的属性
     *
     * @param strResId 点击时的字体颜色
     */
    protected void setRightView3TextColor(int strResId) {
        if (mRightView3 != null) {
            ColorStateList csl = (ColorStateList) getResources().getColorStateList(strResId);
            mRightView3.setTextColor(csl);
        }
    }

    protected void setRightView3Visibility(int visibility) {
        if (mRightView3 != null)
            mRightView3.setVisibility(visibility);

        if (mRightView1 != null)
            mRightView1.setVisibility(View.GONE);
    }

    protected void setRightView3Enable(boolean boo) {
        if (mRightView3 != null) {
            mRightView3.setEnabled(boo);
        }
    }

    protected void setCenterViewContent(int strResId) {
        if (mContentView != null) {
            mContentView.setText(strResId);
        }
    }

    protected void setCenterViewContent(String content) {
        if (mContentView != null) {
            mContentView.setText(content);
        }
    }

    protected TextView setCenterContentView() {
        if (mContentView != null) {
            return mContentView;
        }
        return null;
    }

    protected void setCenterViewBackground(int resId) {
        if (mContentView != null) {
            mContentView.setBackgroundResource(resId);
        }
    }

    protected void setAllBackgroundColor(int resId) {
        if (mTitleLayout != null) {
            mTitleLayout.setBackgroundColor(resId);
        }
    }

    protected void setTitleVisiable(int value) {
        if (mTitleLayout != null) {
            mTitleLayout.setVisibility(value);
        }
    }

    protected void setRightLayoutHide() {
        if (mTitleRightLayout != null) {
            mTitleRightLayout.setVisibility(View.INVISIBLE);
        }
    }

    protected ImageView getmRightView1(){
        if(null != mRightView1){
            return mRightView1;
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public TextView getCenTextView() {
        return mContentView;
    }


    public ImageView getLeftView1() {
        return mLeftView1;
    }
}
