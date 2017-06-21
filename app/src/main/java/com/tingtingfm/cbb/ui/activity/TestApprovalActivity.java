package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.configuration.Constants;

import butterknife.OnClick;

public class TestApprovalActivity extends BaseActivity {

    private int mID;

    @Override
    protected View initContentView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_test_approval,null);
    }

    @Override
    protected void handleCreate() {
        mID = getIntent().getIntExtra(Constants.KEY_MANUSCRIPT_ID,-1);
        showToast("ID:"+mID);
    }

    @Override
    protected void processMessage(Message msg) {

    }

    @OnClick(R.id.btn1)
    public void click1(View view){
        // TODO: 2017/6/8 审批页面
        Intent intent = new Intent(this,ApprovalActivity.class);
        intent.putExtra(Constants.KEY_MANUSCRIPT_INFO,new ManuscriptInfo());
        startActivity(intent);
    }
    @OnClick(R.id.btn2)
    public void click2(View view){
        Intent intent = new Intent(this,CommentManageActivity.class);
        intent.putExtra(Constants.KEY_MANUSCRIPT_ID,mID);
        // TODO: 2017/6/8 查看批注
        startActivity(intent);
    }
}
