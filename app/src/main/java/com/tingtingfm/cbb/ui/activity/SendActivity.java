package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ContactsInfo;
import com.tingtingfm.cbb.bean.DepartmentInfo;
import com.tingtingfm.cbb.bean.GroupInfo;
import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.bean.SendInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.response.GetSendInfoResponse;
import com.tingtingfm.cbb.response.SendMaterialResponse;
import com.tingtingfm.cbb.ui.adapter.SendContactsAdapter;
import com.tingtingfm.cbb.ui.adapter.SendOtherAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 发送界面
 *
 * @author liming
 */
public class SendActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @BindView(R.id.iv_send_contact_bottom_line)
    ImageView ivContactLine;
    @BindView(R.id.iv_send_group_bottom_line)
    ImageView ivGroupLine;
    @BindView(R.id.iv_send_department_bottom_line)
    ImageView ivDepartmentLine;
    @BindView(R.id.tv_send_contact_title)
    TextView tvContactTitle;
    @BindView(R.id.tv_send_group_title)
    TextView tvGroupTitle;
    @BindView(R.id.tv_send_department_title)
    TextView tvDepartmentTitle;
    @BindView(R.id.lv_send_data_view)
    ListView lvSendData;
    @BindView(R.id.ll_send_no_data_layout)
    LinearLayout llSendNoDataLayout;
    @BindView(R.id.tv_send_no_data_content01)
    TextView tvSendNoDataContent01;
    @BindView(R.id.tv_send_no_data_content02)
    TextView tvSendNoDataContent02;
    private static final String TAG = "SendActivity=====>";
    //声明适配器方法
    private SendContactsAdapter mContactsAdapter;
    private SendOtherAdapter mGroupAdapter, mDepartmentAdapter;
    //声明数据对象
    private SendInfo mSendInfo;
    //选择标记
    private int mChoose;
    //声明提交集合对象
    private ContactsInfo mContactsInfo;
    private String mIDList;
    private static final int REQUEST_CORD = 99;

    @Override
    protected View initContentView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_send, null);
    }

    @Override
    protected void handleCreate() {
        mIDList = getIntent().getStringExtra(Constants.SEND_ID_LIST);
        mChoose = Constants.SEND_CONTACT_NAVIGATION_TAG;
        //设置标题
        setCenterViewContent(R.string.send_title);
        //设置右侧显示
        setRightView3Visibility(View.INVISIBLE);
        //设置标题右侧按钮文本
        setRightView3Content(R.string.send_right_submit_text);
        //声明适配器方法
        mContactsAdapter = new SendContactsAdapter(mSendInfo, Constants.SEND_CONTACT_NAVIGATION_TAG, this);
        mGroupAdapter = new SendOtherAdapter(mSendInfo, Constants.SEND_GROUP_NAVIGATION_TAG, this);
        mDepartmentAdapter = new SendOtherAdapter(mSendInfo, Constants.SEND_DEPARTMENT_NAVIGATION_TAG, this);
        //设置
        lvSendData.setAdapter(mContactsAdapter);
        //加载数据
        loadData();
        //添加监听器方法。
        addListener();
    }

    /**
     * 添加监听器方法
     */
    private void addListener() {
        lvSendData.setOnItemClickListener(this);
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case Constants.UPDATE_SEND_DATA_TAG:
                updateAdapterData(msg);
                break;
        }
    }

    private void updateAdapterData(Message msg) {
        if (mChoose == Constants.SEND_CONTACT_NAVIGATION_TAG) {
            if (msg.arg1 != -1) {
                lvSendData.setAdapter(mContactsAdapter);
            }
            mContactsAdapter.setData(mSendInfo, mContactsInfo);
            if (mSendInfo.getContacts() != null && mSendInfo.getContacts().size() == 0) {
                setRightView3Visibility(View.INVISIBLE);
                changeShowNoView(true);
            } else {
                setRightView3Visibility(View.VISIBLE);
                changeShowNoView(false);
            }
        } else if (mChoose == Constants.SEND_GROUP_NAVIGATION_TAG) {
            lvSendData.setAdapter(mGroupAdapter);
            mGroupAdapter.setData(mSendInfo);
            if (mSendInfo.getGroup() != null && mSendInfo.getGroup().size() == 0) {
                changeShowNoView(true);
            } else {
                changeShowNoView(false);
            }
        } else {
            lvSendData.setAdapter(mDepartmentAdapter);
            mDepartmentAdapter.setData(mSendInfo);
            if (mSendInfo.getDepartment() != null && mSendInfo.getDepartment().size() == 0) {
                changeShowNoView(true);
            } else {
                changeShowNoView(false);
            }
        }
    }

    /**
     * 是否显示没有布局
     *
     * @param isVisibility
     */
    private void changeShowNoView(boolean isVisibility) {
        lvSendData.setVisibility(isVisibility ? View.GONE : View.VISIBLE);
        llSendNoDataLayout.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
        if (isVisibility) {
            tvSendNoDataContent01.setVisibility(View.VISIBLE);
            tvSendNoDataContent02.setVisibility(View.VISIBLE);
            switch (mChoose) {
                case Constants.SEND_CONTACT_NAVIGATION_TAG:
                    tvSendNoDataContent01.setTextSize(DensityUtils.px2sp(this, 72.0f));
                    tvSendNoDataContent01.setText(R.string.send_contact_no_data_text);
                    tvSendNoDataContent02.setText(R.string.send_contact_no_data_bottom);
                    setRightView3Visibility(View.INVISIBLE);
                    break;
                case Constants.SEND_GROUP_NAVIGATION_TAG:
                    tvSendNoDataContent02.setVisibility(View.GONE);
                    tvSendNoDataContent01.setTextSize(DensityUtils.px2sp(this, 48.0f));
                    tvSendNoDataContent01.setText(R.string.send_group_no_data_text);
                    break;
                case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:
                    tvSendNoDataContent01.setTextSize(DensityUtils.px2sp(this, 72.0f));
                    tvSendNoDataContent01.setText(R.string.send_department_no_data_text);
                    tvSendNoDataContent02.setText(R.string.send_department_no_data_bottom);
                    break;
            }
        }
    }

    @OnClick({R.id.rl_send_contact_layout, R.id.rl_send_group_layout, R.id.rl_send_department_layout})
    public void onNavigationClick(View view) {
        switch (view.getId()) {
            case R.id.rl_send_contact_layout:   //联系人
                changeNavigationBottomLineShowView(Constants.SEND_CONTACT_NAVIGATION_TAG);
                break;
            case R.id.rl_send_group_layout:     //群组
                changeNavigationBottomLineShowView(Constants.SEND_GROUP_NAVIGATION_TAG);
                break;
            case R.id.rl_send_department_layout://部门
                changeNavigationBottomLineShowView(Constants.SEND_DEPARTMENT_NAVIGATION_TAG);
                break;
        }
    }

    /**
     * 显示导航栏中底部的直线
     *
     * @param p
     */
    private void changeNavigationBottomLineShowView(int p) {
        //是否为同一个标记，数据对象是否为Null
        if (mSendInfo == null || mChoose == p) {
            return;
        }
        mChoose = p;

        ivContactLine.setVisibility(View.GONE);
        ivGroupLine.setVisibility(View.GONE);
        ivDepartmentLine.setVisibility(View.GONE);
        tvContactTitle.setTextColor(getResources().getColor(R.color.color_9da1af));
        tvGroupTitle.setTextColor(getResources().getColor(R.color.color_9da1af));
        tvDepartmentTitle.setTextColor(getResources().getColor(R.color.color_9da1af));
        //判断点击哪一个导航选项
        switch (p) {
            case Constants.SEND_CONTACT_NAVIGATION_TAG: //联系人
                ivContactLine.setVisibility(View.VISIBLE);
                tvContactTitle.setTextColor(getResources().getColor(R.color.color_697FB4));
                setRightView3Visibility(isContactHaveData() ? View.VISIBLE : View.GONE);
                break;
            case Constants.SEND_GROUP_NAVIGATION_TAG:       //群组
                ivGroupLine.setVisibility(View.VISIBLE);
                tvGroupTitle.setTextColor(getResources().getColor(R.color.color_697FB4));
                setRightView3Visibility(View.INVISIBLE);
                break;
            case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:  //部门
                ivDepartmentLine.setVisibility(View.VISIBLE);
                tvDepartmentTitle.setTextColor(getResources().getColor(R.color.color_697FB4));
                setRightView3Visibility(View.INVISIBLE);
                break;
        }

        basicHandler.sendEmptyMessage(Constants.UPDATE_SEND_DATA_TAG);
    }

    private boolean isContactHaveData() {
        List<ContactsInfo> _infos = mSendInfo.getContacts();
        if (_infos != null && _infos.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得所有数据（联系人，群组，部门）
     */
    private void loadData() {
        RequestEntity _entity = new RequestEntity(UrlManager.GET_SEND_INFO);
        HttpRequestHelper.post(_entity, new BaseRequestCallback<GetSendInfoResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(GetSendInfoResponse response) {
                SendInfo _info = response.getData();
                if (_info != null) {
                    mSendInfo = _info;
                }
                basicHandler.sendEmptyMessage(Constants.UPDATE_SEND_DATA_TAG);
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (mChoose) {
            case Constants.SEND_CONTACT_NAVIGATION_TAG:
                //获得点击选项
                ContactsInfo _contactsInfo = (ContactsInfo) mContactsAdapter.getItem(i);
                if (_contactsInfo.getAdmin_id() != AccoutConfiguration.getLoginInfo().getUserid()) {
                    //判断是否为同一个对象
                    if (mContactsInfo == null) {
                        mContactsInfo = _contactsInfo;
                    } else {
                        if (mContactsInfo.getAdmin_id() == _contactsInfo.getAdmin_id()) {
                            mContactsInfo = null;
                        } else {
                            mContactsInfo = _contactsInfo;
                        }
                    }
                    //发送更新消息
                    Message.obtain(basicHandler, Constants.UPDATE_SEND_DATA_TAG, -1, 0).sendToTarget();
                }

                break;
            case Constants.SEND_GROUP_NAVIGATION_TAG:
                //获得群组对象
                GroupInfo _groupInfo = (GroupInfo) mGroupAdapter.getItem(i);
                //跳转界面
//                startActivityForResult(new Intent(this,ContactActivity.class)
//                        .putExtra(Constants.SEND_GOTO_TYPE_KEY,Constants.SEND_GOTO_TYPE_VALUE_GROUP)
//                        .putExtra(Constants.SEND_GOTO_KEY_OTHER_INFO,_groupInfo)
//                        .putExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO,mContactsInfo)
//                        .putExtra(Constants.SEND_ID_LIST,mIDList),REQUEST_CORD);
                startActivity(new Intent(this, ContactActivity.class)
                        .putExtra(Constants.SEND_GOTO_TYPE_KEY, Constants.SEND_GOTO_TYPE_VALUE_GROUP)
                        .putExtra(Constants.SEND_GOTO_KEY_OTHER_INFO, _groupInfo)
//                        .putExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO,mContactsInfo)
                        .putExtra(Constants.SEND_ID_LIST, mIDList));
                mContactsInfo = null;
                break;
            case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:
                //获得部门对象
                DepartmentInfo _departmentInfo = (DepartmentInfo) mDepartmentAdapter.getItem(i);
                //跳转界面
//                startActivityForResult(new Intent(this,ContactActivity.class)
//                        .putExtra(Constants.SEND_GOTO_TYPE_KEY,Constants.SEND_GOTO_TYPE_VALUE_DEPARTMENT)
//                        .putExtra(Constants.SEND_GOTO_KEY_OTHER_INFO,_departmentInfo)
//                        .putExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO,mContactsInfo)
//                        .putExtra(Constants.SEND_ID_LIST,mIDList),REQUEST_CORD);
                startActivity(new Intent(this, ContactActivity.class)
                        .putExtra(Constants.SEND_GOTO_TYPE_KEY, Constants.SEND_GOTO_TYPE_VALUE_DEPARTMENT)
                        .putExtra(Constants.SEND_GOTO_KEY_OTHER_INFO, _departmentInfo)
//                        .putExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO,mContactsInfo)
                        .putExtra(Constants.SEND_ID_LIST, mIDList));
                mContactsInfo = null;
                break;
        }
    }

    @Override
    protected void onRightView3Click() {
        super.onRightView3Click();
        if (mContactsInfo != null) {
            if (mContactsInfo.getAdmin_id() == AccoutConfiguration.getLoginInfo().getUserid()) {
                showToast(R.string.send_title_click_not_send_tips);
            } else {
                sendMaterial();
            }
        } else {
            showToast(R.string.send_title_click_tips);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(resultCode == RESULT_OK){
//            if(requestCode == REQUEST_CORD){
//                mContactsInfo = (ContactsInfo) data.getSerializableExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO);
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    /**
     * 发送素材
     */
    private void sendMaterial() {
        RequestEntity _entity = new RequestEntity(UrlManager.MATERIAL_SEND);
        _entity.addParams("id_list", mIDList);
        _entity.addParams("contact", String.valueOf(mContactsInfo.getAdmin_id()));
        HttpRequestHelper.post(_entity, new BaseRequestCallback<SendMaterialResponse>() {
            @Override
            public void onStart() {
                showLoadDialog();
            }

            @Override
            public void onSuccess(SendMaterialResponse response) {
                switch (response.getData().getSucc()) {
                    case 1:
                        showToast(R.string.send_success);
                        break;
                    case 0:
                        showToast(R.string.send_failure);
                        break;
                }
                mContactsInfo = null;
                //发送更新消息
                Message.obtain(basicHandler, Constants.UPDATE_SEND_DATA_TAG, -1, 0).sendToTarget();
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                dismissDlg();
            }
        });
    }

}
